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

package de.htwg_konstanz.in.uce.hp.parallel.mediator_browser;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.net.SocketFactory;

import junit.framework.Assert;

import org.junit.Test;

import de.htwg_konstanz.in.test.helper.mockito.MockitoSocket;
import de.htwg_konstanz.in.uce.hp.parallel.mediator_browser.MediatorBrowser;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.Message;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;

public class MediatorBrowserTest {
	
	@Test (expected = IOException.class )
	public void testGetSetOfRegisteredTargetsMediatorDown() throws IOException {
		ServerSocket dummy = new ServerSocket(0);
		int availablePort = dummy.getLocalPort();
		dummy.close();
		
		MediatorBrowser browser = new MediatorBrowser(InetAddress.getLocalHost(), availablePort);
		browser.getSetOfRegisteredTargets();
	}
	
	@Test
	public void testGetSetOfRegisteredTargetsDoConnect() throws IOException {
		ServerSocket dummy = new ServerSocket(0);
		final int availablePort = dummy.getLocalPort();
		dummy.close();
		
		ServerSocket ss = new ServerSocket();
		Socket s = null;
		try {
			ss.bind(new InetSocketAddress(InetAddress.getLocalHost(), availablePort));
			ss.setSoTimeout(1000);
			new Thread() {
				public void run() {
					MediatorBrowser browser;
					try {
						browser = new MediatorBrowser(InetAddress.getLocalHost(), availablePort);
						browser.getSetOfRegisteredTargets();					
					} catch (IOException e) {
						//ignore
					}
				};
			}.start();
			s = ss.accept();
			Assert.assertNotNull(s);
			Assert.assertTrue(s.isConnected());
			s.close();
		} finally {
			ss.close();
			if(s != null) {
				s.close();				
			}
		}
	}
	
	@Test
	public void testGetSetOfRegisteredTargetsSendListRequest() throws IOException {
		int port = 1234;
		MediatorBrowser browser = new MediatorBrowser(InetAddress.getLocalHost(), port);
		MockitoSocket mockitoSocket = new MockitoSocket();
		browser.setSocketFactory(new MockitoSocketFactory(mockitoSocket.getSocket()));
		MessageEncoder me = new MessageEncoder();
		Set<String> targets = new HashSet<String>();
		ListResponseMessage lrm = new ListResponseMessage(targets);
		mockitoSocket.setInputStreamData(me.encodeMessage(lrm));
		browser.getSetOfRegisteredTargets();
		MessageDecoder med = new MessageDecoder(mockitoSocket.getOutputStreamDataAsInputStream());
		Message request = med.decodeMessage();
		Assert.assertTrue(request instanceof ListRequestMessage);
	}
	
	@Test
	public void testGetSetOfRegisteredTargetsCloseSocket() throws IOException {
		int port = 1234;
		MediatorBrowser browser = new MediatorBrowser(InetAddress.getLocalHost(), port);
		MockitoSocket mockitoSocket = new MockitoSocket();
		browser.setSocketFactory(new MockitoSocketFactory(mockitoSocket.getSocket()));
		MessageEncoder me = new MessageEncoder();
		Set<String> targets = new HashSet<String>();
		ListResponseMessage lrm = new ListResponseMessage(targets);
		mockitoSocket.setInputStreamData(me.encodeMessage(lrm));
		browser.getSetOfRegisteredTargets();
		verify(mockitoSocket.getSocket()).close();
	}
	
	@Test
	public void testGetSetOfRegisteredTargetsReturn() throws IOException {
		int port = 1234;
		MediatorBrowser browser = new MediatorBrowser(InetAddress.getLocalHost(), port);
		MockitoSocket mockitoSocket = new MockitoSocket();
		MessageEncoder me = new MessageEncoder();
		Set<String> targets = new HashSet<String>();
		targets.add("test");
		targets.add("test1");
		ListResponseMessage lrm = new ListResponseMessage(targets);
		mockitoSocket.setInputStreamData(me.encodeMessage(lrm));
		browser.setSocketFactory(new MockitoSocketFactory(mockitoSocket.getSocket()));
		Set<String> result = browser.getSetOfRegisteredTargets();
		Assert.assertEquals(targets, result);
	}
	
	private static class MockitoSocketFactory extends SocketFactory {
		
		private final Socket s;
		
		private MockitoSocketFactory(Socket s) {
			this.s = s;
		}
		
		@Override
		public Socket createSocket() throws IOException {
			return s;
		}

		@Override
		public Socket createSocket(String host, int port) throws IOException,
				UnknownHostException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Socket createSocket(InetAddress host, int port)
				throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Socket createSocket(String host, int port,
				InetAddress localHost, int localPort) throws IOException,
				UnknownHostException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Socket createSocket(InetAddress address, int port,
				InetAddress localAddress, int localPort) throws IOException {
			throw new UnsupportedOperationException();
		}
		
	}
}
