/**
 * Copyright (C) 2011 Stefan Lohr
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

package de.htwg_konstanz.in.uce.connection_reversal.mediator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listener Class. Listens on UPD-Socket for incoming messages.
 * These messages get interpreted in the HandleMessage class.
 * 
 * @author Stefan Lohr
 */
public class ListenerThread extends Thread {
	
	private Executor executor;
	private DatagramSocket datagramSocket;
	private static final Logger logger = LoggerFactory.getLogger(ListenerThread.class);
	
	/**
	 * Constructor; initializes DatagramSocket, Logger, Executors
	 * 
	 * @param port Port number to listen on
	 */
	public ListenerThread(int port) {
		
		executor = Executors.newCachedThreadPool();
		
		try {
			
			this.datagramSocket = new DatagramSocket(port);
			
			logger.info("Mediator listening on Port {}", port);
		}
		catch (SocketException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * Listen on UDP-Socket for incoming messages.
	 * Endless while-loop, can be stopped with interrupt().
	 * The messages get interpreted in the HandleMessage class.
	 */
	public void run() {
		
		try {
			
			while (!isInterrupted()) {
				
				/*
				 * Maximale Größe anhand der größt möglichen Nachricht:
				 * ####################################################
				 * 20 Bytes Grundnachricht Header mit uuid und so
				 * 22 Bytes Maximal für den SocketEndpoint (IPv6[16]+Port[2]+Header[4])
				 * 52 Bytes UniqueUserName (UserName[48]+Header[4])
				 * --------------------------------------...
				 * 94 Bytes Gesamt (Maximalgröße)
				 * ==============================
				 */
				
				DatagramPacket datagramPacket = new DatagramPacket(new byte[94], 94);
				datagramSocket.receive(datagramPacket);
				
				HandleMessage handleMessage = new HandleMessage(datagramPacket, datagramSocket);
				
				executor.execute(handleMessage);
			}
		}
		catch (SocketException e) {
			
			e.printStackTrace();
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
