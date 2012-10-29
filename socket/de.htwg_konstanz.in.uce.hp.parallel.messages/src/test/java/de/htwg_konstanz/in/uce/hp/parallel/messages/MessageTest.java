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

package de.htwg_konstanz.in.uce.hp.parallel.messages;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Assert;
import org.junit.Test;

import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationAckMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ConnectionRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ForwardEndpointsMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.KeepAliveMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.UnregisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage.Error;

public class MessageTest {

    @Test
    public void testRegisterMessage() throws UnknownHostException {
        String id = "sampleID";
        int port = 23;
        InetAddress ip = InetAddress.getByName("192.168.7.6");
        RegisterMessage m = new RegisterMessage(id, ip, port);
        Assert.assertEquals(id, m.getId());
        Assert.assertEquals(ip, m.getPrivateIP());
        Assert.assertEquals(port, m.getPrivatePort());
        
        id = "sampleID1";
        port = 25;
        ip = InetAddress.getByName("192.168.7.7");
        m = new RegisterMessage(id, ip, port);
        Assert.assertEquals(id, m.getId());
        Assert.assertEquals(ip, m.getPrivateIP());
        Assert.assertEquals(port, m.getPrivatePort());
    }

    @Test
    public void testRegisterMessageEquals() throws UnknownHostException {
        EqualsVerifier.forClass(RegisterMessage.class).withPrefabValues(InetAddress.class, 
                InetAddress.getByName("192.168.7.6"), InetAddress.getByName("192.168.7.7")).verify();
    }
    
    @Test
    public void testRegisterResponseMessage() {
        boolean success = true;
        RegisterResponseMessage m = new RegisterResponseMessage(success);
        Assert.assertEquals(success, m.isSuccess());
        success = false;
        m = new RegisterResponseMessage(success);
        Assert.assertEquals(success, m.isSuccess());
    }

    @Test
    public void testRegisterResponseMessageEquals() {
        EqualsVerifier.forClass(RegisterResponseMessage.class).verify();
    }
    
    @Test
    public void testUnregisterMessage() {
        String id = "sampleID";
        UnregisterMessage m = new UnregisterMessage(id);
        Assert.assertEquals(id, m.getId());
        
        id = "sampleID1";
        m = new UnregisterMessage(id);
        Assert.assertEquals(id, m.getId());
        
    }
    
    @Test
    public void testUnregisterMessageMessageEquals() {
        EqualsVerifier.forClass(UnregisterMessage.class).verify();
    }

    @Test
    public void testForwardEndpointsMessageMessage() throws UnknownHostException {
        int privatePort = 23;
        InetAddress privateIP = InetAddress.getByName("192.168.7.6");
        int publicPort = 25;
        InetAddress publicIP = InetAddress.getByName("192.168.7.7");
        UUID authenticationToken = UUID.randomUUID();
        ForwardEndpointsMessage m = new ForwardEndpointsMessage(privateIP, privatePort, publicIP, 
                publicPort, authenticationToken);
        Assert.assertEquals(privatePort, m.getPrivatePort());
        Assert.assertEquals(privateIP, m.getPrivateIP());
        Assert.assertEquals(publicPort, m.getPublicPort());
        Assert.assertEquals(publicIP, m.getPublicIP());
        Assert.assertEquals(authenticationToken, m.getAuthenticationToken());
        
        privatePort = 27;
        privateIP = InetAddress.getByName("192.168.7.56");
        publicPort = 29;
        publicIP = InetAddress.getByName("192.168.7.23");
        authenticationToken = UUID.randomUUID();
        m = new ForwardEndpointsMessage(privateIP, privatePort, publicIP, 
                publicPort, authenticationToken);
        Assert.assertEquals(privatePort, m.getPrivatePort());
        Assert.assertEquals(privateIP, m.getPrivateIP());
        Assert.assertEquals(publicPort, m.getPublicPort());
        Assert.assertEquals(publicIP, m.getPublicIP());
        Assert.assertEquals(authenticationToken, m.getAuthenticationToken());
    }

    @Test
    public void testForwardEndpointsMessageMessageEquals() throws UnknownHostException {
        EqualsVerifier.forClass(ForwardEndpointsMessage.class).withPrefabValues(InetAddress.class, 
                InetAddress.getByName("192.168.7.6"), InetAddress.getByName("192.168.7.7")).
                withPrefabValues(UUID.class, UUID.randomUUID(), UUID.randomUUID()).verify();
    }

    @Test
    public void testConnectionRequestMessage() throws UnknownHostException {
        String id = "sampleID";
        int port = 23;
        InetAddress ip = InetAddress.getByName("192.168.7.6");
        ConnectionRequestMessage m = new ConnectionRequestMessage(id, ip, port);
        Assert.assertEquals(id, m.getId());
        Assert.assertEquals(ip, m.getPrivateIP());
        Assert.assertEquals(port, m.getPrivatePort());
        
        id = "sampleID1";
        port = 233;
        ip = InetAddress.getByName("192.168.7.4");
        m = new ConnectionRequestMessage(id, ip, port);
        Assert.assertEquals(id, m.getId());
        Assert.assertEquals(ip, m.getPrivateIP());
        Assert.assertEquals(port, m.getPrivatePort());
    }

    @Test
    public void testConnectionRequestMessageEquals() throws UnknownHostException {
        EqualsVerifier.forClass(ConnectionRequestMessage.class)
                .withPrefabValues(InetAddress.class, InetAddress.getByName("192.168.7.6"), 
                        InetAddress.getByName("192.168.7.7")).verify();
    }

    @Test
    public void testAuthenticationMessageMessage() {
        UUID token = UUID.randomUUID();
        AuthenticationMessage m = new AuthenticationMessage(token);
        Assert.assertEquals(token, m.getAuthenticationToken());
    }

    @Test
    public void testAuthenticationMessageMessageEquals() {
        EqualsVerifier.forClass(AuthenticationMessage.class)
                .withPrefabValues(UUID.class, UUID.randomUUID(), UUID.randomUUID()).verify();
    }
    
    @Test
    public void testAuthenticationAckMessage() {
        boolean ack = true;
        AuthenticationAckMessage m = new AuthenticationAckMessage(ack);
        Assert.assertEquals(ack, m.isAcknowledge());
        ack = false;
        m = new AuthenticationAckMessage(ack);
        Assert.assertEquals(ack, m.isAcknowledge());
    }

    @Test
    public void testAuthenticationAckMessageEquals() {
        EqualsVerifier.forClass(AuthenticationAckMessage.class).verify();
    }
    
    @Test
    public void testListRequestMessage() {
        new ListRequestMessage();
    }

    @Test
    public void testListRequestMessageEquals() {
        EqualsVerifier.forClass(ListRequestMessage.class).verify();
    }
    
    @Test
    public void testListResponseMessage() {
    	Set<String> registeredTargets = new HashSet<String>();
        ListResponseMessage m = new ListResponseMessage(registeredTargets);
        org.junit.Assert.assertEquals(registeredTargets, m.getRegisteredTargets());
        registeredTargets = new HashSet<String>();
        registeredTargets.add("test");
        registeredTargets.add("test1");
        registeredTargets.add("test2");
        m = new ListResponseMessage(registeredTargets);
        Assert.assertEquals(registeredTargets, m.getRegisteredTargets());
    }

    @Test
    public void testListResponseMessageEquals() {
        EqualsVerifier.forClass(ListResponseMessage.class).verify();
    }
    
    @Test
    public void testExceptionMessage() throws UnknownHostException {
        ExceptionMessage m = new ExceptionMessage(Error.TargetNotRegistered);
        Assert.assertEquals(Error.TargetNotRegistered.getText(), m.getErrorText());
        Assert.assertEquals(Error.TargetNotRegistered.getCode(), m.getErrorCode());
        Assert.assertSame(Error.TargetNotRegistered, m.getError());
        
        m = new ExceptionMessage(Error.UnknownMessage);
        Assert.assertEquals(Error.UnknownMessage.getText(), m.getErrorText());
        Assert.assertEquals(Error.UnknownMessage.getCode(), m.getErrorCode());
        Assert.assertSame(Error.UnknownMessage, m.getError());
    }

    @Test
    public void testExceptionMessageEquals() throws UnknownHostException {
        EqualsVerifier.forClass(ExceptionMessage.class).verify();
    }
    
    @Test
    public void testKeepAliveMessage() throws UnknownHostException {
        new KeepAliveMessage();
    }

    @Test
    public void testKeepAliveMessageEquals() throws UnknownHostException {
        EqualsVerifier.forClass(KeepAliveMessage.class).verify();
    }
}
