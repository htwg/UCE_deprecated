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
package de.htwg_konstanz.rmi.hp.mediator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Mediator {

	private Map<StuntEndpoint, Socket> endpoints = new HashMap<StuntEndpoint, Socket>();

	public Mediator(int port) {
		try {
			ServerSocket ss = new ServerSocket(port);
			System.out.println("Mediator up and listening on port " + port);
			while (true) {
				Socket cs = ss.accept();
				ConnectionThread t = new ConnectionThread(cs);
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void register(StuntEndpoint endpoint, Socket s) {
		endpoints.put(endpoint, s);
		System.out.println("New endpoint registered: " + endpoint);

	}

	private StuntEndpoint requestConnection(StuntEndpoint endpoint) {
		System.out.println("Incoming Request to Endpoint: " + endpoint.getId());
		for (StuntEndpoint p : endpoints.keySet()) {
			if (p.getId().equals(endpoint.getId())) {
				return p;
			}
		}
		return null;
	}

	private class ConnectionThread extends Thread {
		private Socket s;

		public ConnectionThread(Socket s) throws IOException {
			this.s = s;
		}

		public void run() {
			try {
				ObjectInputStream in = new ObjectInputStream(s.getInputStream());
				String command = (String) in.readObject();
				//TODO deregister?
				if (command.equals("register")) {
					StuntEndpoint toRegister = (StuntEndpoint) in.readObject();
					toRegister.setRemoteInetSocketAddress(new InetSocketAddress(s.getInetAddress(), s.getPort()));
					register(toRegister, s);
					ObjectOutputStream out = new ObjectOutputStream(s
							.getOutputStream());
					out.writeBoolean(true);
					out.flush();
				}
				if (command.equals("requestConnection")) {
					StuntEndpoint requestedEnpoint = (StuntEndpoint) in
							.readObject();
					StuntEndpoint serverEndpoint = requestConnection(requestedEnpoint);
					StuntEndpoint clientEndpoint = new StuntEndpoint(
							requestedEnpoint.getId(), requestedEnpoint.getLocalInetSocketAddress());
					clientEndpoint.setRemoteInetSocketAddress(new InetSocketAddress(s.getInetAddress(), s.getPort()));
					Socket serverSocket = endpoints.get(serverEndpoint);

					// Send Requesting Client Endpoint to Server Object 
					try {
						ObjectOutputStream outServer = new ObjectOutputStream(
								serverSocket.getOutputStream());
						outServer.writeObject(clientEndpoint);
					} catch (Exception e) {
						// Server Object isn't online any longer or isn't registered
						endpoints.remove(serverEndpoint);
						ObjectOutputStream out = new ObjectOutputStream(s
								.getOutputStream());
						out.writeObject(null);
						s.close();
						return;
					}
					// Send found Server Endpoint to client
					ObjectOutputStream out = new ObjectOutputStream(s
							.getOutputStream());
					out.writeObject(serverEndpoint);
					in.readObject();
					in.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
