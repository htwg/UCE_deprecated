/**
 * Copyright (C) 2011 Daniel Maier
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

package de.htwg_konstanz.in.uce.hp.parallel.mediator;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.uce.hp.parallel.mediator.ListenerThread;

public class ListenerThreadTest {

	private int listenerPort;

	@Before
	public void setUp() throws IOException {
		ServerSocket dummy = new ServerSocket(0);
		listenerPort = dummy.getLocalPort();
		dummy.close();
	}

	@Test
	public void testInterrupt() throws IOException, InterruptedException {
		final ThreadGroup tg = new ThreadGroup("testInterruptGroup");
		Thread starter = new Thread(tg, "starter") {
			@Override
			public void run() {
				try {
					final ListenerThread lt = new ListenerThread(listenerPort);
					lt.start();
					Thread.sleep(200);
					lt.interrupt();
					lt.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		starter.start();
		starter.join();
		Socket s = new Socket();
		try {
			s.connect(new InetSocketAddress(listenerPort));
			Assert.fail("Got connection, but listener should be terminated");
		} catch (IOException e) {
			Assert.assertTrue(tg.activeCount() == 0);
		}
	}
}
