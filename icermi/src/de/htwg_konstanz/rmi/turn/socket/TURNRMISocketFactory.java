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
package de.htwg_konstanz.rmi.turn.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

/**
 * A TURNRMISocketFactory is used to create RMI-RemoteObjects,
 * that communicate over Symmetric NAT with the help of a TURN-Server.
 * 
 * @author Andre Erb
 * 
 */
public class TURNRMISocketFactory implements RMIServerSocketFactory,
											 RMIClientSocketFactory, Serializable {

	private static final long serialVersionUID = -7707534555030453027L;
	
	private transient ObjectInputStream ois;	
	private transient ObjectOutputStream oos;	
	private transient InetSocketAddress serverRelayAddress;	
	private InetSocketAddress clientRelayAddress;			
	
	/**
	 * Creates a TURNRMISocketFactory.
	 * 
	 * @param turnServerAddress
	 * 		The public address of a TURN-Server, to which RMI-Servers can connect to.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * 
	 */
	public TURNRMISocketFactory(InetSocketAddress turnServerAddress) throws IOException, 
			ClassNotFoundException  {				
		Socket turnServer = new Socket();
		turnServer.connect(turnServerAddress);
			
		oos = new ObjectOutputStream(turnServer.getOutputStream());
		ois = new ObjectInputStream (turnServer.getInputStream());			

		oos.writeObject("ALLOCATE_RELAY_ADDRESSES");			
		String turnServerResponse = (String) ois.readObject();			

		if (!turnServerResponse.equals("RECEIVE_RELAY_ADDRESSES")) {
			throw new IOException("unexpected response from TURN server");
		}
		
		clientRelayAddress = (InetSocketAddress) ois.readObject();
		serverRelayAddress = (InetSocketAddress) ois.readObject();
	}	
	
	/**
	 * This implementation creates a ServerSocket,
	 * that is used for connections between the RMI-Server and the TURN-Server.
	 * 
	 * @param port
	 * 		Not used in this implementation. 
	 * 
	 * @return 
	 * 		Returns a TURNServerSocket, that implements TURN-driven behaviour.
	 * 		See class TURNServerSocket for more information.
	 * 
	 */
	
	public ServerSocket createServerSocket(int port) throws IOException {		
		return new TURNServerSocket(ois, serverRelayAddress);
	}

	/**
	 * This implementation creates a Socket,
	 * that is used for a connection between the client and the TURN-Server.
	 * 
	 * @param address
	 * 		Not used in this implementation. 
	 * 
	 * @param port
	 * 		Not used in this implementation. 
	 * 
	 * @return Returns Socket, that is connected to a relay address on the TURN-Server.
	 * 
	 */
	
	public Socket createSocket(String address, int port) throws IOException {
		return new Socket(clientRelayAddress.getAddress(), clientRelayAddress.getPort());
	}
	
}
