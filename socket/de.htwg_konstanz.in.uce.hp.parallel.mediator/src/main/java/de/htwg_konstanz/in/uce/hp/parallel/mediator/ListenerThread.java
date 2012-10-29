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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread that listens for messages over TCP. It can handle the following
 * messages: RegisterMessage and ConnectionRequestMessage.
 * 
 * @author Daniel Maier
 * 
 */
final class ListenerThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ListenerThread.class);
    private final ServerSocket ss;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Creates a new ListenerThread.
     * @param bindingPort the port on that the mediator should listen for messages.
     * @throws IOException if an IOException occurs while creating the listener socket.
     */
    ListenerThread(int bindingPort) throws IOException {
        this.ss = new ServerSocket();
        ss.bind(new InetSocketAddress(bindingPort));
    }

    /**
     * Listens for messages until the thread gets interrupted. Starts a new
     * MessageHandlerTask for each connection that gets accepted.
     */
    @Override
    public final void run() {
        try {
            while (!isInterrupted()) {
                Socket s = ss.accept();
                logger.info("New connection from: {}", s);
                executor.execute(new MessageHandlerTask(s));
            }
        } catch (IOException e) {
            logger.error("IOException while accepting connection: {}", e.getMessage());
        } finally {
            logger.info("entered finally block. interrupt status is: {}", isInterrupted());
        	executor.shutdownNow();        	
        }
    }

    @Override
    public final void interrupt() {
        try {
            ss.close();
        } catch (IOException ignore) {
        }
        super.interrupt();
    }
}
