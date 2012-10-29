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
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.messages.KeepAliveMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;

/**
 * Task that sends a {@link KeepAliveMessage} to a given target.
 * 
 * @author Daniel Maier
 * 
 */
final class KeepAliveTask implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(KeepAliveTask.class);
    private final static byte[] encodedKeepAliveMessage = new MessageEncoder()
            .encodeMessage(new KeepAliveMessage());
    private final String targetId;
    private final Socket socketToTarget;

    /**
     * Creates a new {@link KeepAliveTask} with the given target ID.
     * 
     * @param targetId
     *            the ID of the target to that the {@link KeepAliveMessage}
     *            should be sent
     * @param socketToTarget the socket to the desired target
     */
    KeepAliveTask(String targetId, Socket socketToTarget) {
        this.targetId = targetId;
        this.socketToTarget = socketToTarget;
    }

    /**
     * Sends a {@link KeepAliveMessage} to the given target. If an I/O error
     * occurs it unregisters the affected target.
     */
    public void run() {     
        try {
            synchronized (socketToTarget.getOutputStream()) {
                logger.info("sending keep-alive message to {}", targetId);
                socketToTarget.getOutputStream().write(encodedKeepAliveMessage);
            }
        } catch (IOException e) {
            logger.error("IOException while sending KeepAliveMessage: {}", e);
            Repository.INSTANCE.unregisterTarget(targetId);
            try {
                socketToTarget.close();
            } catch (IOException ignore) {
            }
            logger.info("Stopped keep-alive task for {}", targetId);
        }
    }

}
