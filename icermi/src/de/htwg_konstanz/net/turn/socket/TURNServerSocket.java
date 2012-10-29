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
package de.htwg_konstanz.net.turn.socket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A TURNServerSocket is used with a RMITURNSocketFactory createServerSocket()-Method.
 * It has the ability to connect to a TURN-Servers relay address, by command from a TURN-Server.  
 * 
 * @author Andre Erb
 * 
 */
public class TURNServerSocket extends ServerSocket {
	private ObjectInputStream turnServerStream = null;	
	private InetSocketAddress serverRelayAddress = null;	
	
	public TURNServerSocket(ObjectInputStream turnServerCommandStream, 
				InetSocketAddress serverRelayAddress) throws IOException 
	{
		super();
		this.turnServerStream = turnServerCommandStream; 
		this.serverRelayAddress = serverRelayAddress;
	}
	
	/**
	 * Inherited from Java ServerSocket.
	 * This implementation waits for a command from the TURN-Server, in order to connect to it.
	 * 
	 * @return 
	 * 		A Socket that is connected to a TURN-Server.
	 * 		It represents the endpoint from the RMI-Data connection,
	 * 		between a RMI-Server and the TURN-Server.
	 */
	@Override
	public Socket accept() {
		try {
			String command = (String)turnServerStream.readObject();
			
			if (command.equals("CONNECT_TO_SERVER_RELAY_ADDRESS")) {
				// Connect to the relay address to establish a RMI-Data-Connection and return the Socket
				return new Socket(serverRelayAddress.getAddress(), serverRelayAddress.getPort());				
			}
			else return null;			
		} 
		
		catch (IOException e1) {
			System.out.println("The TURN-Server is unavailable!");
			e1.printStackTrace();
			try {
				super.close();	// If the TURN-Server is unavailable close the ServerSocket to stop the accept-loop 
			} 
			catch (IOException e2) {
				e2.printStackTrace();
			}
			return null;			
		} 
		
		catch (ClassNotFoundException e) {
			System.out.println("Received wrong command class in TURNServerSocket.accept()");
			e.printStackTrace();
			return null;
		}	
	}

}
