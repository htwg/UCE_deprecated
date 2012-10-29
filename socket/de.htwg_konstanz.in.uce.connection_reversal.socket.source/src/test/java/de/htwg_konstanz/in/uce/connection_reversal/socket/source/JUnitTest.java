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

package de.htwg_konstanz.in.uce.connection_reversal.socket.source;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.htwg_konstanz.in.uce.messages.CommonUceMethod;
import de.htwg_konstanz.in.uce.messages.SemanticLevel;
import de.htwg_konstanz.in.uce.messages.SocketEndpoint;
import de.htwg_konstanz.in.uce.messages.UceMessage;
import de.htwg_konstanz.in.uce.messages.UceMessageReader;
import de.htwg_konstanz.in.uce.messages.UceMessageStaticFactory;
import de.htwg_konstanz.in.uce.messages.UniqueUserName;

public class JUnitTest {
	
	static String mediatorIP;
	static String uniqueUserName;
	static int mediatorPort;
	static ConnectionReversalSource connectionReversalSource;
	static ConnectionReversalSource.Builder crsBuilder;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		mediatorIP = "127.0.0.1";
		uniqueUserName = "source";
		mediatorPort = 11111;
		
		crsBuilder = new ConnectionReversalSource.Builder();
		
		crsBuilder.setMediatorIP(mediatorIP);
		crsBuilder.setMediatorPort(mediatorPort);
		crsBuilder.setUniqueUserName(uniqueUserName);
	}
	
	@Before
	public void setUpBeforeMethod() throws Exception {
		
		connectionReversalSource = crsBuilder.build();
	}
	
	@Test
	public void getUserListOne() throws Exception {
		
		Set<String> userListOriginal = new HashSet<String>();
		
		userListOriginal.add("JUnitUserA");
		
		MediatorStub mediatorStub = new MediatorStub(userListOriginal);
		mediatorStub.start();
		
		Set<String> userListResponse = connectionReversalSource.getUserList();
		
		Assert.assertEquals(userListOriginal.size(), userListResponse.size());
		
		for (String userName : userListOriginal) {
			
			Assert.assertTrue(userListResponse.contains(userName));
		}
	}
	
	@Test
	public void getUserListNull() throws Exception {
		
		Set<String> userListOriginal = new HashSet<String>();
		
		MediatorStub mediatorStub = new MediatorStub(userListOriginal);
		mediatorStub.start();
		
		Set<String> userListResponse = connectionReversalSource.getUserList();
		
		Assert.assertEquals(userListOriginal.size(), userListResponse.size());
		
		for (String userName : userListOriginal) {
			
			Assert.assertTrue(userListResponse.contains(userName));
		}
	}
	
	@Test
	public void getUserListMore() throws Exception {
		
		String stringWith47Byte = "JUnitUser";
		Set<String> userListOriginal = new HashSet<String>();
		
		for (int i = 48; i < 123; ++i) {
			
			if (i == 48) i = 65;
			if (i == 91) i = 97;
			
			userListOriginal.add(stringWith47Byte + (char)i);
		}
		
		MediatorStub mediatorStub = new MediatorStub(userListOriginal);
		mediatorStub.start();
		
		Set<String> userListResponse = connectionReversalSource.getUserList();
		
		Assert.assertEquals(userListOriginal.size(), userListResponse.size());
		
		for (String userName : userListOriginal) {
			
			Assert.assertTrue(userListResponse.contains(userName));
		}
	}
	
	@Test
	public void getUserListLarge() throws Exception {
		
		String stringWith47Byte = "DieserStringEnthaeltGenau47ByteAnDatenFuerTests";
		Set<String> userListOriginal = new HashSet<String>();
		
		for (int i = 48; i < 123; ++i) {
			
			if (i == 48) i = 65;
			if (i == 91) i = 97;
			
			userListOriginal.add(stringWith47Byte + (char)i);
		}
		
		MediatorStub mediatorStub = new MediatorStub(userListOriginal);
		mediatorStub.start();
		
		Set<String> userListResponse = connectionReversalSource.getUserList();
		
		Assert.assertEquals(userListOriginal.size(), userListResponse.size());
		
		for (String userName : userListOriginal) {
			
			Assert.assertTrue(userListResponse.contains(userName));
		}
	}
	
	@Test
	public void TestConnectionFlex() throws Exception {
		
		MediatorStub mediatorStub = new MediatorStub();
		mediatorStub.start();
		
		connectionReversalSource.connect();
	}
	
	@Test
	public void TestConnectionPort() throws Exception {
		
		MediatorStub mediatorStub = new MediatorStub();
		mediatorStub.start();
		
		connectionReversalSource.connect(22222);
	}
	
	@Test (expected=SocketTimeoutException.class)
	public void TestConnectionNoMedPort() throws Exception {
		
		connectionReversalSource.connect(22222);
	}
	
	@Test (expected=SocketTimeoutException.class)
	public void TestConnectionNoMedFlex() throws Exception {
		
		connectionReversalSource.connect();
	}
	
	@Test (expected=IllegalStateException.class)
	public void TestConnectionMore() throws Exception {
		
		MediatorStub mediatorStub = new MediatorStub();
		mediatorStub.start();
		
		connectionReversalSource.connect();
		connectionReversalSource.connect();
	}
	
	class MediatorStub extends Thread {
		
		private InetAddress sourceAddress;
		private int sourcePort;
		private UceMessage uceRequestMessage;
		private DatagramSocket datagramSocket;
		private DatagramPacket datagramPacket;
		private Set<String> userList;
		
		public MediatorStub() throws SocketException {
			
			datagramSocket = new DatagramSocket(11111);
			datagramPacket = new DatagramPacket(new byte[512], 512);
		}
		
		public MediatorStub(Set<String> userList) throws SocketException {
			
			this.userList = userList;
			
			datagramSocket = new DatagramSocket(11111);
			datagramPacket = new DatagramPacket(new byte[512], 512);
		}
		
		public void run() {
			
			try {
				
				datagramSocket.receive(datagramPacket);
				
				sourceAddress = datagramPacket.getAddress();
				sourcePort = datagramPacket.getPort();
				byte[] data = datagramPacket.getData();
				
				UceMessageReader uceMessageReader = new UceMessageReader();
				uceRequestMessage = uceMessageReader.readUceMessage(data);
				
				if (uceRequestMessage.isMethod(CommonUceMethod.CONNECTION_REQUEST)) connectionRequest();
				else if (uceRequestMessage.isMethod(CommonUceMethod.LIST)) list();
			}
			catch (IOException e) {
				
				e.printStackTrace();
			}
			finally {
				
				datagramSocket.close();
			}
		}
		
		private void list() throws IOException {
			
			UUID uuid = uceRequestMessage.getTransactionId();
			
			UceMessage uceResponseMessage = UceMessageStaticFactory.newUceMessageInstance(
					CommonUceMethod.LIST, SemanticLevel.SUCCESS_RESPONSE, uuid);
			
			for (String userName : userList) {
				
				UniqueUserName uniqueUserName = new UniqueUserName(userName);
				
				uceResponseMessage.addAttribute(uniqueUserName);
			}
			
			byte[] buf = uceResponseMessage.toByteArray();
			
			DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, sourceAddress, sourcePort);
			
			datagramSocket.send(datagramPacket);
			datagramSocket.close();
		}
		
		private void connectionRequest() {
			
			SocketEndpoint socketEndpoint = uceRequestMessage.getAttribute(SocketEndpoint.class);
			
			try {
				
				String hostName = "127.0.0.1";
				int portNumber = socketEndpoint.getEndpoint().getPort();
				
				new Socket(hostName, portNumber);
			}
			catch (Exception e) {
				
				e.printStackTrace();
			}
		}
	}
	
	@After
	public void tearDownAfterClass() throws Exception {
		
		connectionReversalSource = null;
	}
}
