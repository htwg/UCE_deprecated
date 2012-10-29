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

package de.htwg_konstanz.in.uce.rmi.relay.socket_factories;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import de.htwg_konstanz.in.uce.socket.relay.client.RelayClient;

/**
 * {@link ServerSocket} that returns new relaying connections to peers via its
 * {@link #accept()} method.
 * 
 * @author Daniel Maier
 * 
 */
final class RelayServerSocket extends ServerSocket {
    private final RelayClient relayClient;

    /**
     * Creates a new {@link RelayServerSocket}.
     * 
     * @param relayClient
     *            the {@link RelayClient} that is used to communicate with the
     *            rely server
     * @throws IOException
     *             if an I/O error occurs
     */
    RelayServerSocket(RelayClient relayClient) throws IOException {
        this.relayClient = relayClient;

    }

    /**
     * Creates a new allocation on the relay server by using the given
     * {@link #relayClient}.
     * 
     * @return the public endpoint of the allocation on the relay server
     * @throws IOException
     *             if an I/O error occurs
     */
    InetSocketAddress createAllocation() throws IOException {
        // TODO state check
        return relayClient.createAllocation();
    }

    /**
     * Returns sockets that are connected to the relay server to relay data
     * between the client and peers.
     */
    @Override
    public Socket accept() throws IOException {
        try {
            return relayClient.accept();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    /**
     * Discards the relay allocation on the relay server.
     */
    @Override
    public void close() throws IOException {
        this.relayClient.discardAllocation();
        super.close();
    }
}
