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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
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
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;

public class MessageCoderTest {
    private MessageEncoder encoder;
    private static final String LONG_STRING = "dEDhIuLh3RNeO4ifJTtZpcwDvfvAQAUOHkUTN4vaxCmjAe0FeUbjTLNid" +
    		"9ATwQGsAii84ok6xrZ3t8NaxbPZUDsVZshA90NQuXGW28KGcy59UG7HSCzpp2tSfACPtmdlY5v0qKlmRQR81XoFSpiA" +
    		"fs3ugs9JgqF2KB8dvl2Hq9XyPyEtYM2PdNeunWbSdqyt66okUntilXutuQzNYvHITJGOm8JL7voTepCA9hegV48lQq7" +
    		"PBq5RFs5b8y7EZtH";
    private static final String TOO_LONG_STRING = "ddEDhIuLh3RNeO4ifJTtZpcwDvfvAQAUOHkUTN4vaxCmjAe0FeUbjTLNid" +
			"9ATwQGsAii84ok6xrZ3t8NaxbPZUDsVZshA90NQuXGW28KGcy59UG7HSCzpp2tSfACPtmdlY5v0qKlmRQR81XoFSpiA" +
			"fs3ugs9JgqF2KB8dvl2Hq9XyPyEtYM2PdNeunWbSdqyt66okUntilXutuQzNYvHITJGOm8JL7voTepCA9hegV48lQq7" +
			"PBq5RFs5b8y7EZtH";

    @Before
    public void startUP() {
        encoder = new MessageEncoder();
    }

    @Test
    public void testEncodeDecodeRegisterMessage() throws IOException {
        String id = "sampleID";
        int port = 23;
        InetAddress ip = InetAddress.getByName("192.168.7.6");
        RegisterMessage m = new RegisterMessage(id, ip, port);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        RegisterMessage decodedMessage = (RegisterMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(id, decodedMessage.getId());
        Assert.assertEquals(port, decodedMessage.getPrivatePort());
        Assert.assertEquals(ip, decodedMessage.getPrivateIP());

        id = "sampleID1";
        port = 25;
        ip = InetAddress.getByName("192.168.7.5");
        m = new RegisterMessage(id, ip, port);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (RegisterMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(id, decodedMessage.getId());
        Assert.assertEquals(port, decodedMessage.getPrivatePort());
        Assert.assertEquals(ip, decodedMessage.getPrivateIP());
    }
    
    @Test
    public void testEncodeRegisterMessageLongID() throws IOException {
        int port = 23;
        InetAddress ip = InetAddress.getByName("192.168.7.6");
        RegisterMessage m = new RegisterMessage(LONG_STRING, ip, port);
        encoder.encodeMessage(m);
    }
    
    @Test( expected = IllegalArgumentException.class )
    public void testEncodeRegisterMessageTooLongID() throws IOException {
        int port = 23;
        InetAddress ip = InetAddress.getByName("192.168.7.6");
        RegisterMessage m = new RegisterMessage(TOO_LONG_STRING, ip, port);
        encoder.encodeMessage(m);
    }
    
    @Test
    public void testEncodeDecodeRegisterResponseMessage() throws IOException {
        boolean success = true;
        RegisterResponseMessage m = new RegisterResponseMessage(success);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        RegisterResponseMessage decodedMessage = (RegisterResponseMessage) decoder
                .decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(success, m.isSuccess());

        success = false;
        m = new RegisterResponseMessage(success);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (RegisterResponseMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(success, m.isSuccess());
    }
    
    @Test
    public void testEncodeDecodeUnregisterMessage() throws IOException {
        String id = "sampleID";
        UnregisterMessage m = new UnregisterMessage(id);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        UnregisterMessage decodedMessage = (UnregisterMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(id, decodedMessage.getId());

        id = "sampleID1";
        m = new UnregisterMessage(id);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (UnregisterMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(id, decodedMessage.getId());
    }
    
    @Test
    public void testEncodeUnregisterMessageLongID() throws IOException {
    	UnregisterMessage m = new UnregisterMessage(LONG_STRING);
        encoder.encodeMessage(m);
    }
    
    @Test( expected = IllegalArgumentException.class )
    public void testEncodeUnregisterMessageTooLongID() throws IOException {
        UnregisterMessage m = new UnregisterMessage(TOO_LONG_STRING);
        encoder.encodeMessage(m);
    }

    @Test
    public void testEncodeDecodeForwardEndpointsMessage() throws IOException {
        int privatePort = 23;
        InetAddress privateIP = InetAddress.getByName("192.168.7.6");
        int publicPort = 25;
        InetAddress publicIP = InetAddress.getByName("192.168.7.7");
        UUID authenticationToken = UUID.randomUUID();
        ForwardEndpointsMessage m = new ForwardEndpointsMessage(privateIP, privatePort, publicIP,
                publicPort, authenticationToken);
        Assert.assertEquals(authenticationToken, m.getAuthenticationToken());
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        ForwardEndpointsMessage decodedMessage = (ForwardEndpointsMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(privatePort, decodedMessage.getPrivatePort());
        Assert.assertEquals(privateIP, decodedMessage.getPrivateIP());
        Assert.assertEquals(publicPort, decodedMessage.getPublicPort());
        Assert.assertEquals(publicIP, decodedMessage.getPublicIP());

        privatePort = 25;
        privateIP = InetAddress.getByName("192.168.7.4");
        publicPort = 29;
        publicIP = InetAddress.getByName("192.168.7.1");
        authenticationToken = UUID.randomUUID();
        m = new ForwardEndpointsMessage(privateIP, privatePort, publicIP, publicPort,
                authenticationToken);
        Assert.assertEquals(authenticationToken, m.getAuthenticationToken());
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (ForwardEndpointsMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(privatePort, decodedMessage.getPrivatePort());
        Assert.assertEquals(privateIP, decodedMessage.getPrivateIP());
        Assert.assertEquals(publicPort, decodedMessage.getPublicPort());
        Assert.assertEquals(publicIP, decodedMessage.getPublicIP());
    }

    @Test
    public void testEncodeDecodeConnectionRequestMessage() throws IOException {
        String id = "sampleID";
        int port = 23;
        InetAddress ip = InetAddress.getByName("192.168.7.6");
        ConnectionRequestMessage m = new ConnectionRequestMessage(id, ip, port);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        ConnectionRequestMessage decodedMessage = (ConnectionRequestMessage) decoder
                .decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(id, decodedMessage.getId());
        Assert.assertEquals(ip, decodedMessage.getPrivateIP());
        Assert.assertEquals(port, decodedMessage.getPrivatePort());

        id = "sampleID1";
        port = 29;
        ip = InetAddress.getByName("192.168.7.8");
        m = new ConnectionRequestMessage(id, ip, port);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (ConnectionRequestMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(id, decodedMessage.getId());
        Assert.assertEquals(ip, decodedMessage.getPrivateIP());
        Assert.assertEquals(port, decodedMessage.getPrivatePort());
    }

    @Test
    public void testEncodeDecodeAuthenticationMessage() throws IOException {
        UUID token = UUID.randomUUID();
        AuthenticationMessage m = new AuthenticationMessage(token);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        AuthenticationMessage decodedMessage = (AuthenticationMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(token, m.getAuthenticationToken());
    }

    @Test
    public void testEncodeDecodeAuthenticationAckMessage() throws IOException {
        boolean ack = true;
        AuthenticationAckMessage m = new AuthenticationAckMessage(ack);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        AuthenticationAckMessage decodedMessage = (AuthenticationAckMessage) decoder
                .decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(ack, m.isAcknowledge());

        ack = false;
        m = new AuthenticationAckMessage(ack);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (AuthenticationAckMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(ack, m.isAcknowledge());
    }
    
    @Test
    public void testEncodeDecodeListRequestMessage() throws IOException {
        ListRequestMessage m = new ListRequestMessage();
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        Assert.assertTrue(decoder.decodeMessage() instanceof ListRequestMessage);
        //Assert.assertEquals(m, decodedMessage);
    }
    
    @Test
    public void testEncodeDecodeListResponseMessage() throws IOException {
    	Set<String> registeredTargets = new HashSet<String>();
        ListResponseMessage m = new ListResponseMessage(registeredTargets);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        ListResponseMessage decodedMessage = (ListResponseMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(registeredTargets, m.getRegisteredTargets());
        
        registeredTargets = new HashSet<String>();
        registeredTargets.add("test");
        m = new ListResponseMessage(registeredTargets);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (ListResponseMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(registeredTargets, m.getRegisteredTargets());
        
        registeredTargets = new HashSet<String>();
        registeredTargets.add("test");
        registeredTargets.add("test1");
        m = new ListResponseMessage(registeredTargets);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (ListResponseMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(registeredTargets, m.getRegisteredTargets());
        
        registeredTargets = new HashSet<String>();
        registeredTargets.add("test");
        registeredTargets.add("test1");
        registeredTargets.add("testshshshs5");
        m = new ListResponseMessage(registeredTargets);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (ListResponseMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(registeredTargets, m.getRegisteredTargets());
    }
    
    @Test
    public void testEncodeDecodeExceptionMessage() throws IOException {
        ExceptionMessage m = new ExceptionMessage(Error.TargetNotRegistered);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        ExceptionMessage decodedMessage = (ExceptionMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(Error.TargetNotRegistered.getCode(), decodedMessage.getErrorCode());
        Assert.assertEquals(Error.TargetNotRegistered.getText(), decodedMessage.getErrorText());
        
        m = new ExceptionMessage(Error.UnknownMessage);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (ExceptionMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(Error.UnknownMessage.getCode(), decodedMessage.getErrorCode());
        Assert.assertEquals(Error.UnknownMessage.getText(), decodedMessage.getErrorText());
    }
    
    @Test
    public void testEncodeDecodeKeepAliveMessage() throws IOException {
        KeepAliveMessage m = new KeepAliveMessage();
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        Assert.assertTrue(decoder.decodeMessage() instanceof KeepAliveMessage);
        //Assert.assertEquals(m, decodedMessage);
    }
}
