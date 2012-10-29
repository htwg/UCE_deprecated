package de.htwg_konstanz.in.hp.sequential.message;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import de.htwg_konstanz.in.hp.sequential.message.coder.MessageType;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageTypeCoder;

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
    public void testMessageTypeCoderRegisterResponseMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.RegisterResponse;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderLookupRequestMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.LookupRequest;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderLookupResponseMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.LookupResponse;
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
    public void testMessageTypeCoderConnectionResponseMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.ConnectionResponse;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderConnectionRequestDetailsMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.ConnectionRequestDetails;
        byte encoded = mtc.encodeMessageType(toBeEncoded);
        MessageType decoded = mtc.decodeMessageType(encoded);
        Assert.assertSame(toBeEncoded, decoded);
    }
    
    @Test
    public void testMessageTypeCoderConnectionRequestACKMessage() throws IOException {
        MessageTypeCoder mtc = new MessageTypeCoder();
        MessageType toBeEncoded = MessageType.ConnectionRequestAck;
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
