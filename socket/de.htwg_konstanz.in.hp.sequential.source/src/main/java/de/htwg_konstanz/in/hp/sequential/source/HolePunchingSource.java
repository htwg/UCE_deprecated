package de.htwg_konstanz.in.hp.sequential.source;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.log4j.Logger;

import de.htwg_konstanz.in.hp.sequential.message.LookupRequestMessage;
import de.htwg_konstanz.in.hp.sequential.message.LookupResponseMessage;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageDecoder;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageEncoder;

/**
 * Implementation of hole punching source side for sequential hole punching. To
 * get a socket connection to a hole punching target it sends a lookup request
 * message with the id of the target to the mediator and waits for the lookup
 * response message. After it received the response that contains the public
 * endpoint of the target it establishes a connection to the target.
 * 
 * @author Daniel Maier
 * 
 */
public class HolePunchingSource {

    private static final int TOTAL_BINDING_ATTEMP_COUNT = 10; //minimum 1
    private static final int BINDING_ATTEMP_INTERVAL = 300;
    private static final Logger logger = Logger.getLogger(HolePunchingSource.class);

    /**
     * Returns a socket that is connected to the target with the given ID.
     * @param id the ID of the requested target. 
     * @param mediatorEndpoint endpoint of the mediator on that it listens for lookup 
     * request messages.
     * @return a socket that is connected to the requested target.
     * @throws IOException if I/O error occurs.
     */
    public Socket getSocket(String id, SocketAddress mediatorEndpoint) throws IOException {
        logger.info("Trying to get socket to: " + id);
        Socket socketToMediator = new Socket();
        socketToMediator.setReuseAddress(true);
        socketToMediator.connect(mediatorEndpoint);
        int localPort = socketToMediator.getLocalPort();
        logger.info("Connected to mediator from local endpoint: "
                + socketToMediator.getLocalSocketAddress());
        MessageEncoder encoder = new MessageEncoder();
        LookupRequestMessage lookupRequestMessage = new LookupRequestMessage(id);
        socketToMediator.getOutputStream().write(encoder.encodeMessage(lookupRequestMessage));
        MessageDecoder decoder = new MessageDecoder(socketToMediator.getInputStream());
        LookupResponseMessage lookupResponseMessage = (LookupResponseMessage) decoder
                .decodeMessage();
        logger.info("Received LookupResponseMessage: " + lookupResponseMessage);
        // wait until mediator closed socket
        int eos = socketToMediator.getInputStream().read();
        if (eos != -1) {
            throw new IOException("Protocoll error. LookupResponseMessage too long.");
        }
        socketToMediator.close();
        SocketAddress targetSocketAddress = new InetSocketAddress(lookupResponseMessage.getIP(),
                lookupResponseMessage.getPort());
        logger.info("Disconnected from mediator");
        Socket socketToTarget = new Socket();
        socketToTarget.setReuseAddress(true);
        
        for(int i = 0; i < TOTAL_BINDING_ATTEMP_COUNT - 1; i++) {
            try {
                socketToTarget.bind(new InetSocketAddress(localPort));
                break;
            } catch (IOException e) {
                logger.info("Bind of socket to target failed due IOException: " + e.getMessage());
                logger.info("Try again..");
                try {
                    Thread.sleep(BINDING_ATTEMP_INTERVAL);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }            
        }
        if(!socketToTarget.isBound()) {
            socketToTarget.bind(new InetSocketAddress(localPort)); 
        }
        logger.info("Connecting to target...");
        socketToTarget.connect(targetSocketAddress);
        return socketToTarget;
    }
}
