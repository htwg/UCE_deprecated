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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

import de.htwg_konstanz.in.uce.hp.parallel.target.HolePunchingTarget;

/**
 * {@link ServerSocket} that accepts hole punching connections.
 * 
 * @author Daniel Maier
 * 
 */
final class HolePunchingServerSocket extends ServerSocket {

    private final HolePunchingTarget holePunchingTarget;

    /**
     * Creates a new HolePunchingServerSocket.
     * 
     * @param id
     *            the id under that the target gets registered.
     * @param mediatorSocketAddress
     *            the endpoint of the mediator on that it waits for
     *            registrations.
     * @throws IOException rethrown from from {@link ServerSocket#ServerSocket()}
     */
    HolePunchingServerSocket(UUID id, InetSocketAddress mediatorSocketAddress) throws IOException {
        super();
        this.holePunchingTarget = new HolePunchingTarget(mediatorSocketAddress, id.toString());
    }

    /**
     * Registers the target with the mediator.
     * 
     * @throws IOException if an I/O error occurs.
     */
    void register() throws IOException {
        holePunchingTarget.start();
    }

    /**
     * In contrast to the accept() method of {@link ServerSocket}, this method
     * is interruptible. If the thread waiting in this method gets interrupted,
     * an IOException is thrown.
     */
    public Socket accept() throws IOException {
        try {
            return holePunchingTarget.accept();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }
}
