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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.UUID;

import de.htwg_konstanz.rmi.hp.mediator.StuntEndpoint;

public class HolePunchingSocket {
	private Socket mediatorClient = null;

	public HolePunchingSocket() throws IOException {
	}

	public Socket requestConnection(UUID id,
			InetSocketAddress mediatorSocketAddress) throws IOException {
		mediatorClient = new Socket();
		mediatorClient.setReuseAddress(true);
		try {
			mediatorClient.connect(mediatorSocketAddress);
		} catch (IOException e) {
			throw new IOException("Can't connect to Mediator");
		}
		ObjectOutputStream out = new ObjectOutputStream(mediatorClient
				.getOutputStream());

		StuntEndpoint toFind = new StuntEndpoint(id, new InetSocketAddress(
				InetAddress.getLocalHost(), mediatorClient.getLocalPort()));
		out.writeObject("requestConnection");
		out.writeObject(toFind);

		ObjectInputStream in = new ObjectInputStream(mediatorClient
				.getInputStream());
		StuntEndpoint foundServerEndpoint = null;
		try {
			foundServerEndpoint = (StuntEndpoint) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if (foundServerEndpoint == null) {
			mediatorClient.close();
			throw new ConnectException(
					"Requested Server Endpoint could not be notified by Mediator to initiate hole punching");
		}
		HolePuncher hp = new HolePuncher();
		Socket s = hp.doHolePunching(foundServerEndpoint
				.getRemoteInetSocketAddress(), foundServerEndpoint
				.getLocalInetSocketAddress(), mediatorClient.getLocalPort());
		out.writeObject("close");
		mediatorClient.close();
		return s;
	}

}
