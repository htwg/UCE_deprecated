package de.htwg_konstanz.in.hp.sequential.mediator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;

import org.apache.log4j.Logger;

import de.htwg_konstanz.in.hp.sequential.message.Message;
import de.htwg_konstanz.in.hp.sequential.message.RegisterMessage;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageDecoder;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageFormatException;

/**
 * Task to handle a registration message.
 * 
 * @author Daniel Maier
 * 
 */
public class RegisterTask implements Runnable {

    private static final Logger logger = Logger.getLogger(RegisterTask.class);
    private final DatagramPacket registerPacket;

    /**
     * Creates a new RegisterTask.
     * 
     * @param registerPacket
     *            the packet containing the register message.
     */
    public RegisterTask(DatagramPacket registerPacket) {
        this.registerPacket = registerPacket;
    }

    // @Override
    /**
     * Decodes the UDP packet. If it contains a register message, the id gets
     * registered in the Repository.
     */
    public void run() {
        MessageDecoder mdc = new MessageDecoder(new ByteArrayInputStream(registerPacket.getData()));
        Message message;
        try {
            message = mdc.decodeMessage();
            if (message instanceof RegisterMessage) {
                RegisterMessage rm = (RegisterMessage) message;
                String id = rm.getId();
                logger.info("New registration for: " + id);
                // TODO zwischen register und update unterscheiden
                // TODO last known ip mechanismus?
                Repository.INSTANCE.insertOrUpdateID(id, registerPacket.getSocketAddress());
                // TODO antwort senden
            } else {
                throw new MessageFormatException("Malformed registration message");
            }
        } catch (IOException e) {
            logger.error("IOException while registration:" + e.getMessage());
        }

    }

}
