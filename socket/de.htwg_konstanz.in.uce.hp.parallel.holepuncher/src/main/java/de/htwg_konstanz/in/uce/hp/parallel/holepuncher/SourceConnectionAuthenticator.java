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

package de.htwg_konstanz.in.uce.hp.parallel.holepuncher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationAckMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.AuthenticationMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.Message;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;

/**
 * Implementation of {@link ConnectionAuthenticator} on the source side.
 * 
 * @author Daniel Maier
 * 
 */
public class SourceConnectionAuthenticator implements ConnectionAuthenticator {

    private final static Logger logger = LoggerFactory
            .getLogger(SourceConnectionAuthenticator.class);
    private final UUID authentikationToken;

    public SourceConnectionAuthenticator(UUID authentikationToken) {
        this.authentikationToken = authentikationToken;
    }

    /**
     * Authentication mechanism on the source side. It first sends an
     * AuthenticationMessage. Then it waits for the AuthenticationAckMessage of
     * the target. If AuthenticationAckMessage was positive it sends
     * AuthenticationAckMessage to the target. If authentication is successful
     * all hole punching threads get stopped.
     */
    public boolean authenticate(Socket toBeAuthenticated,
            Set<CancelableTask> relatedHolePunchingTasks, CancelableTask ownTask, Object sharedLock)
            throws IOException {
        logger.info("Trying to authenticate socket: {}", toBeAuthenticated);
        MessageEncoder messageEncoder = new MessageEncoder();
        InputStream is = toBeAuthenticated.getInputStream();
        OutputStream os = toBeAuthenticated.getOutputStream();
        AuthenticationMessage authenticationMessage = new AuthenticationMessage(authentikationToken);
        logger.info("Sending AuthenticationMessage: {}", authenticationMessage);
        os.write(messageEncoder.encodeMessage(authenticationMessage));
        MessageDecoder messageDecoder = new MessageDecoder(is);
        Message message = messageDecoder.decodeMessage();
        if (message instanceof AuthenticationAckMessage) {
            AuthenticationAckMessage authenticationACKMessage = (AuthenticationAckMessage) message;
            logger.info("Received AuthenticationAckMessage: {}", authenticationACKMessage);
            if (authenticationACKMessage.isAcknowledge()) {
                logger.info("Acknowledge from target");
                synchronized (sharedLock) {
                    // if(hasAuthenticatedSocket) {
                    // logger.info("Another socket: " + s +
                    // "tried to authenticate");
                    // logger.info("Blocking");
                    // s.close();
                    // return false;
                    // }
                    AuthenticationAckMessage ack = new AuthenticationAckMessage(true);
                    logger.info("Sending AuthenticationAckMessage: {}", ack);
                    os.write(messageEncoder.encodeMessage(ack));
                    logger.info("Start stopping threads...");
                    for (CancelableTask t : relatedHolePunchingTasks) {
                        if (t != ownTask) {
                            logger.debug("Stop task: {}", t);
                            t.cancel();
                        }
                    }
                    logger.info("Authentication successful");
                    // socketQueue.add(s);
                    // hasAuthenticatedSocket = true;
                    return true;
                }
            }
        }
        logger.error("Authentication failed");
        return false;
    }

}
