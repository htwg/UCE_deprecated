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

import junit.framework.Assert;

import org.junit.Test;

public class UceAttributeHeaderDecoderTest {
    
    @Test
    public void testConstructor() {
        new UceAttributeHeaderDecoder();
    }

    @Test
    public void testConstructorWithCustomTypeDecoder() {
        new UceAttributeHeaderDecoder(new UceAttributeTypeDecoder() {
            
            public UceAttributeType decode(int encoded) {
                // TODO Auto-generated method stub
                return null;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNull() {
        new UceAttributeHeaderDecoder(null);
    }
    
    public void testDecodeUceAttributeHeader() throws IOException {
        // expected
        UceAttributeType type = CommonUceAttributeType.SOCKET_ENDPOINT;
        int typeBits = type.encode();
        int length = 500;
        
        // prepare 
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        dout.writeShort(typeBits);
        dout.writeShort(length);
        UceAttributeHeaderDecoder decoder = new UceAttributeHeaderDecoder();
        UceAttributeHeader header = decoder.decodeUceAttributeHeader(bout.toByteArray());
        
        // verify
        Assert.assertSame(type, header.getType());
        Assert.assertSame(length, length);
    }
}
