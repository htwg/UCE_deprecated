package de.htwg_konstanz.in.hp.sequential.target;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestAckMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestDetailsMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionResponseMessage;
import de.htwg_konstanz.in.hp.sequential.message.Message;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageDecoder;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageEncoder;

/**
 * Task to handle connection requests.
 * @author Daniel Maier
 *
 */
public class ConnectionRequestTask implements Runnable {

    private final static Logger logger = Logger.getLogger(ConnectionRequestTask.class);
    private static final int TOTAL_BINDING_ATTEMP_COUNT = 10; // minimum 1
    private static final int BINDING_ATTEMP_INTERVAL = 300;
    /**
     * Timeout of ServerSocket that waits for connection from source.
     */
    private static final int LISTENER_TIMEOUT = 30000;
    /**
     * Timeout for connect to punch hole
     */
    private static final int HOLE_PUNCHING_TIMEOUT = 2000;
    private final SocketAddress mediatorConnectionRequestAddress;
    private final UUID correlator;
    private final BlockingQueue<Socket> socketQueue;

    /**
     * Creates a new ConnectionRequestTask.
     * @param correlator the correlator to identify the connection request and its follow-up messages.
     * @param mediatorConnectionRequestAddress he endpoint of the mediator on that
     * it waits for connection request related messages.
     * @param socketQueue the queue where to put established hole punching connections.
     */
    public ConnectionRequestTask(UUID correlator, SocketAddress mediatorConnectionRequestAddress,
            BlockingQueue<Socket> socketQueue) {
        this.correlator = correlator;
        this.mediatorConnectionRequestAddress = mediatorConnectionRequestAddress;
        this.socketQueue = socketQueue;
    }

	/**
	 * Handles a connection request message. Therefore it sends a connection
	 * request ack message to the mediator and waits for the connection request
	 * details message as answer. Then it punches a hole to the hole punching
	 * source. After that it sends the connection response message to the
	 * mediator and waits for a incoming connection from the hole punching source.
	 * When a a connection is established it puts the socket object in the socket queue.
	 */
    public void run() {
        Socket socketToMediator = new Socket();
        ServerSocket ss = null;
        MessageEncoder encoder = new MessageEncoder();
        try {
            socketToMediator.setReuseAddress(true);
            socketToMediator.connect(mediatorConnectionRequestAddress);
            logger.info("Connected to mediator from local endpoint: "
                    + socketToMediator.getLocalSocketAddress());
            int localPort = socketToMediator.getLocalPort();
            ConnectionRequestAckMessage connectionRequestAckMessage = new ConnectionRequestAckMessage(
                    correlator, socketToMediator.getLocalAddress(), localPort);
            socketToMediator.getOutputStream().write(
                    encoder.encodeMessage(connectionRequestAckMessage));
            MessageDecoder decoder = new MessageDecoder(socketToMediator.getInputStream());
            Message message = decoder.decodeMessage();
            if (message instanceof ConnectionRequestDetailsMessage) {
                ConnectionRequestDetailsMessage connectionRequestDetailsMessage = 
                    (ConnectionRequestDetailsMessage) message;
                logger.info("Received ConnectionRequestDetailsMessage: "
                        + connectionRequestDetailsMessage);
                int eos = socketToMediator.getInputStream().read();
                if (eos != -1) {
                    throw new IOException(
                            "Protocoll error. ConnectionRequestDetailsMessage too long.");
                }
                socketToMediator.close();
                logger.info("Punch hole is: " + connectionRequestDetailsMessage.isPunchHole());
                if (connectionRequestDetailsMessage.isPunchHole()) {
                    Socket socketToSource = new Socket();
                    socketToSource.setReuseAddress(true);
                    try {
                        for (int i = 0; i < TOTAL_BINDING_ATTEMP_COUNT - 1; i++) {
                            try {
                                socketToSource.bind(new InetSocketAddress(localPort));
                                break;
                            } catch (IOException e) {
                                logger.info("Bind of socket to source failed due IOException: "
                                        + e.getMessage());
                                logger.info("Try again..");
                                try {
                                    Thread.sleep(BINDING_ATTEMP_INTERVAL);
                                } catch (InterruptedException e1) {
                                    Thread.currentThread().interrupt();
                                }
                            }
                        }
                        if (!socketToSource.isBound()) {
                            socketToSource.bind(new InetSocketAddress(localPort));
                        }
                        logger.info("Punch hole from local endpoint "
                                + socketToSource.getLocalSocketAddress() + " ...");
                        socketToSource.connect(new InetSocketAddress(
                                connectionRequestDetailsMessage.getIP(),
                                connectionRequestDetailsMessage.getPort()), HOLE_PUNCHING_TIMEOUT);
                    } catch (IOException ignore) {
                        logger.info("Hole punching attempt finished due: " + ignore.getMessage());
                    }
                }
                ss = new ServerSocket();
                ss.setReuseAddress(true);
                for (int i = 0; i < TOTAL_BINDING_ATTEMP_COUNT - 1; i++) {
                    try {
                        ss.bind(new InetSocketAddress(localPort));
                        break;
                    } catch (IOException e) {
                        logger.info("Bind of serversocket failed due IOException: "
                                + e.getMessage());
                        logger.info("Try again..");
                        try {
                            Thread.sleep(BINDING_ATTEMP_INTERVAL);
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                if (!ss.isBound()) {
                    ss.bind(new InetSocketAddress(localPort));
                }
                socketToMediator = new Socket();
                socketToMediator.connect(mediatorConnectionRequestAddress);
                ConnectionResponseMessage connectionResponseMessage = new ConnectionResponseMessage(
                        correlator);
                socketToMediator.getOutputStream().write(
                        encoder.encodeMessage(connectionResponseMessage));
                socketToMediator.close();
                ss.setSoTimeout(LISTENER_TIMEOUT);
                Socket socketToSource = ss.accept();
                ss.close();
                logger.info("Accepted socket from source: "
                        + socketToSource.getRemoteSocketAddress());
                socketQueue.add(socketToSource);
            } else {
                throw new IOException(
                        "Received wrong message. Expected ConnectionRequestDetailsMessage");
            }
        } catch (IOException e1) {
            logger.error("IOException while handling connection response: " + e1.getMessage());
        } finally {
            try {
                socketToMediator.close();
                if (ss != null)
                    ss.close();
            } catch (IOException ignore) {
            }
        }

    }
}
