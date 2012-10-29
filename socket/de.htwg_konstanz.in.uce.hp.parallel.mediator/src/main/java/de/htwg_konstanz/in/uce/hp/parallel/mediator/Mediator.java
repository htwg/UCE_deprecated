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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Mediator for parallel hole punching. It listens for register and connection 
 * request messages over TCP. Registration is saved transient, all data
 * will be lost if the mediator is restarted. The peer that wants to establish a
 * connection to another peer is called source. The peer that waits for
 * connections is called target.
 * 
 * @author Daniel Maier
 * 
 */
public final class Mediator {

    private static final Logger logger = LoggerFactory.getLogger(Mediator.class);
    private static final String WRONG_ARGS = "Wrong arguments (ListenerPort expected)";
    private final ListenerThread listenerThread;
    private boolean started;

    /**
     * Creates a new Mediator.
     * @param bindingPort the port on that the mediator should listen for messages.
     * @throws IOException if an IOException occurs while creating the listener socket.
     */
    public Mediator(int bindingPort) throws IOException {
        this.listenerThread = new ListenerThread(bindingPort);
        logger.info("Created new mediator on port {}", bindingPort);
    }

    
    /**
     * Starts the mediator. Strictly speaking it starts the listener
     * thread. You can't start a mediator twice and you can't (re)start it
     * after stopping it.
     * 
     * @throws IllegalStateException
     *             if the mediator was already started.
     */
    public synchronized void start() throws IllegalStateException {
        if (started) {
            throw new IllegalStateException("Mediator is already started");
        }
        listenerThread.start();
        started = true;
        logger.info("Mediator started");
    }
    
    /**
     * Stops the mediator. Strictly speaking it stops two listener thread.
     * 
     * @throws IllegalStateException
     *             if the mediator was not started yet.
     */
    public synchronized void stop() throws IllegalStateException {
        if(!started) {
            throw new IllegalStateException("Mediator is not started");
        }
        listenerThread.interrupt();
    }

    /**
     * Main method of the mediator. Creates and starts a mediator instance.
     * 
     * @param args
     *            arguments for the mediator. An array with length of one is
     *            expected. It should contain the following value: args[0] port
     *            of listener for messages over TCP.
     * @throws IOException
     *             if an IOException occurs while creating the mediator.
     * @throws IllegalArgumentException
     *             if args don't fit the requirements above.
     */
    public static void main(String[] args) throws IOException, IllegalArgumentException {
        if (args.length != 1) {
            throw new IllegalArgumentException(WRONG_ARGS);
        }

        int port;

        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_ARGS);
        } 
        new Mediator(port).start();
    }
}
