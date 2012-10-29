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
import java.util.concurrent.Executor;

import de.htwg_konstanz.in.helper.sockets.ListenerTaskFactory;

/**
 * Factory to create {@link MessageDispatcherTask} instances.
 * 
 * @author Daniel Maier
 * 
 */
public class MessageDispatcherTaskFactory implements ListenerTaskFactory {

    private final Map<UUID, BlockingQueue<Socket>> connIDToQueue;
    // has to be unbounded
    private final Executor controlConnectionHandlerExecutor;
    // has to be unbounded
    private final Executor relayExecutor;

    /**
     * Creates a new MessageDispatcherTaskFactory instance.
     * 
     * @param connIDToQueue map to match relay connection between client and peers
     * @param controlConnectionHandlerExecutor
     *            executor that is used to handle messages of control
     *            connections
     * @param relayExecutor
     *            executor that is used for task for data relay stuff
     */
    public MessageDispatcherTaskFactory(Map<UUID, BlockingQueue<Socket>> connIDToQueue,
            Executor controlConnectionHandlerExecutor, Executor relayExecutor) {
        this.connIDToQueue = connIDToQueue;
        this.controlConnectionHandlerExecutor = controlConnectionHandlerExecutor;
        this.relayExecutor = relayExecutor;
    }

    /**
     * Returns a new {@link MessageDispatcherTask}.
     */
    public Runnable getTask(Socket s) throws IOException {
        return new MessageDispatcherTask(s, connIDToQueue, controlConnectionHandlerExecutor,
                relayExecutor);
    }

}
