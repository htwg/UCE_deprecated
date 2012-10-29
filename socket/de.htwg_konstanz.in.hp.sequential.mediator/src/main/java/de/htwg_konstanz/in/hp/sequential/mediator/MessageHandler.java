package de.htwg_konstanz.in.hp.sequential.mediator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.UUID;

import org.apache.log4j.Logger;

import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestAckMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestDetailsMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionRequestMessage;
import de.htwg_konstanz.in.hp.sequential.message.ConnectionResponseMessage;
import de.htwg_konstanz.in.hp.sequential.message.LookupRequestMessage;
import de.htwg_konstanz.in.hp.sequential.message.LookupResponseMessage;
import de.htwg_konstanz.in.hp.sequential.message.Message;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageEncoder;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageFormatException;

/**
 * Class to handle messages properly.
 * 
 * @author Daniel Maier
 * 
 */
public class MessageHandler {

    private final static Logger logger = Logger.getLogger(MessageHandler.class);
    private final MessageEncoder messageEncoder;

    /**
     * Creates a new MessageHandler.
     */
    public MessageHandler() {
        this.messageEncoder = new MessageEncoder();
    }

    /**
     * Handles the given message properly. Can handle the following messages: 
     * LookupRequestMessage, ConnectionRequestACKMessage,
     * ConnectionResponseMessage.
     * 
     * @param message
     *            the message to handle.
     * @param socket
     *            the socket over that the message gets received.
     * @throws IOException
     *             if an IOException occurs while handling the message.
     */
    public void handle(Message message, Socket socket) throws IOException {
        if (message instanceof LookupRequestMessage) {
            LookupRequestMessage lookupRequestMessage = (LookupRequestMessage) message;
            logger.info("Handling LookupRequestMessage for target: " + lookupRequestMessage);
            UUID correlator = CorrelatorMappings.INSTANCE.createMapping(socket);
            logger.info("New correlator " + correlator + " for socket: " + socket);
            ConnectionRequestMessage crm = new ConnectionRequestMessage(correlator);
            byte[] crmEncoded = messageEncoder.encodeMessage(crm);
            DatagramPacket connectionRequestPacket = new DatagramPacket(crmEncoded,
                    crmEncoded.length);
            SocketAddress udpConReqAddress = Repository.INSTANCE
                    .getTargetRegisterEndpoint(lookupRequestMessage.getId());
            connectionRequestPacket.setSocketAddress(udpConReqAddress);
            logger.info("Sending connection reuqest to target...");
            Repository.INSTANCE.getRegisterSocket().send(connectionRequestPacket);
        } else if (message instanceof ConnectionRequestAckMessage) {
            ConnectionRequestAckMessage connectionRequestAckMessage = (ConnectionRequestAckMessage) message;
            logger.info("Handling ConnectionRequestAckMessage: " + connectionRequestAckMessage);
            CorrelatorMappings.INSTANCE.addTargetEndpointToMapping(
                    connectionRequestAckMessage.getCorrelator(),
                    new InetSocketAddress(socket.getInetAddress(), socket.getPort()));
            boolean punchHole = !socket.getInetAddress()
                    .equals(connectionRequestAckMessage.getIP());
            logger.info("Private endpoint of target is: " + connectionRequestAckMessage.getIP()
                    + ":" + connectionRequestAckMessage.getPort());
            logger.info("Public endpoint of target is: " + socket.getInetAddress() + ":"
                    + socket.getPort());
            logger.info("Punch hole is: " + punchHole);
            InetAddress publicSourceIP = CorrelatorMappings.INSTANCE.getSourceSocket(
                    connectionRequestAckMessage.getCorrelator()).getInetAddress();
            int publicSourcePort = CorrelatorMappings.INSTANCE.getSourceSocket(
                    connectionRequestAckMessage.getCorrelator()).getPort();
            ConnectionRequestDetailsMessage crd = new ConnectionRequestDetailsMessage(
                    publicSourceIP, publicSourcePort, punchHole);
            logger.info("Sending public endpoint of source: " + crd.getIP() + ":" + crd.getPort());
            socket.getOutputStream().write(messageEncoder.encodeMessage(crd));
            socket.close();
        } else if (message instanceof ConnectionResponseMessage) {
            socket.close();
            ConnectionResponseMessage connectionResponseMessage = (ConnectionResponseMessage) message;
            logger.info("Handling ConnectionResponseMessage: " + connectionResponseMessage);
            Socket sourceSocket = CorrelatorMappings.INSTANCE
                    .getSourceSocket(connectionResponseMessage.getCorrelator());
            InetAddress publicTargetIP = CorrelatorMappings.INSTANCE.getTargetEndpoint(
                    connectionResponseMessage.getCorrelator()).getAddress();
            int publicTargetPort = CorrelatorMappings.INSTANCE.getTargetEndpoint(
                    connectionResponseMessage.getCorrelator()).getPort();
            LookupResponseMessage lrm = new LookupResponseMessage(publicTargetIP, publicTargetPort);
            logger.info("Sending public endpoint of target: " + lrm.getIP() + ":" + lrm.getPort());
            sourceSocket.getOutputStream().write(messageEncoder.encodeMessage(lrm));
            sourceSocket.close();
            CorrelatorMappings.INSTANCE.removeMapping(connectionResponseMessage.getCorrelator());
        } else {
            throw new MessageFormatException("Unknown Message");
        }

    }
}
