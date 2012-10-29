package de.htwg_konstanz.in.hp.sequential.message.coder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestAckMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestDetailsMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionResponseMessage;
import de.htwg_konstanz.in.hp.sequential.message.LookupRequestMessage;
import de.htwg_konstanz.in.hp.sequential.message.LookupResponseMessage;
import de.htwg_konstanz.in.hp.sequential.message.RegisterMessage;
import de.htwg_konstanz.in.hp.sequential.message.RegisterResponseMessage;

/**
 * Class to encode messages.
 * @author Daniel Maier
 *
 */
public class MessageEncoder {
    private static final String STRING_ENCODING = "UTF-8";
    private static final int MAX_UNSIGNED_SHORT = 65535;
    private static final int BYTESHIFT = 8;
    private final MessageTypeCoder mtc;
    private final UUIDCoder uuidCoder;
    
    /**
     * Creates a new MessageEncoder.
     */
    public MessageEncoder() {
        this.mtc = new MessageTypeCoder();
        this.uuidCoder = new UUIDCoder();
    }

    /**
     * Encodes a RegisterMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     */
    public byte[] encodeMessage(RegisterMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.Register);
        baos.write(messageType);
        byte[] id = message.getId().getBytes(STRING_ENCODING);
        baos.write(id.length);
        baos.write(id);
        return baos.toByteArray();
    }

    /**
     * Encodes a RegisterResponseMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     */
    public byte[] encodeMessage(RegisterResponseMessage message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.RegisterResponse);
        baos.write(messageType);
        int success = (message.isSuccess()) ? 1 : 0;
        baos.write(success);
        return baos.toByteArray();
    }

    /**
     * Encodes a LookupRequestMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if the ID is too long.
     */
    public byte[] encodeMessage(LookupRequestMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.LookupRequest);
        baos.write(messageType);
        byte[] id = message.getId().getBytes(STRING_ENCODING);
        if(id.length > 255) {
            throw new IOException("ID is too long");
        }
        baos.write(id.length);
        baos.write(id);
        return baos.toByteArray();
    }

    /**
     * Encodes a ConnectionRequestMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] encodeMessage(ConnectionRequestMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.ConnectionRequest);
        baos.write(messageType);
        baos.write(uuidCoder.asByteArray(message.getCorrelator()));
        return baos.toByteArray();
    }

    /**
     * Encodes a ConnectionRequestAckMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs or the version of
     * the IP is unknown.
     */
    public byte[] encodeMessage(ConnectionRequestAckMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.ConnectionRequestAck);
        baos.write(messageType);
        baos.write(getIPVersion(message.getIP()));
        baos.write(message.getIP().getAddress());
        baos.write(getUnsignedShort(message.getPort()));
        baos.write(uuidCoder.asByteArray(message.getCorrelator()));
        return baos.toByteArray();
        
    }

    
    /**
     * Encodes a ConnectionRequestDetailsMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs or the version of
     * the IP is unknown.
     */
    public byte[] encodeMessage(ConnectionRequestDetailsMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.ConnectionRequestDetails);
        baos.write(messageType);
        int ipVersion = getIPVersion(message.getIP());
        if(message.isPunchHole()) {
            ipVersion |= 0x80;            
        }
        baos.write(ipVersion);
        baos.write(message.getIP().getAddress());
        baos.write(getUnsignedShort(message.getPort()));
        return baos.toByteArray();
    }

    /**
     * Encodes a ConnectionResponseMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] encodeMessage(ConnectionResponseMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.ConnectionResponse);
        baos.write(messageType);
        baos.write(uuidCoder.asByteArray(message.getCorrelator()));
        return baos.toByteArray();
    }

    /**
     * Encodes a LookupResponseMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs or the version of
     * the IP is unknown.
     */
    public byte[] encodeMessage(LookupResponseMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.LookupResponse);
        baos.write(messageType);
        int ipVersion = getIPVersion(message.getIP());
        baos.write(ipVersion);
        baos.write(message.getIP().getAddress());
        baos.write(getUnsignedShort(message.getPort()));
        return baos.toByteArray();
    }
    
    /**
     * Encodes the IP version as an int. It encodes IPv4 as an 4 and
     * IPv6 as an 6.
     * @param address the address its version is requested.
     * @return the IP version as an int.
     * @throws IOException if the IP version is unknown.
     */
    private int getIPVersion(InetAddress address) throws IOException {
        if(address instanceof Inet4Address) {
            return 4;
        } else if(address instanceof Inet6Address) {
            return 6;
        } else {
            throw new IOException("Unknown IP version");
        }
    }
    
    /**
     * Encodes an int as an unsigned short in a byte array of length two.
     * The unsigned short is big endian encoded.
     * @param value the int to be converted.
     * @return the unsigned short as an byte array.
     * @throws IllegalArgumentException if the int value is higher than an unsigned short
     * could be.
     */
    private byte[] getUnsignedShort(int value) throws IllegalArgumentException {
        if(value > MAX_UNSIGNED_SHORT) {
            throw new IllegalArgumentException("Value is too high. Maximum is " + MAX_UNSIGNED_SHORT);
        }
        byte[] unsignedShort = new byte[2];
        unsignedShort[0] = (byte) (value >> BYTESHIFT);
        unsignedShort[1] = (byte) value;
        return unsignedShort;
    }
}
