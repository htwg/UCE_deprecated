package de.htwg_konstanz.in.hp.sequential.target;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * Implementation of hole punching target for sequential hole punching. It
 * consists of two main threads. One for sending register messages periodically
 * and the other one to listen for connection request messages from the
 * mediator.
 * 
 * @author Daniel Maier
 * 
 */
public class HolePunchingTarget {
    private static final Logger logger = Logger.getLogger(HolePunchingTarget.class);
    private final DatagramSocket listenerSocket;
    private final BlockingQueue<Socket> socketQueue;
    private RegisterThread registerThread;
    private ConnectionRequestListenerThread connectionRequestListenerThread;
    private boolean started = false;

    /**
     * Creates a new HolePunchingTarget.
     * @param mediatorRegisterAddress endpoint of mediator on that it listens for register 
     * messages.
     * @param mediatorConnectionRequestAddress endpoint of mediator on that it listens for 
     * messages regarding connection requests.
     * @param regKey the ID under that the target should get registered.
     * @param regIntervall the time between sending two register messages.
     * @throws IOException if an I/O error occurs.
     */
    public HolePunchingTarget(SocketAddress mediatorRegisterAddress,
            SocketAddress mediatorConnectionRequestAddress, String regKey, long regIntervall)
            throws IOException {
        this.listenerSocket = new DatagramSocket();
        this.socketQueue = new LinkedBlockingQueue<Socket>();
        this.registerThread = new RegisterThread(listenerSocket, mediatorRegisterAddress,
                regIntervall, regKey);
        this.connectionRequestListenerThread = new ConnectionRequestListenerThread(listenerSocket,
                mediatorConnectionRequestAddress, socketQueue);
    }
    
    /**
     * Starts this HolePunchingTarget. It can be started only one time.
     * Starts the thread for sending register messages and the thread
     * for listening for conection request messages.
     * @throws IllegalStateException if the HolePunchingTarget was already started.
     */
    public synchronized void start() throws IllegalStateException {
        if(started) {
            throw new IllegalStateException("Target is already started.");
        }
        logger.info("Starting HolePunchingTarget");
        registerThread.start();
        connectionRequestListenerThread.start();
    }
    
    /**
     * Stops this HolePunchingTarget. Stops the thread for sending register messages
     * and the thread for listening for conenction request messages.
     * @throws IllegalStateException if this HolePunchingTarget was not started 
     * before.
     */
    public synchronized void stop() throws IllegalStateException {
        if(!started) {
            throw new IllegalStateException("Target is not started."); 
        }
        registerThread.interrupt();
        connectionRequestListenerThread.interrupt();
    }

    /**
     * Returns a socket that is established via hole-punching to this target. The method blocks 
     * until a connection is made. 
     * @return the new socket.
     * @throws IOException if an I/O error occurs when waiting for a connection. 
     */
    //TODO exception aus ConnectionRequestListenerThread weitergeben
    public Socket accept() throws IOException {
        Socket s;
        try {
            s = socketQueue.take();
            logger.info("Accepting socket: " + s);
            return s;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException(e);
        }
    }
}
