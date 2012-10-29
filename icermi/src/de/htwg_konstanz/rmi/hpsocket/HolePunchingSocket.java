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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class HolePunchingSocket extends ServerSocket {
	private Socket mediatorClient = null;

	public HolePunchingSocket() throws IOException {}

		
	public void register(int port, UUID id) {
		// connect with mediator from local socket
		// with the given port number (use SO_REUSEADDR)
		// pass (id, internalIP, port) to mediator to
		// register the end point
	}

	public void requestConnection(String host, int port, UUID id) {
		// from arbitrary socket, send contact request to mediator server,
		// including
		// own internal IP
		// own internal port
		// remote internal host as given
		// remote internal port as given
		// remote id as set in the variable id
	}

	public Socket accept() {
		// read contact request from mediator
		// perform actual hole punching, i.e.
		// - listen on port
		// - try to contact peer's internal IP
		// - try to contact peer's external IP
		// return connected socket
	}

	public Socket accept(String host, int port, UUID id) {
		// read contact request from mediator
		// if originator = (host, port, id) then
		// perform actual hole punching, i.e.
		// - listen on port
		// - try to contact peer's internal IP
		// - try to contact peer's external IP
		// return connected socket
		// fi
	}	
}
