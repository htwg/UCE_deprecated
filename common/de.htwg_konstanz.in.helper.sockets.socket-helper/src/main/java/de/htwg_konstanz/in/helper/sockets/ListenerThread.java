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

package de.htwg_konstanz.in.helper.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import javax.net.ServerSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ListenerThread} is a thread that waits for incoming connections over
 * a server socket. If such a connection arrives, it starts a task via the
 * executor framework to handle it. The server socket, executor and a factory
 * for the handling tasks are configurable.
 * 
 * @author Daniel Maier
 * 
 */
public final class ListenerThread extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(ListenerThread.class);
    private final ServerSocket ss;
    private final ExecutorService executor;
    private final ListenerTaskFactory taskFactory;

    /**
     * Creates a new {@link ListenerThread}.
     * 
     * @param ss
     *            the server socket on that this {@link ListenerThread} should
     *            wait for new connections
     * @param executor
     *            the executor that is used to execute the handling tasks
     * @param taskFactory
     *            factory that is used to create the handling tasks
     * @throws NullPointerException
     *             if one of the parameters is null
     */
    public ListenerThread(ServerSocket ss, final ExecutorService executor,
            final ListenerTaskFactory taskFactory) {
        if (executor == null || taskFactory == null || ss == null) {
            throw new NullPointerException();
        }
        this.ss = ss;
        this.executor = executor;
        this.taskFactory = taskFactory;
    }

    /**
     * Creates a new {@link ListenerThread}. The needed server socket comes from
     * the given server socket factory.
     * 
     * @param bindingPort
     *            the local port to that the server socket gets bound to
     * @param serverSocketFactory
     *            the server socket factory that is used to create the server
     *            socket
     * @param executor
     *            factory that is used to create the handling tasks
     * @param taskFactory
     *            factory that is used to create the handling tasks
     * @throws IOException
     *             if the server socket cannot be created, or if the bind
     *             operation of the server socket fails
     * @throws NullPointerException
     *             if one of the non-primitive parameters is null
     */
    public ListenerThread(final int bindingPort, final ServerSocketFactory serverSocketFactory,
            final ExecutorService executor, final ListenerTaskFactory taskFactory)
            throws IOException {
        this(serverSocketFactory.createServerSocket(), executor, taskFactory);
        ss.bind(new InetSocketAddress(bindingPort));
    }

    /**
     * Runs in a loop until the interrupt status of this thread is set or an
     * {@link IOException} occurs. Before the thread terminates the given
     * executor gets shutdown. Waits for a new connection via the server socket
     * and then executes a task to handle it via the given executor. The task is
     * created by the given {@link ListenerTaskFactory} and the accepted socket
     * gets handed over to it.
     */
    @Override
    public final void run() {
        try {
            while (!isInterrupted()) {
                Socket s = ss.accept();
                logger.info("New connection from: {}", s);
                executor.execute(taskFactory.getTask(s));
            }
        } catch (IOException e) {
            logger.error("IOException while accepting connection: {}", e.getMessage());
        } finally {
            logger.info("entered finally block. interrupt status is: {}", isInterrupted());
            executor.shutdownNow();
        }
    }

    /**
     * Terminates this thread by closing the given server socket.
     */
    @Override
    public final void interrupt() {
        try {
            ss.close();
        } catch (IOException ignore) {
        } finally {
            super.interrupt();
        }
    }
}
