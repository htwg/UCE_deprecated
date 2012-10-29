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
import java.net.*;


/**
 * A Server that implements TURN-like behaviour, to traverse RMI-Data through Symmetric NATs.
 * The TURN-Server works together with RMI-Servers,
 * that use a RMITURNSocketFactory for their RemoteObjects.
 * 
 * @author Andre Erb
 * 
 */
public class TURNServer {	
	public static final int TURNSERVER_PORT = 9999;
	
	private ServerSocket mainServerSocket = null; 	
	
	/**
     * Creates a TURN-Server.
     * 
     * @param port
     *            The port on which the TURN-Server listens for incoming RMI-Server connections.
	 * @throws IOException 
     */
	public TURNServer(int port) throws IOException {
		mainServerSocket =  new ServerSocket(port);
	}
	
	/**
     * Accepts incoming connections to the TURN-Server.
     * Each TURN-Server Client is handled in a different thread.
	 * @throws IOException 
     */
	public void acceptConnections() throws IOException {
		while (!mainServerSocket.isClosed()) {
			Socket socket = mainServerSocket.accept();
			new TURNServerThread(socket).start();				
		} 
	}
	
	/**
	 * Starts the TURN-Server.
	 * 
	 * @param args
	 * 		The port, at which the TURN-Server listens for incoming connections.
	 * 		If nothing is defined at beginning, port 9999 is chosen as default.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {		
		int port = TURNSERVER_PORT;

		try {
			port = Integer.valueOf(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			// no port argument --> stick with TURNSERVER_PORT
		} catch (NumberFormatException e) {
			// wrong port format
			System.out.println("Unrecognized argument " + args[0]
					+ "; you can optionally specify a port number.");
			System.exit(0);
		}
		
		TURNServer turnServer = new TURNServer(port);
		System.out.println("TURN-Server is running on port " + port);
		turnServer.acceptConnections();
	}

}
