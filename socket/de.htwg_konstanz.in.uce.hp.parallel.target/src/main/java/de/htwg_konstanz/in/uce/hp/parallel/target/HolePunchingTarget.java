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
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.HolePuncher;
import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.ThreadGroupThreadFactory;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.Message;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.UnregisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;

/**
 * Implementation of hole punching target for parallel hole punching. It
 * consists of a task that is listening for forward endpoints messages and
 * keep-live messages from the mediator. If a forward endpoints message arrives
 * it starts tries to establish a connection to the hole punching source with
 * the help of {@link HolePuncher}.
 * 
 * @author Daniel Maier
 * 
 */
public class HolePunchingTarget {

    private final static Logger logger = LoggerFactory.getLogger(HolePunchingTarget.class);
    private final SocketAddress mediatorSocketAddress;
    private final String registrationId;
    private final BlockingQueue<Socket> socketQueue;
    private final ThreadGroupThreadFactory threadFactory;
    private Socket socketToMediator;
    private boolean started;
    private MessageHandlerTask messageHandlerTask;

    /**
     * Creates a new HolePunchingTarget.
     * 
     * @param mediatorSocketAddress
     *            endpoint of the mediator on that it listens for registration
     *            messages.
     * @param registrationId
     *            the ID under that the target should get registered.
     */
    public HolePunchingTarget(SocketAddress mediatorSocketAddress, String registrationId) {
        this.mediatorSocketAddress = mediatorSocketAddress;
        this.registrationId = registrationId;
        this.socketQueue = new LinkedBlockingQueue<Socket>();
        this.threadFactory = new ThreadGroupThreadFactory();
    }

    /**
     * Starts the HolePunchingTarget. Strictly speaking it starts the
     * {@link MessageHandlerTask} after it has registered the target with the
     * mediator. It can be started only one time.
     * 
     * @throws IOException
     *             if an I/O error occurs while registering with the mediator.
     * @throws IllegalStateException
     *             if the target was already started.
     */
    public synchronized void start() throws IOException, IllegalStateException {
        if (started) {
            throw new IllegalStateException("Target is already started");
        }
        started = true;
        logger.info("Trying to register target...");
        socketToMediator = new Socket();
        socketToMediator.setReuseAddress(true);
        socketToMediator.connect(mediatorSocketAddress);
        RegisterMessage registerMessage = new RegisterMessage(registrationId,
                socketToMediator.getLocalAddress(), socketToMediator.getLocalPort());
        MessageEncoder mc = new MessageEncoder();
        logger.info("Sending RegisterMessage: {}", registerMessage);
        socketToMediator.getOutputStream().write(mc.encodeMessage(registerMessage));
        MessageDecoder mdc = new MessageDecoder(socketToMediator.getInputStream());
        Message response = mdc.decodeMessage();
        if (response instanceof RegisterResponseMessage) {
            logger.info("Target registered successful...");
            logger.info("Starting ForwardEndpointsHandlerThread...");
            messageHandlerTask = new MessageHandlerTask(socketToMediator, socketQueue);
            threadFactory.newThread(messageHandlerTask).start();
        } else if (response instanceof ExceptionMessage) {
            ExceptionMessage exm = (ExceptionMessage) response;
            logger.error("Exception while registering target: {}", exm);
            throw new IOException(exm.getErrorText());
        }
    }

    /**
     * Stops the HolePunchingTarget. Strictly speaking it sends an
     * {@link UnregisterMessage} over a new socket connection to the mediator
     * and then closes the both connections to it. Then it stops the
     * {@link MessageHandlerTask}.
     * 
     * @throws IOException
     *             if an I/O error occurs while sending
     *             {@link UnregisterMessage} to mediator or when closing the
     *             socket to it.
     * @throws IllegalStateException
     *             if the target was not started yet.
     */
    public synchronized void stop() throws IOException, IllegalStateException {
        if (!started) {
            throw new IllegalStateException("Target is not started");
        }
        UnregisterMessage unregisterMessage = new UnregisterMessage(registrationId);
        MessageEncoder messageEncoder = new MessageEncoder();
        // we need a new socket to unregister
        Socket s = new Socket();
        s.connect(mediatorSocketAddress);
        s.getOutputStream().write(messageEncoder.encodeMessage(unregisterMessage));
        s.close();
        socketToMediator.close();
        messageHandlerTask.cancel();
    }

    /**
     * Returns a socket thats connection is established via hole-punching to
     * this target. The method blocks until a connection is made.
     * 
     * @return the new socket.
     * @throws IOException
     *             if an I/O error occurs when waiting for a connection.
     * @throws InterruptedException
     *             if the current thread gets interrupted while blocked in
     *             accept().
     */
    // TODO irgendwie io exceptions aus ForwardEndpointsHandlerThread
    // weitergeben
    public Socket accept() throws IOException, InterruptedException {
        try {
            Socket s = socketQueue.take();
            logger.info("Accepting socket: {}", s);
            // received dummy socket for indicating time limit exceeded
            if (!s.isConnected()) {
                throw new IOException("IOException while accepting socket");
            }
            return s;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }

    }
}
