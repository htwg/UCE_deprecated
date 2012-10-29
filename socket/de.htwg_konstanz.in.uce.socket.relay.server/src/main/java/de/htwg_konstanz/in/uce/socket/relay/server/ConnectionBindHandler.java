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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.messages.ErrorCode.ErrorCodes;
import de.htwg_konstanz.in.uce.messages.UceMessage;
import de.htwg_konstanz.in.uce.messages.UniqueId;

/**
 * Handles connection bind request of a client over a data connection.
 * 
 * @author Daniel Maier
 * 
 */
final class ConnectionBindHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionBindHandler.class);
    private final Socket s;
    private final Map<UUID, BlockingQueue<Socket>> connIDToQueue;
    private final UceMessage connBindMessage;

    /**
     * Creates a new {@link ConnectionBindHandler}.
     * 
     * @param s
     *            the data connection to the client
     * @param connBindMessage
     *            the connection bind message
     * @param connIDToQueue
     *            map to match relay connection between client and peers
     */
    ConnectionBindHandler(Socket s, UceMessage connBindMessage,
            Map<UUID, BlockingQueue<Socket>> connIDToQueue) {
        this.s = s;
        this.connIDToQueue = connIDToQueue;
        this.connBindMessage = connBindMessage;
    }

    /**
     * Handles the connection bind request message. Reads the unique id to match
     * the message to a peer request. If no unique id is present a bad request
     * error message is returned to the client. If no peer request to the given
     * id is pending a bad request error message is returned to the client, too.
     */
    void handle() {
        if (connBindMessage.hasAttribute(UniqueId.class)) {
            UUID connectionId = connBindMessage.getAttribute(UniqueId.class).getId();
            BlockingQueue<Socket> queue = connIDToQueue.remove(connectionId);
            if (queue != null) {
                try {
                    // TODO antwort im erfolgsfall?
                    queue.put(s);
                } catch (InterruptedException e) {
                    logger.error("InterruptedException while inserting socket to queue: {}", e);
                    Thread.currentThread().interrupt();
                }
            } else {
                logger.error("Connection id (" + connectionId
                        + ") does not refer to an existing pending connection.");
                try {
                    UceMessage errorResponse = connBindMessage.buildErrorResponse(
                            ErrorCodes.BAD_REQUEST, "Connection ID (" + connectionId
                                    + ") does not refer to an existing pending connection.");
                    errorResponse.writeTo(s.getOutputStream());
                } catch (IOException e) {
                    logger.error("IOException while sending Error Response: {}", e);
                }
            }
        } else {
            logger.error("Received Connection Bind Message without Connection ID");
            try {
                UceMessage errorResponse = connBindMessage.buildErrorResponse(
                        ErrorCodes.BAD_REQUEST,
                        "Connection Bind Message did not contain Connection ID.");

                errorResponse.writeTo(s.getOutputStream());
            } catch (IOException e) {
                logger.error("IOException while sending Error Response: {}", e);
            }
        }
    }
}
