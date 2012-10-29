/**
 * Copyright (C) 2012 HTWG Konstanz, Oliver Haase
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
package de.htwg_konstanz.rmi.hp.socket;

import java.net.InetSocketAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HolePunchingRemoteObject implements Remote {
 
	/**
	 * Creates and exports a new HolePunchingRemoteObject object using an
     * anonymous port.
	 * @param mediatorInetSocketAddress IP Socket Address of the mediator
	 * @throws RemoteException if failed to export object
	 */
	protected HolePunchingRemoteObject(InetSocketAddress mediatorInetSocketAddress) throws RemoteException {
		this(0, mediatorInetSocketAddress);
	}
	
	/**
	 * Creates and exports a new HolePunchingRemoteObject object using the
     * particular supplied port.
     * @param port the port number on which the remote object receives calls
     * (if <code>port</code> is zero, an anonymous port is chosen)
	 * @param mediatorInetSocketAddress IP Socket Address of the mediator
	 * @throws RemoteException if failed to export object
	 */
	protected HolePunchingRemoteObject(int port, InetSocketAddress mediatorInetSocketAddress) throws RemoteException {
		exportObject(this, port, mediatorInetSocketAddress);
	}

	/**
	 * Exports the remote object to make it ready to establish connections via
	 * hole punching.
	 * 
	 * @param obj
	 *            the remote object to be exported
	 * @param port
	 *            the port to export the given object on
	 * @param mediatorInetSocketAddress
	 *            IP Socket Address of the mediator
	 * @return remote object stub
	 * @throws RemoteException
	 *             if the export fails
	 */
	public static Remote exportObject(Remote obj, int port, InetSocketAddress mediatorInetSocketAddress) throws RemoteException {
		HolePunchingRmiSocketFactory fac = new HolePunchingRmiSocketFactory(mediatorInetSocketAddress);
		return UnicastRemoteObject.exportObject(obj, port, fac, fac);
	}

	/**
	 * Does the same as the unexport method of
	 * {@link java.rmi.server.UnicastRemoteObject}
	 * 
	 * @param obj
	 *            the remote object to be unexported
	 * @param force
	 *            if true, unexports the object even if there are pending or
	 *            in-progress calls; if false, only unexports the object if
	 *            there are no pending or in-progress calls
	 * @return true if operation is successful, false otherwise
	 * @throws NoSuchObjectException
	 *             if the remote object is not currently exported
	 */
	public static boolean unexportObject(Remote obj, boolean force) throws NoSuchObjectException {
		return UnicastRemoteObject.unexportObject(obj, force);
	}
}
