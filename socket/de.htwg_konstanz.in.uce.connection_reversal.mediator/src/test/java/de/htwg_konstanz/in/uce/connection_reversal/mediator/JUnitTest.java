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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.htwg_konstanz.in.test.helper.concurrency.MultiThreadedExceptionsRunner;
import de.htwg_konstanz.in.uce.messages.CommonUceMethod;
import de.htwg_konstanz.in.uce.messages.SemanticLevel;
import de.htwg_konstanz.in.uce.messages.SocketEndpoint;
import de.htwg_konstanz.in.uce.messages.SocketEndpoint.EndpointClass;
import de.htwg_konstanz.in.uce.messages.UceMessage;
import de.htwg_konstanz.in.uce.messages.UceMessageReader;
import de.htwg_konstanz.in.uce.messages.UceMessageStaticFactory;
import de.htwg_konstanz.in.uce.messages.UniqueUserName;

@RunWith (MultiThreadedExceptionsRunner.class)
public class JUnitTest {
	
	private static DatagramSocket datagramSocket;
	private static SocketAddress socketAddress;
	private static ListenerThread listenerThread;
	private static UserCleaner userCleaner;
	private static String mediatorIP;
	private static int mediatorPort;
	private static int iterationTimeInSeconds;
	private static int maxLifeTimeInSeconds;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		mediatorPort = 11111;
		iterationTimeInSeconds = 30;
		maxLifeTimeInSeconds = 30;
		mediatorIP = "127.0.0.1";
		
		socketAddress = new InetSocketAddress(mediatorIP, mediatorPort);
		datagramSocket = new DatagramSocket();
		
		listenerThread = new ListenerThread(mediatorPort);
		userCleaner = new UserCleaner(iterationTimeInSeconds, maxLifeTimeInSeconds);
		
		listenerThread.start();
		userCleaner.start();
	}
	
	@Test
	public void registerOneTarget() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		registerTarget("JUnitTargetA");
		
		userList.clearUserList();
	}
	
	@Test (expected=NoSuccessException.class)
	public void registerNullTarget() throws Exception {
		
		UserList.getInstance().clearUserList();
		
		registerTarget(null);
	}
	
	@Test
	public void registerMoreTargets() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		registerTarget("JUnitTargetA");
		registerTarget("JUnitTargetB");
		registerTarget("JUnitTargetC");
		registerTarget("JUnitTargetD");
		registerTarget("JUnitTargetE");
		registerTarget("JUnitTargetF");
		
		userList.clearUserList();
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void registerLargeUserName() throws Exception {
		
		String stringWith49Byte = "DieserStringEnthaeltGenauEinByteZuvielAnTestDaten";
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		registerTarget(stringWith49Byte);
		
		userList.clearUserList();
	}
	
	@Test
	public void deregisterOneTarget() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		userList.addUser(new UserData("JUnitTargetA", new InetSocketAddress(0)));
		
		deregisterTarget("JUnitTargetA");
		
		userList.clearUserList();
	}
	
	@Test (expected=NoSuccessException.class)
	public void deregisterNullTarget() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		deregisterTarget(null);
	}
	
	@Test
	public void deregisterMoreTargets() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		userList.addUser(new UserData("JUnitTargetA", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitTargetB", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitTargetC", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitTargetD", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitTargetE", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitTargetF", new InetSocketAddress(0)));
		
		deregisterTarget("JUnitTargetA");
		deregisterTarget("JUnitTargetB");
		deregisterTarget("JUnitTargetC");
		deregisterTarget("JUnitTargetD");
		deregisterTarget("JUnitTargetE");
		deregisterTarget("JUnitTargetF");
		
		userList.clearUserList();
	}
	
	@Test
	public void getUserListSmall() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		userList.addUser(new UserData("JUnitUserA", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitUserB", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitUserC", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitUserD", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitUserE", new InetSocketAddress(0)));
		userList.addUser(new UserData("JUnitUserF", new InetSocketAddress(0)));
		
		Set<String> users = getUserList();
		
		Assert.assertEquals(6, users.size());
		Assert.assertTrue(users.contains("JUnitUserF"));
	}
	
	@Test
	public void getUserListLarge() throws Exception {
		
		String stringWith47Byte = "DieserStringEnthaeltGenau47ByteAnDatenFuerTests";
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		for (int i = 48; i < 123; ++i) {
			
			if (i == 48) i = 65;
			if (i == 91) i = 97;
			
			userList.addUser(new UserData(stringWith47Byte + (char)i, new InetSocketAddress(0)));
		}
		
		Set<String> users = getUserList();
		
		Assert.assertEquals(52, users.size());
		Assert.assertTrue(users.contains(stringWith47Byte + (char)122));
	}
	
	@Test
	public void getUserListEmpty() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		Set<String> users = getUserList();
		
		Assert.assertEquals(0, users.size());
	}
	
	@Test
	public void sendKeepAliveOne() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		userList.addUser(new UserData("JUnitTargetA", new InetSocketAddress(0)));
		
		sendKeepAlive("JUnitTargetA");
	}
	
	@Test (expected=NoSuccessException.class)
	public void sendKeepAliveUnknown() throws Exception {
		
		UserList.getInstance().clearUserList();
		
		sendKeepAlive("JUnitTargetA");
	}
	
	@Test (expected=NoSuccessException.class)
	public void sendKeepAliveNull() throws Exception {
		
		sendKeepAlive(null);
	}
	
	@Test (expected=NoSuccessException.class)
	public void sendUnknownMessage() throws Exception {
		
		UceMessage uceMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.AUTHENTICATE, SemanticLevel.REQUEST, UUID.randomUUID());
		
		byte[] buf = uceMessage.toByteArray();
		
		DatagramPacket datagrammPacket = new DatagramPacket(buf, buf.length, socketAddress);
		
		datagramSocket.send(datagrammPacket);
		
		DatagramPacket datagramPacket = new DatagramPacket(new byte[512], 512);
		
		datagramSocket.setSoTimeout(3000);
		try {
			
			datagramSocket.receive(datagramPacket);
		}
		finally {
			
			datagramSocket.setSoTimeout(0);
		}
		
		byte[] data = datagramPacket.getData();
		
		UceMessageReader uceMessageReader = new UceMessageReader();
		UceMessage uceResponseMessage = uceMessageReader.readUceMessage(data);
		
		if (!uceResponseMessage.isMethod(CommonUceMethod.AUTHENTICATE)) throw new Exception("Wrong response message");
		if (!uceResponseMessage.isSuccessResponse()) throw new NoSuccessException();
	}
	
	@Test
	public void connectionRequestExist() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		userList.addUser(new UserData("JUnitUserA", new InetSocketAddress("127.0.0.1", 22222)));
		
		SourceListenerThread sourceListenerThread = new SourceListenerThread(22222);
		sourceListenerThread.start();
		
		connectionRequest("JUnitUserA");
	}
	
	@Test (expected=SocketTimeoutException.class)
	public void connectionRequestUnknown() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		TargetListenerThread targetListenerThread = new TargetListenerThread(datagramSocket);
		targetListenerThread.start();
		
		connectionRequest("JUnitUserUnknown");
	}
	
	@Test (expected=SocketTimeoutException.class)
	public void connectionRequestNull() throws Exception {
		
		UserList userList = UserList.getInstance();
		
		userList.clearUserList();
		
		connectionRequest(null);
	}
	
	private void connectionRequest(String uniqueUserName) throws Exception {
		
		ServerSocket serverSocket = new ServerSocket(0);
		
		UceMessage uceConnectionRequestMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.CONNECTION_REQUEST, SemanticLevel.REQUEST, UUID.randomUUID());
		
		InetAddress sourceAddress = serverSocket.getInetAddress();
		int sourcePort = serverSocket.getLocalPort();
		InetSocketAddress endpoint = new InetSocketAddress(sourceAddress, sourcePort);
		EndpointClass endpointClass = SocketEndpoint.EndpointClass.CONNECTION_REVERSAL;
		SocketEndpoint socketEndpoint = new SocketEndpoint(endpoint, endpointClass);
		
		if (uniqueUserName != null) uceConnectionRequestMessage.addAttribute(new UniqueUserName(uniqueUserName));
		uceConnectionRequestMessage.addAttribute(socketEndpoint);
		
		byte[] buf = uceConnectionRequestMessage.toByteArray();
		
		DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, socketAddress);
		
		datagramSocket.send(datagramPacket);
		
		serverSocket.setSoTimeout(1500);
		
		try {
			
			serverSocket.accept();
		}
		catch (SocketTimeoutException e) {
			
			serverSocket.setSoTimeout(0);
			
			throw new SocketTimeoutException();
		}
	}
	
	private void sendKeepAlive(String uniqueUserName) throws Exception {
		
		UceMessage uceMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.KEEP_ALIVE, SemanticLevel.REQUEST, UUID.randomUUID());
		
		if (uniqueUserName != null) uceMessage.addAttribute(new UniqueUserName(uniqueUserName));
		
		byte[] buf = uceMessage.toByteArray();
		
		DatagramPacket datagrammPacket = new DatagramPacket(buf, buf.length, socketAddress);
		
		datagramSocket.send(datagrammPacket);
		
		DatagramPacket datagramPacket = new DatagramPacket(new byte[512], 512);
		
		datagramSocket.setSoTimeout(3000);
		try {
			
			datagramSocket.receive(datagramPacket);
		}
		finally {
			
			datagramSocket.setSoTimeout(0);
		}
		
		byte[] data = datagramPacket.getData();
		
		UceMessageReader uceMessageReader = new UceMessageReader();
		UceMessage uceResponseMessage = uceMessageReader.readUceMessage(data);
		
		if (!uceResponseMessage.isMethod(CommonUceMethod.KEEP_ALIVE)) throw new Exception("Wrong response message");
		if (!uceResponseMessage.isSuccessResponse()) throw new NoSuccessException();
	}
	
	private Set<String> getUserList() throws Exception {
		
		UceMessage uceListMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.LIST, SemanticLevel.REQUEST, UUID.randomUUID());
		
		byte[] requestBuffer = uceListMessage.toByteArray();
		
		DatagramPacket datagramPacketSend = new DatagramPacket(requestBuffer, requestBuffer.length, socketAddress);
		DatagramPacket datagramPacketReceive = new DatagramPacket(new byte[65536], 65536);
		
		datagramSocket.send(datagramPacketSend);
		
		datagramSocket.setSoTimeout(3000);
		try {
			
			datagramSocket.receive(datagramPacketReceive);
		}
		catch (SocketTimeoutException ste) {
			
			ste.printStackTrace();
			
			return new HashSet<String>();
		}
		finally {
			
			datagramSocket.setSoTimeout(0);
		}
		
		byte[] userListBuffer = datagramPacketReceive.getData();
		
		UceMessageReader uceMessageReader = new UceMessageReader();
		UceMessage uceMessage = uceMessageReader.readUceMessage(userListBuffer);
		
		List<UniqueUserName> uniqueUserNameList = uceMessage.getAttributes(UniqueUserName.class);
		Set<String> userList = new HashSet<String>();
		
		for (UniqueUserName uniqueUserName : uniqueUserNameList) {
			
			userList.add(uniqueUserName.getUniqueUserName());
		}
		
		return userList;
	}
	
	private void deregisterTarget(String uniqueUserName) throws Exception {
		
		UceMessage uceDeregisterMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.DEREGISTER, SemanticLevel.REQUEST, UUID.randomUUID());
		
		if (uniqueUserName != null) uceDeregisterMessage.addAttribute(new UniqueUserName(uniqueUserName));
		
		byte[] buf = uceDeregisterMessage.toByteArray();
		
		DatagramPacket datagrammPacket = new DatagramPacket(buf, buf.length, socketAddress);
		
		datagramSocket.send(datagrammPacket);
		
		DatagramPacket datagramPacket = new DatagramPacket(new byte[512], 512);
		
		datagramSocket.setSoTimeout(3000);
		try {
			
			datagramSocket.receive(datagramPacket);
		}
		finally {
			
			datagramSocket.setSoTimeout(0);
		}
		
		byte[] data = datagramPacket.getData();
		
		UceMessageReader uceMessageReader = new UceMessageReader();
		UceMessage uceResponseMessage = uceMessageReader.readUceMessage(data);
		
		if (!uceResponseMessage.isMethod(CommonUceMethod.DEREGISTER)) throw new Exception("Wrong response message");
		if (!uceResponseMessage.isSuccessResponse()) throw new NoSuccessException();
	}
	
	private void registerTarget(String uniqueUserName) throws Exception {
		
		UceMessage uceRegisterMessage = UceMessageStaticFactory.newUceMessageInstance(
				CommonUceMethod.REGISTER, SemanticLevel.REQUEST, UUID.randomUUID());
		
		if (uniqueUserName != null) uceRegisterMessage.addAttribute(new UniqueUserName(uniqueUserName));
		
		byte[] buf = uceRegisterMessage.toByteArray();
		
		DatagramPacket datagrammPacket = new DatagramPacket(buf, buf.length, socketAddress);
		
		datagramSocket.send(datagrammPacket);
		
		DatagramPacket datagramPacket = new DatagramPacket(new byte[512], 512);
		
		datagramSocket.setSoTimeout(3000);
		try {
			
			datagramSocket.receive(datagramPacket);
		}
		finally {
			
			datagramSocket.setSoTimeout(0);
		}
		
		byte[] data = datagramPacket.getData();
		
		UceMessageReader uceMessageReader = new UceMessageReader();
		UceMessage uceResponseMessage = uceMessageReader.readUceMessage(data);
		
		if (!uceResponseMessage.isMethod(CommonUceMethod.REGISTER)) throw new Exception("Wrong response message");
		if (!uceResponseMessage.isSuccessResponse()) throw new NoSuccessException();
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
		listenerThread.interrupt();
		userCleaner.interrupt();
	}
	
	class TargetListenerThread extends Thread {
		
		DatagramSocket datagramSocket;
		DatagramPacket datagramPacket;
		
		public TargetListenerThread(DatagramSocket datagramSocket) {
			
			this.datagramSocket = datagramSocket;
			datagramPacket = new DatagramPacket(new byte[512], 512);
		}
		
		public void run() {
			
			try {
				
				datagramSocket.setSoTimeout(3000);
				
				datagramSocket.receive(datagramPacket);
				
				byte[] data = datagramPacket.getData();
				
				UceMessageReader uceMessageReader = new UceMessageReader();
				UceMessage uceResponseMessage = uceMessageReader.readUceMessage(data);
				
				if (!uceResponseMessage.isMethod(CommonUceMethod.CONNECTION_REQUEST)) Assert.fail("wrong message");
				if (!uceResponseMessage.isErrorResponse()) Assert.fail("error response expected");
			}
			catch (SocketException e) {
				
				Assert.fail("Set Socket Timeout");
			}
			catch (IOException e) {
				
				Assert.fail("Socket Receive Fail");
			}
			finally {
				
				try {
					
					datagramSocket.setSoTimeout(0);
				}
				catch (SocketException e) {
					
					Assert.fail("Set Socket Timeout or Close");
				}
			}
		}
	}
	
	class SourceListenerThread extends Thread {
		
		DatagramSocket datagramSocket;
		DatagramPacket datagramPacket;
		
		public SourceListenerThread(int port) throws SocketException {
			
			datagramSocket = new DatagramSocket(port);
			datagramPacket = new DatagramPacket(new byte[512], 512);
		}
		
		public void run() {
			
			try {
				
				datagramSocket.setSoTimeout(3000);
				
				datagramSocket.receive(datagramPacket);
				
				byte[] data = datagramPacket.getData();
				
				UceMessageReader uceMessageReader = new UceMessageReader();
				UceMessage uceResponseMessage = uceMessageReader.readUceMessage(data);
				
				if (!uceResponseMessage.isMethod(CommonUceMethod.CONNECTION_REQUEST)) Assert.fail("wrong message");
				
				if (!uceResponseMessage.hasAttribute(SocketEndpoint.class)) {
					
					Assert.fail("SocketEndpoint Attribute Expected");
				}
				
				SocketEndpoint socketEndpoint = uceResponseMessage.getAttribute(SocketEndpoint.class);
				
				InetAddress inetAddress = socketEndpoint.getEndpoint().getAddress();
				int port = socketEndpoint.getEndpoint().getPort();
				
				System.out.println(inetAddress.toString() + ':' + port);
				
				Socket socket = new Socket(inetAddress, port);
				
				socket.close();
			}
			catch (SocketException e) {
				
				Assert.fail("Set Socket Timeout");
			}
			catch (IOException e) {
				
				Assert.fail("Socket Receive Fail");
			}
			finally {
				
				try {
					
					datagramSocket.setSoTimeout(0);
					datagramSocket.close();
				}
				catch (SocketException e) {
					
					Assert.fail("Set Socket Timeout or Close");
				}
			}
		}
	}
	
	class NoSuccessException extends Exception {

		private static final long serialVersionUID = 1L;
	}
}
