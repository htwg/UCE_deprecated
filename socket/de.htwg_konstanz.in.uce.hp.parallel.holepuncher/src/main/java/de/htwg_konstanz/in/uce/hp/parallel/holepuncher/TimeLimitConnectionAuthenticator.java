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
import java.net.Socket;
import java.util.Set;

/**
 * Enhances a {@link ConnectionAuthenticator} object with the ability to perform
 * the authentication with a timeout.
 * 
 * @author Daniel Maier
 * 
 */
final class TimeLimitConnectionAuthenticator implements ConnectionAuthenticator {

    private final ConnectionAuthenticator authenticator;
    private final int timeout;

    public TimeLimitConnectionAuthenticator(ConnectionAuthenticator authenticator, int timeout) {
        this.authenticator = authenticator;
        this.timeout = timeout;
    }

    /**
     * Authenticates the given socket with the timeout that is specified in in
     * the constructor. Uses the {@link ConnectionAuthenticator} object passed
     * to the constructor for the authentication mechanism.
     */
    public boolean authenticate(Socket toBeAuthenticated,
            Set<CancelableTask> relatedHolePunchingTasks, CancelableTask ownTask, Object sharedLock)
            throws IOException {
        int oldTimeout = toBeAuthenticated.getSoTimeout();
        toBeAuthenticated.setSoTimeout(timeout);
        boolean auth;
        try {
            auth = authenticator.authenticate(toBeAuthenticated, relatedHolePunchingTasks, ownTask,
                    sharedLock);
        } finally {
            toBeAuthenticated.setSoTimeout(oldTimeout);
        }
        return auth;
    }

}
