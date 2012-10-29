/**
 * Copyright (C) 2011 Thomas Zink
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

/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.connection_reversal.socket_factories;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import de.htwg_konstanz.in.uce.connection_reversal.socket.target.ConnectionReversalTarget;

/**
 * @author zink
 * 
 */
public final class ConnectionReversalServerSocket extends ServerSocket {
	private final static int KEEPALIVE_INTERVAL = 30;

	private final ConnectionReversalTarget target;

	public ConnectionReversalServerSocket(UUID id, InetSocketAddress mediatorSocketAddress) throws IOException {
		ConnectionReversalTarget.Builder builder = new ConnectionReversalTarget.Builder(
				id.toString(), KEEPALIVE_INTERVAL, 
				mediatorSocketAddress.getHostName(),
				mediatorSocketAddress.getPort());
		target = builder.build();
	}
	
	public void register() throws IOException {
		try {
			target.register();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public Socket accept() throws IOException {
		try {
			return target.accept();
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public void close() throws IOException {
		try {
			target.deregister();
		} catch (Exception e) {
			throw new IOException();
		}
		super.close();
	}

}
