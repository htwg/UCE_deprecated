/**
 * Copyright (C) 2011 Thomas Zink
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
 
package de.htwg_konstanz.in.uce.common.HelloRemote;

import java.rmi.RemoteException;

/**
 * Example implementation of {@link IHelloRemote}.
 * @author thomas zink
 */
public class HelloRemote implements IHelloRemote {
	private static final String DEFAULT_MESSAGE = "Hello Remote World!";
	private final String message;
	
	/**
	 * @param msg the message to display
	 * @throws RemoteException
	 */
	public HelloRemote(final String msg) throws RemoteException {
		this.message = msg == null ? DEFAULT_MESSAGE : msg;
	}

	/**
	 * Default constructor.
	 * @throws RemoteException
	 */
	public HelloRemote() throws RemoteException {
		this(null);
	}
	
	/* (non-Javadoc)
	 * @see de.htwg_konstanz.in.uce.common.IHelloRemote#hello()
	 */
	public String message() throws RemoteException {
		return this.message;
	}

}
