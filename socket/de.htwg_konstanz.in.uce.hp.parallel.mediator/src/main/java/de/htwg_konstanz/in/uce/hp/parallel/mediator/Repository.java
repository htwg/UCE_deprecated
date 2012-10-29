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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Repository for registered targets. Implemented as a singleton.
 * 
 * @author Daniel Maier
 * 
 */
enum Repository {
    INSTANCE;

    /**
     * Class to hold the values for a registered target. The values are the
     * private endpoint of the target, the socket to the target and the
     * {@link ScheduledFuture} of its {@link KeepAliveTask}.
     * 
     * @author Daniel Maier
     * 
     */
    static class RepositoryValue {
        final InetSocketAddress privateEndpoint;
        final Socket registerSocket;
        final ScheduledFuture<?> keepAliveFuture;

        public RepositoryValue(InetSocketAddress privateEndpoint, Socket registerSocket,
                ScheduledFuture<?> keepAliveFuture) {
            this.privateEndpoint = privateEndpoint;
            this.registerSocket = registerSocket;
            this.keepAliveFuture = keepAliveFuture;
        }
    }

    private static final int KEEP_ALIVE_POOL_SIZE = 10;
    private static final int KEEP_ALIVE_INTERVAL = 10 * 60; // in seconds
    private final Map<String, RepositoryValue> repo = new ConcurrentHashMap<String, Repository.RepositoryValue>();
    private final ScheduledExecutorService keepAliveExecutor = Executors
            .newScheduledThreadPool(KEEP_ALIVE_POOL_SIZE);

    /**
     * Inserts or updates a new entry for the given ID. This method also starts
     * a {@link KeepAliveTask} for the given target.
     * 
     * @param id
     *            the ID under that the target should get registered.
     * @param privateEndpoint
     *            the private endpoint of the registering target.
     * @param socketToTarget
     *            the socket to the target.
     * @throws NullPointerException
     *             if at least one of the parameters is null.
     */
    void insertOrUpdateID(String id, InetSocketAddress privateEndpoint, Socket socketToTarget)
            throws NullPointerException {
        if (id == null || privateEndpoint == null || socketToTarget == null) {
            throw new NullPointerException();
        }
        unregisterTarget(id);
        // start keep alive task
        ScheduledFuture<?> future = keepAliveExecutor.scheduleWithFixedDelay(new KeepAliveTask(id,
                socketToTarget), KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, TimeUnit.SECONDS);
        RepositoryValue value = new RepositoryValue(privateEndpoint, socketToTarget, future);
        repo.put(id, value);
    }

    /**
     * Returns the repository entry of the target with the given id.
     * 
     * @param the
     *            id of the desired target.
     * @return id the repository entry of the target.
     * @throws NoSuchElementException
     *             if no entry with given id exists.
     */
    RepositoryValue getRepositoryEntry(String id) {
        RepositoryValue value = repo.get(id);
        if (value == null) {
            throw new NoSuchElementException("No entry with given id found.");
        }
        return value;
    }

    /**
     * Turns the singleton object into original state.
     */
    void reset() {
        repo.clear();
    }

    /**
     * Returns all targets that are registered at the moment.
     * 
     * @return all registered targets.
     */
    Set<String> getRegisteredTargets() {
        return repo.keySet();
    }

    /**
     * Unregisters the given target from this repository and cancels the
     * {@link KeepAliveTask} of the affected target.
     * 
     * @param id
     *            the ID of the target to be unregistered
     */
    void unregisterTarget(String id) {
        RepositoryValue value = repo.remove(id);
        // stop keep-alive task
        if (value != null) {
            value.keepAliveFuture.cancel(true);
        }
    }
}
