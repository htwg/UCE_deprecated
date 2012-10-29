package de.htwg_konstanz.in.hp.sequential.mediator;

import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import de.htwg_konstanz.in.hp.sequential.message.Message;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageDecoder;

/**
 * Task to handle incoming messages.
 * 
 * @author Daniel Maier
 * 
 */
public class MessageHandlerTask implements Runnable {

    private static final Logger logger = Logger.getLogger(MessageHandlerTask.class);
    private Socket socket;

    /**
     * Creates a new MessageHandlerTask.
     * 
     * @param socket
     *            the socket of the connection from which the message is
     *            expected.
     */
    public MessageHandlerTask(Socket socket) {
        this.socket = socket;
    }

    // @Override
    /**
     * Reads and decodes the message. Than uses MesageHandler to handle the
     * message properly.
     */
    public void run() {
        try {
            MessageDecoder md = new MessageDecoder(socket.getInputStream());
            Message message = md.decodeMessage();
            MessageHandler handler = new MessageHandler();
            handler.handle(message, socket);
        } catch (IOException e) {
            logger.error("IOException while handling message: " + e.getMessage());
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }

    }
}
