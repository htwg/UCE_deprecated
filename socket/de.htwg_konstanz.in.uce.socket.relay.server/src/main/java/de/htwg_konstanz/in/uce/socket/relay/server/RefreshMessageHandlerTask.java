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

package de.htwg_konstanz.in.uce.socket.relay.server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.helper.sockets.ListenerThread;
import de.htwg_konstanz.in.uce.messages.ErrorCode.ErrorCodes;
import de.htwg_konstanz.in.uce.messages.UceMessage;
import de.htwg_konstanz.in.uce.socket.relay.messages.Lifetime;
import de.htwg_konstanz.in.uce.socket.relay.messages.MessageWriter;
import de.htwg_konstanz.in.uce.socket.relay.messages.RelayMessageReader;
import de.htwg_konstanz.in.uce.socket.relay.messages.RelayUceMethod;

/**
 * Task that handles refresh messages of client.
 * 
 * @author Daniel Maier
 * 
 */
public class RefreshMessageHandlerTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RefreshMessageHandlerTask.class);

    private final Socket controlConnection;
    private final MessageWriter controlConnectionWriter;
    private final int initRefreshInterval;
    private final Thread peerListener;

    /**
     * Creates a new {@link RefreshMessageHandlerTask}.
     * 
     * @param controlConnection
     *            control connection to the client
     * @param controlConnectionWriter
     *            a {@link MessageWriter} to the control connection
     * @param lifetime
     *            the lifetime of the control connection without refresh
     *            messages. If lifetime is 0,
     *            {@link Constants#DEFAULT_ALLOCATION_REFRESH_INTERVAL} is used.
     *            If lifetime is greater than
     *            {@link Constants#MAX_ALLOCATION_LIFETIME},
     *            {@link Constants#MAX_ALLOCATION_LIFETIME} is used.
     * @param peerListener
     *            listener thread that waits for peer connections to the
     *            relevant client
     */
    public RefreshMessageHandlerTask(Socket controlConnection,
            MessageWriter controlConnectionWriter, int lifetime, ListenerThread peerListener) {
        this.controlConnection = controlConnection;
        this.controlConnectionWriter = controlConnectionWriter;
        this.peerListener = peerListener;
        if (lifetime == 0) {
            this.initRefreshInterval = Constants.DEFAULT_ALLOCATION_REFRESH_INTERVAL;
        } else {
            this.initRefreshInterval = (lifetime < Constants.MAX_ALLOCATION_LIFETIME) ? lifetime
                    : Constants.MAX_ALLOCATION_LIFETIME;
        }
    }

    /**
     * Waits for refresh messages from the client. If no refresh message arrives
     * during the specified lifetime, the task gets stopped and the given
     * listener thread for peer connections gets stopped too. Moreover if
     * lifetime of a refresh message is equal or less than 0, the task and the
     * listener thread get stopped.
     */
    private void handle() {
        int refreshInterval = initRefreshInterval;
        try {
            while (true) {
                controlConnection.setSoTimeout(refreshInterval * 1000);
                UceMessage message = RelayMessageReader.read(controlConnection.getInputStream());
                // check if it is refresh message (atm the only one that is
                // allowed)
                if (message == null) {
                    // connection closed
                    logger.error("Received message was null");
                    break;
                }
                if (message.isMethod(RelayUceMethod.REFRESH) && message.isRequest()) {
                    int lifetime = message.getAttribute(Lifetime.class).getLifeTime();
                    logger.info("Received refresh request with lifetime {}", lifetime);
                    if (lifetime > 0) {
                        refreshInterval = (lifetime < Constants.MAX_ALLOCATION_LIFETIME) ? lifetime
                                : Constants.MAX_ALLOCATION_LIFETIME;
                        // send refresh response
                        UceMessage successResponse = message.buildSuccessResponse();
                        successResponse.addAttribute(new Lifetime(refreshInterval));
                        controlConnectionWriter.writeMessage(successResponse);
                    } else {
                        // send refresh response (unallocate)
                        UceMessage successResponse = message.buildSuccessResponse();
                        successResponse.addAttribute(new Lifetime(0));
                        controlConnectionWriter.writeMessage(successResponse);
                        break;
                    }
                } else {
                    logger.error("Received unexpected message: {}", message.getMethod());
                    UceMessage errorResponse = message.buildErrorResponse(ErrorCodes.BAD_REQUEST,
                            "Expected refresh request");
                    controlConnectionWriter.writeMessage(errorResponse);
                }
            }
        } catch (SocketTimeoutException e) {
            logger.info("No refresh request within specified timeout. Closing control conection and "
                    + "destroy allocation");
        } catch (IOException e) {
            logger.error("IOException while reading from control connection. Closing control "
                    + "conection and destroy allocation");
            e.printStackTrace();
        } finally {
            // something went wrong -> destroy allocation
            peerListener.interrupt();
            try {
                controlConnection.close();
            } catch (IOException ignore) {
            }
        }

    }

    public void run() {
        handle();
    }
}
