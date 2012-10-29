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

package de.htwg_konstanz.in.uce.hp.parallel.mediator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.mediator.Repository.RepositoryValue;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ConnectionRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ForwardEndpointsMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.Message;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.RegisterResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.UnregisterMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage.Error;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;

/**
 * Task to handle incoming messages.
 * 
 * @author Daniel Maier
 * 
 */
final class MessageHandlerTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerTask.class);
    private final Socket s;

    /**
     * Creates a new MessageHandlerTask.
     * 
     * @param s
     *            the socket of the connection from which the message is
     *            expected.
     * @throws NullPointerException
     *             if the given socket is null.
     * @throws IllegalArgumentException
     *             if the given socket is closed or not connected.
     */
    MessageHandlerTask(Socket s) throws NullPointerException, IllegalArgumentException {
        if (s == null) {
            throw new NullPointerException();
        }
        if (s.isClosed() || !s.isConnected()) {
            throw new IllegalArgumentException(
                    "Wrong argument: socket must not be closed and has to be connected");
        }
        this.s = s;
    }

    /**
     * Reads, decodes and handles the message. Can handle the following
     * messages: {@link RegisterMessage}, {@link ConnectionRequestMessage},
     * {@link ListRequestMessage} and {@link UnregisterMessage}.
     */
    public final void run() {
        try {
            MessageDecoder md = new MessageDecoder(s.getInputStream());
            Message message = md.decodeMessage();
            MessageEncoder me = new MessageEncoder();
            if (message instanceof RegisterMessage) {
                RegisterMessage registerMessage = (RegisterMessage) message;
                logger.info("Handling RegisterMessage for target: {}", registerMessage);
                synchronized (s.getOutputStream()) {
                    Repository.INSTANCE.insertOrUpdateID(
                            registerMessage.getId(),
                            new InetSocketAddress(registerMessage.getPrivateIP(), registerMessage
                                    .getPrivatePort()), s);
                    RegisterResponseMessage response = new RegisterResponseMessage(true);
                    s.getOutputStream().write(me.encodeMessage(response));
                }
            } else if (message instanceof ConnectionRequestMessage) {
                ConnectionRequestMessage connectionRequestMessage = (ConnectionRequestMessage) message;
                logger.info("Handling ConnectionRequestMessage for target: {}",
                        connectionRequestMessage);
                InetAddress privateIPAddressOfSource = connectionRequestMessage.getPrivateIP();
                int privatePortOfSource = connectionRequestMessage.getPrivatePort();
                InetAddress publicIPAddressOfSource = s.getInetAddress();
                int publicPortOfSource = s.getPort();
                RepositoryValue repoValue;
                try {
                    repoValue = Repository.INSTANCE.getRepositoryEntry(connectionRequestMessage
                            .getId());
                } catch (NoSuchElementException e) {
                    ExceptionMessage exm = new ExceptionMessage(Error.TargetNotRegistered);
                    s.getOutputStream().write(me.encodeMessage(exm));
                    s.close();
                    return;
                }
                InetSocketAddress privateEndpointOfTarget = repoValue.privateEndpoint;
                InetAddress privateIPAddressOfTarget = privateEndpointOfTarget.getAddress();
                int privatePortOfTarget = privateEndpointOfTarget.getPort();
                Socket socketToTarget = repoValue.registerSocket;
                InetAddress publicIPAddressOfTarget = socketToTarget.getInetAddress();
                int publicPortOfTarget = socketToTarget.getPort();
                UUID authenticationToken = UUID.randomUUID();
                ForwardEndpointsMessage toTarget = new ForwardEndpointsMessage(
                        privateIPAddressOfSource, privatePortOfSource, publicIPAddressOfSource,
                        publicPortOfSource, authenticationToken);
                ForwardEndpointsMessage toSource = new ForwardEndpointsMessage(
                        privateIPAddressOfTarget, privatePortOfTarget, publicIPAddressOfTarget,
                        publicPortOfTarget, authenticationToken);
                logger.info("Sending Endpoints of source to target: {}", toTarget);
                synchronized (socketToTarget.getOutputStream()) {
                    socketToTarget.getOutputStream().write(me.encodeMessage(toTarget));
                }
                logger.info("Sending Endpoints of target to source: {}", toSource);
                s.getOutputStream().write(me.encodeMessage(toSource));
                s.close();
            } else if (message instanceof ListRequestMessage) {
                logger.info("Handling ListRequestMessage");
                ListResponseMessage response = new ListResponseMessage(
                        Repository.INSTANCE.getRegisteredTargets());
                s.getOutputStream().write(me.encodeMessage(response));
                s.close();
            } else if (message instanceof UnregisterMessage) {
                UnregisterMessage unregisterMessage = (UnregisterMessage) message;
                logger.info("Handling UnregisterMessage for target: {}", unregisterMessage.getId());
                Repository.INSTANCE.unregisterTarget(unregisterMessage.getId());
                s.close();
            } else {
                ExceptionMessage exm = new ExceptionMessage(Error.UnknownMessage);
                s.getOutputStream().write(me.encodeMessage(exm));
                s.close();
                return;
            }
        } catch (IOException e) {
            logger.error("IOException while handling message: {}", e.getMessage());
            try {
                s.close();
            } catch (IOException ignore) {
            }
        }
    }

}
