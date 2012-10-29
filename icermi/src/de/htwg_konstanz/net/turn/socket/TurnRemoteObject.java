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
package de.htwg_konstanz.net.turn.socket;

import java.net.InetSocketAddress;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class TurnRemoteObject implements Remote {
	
	public TurnRemoteObject(InetSocketAddress turnInetSocketAddress) 
			throws RemoteException {
		this(0, turnInetSocketAddress);
	}
	
	public TurnRemoteObject(int port, InetSocketAddress turnInetSocketAddress) 
			throws RemoteException {
		exportObject(this, port, turnInetSocketAddress);
	}
	

	/**
	 * Exports the remote object to make it ready to establish connections via hole punching. 
	 * @param obj the remote object to be exported
	 * @param port the port to export the given object on
	 * @param mediatorInetSocketAddress IP Socket Address of the mediator
	 * @return remote object stub
	 * @throws RemoteException if the export fails
	 */
	public static Remote exportObject(Remote obj, int port, InetSocketAddress turnInetSocketAddress) throws RemoteException {
		TurnSocketFactory fac;
		try {
			fac = new TurnSocketFactory(turnInetSocketAddress);
		} catch (Exception e) {
			throw new RemoteException(e.toString());
		} 
		return UnicastRemoteObject.exportObject(obj, port, fac, fac);
	}
	
	public static boolean unexportObject(Remote obj, boolean force) throws NoSuchObjectException {
		return UnicastRemoteObject.unexportObject(obj, force);
	}
	
}
