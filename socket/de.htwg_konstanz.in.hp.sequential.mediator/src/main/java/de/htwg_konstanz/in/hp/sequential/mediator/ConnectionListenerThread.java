package de.htwg_konstanz.in.hp.sequential.mediator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

/**
 * Thread that listens for messages over TCP. It can handle the following
 * messages: LookupRequestMessage, ConnectionRequestACKMessage, 
 * ConnectionResponseMessage.
 * 
 * @author Daniel Maier
 * 
 */
public class ConnectionListenerThread extends Thread {

    private final static Logger logger = Logger.getLogger(ConnectionListenerThread.class);
    private final ServerSocket connectionRequestListener;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Creates a new ConnectionListenerThread.
     * 
     * @param connectionRequestListener
     *            the socket that is said to listen for messages.
     */
    public ConnectionListenerThread(ServerSocket connectionRequestListener) {
        this.connectionRequestListener = connectionRequestListener;
    }

    /**
     * Listens for messages until the thread gets interrupted. Starts a new
     * MessageHandlerTask for each connection that gets accepted.
     */
    @Override
    public void run() {
        try {
            while (!interrupted()) {
                Socket requestorSocket = connectionRequestListener.accept();
                logger.info("New connection from: " + requestorSocket.getRemoteSocketAddress());
                executor.execute(new MessageHandlerTask(requestorSocket));
            }
        } catch (IOException e) {
            logger.error("IOException in ListenerThread:" + e.getMessage());
        }
        executor.shutdown();
    }

    @Override
    public void interrupt() {
        try {
            connectionRequestListener.close();
        } catch (IOException ignore) {

        }
        super.interrupt();
    }
}
