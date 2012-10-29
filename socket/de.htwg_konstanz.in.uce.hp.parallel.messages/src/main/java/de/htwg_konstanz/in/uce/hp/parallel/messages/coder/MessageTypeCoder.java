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

/**
 * Class that handles the coding of the first byte of the messages.
 * It sets the magic bits and the message type.
 * @author Daniel Maier
 *
 */
final class MessageTypeCoder {

    /**
     * magic bits
     */
    private static final byte MAGIC = 0xD;
    /**
     * Number of magic bits. Magic bits will get
     * shifted to the left by MAGIC_SHIFT. 
     */
    private static final int MAGIC_SHIFT = 4;
    /**
     * bitmask of the magic bits
     */
    private static final int MAGIC_MASK = 0xF0;
    /**
     * bitmask of the message type
     */
    private static final byte MESSAGE_TYPE_MASK = 0xF;

    /**
     * Checks if the magic bits are correct. It reads the message type and returns
     * a corresponding message object.
     * @param messageTypeField an int that contains the first byte of the message in its
     * eight least significant bits.
     * @return a corresponding message object.
     * @throws IOException if the magic bits are wrong or the message type is unknown.
     */
     final MessageType decodeMessageType(int messageTypeField) throws IOException {
        if ((messageTypeField & MAGIC_MASK) != MAGIC << MAGIC_SHIFT) {
            throw new IOException("Malformed Message: Bad Magic # "
                    + ((messageTypeField & MAGIC_MASK) >> MAGIC_SHIFT));
        }
        int messageType = messageTypeField & MESSAGE_TYPE_MASK;
        if (messageType >= MessageType.values().length) {
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
     final byte encodeMessageType(MessageType messageType) {
        int magicMessageType = MAGIC << MAGIC_SHIFT;
        return (byte) (magicMessageType | messageType.ordinal());
    }
}
