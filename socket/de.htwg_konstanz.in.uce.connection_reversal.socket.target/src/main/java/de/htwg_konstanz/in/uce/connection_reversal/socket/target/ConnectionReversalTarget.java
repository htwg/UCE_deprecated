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

package de.htwg_konstanz.in.uce.connection_reversal.socket.target;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.messages.CommonUceMethod;
import de.htwg_konstanz.in.uce.messages.SemanticLevel;
import de.htwg_konstanz.in.uce.messages.UceMessage;
import de.htwg_konstanz.in.uce.messages.UceMessageReader;
import de.htwg_konstanz.in.uce.messages.UceMessageStaticFactory;
import de.htwg_konstanz.in.uce.messages.UniqueUserName;

/**
 * Class which contains the Target-Side of the ConnetionReversal implementation
 * 
 * With the integrated Builder-Class there can be build a configuration.
 * With this configuration a ConnectionReversalTarget can be initialized
 * by calling the build-Method on the inner configuration class.
 * 
 * The class provides to register and deregister a target on the Mediator,
 * which port and address is defined by the inner configuration class.
 * With the accept Method, there can be accepted connection requests
 * of the source-side in the ConnetionReversal implementation.
 * 
 * @author Stefan Lohr
 */
public class ConnectionReversalTarget {
	
	private DatagramSocket datagramSocket;
	private SocketAddress socketAddress;
	private KeepAliveThread keepAliveThread;
	private BlockingQueue<Socket> blockingSocketQueue;
	private ListenerThread listenerThread;
	private int iterationTime;
	private static final Logger logger = LoggerFactory.getLogger(ConnectionReversalTarget.class);
	private boolean registered;
	private String uniqueUserName;
	
	/**
	 * Private Constructor, can only be called by using inner configuration-class.
	 * 
	 * @param configuration Builder-Configuration for initialization
	 */
	private ConnectionReversalTarget(Builder configuration) {
		
		this.registered = false;
		this.uniqueUserName = configuration.getUniqueUserName();
		this.iterationTime = configuration.getIterationTime();
		int mediatorPort = configuration.getMediatorPort();
		String mediatorIP = configuration.getMediatorIP();
		
		blockingSocketQueue = new LinkedBlockingQueue<Socket>();
		
		try {
			
			socketAddress = new InetSocketAddress(mediatorIP, mediatorPort);
			datagramSocket = new DatagramSocket(configuration.getLocalPort());
		}
		catch (SocketException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Method for deregistration of the target on the mediator
	 * 
	 * @throws Exception
	 */
	public void deregister() throws Exception {
		
		if (!registered) throw new IllegalStateException("not yet registered");
		
		listenerThread.interrupt();
		keepAliveThread.interrupt();
		blockingSocketQueue.clear();
		
		UceMessage uceDeregisterMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.DEREGISTER, SemanticLevel.REQUEST, UUID.randomUUID());
		
		uceDeregisterMessage.addAttribute(new UniqueUserName(uniqueUserName));
		
		byte[] buf = uceDeregisterMessage.toByteArray();
		
		DatagramPacket datagrammPacket = new DatagramPacket(buf, buf.length, socketAddress);
		
		while (listenerThread.isReceiving) Thread.sleep(100);
		
		logger.info("send deregister message to mediator");
		datagramSocket.send(datagrammPacket);
		
		if (waitForSuccessResponse(CommonUceMethod.DEREGISTER)) {
			
			logger.info("success message for deregistration received");
		}
		else {
			
			logger.error("no success message for deregistration received");
			throw new Exception("Could not deregister");
		}
	}
	
	/**
	 * Method for registration of the target on the mediator
	 * 
	 * @throws Exception
	 */
	public void register() throws Exception {
		
		if (registered) throw new IllegalStateException("already registered");
		
		UceMessage uceRegisterMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.REGISTER, SemanticLevel.REQUEST, UUID.randomUUID());
		
		uceRegisterMessage.addAttribute(new UniqueUserName(uniqueUserName));
		
		byte[] buf = uceRegisterMessage.toByteArray();
		
		DatagramPacket datagrammPacket = new DatagramPacket(buf, buf.length, socketAddress);
		
		logger.info("send register message to mediator");
		datagramSocket.send(datagrammPacket);
		
		if (waitForSuccessResponse(CommonUceMethod.REGISTER)) {
			
			logger.info("success message for registration received");
			
			keepAliveThread = new KeepAliveThread(uniqueUserName, datagramSocket, socketAddress, iterationTime);
			listenerThread = new ListenerThread(datagramSocket, blockingSocketQueue);
			
			logger.info("start listenerThread");
			listenerThread.start();
			
			logger.info("start keepAliveThread");
			keepAliveThread.start();
			
			registered = true;
		}
		else {
			
			logger.error("no success message received");
			
			throw new IOException("Could not register");
		}
	}
	
	/**
	 * Method for accepting incoming connection from the source-side
	 * 
	 * @return Socket of the ConnectionReversal connection
	 * @throws InterruptedException
	 */
	public Socket accept() throws InterruptedException {
		
		/**
		 * TODO: prio2: hier evtl. exception wenn im keepAliveThread keine successResponses kommen
		 * es könnte ein leerer socket rein geschrieben werden, das dann vor der rückgabe überprüft wird
		 * und bei leerem socket dann eine execption werfen 
		 */
		
		if (!registered) throw new IllegalStateException("not yet registered");
		
		return blockingSocketQueue.take();
	}
	
	/**
	 * Method to get the local listener port of the listenerThread
	 * which listens for incoming connection requests of the source-side.
	 * 
	 * @return int of the port number
	 */
	public int getListenerPort() {
		
		return datagramSocket.getLocalPort();
	}
	
	/**
	 * Private method for waiting for success response messages.
	 * This method waits for the correct response message of the request messages from parameter.
	 * After receiving message or a timeout this method returns true or false.
	 * 
	 * @param commonUceMethod Message-Type
	 * @return true or false, dependent on if the received message is correct
	 * @throws IOException
	 */
	private boolean waitForSuccessResponse(CommonUceMethod commonUceMethod) throws IOException {
		
		DatagramPacket datagramPacket = new DatagramPacket(new byte[65536], 65536);
		
		// Wirft nach 10 Sekunden eine SocketTimeoutException
		datagramSocket.setSoTimeout(10000);
		try {
			
			datagramSocket.receive(datagramPacket);
		}
		catch (SocketTimeoutException ste) {
			
			datagramSocket.setSoTimeout(0);
			return false;
		}
		finally {
			
			datagramSocket.setSoTimeout(0);
		}
		
		byte[] data = datagramPacket.getData();
		
		UceMessageReader uceMessageReader = new UceMessageReader();
		UceMessage uceResponseMessage = uceMessageReader.readUceMessage(data);
		
		if (uceResponseMessage.isMethod(commonUceMethod)
				&& uceResponseMessage.isSuccessResponse()) return true;
		else return false;
	}
	
	/**
	 * Builder class to creat a ConnectionReversalTarget class
	 * 
	 * @author Stefan Lohr
	 */
	public static class Builder {
		
		private int mediatorPort;
		private String mediatorIP;
		private int iterationTime;
		private String uniqueUserName;
		private int localPort;
		
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
		public Builder(String uniqueUserName, int iterationTime, String mediatorIP, int mediatorPort) {
			
			this.uniqueUserName = uniqueUserName;
			this.iterationTime = iterationTime;
			this.mediatorIP = mediatorIP;
			this.mediatorPort = mediatorPort;
			this.localPort = 0;
		}
		
		/**
		 * Method to build a ConnectionReversalTarget
		 * 
		 * @return ConnectionReversalSource
		 */
		public ConnectionReversalTarget build() {
			
			// TODO: werfe Exception wenn parameter fehlen
			
			return new ConnectionReversalTarget(this);
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
		 * Method to get the iteration time in seconds
		 * 
		 * @return int of iteration time in seconds
		 */
		public int getIterationTime() {
			
			return iterationTime;
		}
		
		/**
		 * Method to set the iteration time in seconds
		 * 
		 * @param iterationTime
		 */
		public void setIterationTime(int iterationTime) {
			
			this.iterationTime = iterationTime;
		}

		/**
		 * @return the mediatorPort
		 */
		public int getMediatorPort() {
			
			return mediatorPort;
		}
		
		/**
		 * @param port sets the local port number
		 */
		public void setLocalPort(int localPort) {
			
			this.localPort = localPort;
		}
		
		/**
		 * @return local port number
		 */
		public int getLocalPort() {
			
			return this.localPort;
		}
		
		/**
		 * @param mediatorPort the mediatorPort to set
		 */
		public void setMediatorPort(int mediatorPort) {
			
			this.mediatorPort = mediatorPort;
		}

		/**
		 * @return the mediatorIP
		 */
		public String getMediatorIP() {
			
			return mediatorIP;
		}

		/**
		 * @param mediatorIP the mediatorIP to set
		 */
		public void setMediatorIP(String mediatorIP) {
			
			this.mediatorIP = mediatorIP;
		}
	}
}
