package de.htwg_konstanz.in.hp.sequential.target;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

import org.apache.log4j.Logger;

import de.htwg_konstanz.in.hp.sequential.message.RegisterMessage;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageEncoder;

/**
 * Thread for sending periodic register messages to the mediator.
 * @author Daniel Maier
 *
 */
public class RegisterThread extends Thread {

    private final static Logger logger = Logger.getLogger(RegisterThread.class);
    private final SocketAddress registerAddress;
    private final DatagramSocket registerSocket;
    private final long sleep;
    private final byte[] encodedRegisterMessage;

    /**
     * Creates a new RegisterThread. It is created as a daemon thread.
     * @param registerSocket the socket that is used to send register messages.
     * @param registerAddress the endpoint of the mediator on that it expects register messages.
     * @param sleep the time betweeen two register messages.
     * @param regKey the ID under that the target gets registered.
     * @throws IOException if an I/O error occurs while creating the register message.
     */
    public RegisterThread(DatagramSocket registerSocket, SocketAddress registerAddress, long sleep,
            String regKey) throws IOException {
        this.registerAddress = registerAddress;
        this.registerSocket = registerSocket;
        this.sleep = sleep;
        this.encodedRegisterMessage = new MessageEncoder()
                .encodeMessage(new RegisterMessage(regKey));
        this.setDaemon(true);
    }

    /**
     * Sends periodic register message to mediator until the Thread gets interrupted.
     */
    public void run() {
        DatagramPacket registerPacket = new DatagramPacket(encodedRegisterMessage,
                encodedRegisterMessage.length);
        registerPacket.setSocketAddress(registerAddress);
        while (!isInterrupted()) {
            try {
                logger.info("Sending register packet to " + registerAddress
                        + " from local endpoint " + registerSocket.getLocalSocketAddress() + "...");
                registerSocket.send(registerPacket);
                Thread.sleep(sleep);
            } catch (IOException e) {
                logger.error("IOException while sending register packet: " + e.getMessage());
            } catch (InterruptedException e) {
                // Restore the interrupted status
                Thread.currentThread().interrupt();
            }
        }
    }
}
