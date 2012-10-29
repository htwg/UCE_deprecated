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

import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;

/**
 * Used to export remote objects using a {@link HolePunchingRmiSocketFactory}
 * both as {@link RMISocketFactory} and {@link RMIServerSocketFactory}
 * .
 * 
 * @author Daniel Maier
 * 
 */
public final class HolePunchingRemoteObject extends UnicastRemoteObject {

    private static final long serialVersionUID = -246789676359150232L;

    /*
     * Dummy constructor to call super constructor with single custom RMI
     * factory (same for client and server)
     */
    private HolePunchingRemoteObject(int port, HolePunchingRmiSocketFactory fac)
            throws RemoteException {
        super(port, fac, fac);
    }

    /**
     * Creates and exports a new HolePunchingRemoteObject object using an
     * anonymous port.
     * 
     * @param mediatorInetSocketAddress
     *            Register Endpoint of the mediator
     * @throws RemoteException
     *             if failed to export object
     */
    protected HolePunchingRemoteObject(InetSocketAddress mediatorInetSocketAddress)
            throws RemoteException {
        this(0, new HolePunchingRmiSocketFactory(mediatorInetSocketAddress));
    }

    /**
     * Creates and exports a new HolePunchingRemoteObject object. The supplied
     * port is ignored in current implementation. Instead an anonymous port is
     * chosen.
     * 
     * @param port
     *            ignored in current implementation
     * @param mediatorInetSocketAddress
     *            IP Socket Address of the mediator
     * @throws RemoteException
     *             if failed to export object
     */
    protected HolePunchingRemoteObject(int port, InetSocketAddress mediatorInetSocketAddress)
            throws RemoteException {
        this(port, new HolePunchingRmiSocketFactory(mediatorInetSocketAddress));
    }

    /**
     * Exports the remote object to make it ready to establish connections via
     * hole punching.
     * 
     * @param obj
     *            the remote object to be exported
     * @param port
     *            the port to export the given object on (ignored in current
     *            implementation)
     * @param mediatorInetSocketAddress
     *            Register Endpoint of the mediator
     * @return remote object stub
     * @throws RemoteException
     *             if the export fails
     */
    public static Remote exportObject(Remote obj, int port,
            InetSocketAddress mediatorInetSocketAddress) throws RemoteException {
        HolePunchingRmiSocketFactory fac = new HolePunchingRmiSocketFactory(
                mediatorInetSocketAddress);
        return UnicastRemoteObject.exportObject(obj, port, fac, fac);
    }

}
