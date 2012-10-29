/**
 * Copyright (C) 2012 HTWG Konstanz, Oliver Haase
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
package de.htwg_konstanz.net.turn.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;


/**
 * A thread that serves RMI-Servers. 
 * A TurnServerThread relays data between multiple RMI-Clients and a RMI-Server.
 * 
 * @author Andre Erb
 * 
 */
public class TURNServerThread extends Thread {
	public static final int MIN_PORT = 10000;
	public static final int MAX_PORT = 10100;
	
	private ObjectInputStream ois = null;
	private ObjectOutputStream oos = null;
	private ServerSocket clientSS = null;
	private ServerSocket serverSS = null;
	private Random rand;
	
	/**
	 * Creates a TURNServerThread.
	 * 
	 * @param socket
	 * 	A Socket that is connected to a RMI-Server.
	 * @throws IOException 
	 * 
	 */
	public TURNServerThread(Socket socket) throws IOException {
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		rand = new Random();
	}	
	
	
	private void handleClientConnectRequest() throws IOException {
		while (!clientSS.isClosed()) {					
			Socket clientSocket = clientSS.accept();
			sendToServer("CONNECT_TO_SERVER_RELAY_ADDRESS");
			Socket serverSocket = serverSS.accept();
			relayData(clientSocket, serverSocket);
		}
	}
	
	
	/**
	 * Inherited from Java Thread. run() is executed by calling TURNServerThread.start().
	 * It implements the connection establishment between TURN-Server and RMI-Server.
	 * It allows connections to RelayedTransportAddress from RMI-Clients an relays
	 * data between the RMI-Server an its clients. 
	 * 
	 * 
	 */
	public void run () {		
		try {
			String turnCommand = (String) ois.readObject();
			if (turnCommand.equals("ALLOCATE_RELAY_ADDRESSES")) {					
				allocateRelayAddresses();		
				returnRelayAddresses();
				
				handleClientConnectRequest();
			}			
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}	
		catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	private ServerSocket createServerSocketWithinPortRange() throws IOException {
		boolean bound;
		ServerSocket ret = null;
		do {
			int port = rand.nextInt(MAX_PORT - MIN_PORT + 1) + MIN_PORT;
			try {
				ret = new ServerSocket(port);
				bound = true;
			}
			catch ( BindException be) {
				bound = false;
			}
		} while ( !bound );
		
		return ret;
	}

	/**
	 * Allocates the relay addresses for the RMI-Server and the RMI-Clients.
	 * @throws IOException 
	 * @throws IOException 
	 * 
	 */
	private void allocateRelayAddresses() throws IOException  {
		clientSS = createServerSocketWithinPortRange();	
		serverSS = createServerSocketWithinPortRange();	
	}
	
	
	private void sendToServer(String command, Object...args) throws IOException {
		oos.writeObject(command);
		for ( Object arg: args ) {
			oos.writeObject(arg);
		}
		oos.flush();	
	}
	
	private void returnRelayAddresses() throws UnknownHostException, IOException {
		sendToServer("RECEIVE_RELAY_ADDRESSES", 
				new InetSocketAddress(InetAddress.getLocalHost(), clientSS.getLocalPort()),
				new InetSocketAddress(InetAddress.getLocalHost(), serverSS.getLocalPort()));
	}
	
	
	/**
	 * Relays data between one RMI-Client and the RMI-Server.
	 * 
	 * @param clientSocket
	 * 	A RMI-Client.
	 * 
	 * @param clientSocket
	 * 	A rmiServer.
	 */
	private void relayData(Socket clientSocket, Socket serverSocket) {
		try {				
			// Create Input/Output-Streams to and from RMI-Client and RMI-Server
			InputStream clientIn = clientSocket.getInputStream();
			InputStream serverIn = serverSocket.getInputStream();
			OutputStream clientOut = clientSocket.getOutputStream();
			OutputStream serverOut = serverSocket.getOutputStream();
						
			// Create two threads to relay data between the RMI-Client and the RMI-Server
			TURNRelayThread clientToServerRelay = new TURNRelayThread(clientIn, serverOut);
			TURNRelayThread serverToClientRelay = new TURNRelayThread(serverIn, clientOut);

			// Relay data between the RMI-Client and the RMI-Server 
			clientToServerRelay.start();
			serverToClientRelay.start();	
		}
		catch (IOException e) {
			System.out.println("Error during relaying data between RMI-Server and RMI-Client");
			e.printStackTrace();
		}
	}
}
