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

package de.htwg_konstanz.in.uce.hp.parallel.target;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.CancelableTask;
import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.ConnectionListener;
import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.HolePuncher;
import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.TargetConnectionAuthenticator;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ForwardEndpointsMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.KeepAliveMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.Message;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageFormatException;

/**
 * Task that is waiting for forward endpoints messages and keep-live messages.
 * If a forward endpoints message arrives it starts the hole punching process.
 * If a keep-live messages it does nothing.
 * 
 * @author dmaier
 * 
 */
public final class MessageHandlerTask implements CancelableTask {

    private final static Logger logger = LoggerFactory.getLogger(MessageHandlerTask.class);
    private final Socket socketToMediator;
    private final ConnectionListener connectionListener;
    private final HolePuncher hp;
    private boolean cancelled = false;

    /**
     * Creates a new {@link MessageHandlerTask}.
     * 
     * @param socketToMediator
     *            connection to the mediator.
     * @param socketQueue
     *            queue to put established and authenticated hole punching
     *            connections.
     */
    public MessageHandlerTask(Socket socketToMediator, BlockingQueue<Socket> socketQueue) {
        this.socketToMediator = socketToMediator;
        SocketAddress localSocketAddress = socketToMediator.getLocalSocketAddress();
        this.connectionListener = new ConnectionListener(socketToMediator.getLocalAddress(),
                socketToMediator.getLocalPort());
        this.hp = new HolePuncher(connectionListener, localSocketAddress, socketQueue);
    }

    /**
     * Waits in a loop for forward endpoints messages and keep-live messages
     * until the task gets canceled. If a forward endpoints message arrives it
     * tries to establish a hole punching connection. To do this it uses for
     * each arriving forward endpoints messages the same {@link HolePuncher}
     * object. If a keep-live messages it does nothing.
     */
    // TODO IOexception irgendwie an accpet weitergeben, sonst wartet das accept
    // ewig
    public void run() {
        try {
            while (!cancelled) {
                InputStream is = socketToMediator.getInputStream();
                MessageDecoder md = new MessageDecoder(is);
                try {
                    Message message = md.decodeMessage();
                    if (message instanceof ForwardEndpointsMessage) {
                        logger.info("New ForwardEndpointsMessage: {}", message);
                        ForwardEndpointsMessage fem = (ForwardEndpointsMessage) message;
                        logger.info("Starting HolePuncher...");
                        TargetConnectionAuthenticator authenticator = new TargetConnectionAuthenticator(
                                fem.getAuthenticationToken());
                        hp.establishHolePunchingConnection(fem.getPrivateIP(),
                                fem.getPrivatePort(), fem.getPublicIP(), fem.getPublicPort(),
                                authenticator);
                    } else if (message instanceof KeepAliveMessage) {
                        logger.info("Keep-alive message received.");
                    } else {
                        logger.error("Received wrong message. Expected ForwardEndpointsMessage");
                    }
                } catch (MessageFormatException mfe) {
                    logger.error("MessageFormatException while decoding message: {}",
                            mfe.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("IOException while receiving ForwardEndpointsMessage: {}", e.getMessage());
        }
    }

    public void cancel() {
        cancelled = true;
        connectionListener.shutdown();
        hp.shutdownNow();
    }

}
