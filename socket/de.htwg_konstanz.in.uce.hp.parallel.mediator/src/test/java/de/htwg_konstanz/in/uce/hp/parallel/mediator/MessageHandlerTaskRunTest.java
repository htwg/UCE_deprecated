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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Set;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.test.helper.mockito.MockitoSocket;
import de.htwg_konstanz.in.uce.hp.parallel.mediator.MessageHandlerTask;
import de.htwg_konstanz.in.uce.hp.parallel.mediator.Repository;
import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationAckMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ConnectionRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ForwardEndpointsMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.UnregisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;

public class MessageHandlerTaskRunTest {
	
	private MockitoSocket mockitoTargetSocket;
	private MessageEncoder mec;
	
	@Before
	public void setUP() throws IOException {
		Repository.INSTANCE.reset();
		mockitoTargetSocket = new MockitoSocket();
		InetSocketAddress publicEndpointTarget = new InetSocketAddress(InetAddress.getByName("217.34.56.7"), 7654);
		mockitoTargetSocket.setClosed(false).setConnected(true).setRemoteSocketAddress(publicEndpointTarget);
		mec = new MessageEncoder();
	}
	
	@AfterClass
	public static void tearDown() {
		Repository.INSTANCE.reset();
	}
	
	@Test
	public void testReceiveRegisterMessage() throws IOException {
		MessageHandlerTask mht = new MessageHandlerTask(mockitoTargetSocket.getSocket());
		String id = "test1";
		InetAddress privateIP = InetAddress.getByName("192.168.2.3");
		int privatePort = 1234;
		RegisterMessage registerMessage = new RegisterMessage(id, privateIP, privatePort);
		mockitoTargetSocket.setInputStreamData(mec.encodeMessage(registerMessage));
		Set<String> registeredTargets = Repository.INSTANCE.getRegisteredTargets();
		Assert.assertFalse(registeredTargets.contains(id));
		mht.run();
		registeredTargets = Repository.INSTANCE.getRegisteredTargets();
		Assert.assertTrue(registeredTargets.contains(id));
		Assert.assertFalse(mockitoTargetSocket.getSocket().isClosed());
		MessageDecoder med = new MessageDecoder(mockitoTargetSocket.getOutputStreamDataAsInputStream());
		RegisterResponseMessage response = (RegisterResponseMessage) med.decodeMessage();
		Assert.assertTrue(response.isSuccess());
	}
	
	@Test
	public void testReceiveConnectionRequestMessage() throws IOException {
		//register target
		String targetID = "test1";
		InetSocketAddress targetPrivateEndpoint = new InetSocketAddress(InetAddress.getByName("192.168.3.1"), 8671);
		Repository.INSTANCE.insertOrUpdateID(targetID, targetPrivateEndpoint, mockitoTargetSocket.getSocket());
		//connection request from source
		MockitoSocket mockitoSourceSocket = new MockitoSocket();
		mockitoSourceSocket.setClosed(false).setConnected(true);
		InetSocketAddress publicEndpointSource = new InetSocketAddress(InetAddress.getByName("141.37.121.34"), 7654);
		mockitoSourceSocket.setRemoteSocketAddress(publicEndpointSource);
		InetAddress sourcePrivateIP = InetAddress.getByName("192.168.2.3");
		int sourcePrivatePort = 9167;
		ConnectionRequestMessage crm = new ConnectionRequestMessage(targetID, sourcePrivateIP, sourcePrivatePort);
		mockitoSourceSocket.setInputStreamData(mec.encodeMessage(crm));
		MessageHandlerTask mht = new MessageHandlerTask(mockitoSourceSocket.getSocket());
		mht.run();
		//assert responses
		//response to source
		MessageDecoder med = new MessageDecoder(mockitoSourceSocket.getOutputStreamDataAsInputStream());
		ForwardEndpointsMessage femSource = (ForwardEndpointsMessage) med.decodeMessage();
		Assert.assertEquals(targetPrivateEndpoint.getAddress(), femSource.getPrivateIP());
		Assert.assertEquals(targetPrivateEndpoint.getPort(), femSource.getPrivatePort());
		Assert.assertEquals(mockitoTargetSocket.getSocket().getInetAddress(), femSource.getPublicIP());
		Assert.assertEquals(mockitoTargetSocket.getSocket().getPort(), femSource.getPublicPort());
		//response to target
		med = new MessageDecoder(mockitoTargetSocket.getOutputStreamDataAsInputStream());
		ForwardEndpointsMessage femTarget = (ForwardEndpointsMessage) med.decodeMessage();
		Assert.assertEquals(sourcePrivateIP, femTarget.getPrivateIP());
		Assert.assertEquals(sourcePrivatePort, femTarget.getPrivatePort());
		Assert.assertEquals(publicEndpointSource.getAddress(), femTarget.getPublicIP());
		Assert.assertEquals(publicEndpointSource.getPort(), femTarget.getPublicPort());
		//test auth tokens match
		Assert.assertEquals(femSource.getAuthenticationToken(), femTarget.getAuthenticationToken());
	}
	
	@Test
	public void testReceiveConnectionRequestMessageForUnknownTarget() throws IOException {
		String targetID = "test1";
		MockitoSocket mockitoSourceSocket = new MockitoSocket();
		mockitoSourceSocket.setClosed(false).setConnected(true);
		InetAddress sourcePrivateIP = InetAddress.getByName("192.168.2.3");
		int sourcePrivatePort = 9167;
		ConnectionRequestMessage crm = new ConnectionRequestMessage(targetID, sourcePrivateIP, sourcePrivatePort);
		mockitoSourceSocket.setInputStreamData(mec.encodeMessage(crm));
		MessageHandlerTask mht = new MessageHandlerTask(mockitoSourceSocket.getSocket());
		mht.run();
		//check for exception message
		MessageDecoder med = new MessageDecoder(mockitoSourceSocket.getOutputStreamDataAsInputStream());
		ExceptionMessage exceptionMessage = (ExceptionMessage) med.decodeMessage();
		Assert.assertSame(ExceptionMessage.Error.TargetNotRegistered, exceptionMessage.getError());
	}
	
	@Test
	public void testReceiveUnregisterMessage() throws IllegalArgumentException, IOException {		
        String id = "test1";
        InetSocketAddress privateEndpoint = new InetSocketAddress(1234);
        Socket socketToTarget = new Socket();
        Repository.INSTANCE.insertOrUpdateID(id, privateEndpoint, socketToTarget);
        Assert.assertTrue(Repository.INSTANCE.getRegisteredTargets().contains(id));
        UnregisterMessage unregisterMessage = new UnregisterMessage(id);
        mockitoTargetSocket.setInputStreamData(mec.encodeMessage(unregisterMessage));
        MessageHandlerTask mht = new MessageHandlerTask(mockitoTargetSocket.getSocket());
        mht.run();
        Assert.assertFalse(Repository.INSTANCE.getRegisteredTargets().contains(id));
	}
	
	@Test
	public void testReceiveListRequestMessageEmpty() throws IOException {
		ListRequestMessage lrm = new ListRequestMessage();
		mockitoTargetSocket.setInputStreamData(mec.encodeMessage(lrm));
		MessageHandlerTask mht = new MessageHandlerTask(mockitoTargetSocket.getSocket());
		mht.run();
		MessageDecoder med = new MessageDecoder(mockitoTargetSocket.getOutputStreamDataAsInputStream());
		ListResponseMessage response = (ListResponseMessage) med.decodeMessage();
		Assert.assertTrue(response.getRegisteredTargets().isEmpty());
	}
	
	@Test
	public void testReceiveListRequestMessageNotEmpty() throws IOException {
		//register some targets
		//register target
		String targetID = "test1";
		InetSocketAddress targetPrivateEndpoint = new InetSocketAddress(InetAddress.getByName("192.168.3.1"), 8671);
		Repository.INSTANCE.insertOrUpdateID(targetID, targetPrivateEndpoint, mockitoTargetSocket.getSocket());
		
		//register target
		String targetID1 = "test2";
		targetPrivateEndpoint = new InetSocketAddress(InetAddress.getByName("192.168.3.2"), 9671);
		Repository.INSTANCE.insertOrUpdateID(targetID1, targetPrivateEndpoint, mockitoTargetSocket.getSocket());
		
		ListRequestMessage lrm = new ListRequestMessage();
		mockitoTargetSocket.setInputStreamData(mec.encodeMessage(lrm));
		MessageHandlerTask mht = new MessageHandlerTask(mockitoTargetSocket.getSocket());
		mht.run();
		MessageDecoder med = new MessageDecoder(mockitoTargetSocket.getOutputStreamDataAsInputStream());
		ListResponseMessage response = (ListResponseMessage) med.decodeMessage();
		Assert.assertTrue(response.getRegisteredTargets().size() == 2);
		Assert.assertTrue(response.getRegisteredTargets().contains(targetID));
		Assert.assertTrue(response.getRegisteredTargets().contains(targetID1));
	}
	
	@Test
	public void testUnknownMessage() throws IOException {
		AuthenticationAckMessage message = new AuthenticationAckMessage(true);
		mockitoTargetSocket.setInputStreamData(mec.encodeMessage(message));
		MessageHandlerTask mht = new MessageHandlerTask(mockitoTargetSocket.getSocket());
		mht.run();		
		MessageDecoder med = new MessageDecoder(mockitoTargetSocket.getOutputStreamDataAsInputStream());
		ExceptionMessage exceptionMessage = (ExceptionMessage) med.decodeMessage();
		Assert.assertSame(ExceptionMessage.Error.UnknownMessage, exceptionMessage.getError());
	}
}
