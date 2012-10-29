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
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Set;

import net.jcip.annotations.Immutable;

import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationAckMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ConnectionRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ForwardEndpointsMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.KeepAliveMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.UnregisterMessage;

/**
 * Class to encode messages. Strings get encoded in UTF-8.
 * @author Daniel Maier
 *
 */
@Immutable
public final class MessageEncoder {
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
     * @throws IOException if an I/O error occurs.
     * @throws IllegalArgumentException if the id in the given 
     * RegisterMessage is longer than 255 bytes with the used encoding.
     */
    public byte[] encodeMessage(RegisterMessage message) throws IOException, IllegalArgumentException {
    	byte[] id = message.getId().getBytes(STRING_ENCODING);
    	if(id.length > 255) {
    		throw new IllegalArgumentException("The given ID is longer than 255 bytes with the used encoding.");
    	}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.Register);
        baos.write(messageType);
        baos.write(id.length);
        baos.write(id);
        baos.write(getUnsignedShort(message.getPrivatePort()));
        baos.write(getIPVersion(message.getPrivateIP()));
        baos.write(message.getPrivateIP().getAddress());
        return baos.toByteArray();
    }
    
    /**
     * Encodes a UnregisterMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs.
     * @throws IllegalArgumentException if the id in the given 
     * UnregisterMessage is longer than 255 bytes with the used encoding. 
     */
    public byte[] encodeMessage(UnregisterMessage message) throws IOException, IllegalArgumentException {
    	byte[] id = message.getId().getBytes(STRING_ENCODING);
    	if(id.length > 255) {
    		throw new IllegalArgumentException("The given ID is longer than 255 bytes with the used encoding.");
    	}
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.Unregister);
        baos.write(messageType);
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
        byte[] id = message.getId().getBytes(STRING_ENCODING);
        baos.write(id.length);
        baos.write(id);
        baos.write(getUnsignedShort(message.getPrivatePort()));
        baos.write(getIPVersion(message.getPrivateIP()));
        baos.write(message.getPrivateIP().getAddress());
        return baos.toByteArray();
    }

    /**
     * Encodes a ForwardEndpointsMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] encodeMessage(ForwardEndpointsMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.ForwardEndpoints);
        baos.write(messageType);
        baos.write(getIPVersion(message.getPrivateIP()));
        baos.write(getUnsignedShort(message.getPrivatePort()));
        baos.write(message.getPrivateIP().getAddress());
        baos.write(getUnsignedShort(message.getPublicPort()));
        baos.write(getIPVersion(message.getPublicIP()));
        baos.write(message.getPublicIP().getAddress());
        baos.write(uuidCoder.asByteArray(message.getAuthenticationToken()));
        return baos.toByteArray();     
    }
    
    /**
     * Encodes a AuthenticationMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] encodeMessage(AuthenticationMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.Authentication);
        baos.write(messageType);
        baos.write(uuidCoder.asByteArray(message.getAuthenticationToken()));
        return baos.toByteArray(); 
    }
    
    /**
     * Encodes a AuthenticationAckMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     */
    public byte[] encodeMessage(AuthenticationAckMessage message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.AuthenticationAck);
        baos.write(messageType);
        if(message.isAcknowledge()) {
            baos.write(1);
        } else {
            baos.write(0);
        }
        return baos.toByteArray(); 
    }
    
    /**
     * Encodes a ListRequestMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     */
    public byte[] encodeMessage(ListRequestMessage message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.ListRequest);
        baos.write(messageType);
        return baos.toByteArray(); 
    }
    
    /**
     * Encodes a ListResponseMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] encodeMessage(ListResponseMessage message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.ListResponse);
        baos.write(messageType);
        Set<String> registeredTargets = message.getRegisteredTargets();
        if(registeredTargets.size() == 0) {
            baos.write(0);
            baos.write(0);
            return baos.toByteArray();
        }
        for(String registeredTarget : registeredTargets) {
            byte[] id = registeredTarget.getBytes(STRING_ENCODING);
            baos.write(id);
            baos.write(0);
        }
        baos.write(0);
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
        if(message.isSuccess()) {
            baos.write(1);
        } else {
            baos.write(0);
        }
        return baos.toByteArray(); 
    }
    
    /**
     * Encodes a KeepAliveMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     */
    public byte[] encodeMessage(KeepAliveMessage message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.KeepAlive);
        baos.write(messageType);
        return baos.toByteArray(); 
    }
    
    /**
     * Encodes a ExceptionMessage to a byte array.
     * @param message the message to be encoded.
     * @return the byte array with the encoded message.
     * @throws IOException if an I/O error occurs.
     * @throws IllegalArgumentException if the error text in the given 
     * ExceptionMessage is longer than 255 bytes with the used encoding. Or if
     * error code of the ExceptionMessage is greater than 255.
     */
    public byte[] encodeMessage(ExceptionMessage message) throws IOException, IllegalArgumentException {
    	byte[] errorText = message.getErrorText().getBytes(STRING_ENCODING);
    	if(errorText.length > 255) {
    		throw new IllegalArgumentException("The given error text is longer than 255 bytes with the used encoding.");
    	}
       	if(message.getErrorCode() > 255) {
    		throw new IllegalArgumentException("The given error code is greater than 255.");
    	}
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte messageType = mtc.encodeMessageType(MessageType.Exception);
        baos.write(messageType);
        int errorCode = message.getErrorCode();
        baos.write(errorCode);
        baos.write(errorText.length);
        baos.write(errorText);
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
    
    public static void main(String[] args) throws IOException {
        
    }
}
