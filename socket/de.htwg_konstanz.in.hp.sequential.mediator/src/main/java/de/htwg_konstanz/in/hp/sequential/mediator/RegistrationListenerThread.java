package de.htwg_konstanz.in.hp.sequential.mediator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/**
 * Thread that listens for registration messages over UDP.
 * 
 * @author Daniel Maier
 * 
 */
public class RegistrationListenerThread extends Thread {

    private static final int MAX_REGISTER_PACKET_SIZE = 256;
    private static final Logger logger = Logger.getLogger(RegistrationListenerThread.class);
    private final DatagramSocket registerSocket;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Creates a new RegistrationListenerThread.
     * 
     * @param registerSocket
     *            socket that is said to listen for the register messages.
     */
    public RegistrationListenerThread(DatagramSocket registerSocket) {
        this.registerSocket = registerSocket;
    }

    /**
     * Listens for registration messages until the thread gets interrupted.
     * Starts a new RegisterTask for each packet that arrives.
     */
    @Override
    public void run() {
        try {
            while (!interrupted()) {
                DatagramPacket registerPacket = new DatagramPacket(
                        new byte[MAX_REGISTER_PACKET_SIZE], MAX_REGISTER_PACKET_SIZE);
                registerSocket.receive(registerPacket);
                logger.info("Received new registration packet");
                executor.execute(new RegisterTask(registerPacket));
            }
        } catch (SocketException e) {
            // socket has been closed by a call to interrupt().
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IOException while registration:" + e.getMessage());
        }
    }

    @Override
    public void interrupt() {
        registerSocket.close();
        executor.shutdown();
        super.interrupt();
    }
}
