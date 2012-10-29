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
package de.htwg_konstanz.rmi.hpsocket;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMIClientSocketFactory;
import java.util.UUID;


public class HolePunchingRmiSocketFactory implements RMIClientSocketFactory, 
	RMIServerSocketFactory, Serializable {
	
	private UUID id;

	public HolePunchingRmiSocketFactory() {
		id = UUID.randomUUID();
	}

	public Socket createSocket(String host, int port) throws IOException {
		HolePunchingSocket hpSocket = new HolePunchingSocket();
		hpSocket.requestConnection(host, port, id);
		return hpSocket.accept(host, port, id);
	}

	public ServerSocket createServerSocket(int port) throws IOException {
		HolePunchingSocket hpSocket = new HolePunchingSocket();
		hpSocket.register(port, id);
		return hpSocket;
	}

}
