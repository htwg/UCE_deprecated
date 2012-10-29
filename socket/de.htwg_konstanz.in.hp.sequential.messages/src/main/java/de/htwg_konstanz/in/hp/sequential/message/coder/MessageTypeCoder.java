package de.htwg_konstanz.in.hp.sequential.message.coder;

import java.io.IOException;

/**
 * Class that handles the coding of the first byte of the messages.
 * It sets the magic bits and the message type.
 * @author Daniel Maier
 *
 */
public class MessageTypeCoder {
    
    private static final byte MAGIC = 0x17;
    private static final int MAGIC_SHIFT = 3;
    private static final int MAGIC_MASK = 0xF8;
    private static final byte MESSAGE_TYPE_MASK = 0x7;
    
    /**
     * Checks if the magic bits are correct. It reads the message type and returns
     * a corresponding message object.
     * @param messageTypeField an int that contains the first byte of the message in its
     * eight least significant bits.
     * @return a corresponding message object.
     * @throws IOException if the magic bits are wrong or the message type is unknown.
     */
    public MessageType decodeMessageType(int messageTypeField) throws IOException {
        if((messageTypeField & MAGIC_MASK) != MAGIC << MAGIC_SHIFT) {
            throw new IOException("Malformed Message: Bad Magic # " + ((messageTypeField & MAGIC_MASK) >> MAGIC_SHIFT));
        }
        int messageType = messageTypeField & MESSAGE_TYPE_MASK;
        if(messageType >= MessageType.values().length) {
            throw new IOException("Bad message: " + messageType);
        }
        return MessageType.values()[messageType];
    }
    
    /**
     * Encodes a message type to a byte. It sets the magic bits and the 
     * message type.
     * @param messageType the message type to be encoded.
     * @return the encoded message type.
     */
    public byte encodeMessageType(MessageType messageType) {
        int magicMessageType = MAGIC << MAGIC_SHIFT;
        return (byte) (magicMessageType | messageType.ordinal());
    }
}
