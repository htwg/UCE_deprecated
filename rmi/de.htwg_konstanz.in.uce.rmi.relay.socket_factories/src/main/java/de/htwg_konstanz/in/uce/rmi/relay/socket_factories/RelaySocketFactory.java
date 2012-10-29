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
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

import de.htwg_konstanz.in.uce.socket.relay.client.RelayClient;

/**
 * A {@link RelaySocketFactory} is both a {@link RMIClientSocketFactory} and
 * {@link RMIServerSocketFactory}. Instead of using standard socket connection
 * establishment, it uses relaying.
 * 
 * @author Daniel Maier
 * 
 */
public class RelaySocketFactory implements RMIServerSocketFactory, RMIClientSocketFactory,
        Serializable {

    private static final long serialVersionUID = -7707534555030453027L;

    private InetSocketAddress peerRelayEndpoint;
    private final InetSocketAddress relayServerEndpoint;

    /**
     * Creates a {@link RelaySocketFactory}.
     * 
     * @param relayServerEndpoint
     *            endpoint of the relay server on that it is waiting for new
     *            control connections
     */
    public RelaySocketFactory(InetSocketAddress relayServerEndpoint) {
        this.relayServerEndpoint = relayServerEndpoint;
    }

    /**
     * Returns a new {@link RelayServerSocket} and creates a new allocation on
     * the relay server by using it.
     */
    public ServerSocket createServerSocket(int port) throws IOException {
        // TODO was wenn das mehrmals aufgerufen wird?
        RelayClient relayClient = new RelayClient(relayServerEndpoint, port);
        RelayServerSocket rss = new RelayServerSocket(relayClient);
        peerRelayEndpoint = rss.createAllocation();
        return rss;
    }

    /**
     * Creates a new socket to the allocation that was created by the invocation
     * of the {@link #createServerSocket(int)} method on the relay server (acts
     * as a peer).
     */
    public Socket createSocket(String address, int port) throws IOException {
        return new Socket(peerRelayEndpoint.getAddress(), peerRelayEndpoint.getPort());
    }

}
