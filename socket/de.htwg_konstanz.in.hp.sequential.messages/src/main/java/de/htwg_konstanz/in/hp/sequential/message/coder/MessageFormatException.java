package de.htwg_konstanz.in.hp.sequential.message.coder;

import java.io.IOException;

/**
 * Exception that is thrown if a message was wrong encoded.
 * @author Daniel Maier
 *
 */
public class MessageFormatException extends IOException {

    private static final long serialVersionUID = -9016498891826470863L;
    
    /**
     * Creates a new MessageFormatException.
     * @param message the detail message.
     */
    public MessageFormatException(String message) {
        super(message);
    }
    
    /**
     * Creates a new MessageFormatException.
     * @param message the detail message.
     * @param cause the cause.
     */
    public MessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
