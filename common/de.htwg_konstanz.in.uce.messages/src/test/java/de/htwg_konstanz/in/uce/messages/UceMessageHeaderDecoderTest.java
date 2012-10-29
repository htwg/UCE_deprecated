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
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Test;

public class UceMessageHeaderDecoderTest {

    private static final int MAGIC = 0x5;
    private static final int MAGIC_SHIFT = 0xC;
    private static final int METHOD_SHIFT = 0x2;

    @Test
    public void testConstructor() {
        new UceMessageHeaderDecoder();
    }

    @Test
    public void testConstructorWithCustomMethodDecoder() {
        new UceMessageHeaderDecoder(new UceMethodDecoder() {

            public UceMethod decode(int encoded) {
                return null;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNull() {
        new UceMessageHeaderDecoder(null);
    }

    @Test
    public void testDecodeUceMessageHeader() throws IOException {

        // expected
        UceMethod method = CommonUceMethod.AUTHENTICATE;
        SemanticLevel semanticLevel = SemanticLevel.SUCCESS_RESPONSE;
        int methodBits = method.encode();
        int semanticLevelBits = semanticLevel.encode();
        int length = 500;
        UUID transactionId = UUID.randomUUID();

        // prepare
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        byte[] transactionIdBytes = UUIDCoder.asByteArray(transactionId);
        int leading16Bits = (MAGIC << MAGIC_SHIFT) | (methodBits << METHOD_SHIFT)
                | semanticLevelBits;
        dout.writeShort(leading16Bits);
        dout.writeShort(length);
        dout.write(transactionIdBytes);
        UceMessageHeaderDecoder decoder = new UceMessageHeaderDecoder();
        UceMessageHeader header = decoder.decodeUceMessageHeader(bout.toByteArray());

        // verify
        Assert.assertSame(method, header.getMethod());
        Assert.assertSame(semanticLevel, header.getSemanticLevel());
        Assert.assertEquals(length, header.getLength());
        Assert.assertEquals(transactionId, header.getTransactionId());
    }
}
