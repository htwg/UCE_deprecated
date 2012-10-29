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
package de.htwg_konstanz.rmi.hp.socket;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.UUID;


public class HolePunchingRmiSocketFactory implements RMIClientSocketFactory, 
	RMIServerSocketFactory, Serializable {
	
	private static final long serialVersionUID = 3603126460143331294L;
	
	private UUID id;
	
	private InetSocketAddress mediatorSocketAddress;
	
	public HolePunchingRmiSocketFactory(InetSocketAddress mediatorSocketAddress) {
		id = UUID.randomUUID();
		this.mediatorSocketAddress = mediatorSocketAddress;
	}

	public Socket createSocket(String host, int port) throws IOException {
		HolePunchingSocket hpSocket = new HolePunchingSocket();
		return hpSocket.requestConnection(id, mediatorSocketAddress);
	}

	public ServerSocket createServerSocket(int port) throws IOException {
		HolePunchingServerSocket hpSocket = new HolePunchingServerSocket(port, id, mediatorSocketAddress);
		hpSocket.register();
		return hpSocket;
	}
}
