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

import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;

import de.htwg_konstanz.in.helper.sockets.ListenerTaskFactory;
import de.htwg_konstanz.in.uce.socket.relay.messages.MessageWriter;

/**
 * Factory to create new {@link PeerHandlerTask}.
 * 
 * @author Daniel Maier
 * 
 */
public class PeerHandlerTaskFactory implements ListenerTaskFactory {

    private final Map<UUID, BlockingQueue<Socket>> connIDToQueue;
    private final MessageWriter controlConnection;
    // has to be unbounded
    private final Executor relayExecutor;

    /**
     * Creates a new {@link PeerHandlerTask}.
     * 
     * @param connIDToQueue
     *            map to match relay connection between client and peers
     * @param controlConnection
     *            a {@link MessageWriter} to the control connection to the
     *            client
     * @param relayExecutor
     *            the executor that gets used to execute task for the real relay
     *            stuff.
     */
    public PeerHandlerTaskFactory(Map<UUID, BlockingQueue<Socket>> connIDToQueue,
            MessageWriter controlConnection, Executor relayExecutor) {
        this.connIDToQueue = connIDToQueue;
        this.controlConnection = controlConnection;
        this.relayExecutor = relayExecutor;
    }

    /**
     * Returns a new {@link PeerHandlerTask}
     */
    public Runnable getTask(Socket s) {
        return new PeerHandlerTask(s, connIDToQueue, controlConnection, relayExecutor);
    }

}
