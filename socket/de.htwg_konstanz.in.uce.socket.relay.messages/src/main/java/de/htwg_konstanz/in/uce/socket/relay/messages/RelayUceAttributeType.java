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

package de.htwg_konstanz.in.uce.socket.relay.messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.htwg_konstanz.in.uce.messages.MessageFormatException;
import de.htwg_konstanz.in.uce.messages.UceAttribute;
import de.htwg_konstanz.in.uce.messages.UceAttributeHeader;
import de.htwg_konstanz.in.uce.messages.UceAttributeType;

/**
 * Enum for specific relay attribute types.
 * 
 * @author Daniel Maier
 * 
 */
public enum RelayUceAttributeType implements UceAttributeType {
    LIFETIME(0x33) {

        public UceAttribute fromBytes(byte[] encoded, UceAttributeHeader header)
                throws MessageFormatException, IOException {
            return Lifetime.fromBytes(encoded);
        }
    };

    private static final Map<Integer, RelayUceAttributeType> intToEnum = new HashMap<Integer, RelayUceAttributeType>();

    static {
        for (RelayUceAttributeType l : values()) {
            intToEnum.put(l.encoded, l);
        }
    }

    private final int encoded;

    /**
     * Creates a new {@link RelayUceAttributeType}.
     * 
     * @param encoded
     *            the encoded attribute type
     */
    private RelayUceAttributeType(int encoded) {
        this.encoded = encoded;
    }

    public int encode() {
        return encoded;
    }

    /**
     * Decodes a given encoded attribute type.
     * 
     * @param encoded
     *            the encoded attribute type
     * @return the decoded attribute type, or null if the attribute type is
     *         unknown.
     */
    public static RelayUceAttributeType fromEncoded(int encoded) {
        return intToEnum.get(encoded);
    }
}
