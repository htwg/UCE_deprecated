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
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.htwg_konstanz.in.uce.messages.SocketEndpoint.EndpointClass;

public class UceAttributeDecoderTest {

    @Test
    public void testDecodeUceAttributes() throws IOException {
        // expected
        EndpointClass endpointClass = EndpointClass.PUBLIC;
        int family = 4;
        InetAddress addr = InetAddress.getByName("192.145.2.3");
        int port = 1234;
        int length = 8;
        UceAttributeType type = CommonUceAttributeType.SOCKET_ENDPOINT;

        SocketEndpoint endpoint = new SocketEndpoint(new InetSocketAddress(addr, port),
                endpointClass);

        // prepare
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        // header
        dout.writeShort(type.encode());
        dout.writeShort(length);
        // value
        dout.writeByte(endpointClass.encode());
        dout.writeByte(family);
        dout.writeShort(port);
        dout.write(addr.getAddress());
        UceAttributeDecoder decoder = new UceAttributeDecoder();
        List<UceAttribute> attributes = decoder.decodeUceAttributes(bout.toByteArray());
        
        // verifiy
        Assert.assertTrue(attributes.size() == 1);
        UceAttribute attribute = attributes.get(0);
        Assert.assertSame(SocketEndpoint.class, attribute.getClass());
        Assert.assertEquals(endpoint, attribute);
    }
}
