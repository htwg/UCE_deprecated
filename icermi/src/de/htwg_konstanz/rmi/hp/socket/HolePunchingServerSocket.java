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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import de.htwg_konstanz.rmi.hp.mediator.StuntEndpoint;

public class HolePunchingServerSocket extends ServerSocket {

	private Socket mediatorClient = null;
	private int port;
	private UUID id;
	private InetSocketAddress mediatorSocketAddress;

	public HolePunchingServerSocket(int port, UUID id,
			InetSocketAddress mediatorSocketAddress) throws IOException {
		this.port = port;
		this.id = id;
		this.mediatorSocketAddress = mediatorSocketAddress;
	}

	public void register() throws IOException {
		mediatorClient = new Socket();
		mediatorClient.setReuseAddress(true);
		mediatorClient.bind(new InetSocketAddress(port));
		try {
			mediatorClient.connect(mediatorSocketAddress);
		} catch (IOException e) {
			throw new IOException("Can't connect to mediator");
		}

		StuntEndpoint p = new StuntEndpoint(id, new InetSocketAddress(
				InetAddress.getLocalHost(), mediatorClient.getLocalPort()));
		ObjectOutputStream out = new ObjectOutputStream(mediatorClient
				.getOutputStream());
		out.writeObject("register");
		out.writeObject(p);
		out.flush();
		ObjectInputStream in = new ObjectInputStream(mediatorClient
				.getInputStream());
		//wait until registration is finished
		boolean success = in.readBoolean();
	}

	public Socket accept() throws IOException {
		//TODO mediator wird ausgeschaltet während server object hier wartet (EOFException) reregister?
		StuntEndpoint clientEndpoint = null;
		try {
			ObjectInputStream in = new ObjectInputStream(mediatorClient
					.getInputStream());
			clientEndpoint = (StuntEndpoint) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			//Something is wrong with Mediator, try to reregister
			System.err.println("Connection lost to mediator, trying to reregister..");
			e.printStackTrace();
			register();
		}
		HolePuncher hp = new HolePuncher();
		return hp.doHolePunching(clientEndpoint.getRemoteInetSocketAddress(),
				clientEndpoint.getLocalInetSocketAddress(), mediatorClient
						.getLocalPort());
	}
}
