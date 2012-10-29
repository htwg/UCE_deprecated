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

package de.htwg_konstanz.in.uce.messages;

import java.io.IOException;

/**
 * Exception that signals that a message was malformed.
 * 
 * @author Daniel Maier
 * 
 */
public class MessageFormatException extends IOException {

    private static final long serialVersionUID = 6400052438768955799L;

    /**
     * Creates a new MessageFormatException.
     * 
     * @param message
     *            the detail message
     */
    public MessageFormatException(String message) {
        super(message);
    }

    /**
     * Creates a new MessageFormatException.
     * 
     * @param message
     *            the detail message
     * @param cause
     *            the cause
     */
    public MessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
