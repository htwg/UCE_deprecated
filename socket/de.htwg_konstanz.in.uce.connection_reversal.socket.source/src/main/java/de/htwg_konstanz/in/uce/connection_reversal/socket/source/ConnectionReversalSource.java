/**
 * Copyright (C) 2011 Stefan Lohr
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htwg_konstanz.in.uce.connection_reversal.socket.source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.messages.CommonUceMethod;
import de.htwg_konstanz.in.uce.messages.SemanticLevel;
import de.htwg_konstanz.in.uce.messages.SocketEndpoint;
import de.htwg_konstanz.in.uce.messages.UceMessage;
import de.htwg_konstanz.in.uce.messages.UceMessageReader;
import de.htwg_konstanz.in.uce.messages.UceMessageStaticFactory;
import de.htwg_konstanz.in.uce.messages.UniqueUserName;
import de.htwg_konstanz.in.uce.messages.SocketEndpoint.EndpointClass;

/**
 * Class which contains the Source-Side of the ConnetionReversal implementation
 * 
 * With the integrated Builder-Class there can be build a configuration.
 * With this configuration a ConnectionReversalSource can be initialized
 * by calling the build-Method on the inner configuration class.
 * 
 * The Class provides methods to connect to the target of the configuration.
 * There are also the possibility to connect by a special port and to get
 * the hole list of the registered targets at the Mediator server.
 * 
 * @author Stefan Lohr
 */
public class ConnectionReversalSource {
	
	private DatagramSocket datagramSocket;
	private SocketAddress socketAddress;
	private static final Logger logger = LoggerFactory.getLogger(ConnectionReversalSource.class);
	private String uniqueUserName;
	private boolean isConnected;
	
	/**
	 * Private Constructor, can only be called by using inner configuration-class.
	 * 
	 * @param configuration Builder-Configuration for initialization
	 */
	private ConnectionReversalSource(Builder configuration) {
		
		int mediatorPort = configuration.getMediatorPort();
		String mediatorIP = configuration.getMediatorIP();
		uniqueUserName = configuration.getUniqueUserName(); 
		
		try {
			
			socketAddress = new InetSocketAddress(mediatorIP, mediatorPort);
			datagramSocket = new DatagramSocket();
		}
		catch (SocketException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Method to connect to target of the configuration.
	 * Uses a random port for the local socket.
	 * The target connects to this port. 
	 * 
	 * @return Socket to the target
	 * @throws Exception
	 */
	public Socket connect() throws Exception {
		
		return connect(0);
	}
	
	/**
	 * Method to connect to target of the configuration.
	 * The port where the target connect to is specified as argument.
	 * By 0 as port number, a random port is used.
	 * 
	 * @param port Port-Number for Target-connection
	 * @return Socket with connection to target
	 * @throws IOException
	 * @throws SocketTimeoutException
	 */
	public Socket connect(int port) throws SocketTimeoutException, IOException {
		
		if (isConnected) throw new IllegalStateException("already connected");
		else isConnected = true;
		
		logger.info("initialize lokal socket");
		ServerSocket serverSocket = new ServerSocket(port);
		
		UceMessage uceConnectionRequestMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.CONNECTION_REQUEST, SemanticLevel.REQUEST, UUID.randomUUID());
		
		InetAddress sourceAddress = serverSocket.getInetAddress();
		int sourcePort = serverSocket.getLocalPort();
		InetSocketAddress endpoint = new InetSocketAddress(sourceAddress, sourcePort);
		EndpointClass endpointClass = SocketEndpoint.EndpointClass.CONNECTION_REVERSAL;
		SocketEndpoint socketEndpoint = new SocketEndpoint(endpoint, endpointClass);
		
		uceConnectionRequestMessage.addAttribute(new UniqueUserName(uniqueUserName));
		uceConnectionRequestMessage.addAttribute(socketEndpoint);
		
		byte[] buf = uceConnectionRequestMessage.toByteArray();
		
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, socketAddress);
		
		logger.info("send connection request");
		datagramSocket.send(datagramPacket);
		
		serverSocket.setSoTimeout(10000);
		logger.info("listen on serverSocket {}:{}", sourceAddress, sourcePort);
		
		Socket socket;
		
		try {
			
			socket = serverSocket.accept();
		}
		catch (SocketTimeoutException e) {
			
			serverSocket.setSoTimeout(0);
			
			throw new SocketTimeoutException();
		}
		finally {
			
			serverSocket.close();
		}
		
		logger.info("connection established");
		
		return socket;
	}
	
	/**
	 * Returns a set of Strings of all users are registered on Mediator
	 * 
	 * @return Set of Strings with registered users on Mediator
	 * @throws IOException
	 */
	public Set<String> getUserList() throws IOException {
		
		UceMessage uceListMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.LIST, SemanticLevel.REQUEST, UUID.randomUUID());
		
		byte[] requestBuffer = uceListMessage.toByteArray();
		
		/*
		 * TODO: Package sollte eigentlich nur 512 Byte groﬂ sein,
		 * was passiert wenn mehr benutzer registriert sind?
		 *  Vorschlag: siehe Mediator Klasse
		 */
		DatagramPacket datagramPacketSend = new DatagramPacket(requestBuffer, requestBuffer.length, socketAddress);
		DatagramPacket datagramPacketReceive = new DatagramPacket(new byte[65536], 65536);
		
		logger.info("send request for userList");
		datagramSocket.send(datagramPacketSend);
		logger.info("waiting for userList");
		
		datagramSocket.setSoTimeout(10000);
		try {
			
			datagramSocket.receive(datagramPacketReceive);
		}
		catch (SocketTimeoutException ste) {
			
			logger.error("no userList received");
			ste.printStackTrace();
			
			return new HashSet<String>();
		}
		finally {
			
			datagramSocket.setSoTimeout(0);
		}
		
		logger.info("userList received, generate return value");
		
		byte[] userListBuffer = datagramPacketReceive.getData();
		
		UceMessageReader uceMessageReader = new UceMessageReader();
		UceMessage uceMessage = uceMessageReader.readUceMessage(userListBuffer);
		
		List<UniqueUserName> uniqueUserNameList = uceMessage.getAttributes(UniqueUserName.class);
		Set<String> userList = new HashSet<String>();
		
		for (UniqueUserName uniqueUserName : uniqueUserNameList) {
			
			userList.add(uniqueUserName.getUniqueUserName());
		}
		
		return userList;
	}
	
	/**
	 * Builder class to creat a ConnectionReversalSource class
	 * 
	 * @author Stefan Lohr
	 */
	public static class Builder {
		// TODO: makes no sense to initialize here, better remove the empty builder
		private int mediatorPort = 11111;
		private String mediatorIP = "141.37.121.124";
		private String uniqueUserName = "source";
		
		/**
		 * Constructor with no arguments.
		 * Configuration should be set by setters.
		 */
		public Builder() {
			
		}
		
		/**
		 * Constructor with all required arguments.
		 * No additional setters are needed.
		 */
		public Builder(String uniqueUserName, String mediatorIP, int mediatorPort) {
			
			this.uniqueUserName = uniqueUserName;
			this.mediatorIP = mediatorIP;
			this.mediatorPort = mediatorPort;
		}
		
		/**
		 * Method to build a ConnectionReversalSource
		 * 
		 * @return ConnectionReversalSource
		 */
		public ConnectionReversalSource build() {
			
			// TODO: werfe Exception wenn parameter fehlen

			return new ConnectionReversalSource(this);
		}
		
		/**
		 * Method to get the UniqueUserName
		 * 
		 * @return UniqueUserName
		 */
		public String getUniqueUserName() {
			
			return uniqueUserName;
		}
		
		/**
		 * Method to set the UniqueUserName
		 * 
		 * @param uniqueUserName
		 */
		public void setUniqueUserName(String uniqueUserName) {
			
			this.uniqueUserName = uniqueUserName;
		}

		/**
		 * Method to the port of the Mediator
		 * 
		 * @return the mediatorPort
		 */
		public int getMediatorPort() {
			
			return mediatorPort;
		}

		/**
		 * Method to set the port of the Mediator
		 * 
		 * @param mediatorPort the mediatorPort to set
		 */
		public void setMediatorPort(int mediatorPort) {
			
			this.mediatorPort = mediatorPort;
		}

		/**
		 * Method to get the IP (as String) of the Mediator
		 * 
		 * @return the mediatorIP
		 */
		public String getMediatorIP() {
			
			return mediatorIP;
		}

		/**
		 * Method to set the IP (as String) of the Mediator
		 * 
		 * @param mediatorIP the mediatorIP to set
		 */
		public void setMediatorIP(String mediatorIP) {
			
			this.mediatorIP = mediatorIP;
		}
	}
}
