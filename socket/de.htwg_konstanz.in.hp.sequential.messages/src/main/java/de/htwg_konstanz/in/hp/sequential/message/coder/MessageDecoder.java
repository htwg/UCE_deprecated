package de.htwg_konstanz.in.hp.sequential.message.coder;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestAckMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestDetailsMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionResponseMessage;
import de.htwg_konstanz.in.hp.sequential.message.LookupRequestMessage;
import de.htwg_konstanz.in.hp.sequential.message.LookupResponseMessage;
import de.htwg_konstanz.in.hp.sequential.message.Message;
import de.htwg_konstanz.in.hp.sequential.message.RegisterMessage;
import de.htwg_konstanz.in.hp.sequential.message.RegisterResponseMessage;

/**
 * Class to decode messages from wire to message objects.
 * @author Daniel Maier
 *
 */
public class MessageDecoder {
    private static final String STRING_ENCODING = "UTF-8";
    private static final int UUID_LENGTH = 16;
    private static final int IP_V4_LENGTH = 4;
    private static final int IP_V6_LENGTH = 16;
    private static final int IP_VERSION_MASK = 0x7F;
    private static final int PUNCH_HOLE_MASK = 0x80;
    private static final int REGISTER_RESPONSE_MASK = 0x1;
    private final DataInputStream dis;
    private final UUIDCoder uuidCoder;

    /**
     * Creates an new MessageDecoder.
     * @param in InputStream from that the message gets read.
     */
    public MessageDecoder(InputStream in) {
        this.dis = new DataInputStream(in);
        this.uuidCoder = new UUIDCoder();
    }

    /**
     * Reads and decodes the message from the InputStream.
     * @return the decoded message object.
     * @throws IOException if an I/O error occurs.
     */
    public Message decodeMessage() throws IOException {
        MessageTypeCoder mc = new MessageTypeCoder();
        MessageType messageType = mc.decodeMessageType(dis.read());
        switch (messageType) {
        case Register:
            return decodeRegisterMessage();
        case RegisterResponse:
            return decodeRegisterResponseMessage();
        case LookupRequest:
            return decodeLookupRequest();
        case ConnectionRequest:
            return decodeConnectionRequestMessage();
        case ConnectionRequestAck:
            return decodeConnectionRequestAckMessage();
        case ConnectionRequestDetails:
            return decodeConnectionRequestDetails();
        case ConnectionResponse:
            return decodeConnectionResponseMessage();
        case LookupResponse:
            return decodeLookupResponse();
        }
        return null;
    }

    /**
     * Decodes a LookupResponseMessage.
     * @return the decoded LookupResponseMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeLookupResponse() throws IOException {
        int ipVersion = dis.readUnsignedByte();
        ipVersion &= IP_VERSION_MASK; 
        byte[] ip = readIP(ipVersion);
        int port = dis.readUnsignedShort();
        return new LookupResponseMessage(InetAddress.getByAddress(ip), port);
    }

    /**
     * Decodes a ConnectionRequestDetailsMessage.
     * @return the decoded ConnectionRequestDetailsMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeConnectionRequestDetails() throws IOException {
        int ipVersion = dis.readUnsignedByte();
        byte[] ip = readIP(ipVersion & IP_VERSION_MASK);
        boolean punchHole = (ipVersion & PUNCH_HOLE_MASK) != 0;
        int port = dis.readUnsignedShort();
        return new ConnectionRequestDetailsMessage(InetAddress.getByAddress(ip), port, punchHole);
    }

    /**
     * Decodes a RegisterResponseMessage.
     * @return the decoded RegisterResponseMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeRegisterResponseMessage() throws IOException {
        boolean success = (dis.readUnsignedByte() & REGISTER_RESPONSE_MASK) != 0;
        return new RegisterResponseMessage(success);
    }

    /**
     * Decodes a RegisterMessage.
     * @return the decoded RegisterMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeRegisterMessage() throws IOException {
        int length = dis.readUnsignedByte();
        byte[] data = new byte[length];
        try {
            dis.readFully(data);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length");
        }     
        return new RegisterMessage(new String(data, STRING_ENCODING));
    }

    /**
     * Decodes a LookupRequestMessage.
     * @return the decoded LookupRequestMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeLookupRequest() throws IOException {
        int length = dis.readUnsignedByte();
        byte[] data = new byte[length];
        try {
            dis.readFully(data);
        } catch(EOFException e) {
            throw new IOException("Wrong message length");
        } 
        return new LookupRequestMessage(new String(data, STRING_ENCODING));
    }

    /**
     * Decodes a ConnectionRequestMessage.
     * @return the decoded ConnectionRequestMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeConnectionRequestMessage() throws IOException {
        byte[] data = new byte[UUID_LENGTH];
        try {
            dis.readFully(data);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length");
        } 
        return new ConnectionRequestMessage(uuidCoder.toUUID(data));
    }

    /**
     * Decodes a ConnectionRequestAckMessage.
     * @return the decoded ConnectionRequestAckMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeConnectionRequestAckMessage() throws IOException {
        int ipVersion = dis.readUnsignedByte();
        ipVersion &= IP_VERSION_MASK; 
        byte[] ip = readIP(ipVersion);
        byte[] uuid = new byte[UUID_LENGTH];
        int port = dis.readUnsignedShort();
        try {
            dis.readFully(uuid);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length");
        } 
        return new ConnectionRequestAckMessage(uuidCoder.toUUID(uuid), InetAddress.getByAddress(ip), port);
    }
    
    /**
     * Decodes a ConnectionResponseMessage.
     * @return the decoded ConnectionResponseMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeConnectionResponseMessage() throws IOException {
        byte[] data = new byte[UUID_LENGTH];
        try {
            dis.readFully(data);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length");
        } 
        return new ConnectionResponseMessage(uuidCoder.toUUID(data));
    }
    
    /**
     * Decodes the IP address based on the given IP version.
     * If IP version is IPv4 it reads the next four bytes. If IP version
     * is IPv6 it reads the next 16 bytes as IP address.
     * @param ipVersion
     * @return a byte array containing the IP address.
     * @throws IOException if the IP version is unknown or an I/O error
     * occurs. 
     */
    private byte[] readIP(int ipVersion) throws IOException {
        byte ip[];
        switch (ipVersion) {
        case 4:
            ip = new byte[IP_V4_LENGTH];
            break;
        case 6:
            ip = new byte[IP_V6_LENGTH];
            break;
        default:
            throw new MessageFormatException("Unknown IP version: " + ipVersion);
        }
        try {
            dis.readFully(ip);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length");
        } 
        return ip;
    }
}
