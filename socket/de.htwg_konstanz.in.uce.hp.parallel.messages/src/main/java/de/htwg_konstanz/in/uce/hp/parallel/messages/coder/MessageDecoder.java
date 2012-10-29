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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationAckMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ConnectionRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ForwardEndpointsMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.KeepAliveMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.Message;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.UnregisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage.Error;

/**
 * Class to decode messages from wire to message objects.
 * @author Daniel Maier
 *
 */
public final class MessageDecoder {
    private static final String STRING_ENCODING = "UTF-8";
    private static final int UUID_LENGTH = 16;
    private static final int IP_V4_LENGTH = 4;
    private static final int IP_V6_LENGTH = 16;
    private static final int IP_VERSION_MASK = 0x7F;
    private static final int AUTHENTICATION_ACK_MASK = 0x1;
    private static final int SUCCESS_MASK = 0x1;
    private static final Message KEEP_ALIVE_MESSAGE = new KeepAliveMessage();
    private static final Message LIST_REQUEST_MESSAGE = new ListRequestMessage();
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
        case ConnectionRequest:
            return decodeConnectionRequestMessage();
        case ForwardEndpoints:
            return decodeForwardEndpointsMessage();
        case Authentication:
            return decodeAuthenticationMessage();
        case AuthenticationAck: 
            return decodeAuthenticationAckMessage();
        case Unregister: 
            return decodeUnregisterMessage();
        case ListRequest: 
            return decodeListRequestMessage();
        case ListResponse: 
            return decodeListResponseMessage();
        case KeepAlive:
            return decodeKeepAliveMessage();
        case Exception:
        	return decodeExceptionMessage();
        }
        return null;
    }

	/**
     * Decodes a AuthenticationAckMessage.
     * @return the decoded AuthenticationAckMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeAuthenticationAckMessage() throws IOException {
       boolean ack = (dis.readUnsignedByte() & AUTHENTICATION_ACK_MASK) != 0;
       return new AuthenticationAckMessage(ack);
    }

    
    /**
     * Decodes a AuthenticationMessage.
     * @return the decoded AuthenticationMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeAuthenticationMessage() throws IOException {
        byte[] data = new byte[UUID_LENGTH];
        try {
            dis.readFully(data);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length");
        } 
        return new AuthenticationMessage(uuidCoder.toUUID(data));
    }

    /**
     * Decodes a RegisterMessage.
     * @return the decoded RegisterMessage.
     * @throws IOException if the IP version is unknown or an I/O error
     * occurs. 
     */
    private Message decodeRegisterMessage() throws IOException {
        int length = dis.readUnsignedByte();
        byte[] data = new byte[length];
        try {
            dis.readFully(data);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length", e);
        }
        int privatePort = dis.readUnsignedShort();
        int ipVersion = dis.readUnsignedByte();
        ipVersion &= IP_VERSION_MASK; 
        byte[] ip = readIP(ipVersion);
        return new RegisterMessage(new String(data, STRING_ENCODING), InetAddress.getByAddress(ip), 
                privatePort);
    }
    
	/**
     * Decodes a RegisterResponseMessage.
     * @return the decoded RegisterResponseMessage.
	 * @throws IOException 
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeRegisterResponseMessage() throws IOException {
        boolean ack = (dis.readUnsignedByte() & SUCCESS_MASK) != 0;
        return new RegisterResponseMessage(ack);
	}
    
    /**
     * Decodes a UnregisterMessage.
     * @return the decoded UnregisterMessage.
     * @throws IOException if an I/O error occurs. 
     */
    private Message decodeUnregisterMessage() throws IOException {
        int length = dis.readUnsignedByte();
        byte[] data = new byte[length];
        try {
            dis.readFully(data);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length", e);
        }
        return new UnregisterMessage(new String(data, STRING_ENCODING));
    }

    /**
     * Decodes a ConnectionRequestMessage.
     * @return the decoded ConnectionRequestMessage.
     * @throws IOException if the IP version is unknown or an I/O error
     * occurs. 
     */
    private Message decodeConnectionRequestMessage() throws IOException {
        int length = dis.readUnsignedByte();
        byte[] data = new byte[length];
        try {
            dis.readFully(data);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length", e);
        }
        int privatePort = dis.readUnsignedShort();
        int ipVersion = dis.readUnsignedByte();
        ipVersion &= IP_VERSION_MASK; 
        byte[] ip = readIP(ipVersion);
        return new ConnectionRequestMessage(new String(data, STRING_ENCODING), 
                InetAddress.getByAddress(ip), privatePort);
    }

    /**
     * Decodes a ForwardEndpointsMessage.
     * @return the decoded ForwardEndpointsMessage.
     * @throws IOException if the IP version is unknown or an I/O error
     * occurs. 
     */
    private Message decodeForwardEndpointsMessage() throws IOException {
        int ipVersion = dis.readUnsignedByte();
        int privatePort = dis.readUnsignedShort();
        ipVersion &= IP_VERSION_MASK; 
        byte[] privateIp = readIP(ipVersion);
        int publicPort = dis.readUnsignedShort();
        ipVersion = dis.readUnsignedByte();
        ipVersion &= IP_VERSION_MASK; 
        byte[] publicIp = readIP(ipVersion);
        byte[] authToken = new byte[UUID_LENGTH];
        try {
            dis.readFully(authToken);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length");
        } 
        return new ForwardEndpointsMessage(InetAddress.getByAddress(privateIp), privatePort,
                InetAddress.getByAddress(publicIp), publicPort, uuidCoder.toUUID(authToken));
    }
    
    /**
     * Decodes a ListRequestMessage.
     * @return the decoded ListRequestMessage.
     */
    private Message decodeListRequestMessage() {
        return LIST_REQUEST_MESSAGE;
    }
    
    /**
     * Decodes a ListResponseMessage.
     * @return the decoded ListResponseMessage.
     * @throws IOException if an I/O error occurs.
     */
    private Message decodeListResponseMessage() throws IOException {
        final int delimiter = 0;
        int lastByte = dis.read();
        int nextByte;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Set<String> ids = new HashSet<String>();
        while(!(lastByte == delimiter & (nextByte = dis.read()) == delimiter)) {
            if(lastByte != delimiter) {
                baos.write(lastByte);                
            }
            //new id
            if(nextByte == delimiter) {
                String id = baos.toString(STRING_ENCODING);
                ids.add(id);
                baos.reset();
            } 
            lastByte = nextByte;
        }
        return new ListResponseMessage(ids);
    }
    
    /**
     * Decodes a ExceptionMessage.
     * @return the decoded ExceptionMessage.
     * @throws IOException if an I/O error occurs.
     * @throws MessageFormatException if length of error text isn't accurate, 
     * or the error code is unknown.
     */
    private Message decodeExceptionMessage() throws IOException {
    	int errorCode = dis.readUnsignedByte();
    	int length = dis.readUnsignedByte();
        byte[] errorText = new byte[length];
        try {
            dis.readFully(errorText);
        } catch(EOFException e) {
            throw new MessageFormatException("Wrong message length", e);
        }
        for(Error e : Error.values()) {
        	if(e.getCode() == errorCode) {
        		return new ExceptionMessage(e);
        	}
        }
        throw new MessageFormatException("Unknown error code");
	}
    
    /**
     * Decodes a KeepAliveMessage.
     * @return the decoded ListRequestMessage.
     */
    private Message decodeKeepAliveMessage() {
        return KEEP_ALIVE_MESSAGE;
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
            throw new MessageFormatException("Wrong message length", e);
        } 
        return ip;
    }

    public static void main(String[] args) throws IOException {
    }
}
