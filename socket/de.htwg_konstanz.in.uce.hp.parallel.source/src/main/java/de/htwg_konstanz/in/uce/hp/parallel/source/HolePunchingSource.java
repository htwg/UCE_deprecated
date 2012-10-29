/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htwg_konstanz.in.uce.hp.parallel.source;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.ConnectionListener;
import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.HolePuncher;
import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.SourceConnectionAuthenticator;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ConnectionRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ForwardEndpointsMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.Message;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage.Error;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageFormatException;

/**
 * Implementation of hole punching source side for parallel hole punching. To
 * get a socket connection to a hole punching target it sends a connection
 * request message with the id of the target to the mediator and waits for the
 * forward endpoints message. After it received the message that contains the
 * public and private endpoint of the target it establishes a connection to the
 * target with the help of the {@link ConnectionListener} and {@link HolePuncher} class.
 * 
 * @author Daniel Maier
 * 
 */
public final class HolePunchingSource {
    private final static Logger logger = LoggerFactory.getLogger(HolePunchingSource.class);

    /**
     * Returns a Socket that is connected to the target with the given ID.
     * 
     * @param id
     *            the ID of the desired target
     * @param mediatorAddress
     *            the mediator endpoint on that it is waiting for connection
     *            request messages
     * @return a socket that is connected to the requested target
     * @throws IOException
     *             if I/O error occurs
     * @throws TargetNotRegisteredException
     *             if the desired target was not registered at the mediator
     */
    public Socket getSocket(String id, SocketAddress mediatorAddress) throws IOException,
            TargetNotRegisteredException {
        logger.info("Trying to get socket to: {}", id);
        final Socket socketToMediator = new Socket();
        final Message message;
        final InetSocketAddress localSocketAddress;
        MessageEncoder messageEncoder = new MessageEncoder();
        try {
            socketToMediator.setReuseAddress(true);
            socketToMediator.connect(mediatorAddress);
            localSocketAddress = (InetSocketAddress) socketToMediator.getLocalSocketAddress();
            logger.info("Connected to mediator from local endpoint: {}", localSocketAddress);
            ConnectionRequestMessage crm = new ConnectionRequestMessage(id,
                    localSocketAddress.getAddress(), localSocketAddress.getPort());
            logger.info("Sending ConnectionRequestMessage to mediator: {}", crm);
            socketToMediator.getOutputStream().write(messageEncoder.encodeMessage(crm));
            MessageDecoder messageDecoder = new MessageDecoder(socketToMediator.getInputStream());
            message = messageDecoder.decodeMessage();
        } finally {
            socketToMediator.close();
        }
        if (message instanceof ForwardEndpointsMessage) {
            ForwardEndpointsMessage fem = (ForwardEndpointsMessage) message;
            logger.info("Received ForwardEndpointsMessage: {}", fem);
            BlockingQueue<Socket> socketQueue = new ArrayBlockingQueue<Socket>(1);
            ConnectionListener connectionListener = new ConnectionListener(
                    localSocketAddress.getAddress(), localSocketAddress.getPort());
            logger.info("Starting HolePuncher...");
            SourceConnectionAuthenticator authenticator = new SourceConnectionAuthenticator(
                    fem.getAuthenticationToken());
            HolePuncher hp = new HolePuncher(connectionListener, localSocketAddress, socketQueue);
            hp.establishHolePunchingConnection(fem.getPrivateIP(), fem.getPrivatePort(),
                    fem.getPublicIP(), fem.getPublicPort(), authenticator);
            Socket s = null;
            boolean interrupted = false;
            try {
                while (s == null) {
                    try {
                        s = socketQueue.take();
                    } catch (InterruptedException e) {
                        interrupted = true;
                        // fall through and retry
                        logger.info("InterruptedException (fall through and retry)");
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
            connectionListener.shutdown();
            hp.shutdownNow();
            // received dummy socket for indicating time limit exceeded
            if (!s.isConnected()) {
                throw new IOException("Could not get socket to: " + id);
            }
            logger.info("Returning socket: {}", s);
            return s;
        } else if (message instanceof ExceptionMessage) {
            ExceptionMessage exm = (ExceptionMessage) message;
            if (exm.getError() == Error.TargetNotRegistered) {
                logger.info("Desired Target was not registered.");
                throw new TargetNotRegisteredException(exm.getErrorText());
            } else {
                logger.error("Unexpected exception in hole punching source: {}", exm);
                throw new IOException(exm.getErrorText());
            }
        } else {
            logger.error("Received wrong message. Expected ForwardEndpointsMessage");
            throw new MessageFormatException(
                    "Received wrong message. Expected ForwardEndpointsMessage");
        }
    }
}
