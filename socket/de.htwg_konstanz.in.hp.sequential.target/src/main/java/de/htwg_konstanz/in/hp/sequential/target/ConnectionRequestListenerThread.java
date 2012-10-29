package de.htwg_konstanz.in.hp.sequential.target;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestMessage;
import de.htwg_konstanz.in.hp.sequential.message.Message;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageDecoder;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageFormatException;

/**
 * Thread that waits for connection requests from the mediator.
 * @author Daniel Maier
 *
 */
public class ConnectionRequestListenerThread extends Thread {

    private final static Logger logger = Logger.getLogger(ConnectionRequestListenerThread.class);
    private static final int MAX_CONNECTION_REQUEST_PACKET_SIZE = 17;
    private final DatagramSocket listenerSocket;
    private final DatagramPacket connectionRequestPacket;
    private final ExecutorService executor;
    private final SocketAddress mediatorConnectionRequestAddress;
    private final BlockingQueue<Socket> socketQueue;

    /**
     * Factory to create threads as daemon threads.
     * @author Daniel Maier
     *
     */
    private class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    }

    /**
     * Creates a new ConnectionRequestListenerThread. It is created as a daemon thread.
     * @param listenerSocket the socket to receive connection request messages.
     * @param mediatorConnectionRequestAddress the endpoint of the mediator on that
     * it waits for connection request related messages.
     * @param socketQueue the queue where to put established hole punching connections.
     */
    public ConnectionRequestListenerThread(DatagramSocket listenerSocket,
            SocketAddress mediatorConnectionRequestAddress, BlockingQueue<Socket> socketQueue) {
        this.listenerSocket = listenerSocket;
        this.connectionRequestPacket = new DatagramPacket(
                new byte[MAX_CONNECTION_REQUEST_PACKET_SIZE], MAX_CONNECTION_REQUEST_PACKET_SIZE);
        this.mediatorConnectionRequestAddress = mediatorConnectionRequestAddress;
        this.socketQueue = socketQueue;
        this.executor = Executors.newCachedThreadPool(new DaemonThreadFactory());
        this.setDaemon(true);
    }

    /**
     * Listens for connection request messages until the thread gets interrupted.
     * If it receives a connection request it starts a new ConnectionRequestTask in a daemon 
     * thread to handle it.
     */
    public void run() {
        try {
            while (!isInterrupted()) {
                listenerSocket.receive(connectionRequestPacket);
                MessageDecoder decoder = new MessageDecoder(new ByteArrayInputStream(
                        connectionRequestPacket.getData()));
                try {
                    Message message = decoder.decodeMessage();
                    if (message instanceof ConnectionRequestMessage) {
                        ConnectionRequestMessage connectionRequestMessage = 
                            (ConnectionRequestMessage) message;
                        logger.info("Received ConnectionRequestMessage: " + 
                                connectionRequestMessage);
                        executor.execute(new ConnectionRequestTask(connectionRequestMessage
                                .getCorrelator(), mediatorConnectionRequestAddress, socketQueue));
                    } else {
                        logger.error("Received wrong message. Expected ConnectionRequestMessage");
                    }     
                } catch(MessageFormatException mfe) {
                    logger.error("MessageFormatException while handling connection request: " + 
                            mfe.getMessage());
                }
            }
        } catch (SocketException e) {
            // happens if gets interrupted
            logger.error("SocketException in ConnectionRequestListenerThread: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IOException while receiving connection request:" + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * Closes the listener socket to stop the thread.
     */
    public void interrupt() {
        listenerSocket.close();
        super.interrupt();
    }

}
