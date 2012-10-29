package de.htwg_konstanz.in.hp.sequential.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.hp.sequential.message.coder.MessageDecoder;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageEncoder;

public class MessageCoderTest {
    private MessageEncoder encoder;

    @Before
    public void startUP() {
        encoder = new MessageEncoder();
    }

    @Test
    public void testEncodeDecodeRegisterMessage() throws IOException {
        String id = "sampleID";
        RegisterMessage m = new RegisterMessage(id);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        RegisterMessage decodedMessage = (RegisterMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(id, decodedMessage.getId());
    }

    @Test
    public void testEncodeDecodeRegisterResponseMessage() throws IOException {
        boolean success = true;
        RegisterResponseMessage m = new RegisterResponseMessage(success);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        RegisterResponseMessage decodedMessage = (RegisterResponseMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(success, decodedMessage.isSuccess());
        success = false;
        m = new RegisterResponseMessage(success);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (RegisterResponseMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(success, decodedMessage.isSuccess());
    }

    @Test
    public void testEncodeDecodeLookupRequestMessage() throws IOException {
        String id = "sampleID";
        LookupRequestMessage m = new LookupRequestMessage(id);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        LookupRequestMessage decodedMessage = (LookupRequestMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(id, decodedMessage.getId());    
    }

    @Test
    public void testEncodeDecodeLookupResponseMessage() throws IOException {
        InetAddress ip = InetAddress.getLocalHost();
        int port = 1234;
        LookupResponseMessage m = new LookupResponseMessage(ip, port);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        LookupResponseMessage decodedMessage = (LookupResponseMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(ip, decodedMessage.getIP());
        Assert.assertEquals(port, decodedMessage.getPort());
    }

    @Test
    public void testEncodeDecodeConnectionRequestMessage() throws IOException {
        UUID correlator = UUID.randomUUID();
        ConnectionRequestMessage m = new ConnectionRequestMessage(correlator);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        ConnectionRequestMessage decodedMessage = (ConnectionRequestMessage) decoder
                .decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(correlator, decodedMessage.getCorrelator());
    }

    @Test
    public void testEncodeDecodeConnectionResponseMessage() throws IOException {
        UUID correlator = UUID.randomUUID();
        ConnectionResponseMessage m = new ConnectionResponseMessage(correlator);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        ConnectionResponseMessage decodedMessage = (ConnectionResponseMessage) decoder
                .decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(correlator, decodedMessage.getCorrelator());
    }

    @Test
    public void testEncodeDecodeConnectionRequestDetailsMessage() throws IOException {
        InetAddress ip = InetAddress.getLocalHost();
        int port = 1234;
        boolean punchHole = true;
        ConnectionRequestDetailsMessage m = new ConnectionRequestDetailsMessage(ip, port, punchHole);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        ConnectionRequestDetailsMessage decodedMessage = (ConnectionRequestDetailsMessage) decoder
                .decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(ip, decodedMessage.getIP());
        Assert.assertEquals(port, decodedMessage.getPort());
        Assert.assertEquals(punchHole, decodedMessage.isPunchHole());
        punchHole = false;
        m = new ConnectionRequestDetailsMessage(ip, port, punchHole);
        encodedMessage = encoder.encodeMessage(m);
        decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        decodedMessage = (ConnectionRequestDetailsMessage) decoder.decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(ip, decodedMessage.getIP());
        Assert.assertEquals(port, decodedMessage.getPort());
        Assert.assertEquals(punchHole, decodedMessage.isPunchHole());
    }

    @Test
    public void testEncodeDecodeConnectionRequestAckMessage() throws IOException {
        UUID correlator = UUID.randomUUID();
        InetAddress ip = InetAddress.getLocalHost();
        int port = 1234;
        ConnectionRequestAckMessage m = new ConnectionRequestAckMessage(correlator, ip, port);
        byte[] encodedMessage = encoder.encodeMessage(m);
        MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(encodedMessage));
        ConnectionRequestAckMessage decodedMessage = (ConnectionRequestAckMessage) decoder
                .decodeMessage();
        Assert.assertEquals(m, decodedMessage);
        Assert.assertEquals(ip, decodedMessage.getIP());
        Assert.assertEquals(port, decodedMessage.getPort());
        Assert.assertEquals(correlator, decodedMessage.getCorrelator());

    }
}
