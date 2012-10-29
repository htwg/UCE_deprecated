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

package de.htwg_konstanz.in.uce.rmi.hp.socket_factrories;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.UUID;

import de.htwg_konstanz.in.uce.hp.parallel.source.HolePunchingSource;
import de.htwg_konstanz.in.uce.hp.parallel.source.TargetNotRegisteredException;

/**
 * A {@link HolePunchingRmiSocketFactory} is both a
 * {@link RMIClientSocketFactory} and {@link RMIServerSocketFactory}. Instead of
 * using standard socket connection establishment, it uses hole punching.
 * 
 * @author Daniel Maier
 * 
 */
public final class HolePunchingRmiSocketFactory implements RMIClientSocketFactory,
        RMIServerSocketFactory, Serializable {

    private static final long serialVersionUID = 3603126460143331294L;

    private final UUID id;

    private final InetSocketAddress mediatorSocketAddress;

    /**
     * Creates a new HolePunchingRmiSocketFactory.
     * 
     * @param mediatorSocketAddress
     *            the endpoint of the mediator on that it waits for
     *            registrations.
     */
    public HolePunchingRmiSocketFactory(InetSocketAddress mediatorSocketAddress) {
        id = UUID.randomUUID();
        this.mediatorSocketAddress = mediatorSocketAddress;
    }

    /**
     * Creates a new client socket to the target identified by the internal
     * <i>id</i>. Ignores host and port parameters.
     */
    public Socket createSocket(String host, int port) throws IOException {
        HolePunchingSource hpSource = new HolePunchingSource();
        try {
            return hpSource.getSocket(id.toString(), mediatorSocketAddress);
        } catch (TargetNotRegisteredException e) {
            throw new IOException(e);
        }
    }

    /**
     * Creates a new {@link HolePunchingServerSocket}, registers it under the
     * internal <i>id</i> and returns it. Ignores <i>port</i> parameter.
     */
    public ServerSocket createServerSocket(int port) throws IOException {
        HolePunchingServerSocket hpSocket = new HolePunchingServerSocket(id,
                mediatorSocketAddress);
        hpSocket.register();
        return hpSocket;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((mediatorSocketAddress == null) ? 0 : mediatorSocketAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof HolePunchingRmiSocketFactory)) {
            return false;
        }
        HolePunchingRmiSocketFactory other = (HolePunchingRmiSocketFactory) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (mediatorSocketAddress == null) {
            if (other.mediatorSocketAddress != null) {
                return false;
            }
        } else if (!mediatorSocketAddress.equals(other.mediatorSocketAddress)) {
            return false;
        }
        return true;
    }
}
