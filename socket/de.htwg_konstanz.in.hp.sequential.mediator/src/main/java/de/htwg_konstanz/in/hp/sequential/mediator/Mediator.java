package de.htwg_konstanz.in.hp.sequential.mediator;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

import org.apache.log4j.Logger;

/**
 * Mediator for sequential hole punching. It consists of two main threads. One
 * for listening for registration messages over UDP. And one for listening for
 * all the other messages over TCP. Registration is saved transient, all data
 * will be lost if the mediator is restarted. The peer that wants to establish a
 * connection to another peer is called source. The peer that waits for
 * connections is called target.
 * 
 * @author Daniel Maier
 * 
 */
public class Mediator {

    private static final Logger logger = Logger.getLogger(Mediator.class);
    private static final String WRONG_ARGS = "Wrong arguments (ConnectionHandlerTCPPort and "
            + "RegisterUDPPort expected)";
    private final ConnectionListenerThread listener;
    private final RegistrationListenerThread registerThread;
    private boolean started = false;

    /**
     * Creates a new Mediator.
     * 
     * @param listenerPort
     *            port to listen for messages over TCP.
     * @param registerPort
     *            port to listen for registration messages over UDP.
     * @throws IOException
     *             if an IOException occurs while creating the sockets.
     */
    public Mediator(int listenerPort, int registerPort) throws IOException {
        ServerSocket ss = new ServerSocket(listenerPort);
        DatagramSocket registerSocket = new DatagramSocket(registerPort);
        Repository.INSTANCE.setRegisterSocket(registerSocket);
        listener = new ConnectionListenerThread(ss);
        registerThread = new RegistrationListenerThread(registerSocket);
        logger.info("Created mediator on port " + listenerPort + " (Connection Listener) and "
                + registerPort + " (Registration Listener)");
    }

    /**
     * Starts the mediator. Strictly speaking it starts the two listener
     * threads. You can't start a mediator twice and you can't (re)start it
     * after stopping it.
     * 
     * @throws IllegalStateException
     *             if the mediator was already started.
     */
    public synchronized void start() throws IllegalStateException {
        if (started) {
            throw new IllegalStateException("Mediator is already started");
        }
        logger.info("Starting mediator...");
        listener.start();
        registerThread.start();
        started = true;
    }

    /**
     * Stops the mediator. Strictly speaking it stops the two listener threads.
     * 
     * @throws IllegalStateException
     *             if the mediator was not started yet.
     */
    public synchronized void stop() throws IllegalStateException {
        if (!started) {
            throw new IllegalStateException("Mediator is not started yet");
        }
        logger.info("Stoping mediator...");
        listener.interrupt();
        registerThread.interrupt();
    }

    /**
     * Main method of the mediator. Creates and starts a mediator instance.
     * 
     * @param args
     *            arguments for the mediator. An array with length of two is
     *            expected. It should contain the following values: args[0] port
     *            of listener for messages over TCP; args[1] port of listener
     *            for registration messages over UDP.
     * @throws IOException
     *             if an IOException occurs while creating the mediator.
     * @throws IllegalArgumentException
     *             if args don't fit the requirements above.
     */
    public static void main(String[] args) throws IOException, IllegalArgumentException {
        if (args.length != 2) {
            throw new IllegalArgumentException(WRONG_ARGS);
        }

        int registerPort;
        int connectionHandlerPort;

        try {
            connectionHandlerPort = Integer.parseInt(args[0]);
            registerPort = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_ARGS);
        }

        new Mediator(connectionHandlerPort, registerPort).start();
    }

}
