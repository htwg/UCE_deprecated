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

package de.htwg_konstanz.in.uce.hp.parallel.messages.coder;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageType;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageTypeCoder;

public class MessageTypeCoderTest {
    
    private static final int MAGIC_SHIFT = 3;
    
    @Test
    public void testMessageTypeCoderRegisterMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.Register;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderUnregisterMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.Unregister;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderConnectionRequestMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.ConnectionRequest;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
   
    
    @Test
    public void testMessageTypeCoderForwardEndpointsMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.ForwardEndpoints;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderAuthenticationMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.Authentication;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderAuthenticationAckMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.AuthenticationAck;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderListRequestMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.ListRequest;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderListResponseMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.ListResponse;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderKeepAliveMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.KeepAlive;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test(expected = IOException.class)
    public void testMessageTypeCoderDecodeBadMagic() throws IOException {
        int magicMessageType = 24 << MAGIC_SHIFT;
        byte encoded =  (byte) (magicMessageType | 7);
        MessageTypeCoder mtc = new MessageTypeCoder();
        mtc.decodeMessageType(encoded);
    }
}
