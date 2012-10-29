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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import de.htwg_konstanz.in.uce.messages.SocketEndpoint.EndpointClass;

public class UceMessageImplTest {
    
    private static final int MAGIC = 0x5;
    private static final int MAGIC_SHIFT = 0xC;
    private static final int METHOD_SHIFT = 0x2;
    
    @Test
    public void testWriteTo() throws IOException {
        // expected
        // message header
        UceMethod method = CommonUceMethod.AUTHENTICATE;
        SemanticLevel semanticLevel = SemanticLevel.SUCCESS_RESPONSE;
        int methodBits = method.encode();
        int semanticLevelBits = semanticLevel.encode();
        int length = 4 + 8; // attribute header length + attribute value length
        UUID transactionId = UUID.randomUUID();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        byte[] transactionIdBytes = UUIDCoder.asByteArray(transactionId);
        int leading16Bits = (MAGIC << MAGIC_SHIFT) | (methodBits << METHOD_SHIFT)
                | semanticLevelBits;
        dout.writeShort(leading16Bits);
        dout.writeShort(length);
        dout.write(transactionIdBytes);
        
        // attribute
        EndpointClass endpointClass = EndpointClass.PUBLIC;
        int family = 4;
        InetAddress addr = InetAddress.getByName("192.145.2.3");
        int port = 1234;
        int attributeLength = 8;
        UceAttributeType type = CommonUceAttributeType.SOCKET_ENDPOINT;
        SocketEndpoint endpoint = new SocketEndpoint(new InetSocketAddress(addr, port),
                endpointClass);

        // attribute header
        dout.writeShort(type.encode());
        dout.writeShort(attributeLength);
        // value
        dout.writeByte(endpointClass.encode());
        dout.writeByte(family);
        dout.writeShort(port);
        dout.write(addr.getAddress());
        
        // execution
        UceMessageImpl uceMessage = new UceMessageImpl(method, semanticLevel, transactionId);
        uceMessage.addAttribute(endpoint);
        ByteArrayOutputStream resultBout = new ByteArrayOutputStream();
        uceMessage.writeTo(resultBout);
        // verify
        Assert.assertArrayEquals(bout.toByteArray(), resultBout.toByteArray());
        
    }
}
