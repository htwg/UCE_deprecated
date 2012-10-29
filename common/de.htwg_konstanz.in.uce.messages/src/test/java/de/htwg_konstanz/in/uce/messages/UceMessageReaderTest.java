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

package de.htwg_konstanz.in.uce.messages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

import de.htwg_konstanz.in.uce.messages.SocketEndpoint.EndpointClass;

public class UceMessageReaderTest {
    
    private static final int MAGIC = 0x5;
    private static final int MAGIC_SHIFT = 0xC;
    private static final int METHOD_SHIFT = 0x2;
    
    @Test
    public void testReadUceMessage() throws IOException {
        // expected
        
        // message header
        UceMethod method = CommonUceMethod.AUTHENTICATE;
        SemanticLevel semanticLevel = SemanticLevel.SUCCESS_RESPONSE;
        int methodBits = method.encode();
        int semanticLevelBits = semanticLevel.encode();
        int length = 8 + 4; // length
        UUID transactionId = UUID.randomUUID();
        
        // attributes
        EndpointClass endpointClass = EndpointClass.PUBLIC;
        int family = 4;
        InetAddress addr = InetAddress.getByName("192.145.2.3");
        int port = 1234;

        UceAttributeType type = CommonUceAttributeType.SOCKET_ENDPOINT;
        int attributeLength = 8;
        SocketEndpoint endpoint = new SocketEndpoint(new InetSocketAddress(addr, port),
                endpointClass);
        
        
        
        // prepare
        
        // message header
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        byte[] transactionIdBytes = UUIDCoder.asByteArray(transactionId);
        int leading16Bits = (MAGIC << MAGIC_SHIFT) | (methodBits << METHOD_SHIFT)
                | semanticLevelBits;
        dout.writeShort(leading16Bits);
        dout.writeShort(length);
        dout.write(transactionIdBytes);
        
        // attributes
        
        // prepare
        // header
        dout.writeShort(type.encode());
        dout.writeShort(attributeLength); // length
        // value
        dout.writeByte(endpointClass.encode());
        dout.writeByte(family);
        dout.writeShort(port);
        dout.write(addr.getAddress());
        
        UceMessageReader reader = new UceMessageReader();
        UceMessage message = reader.readUceMessage(new ByteArrayInputStream(bout.toByteArray()));
        
        // verify
        List<UceAttribute> attributes = message.getAttributes();
        Assert.assertTrue(attributes.size() == 1);
        UceAttribute attribute = attributes.get(0);
        Assert.assertSame(SocketEndpoint.class, attribute.getClass());
        Assert.assertEquals(endpoint, attribute);
        
        Assert.assertSame(method, message.getMethod());
        Assert.assertSame(semanticLevel, message.getSemanticLevel());
        Assert.assertEquals(length, message.getLength());
        Assert.assertEquals(transactionId, message.getTransactionId());
        
        // ask for specific attribute
        List<SocketEndpoint> socketEndpoints = message.getAttributes(SocketEndpoint.class);
        Assert.assertEquals(socketEndpoints.get(0), endpoint);
    }
}
