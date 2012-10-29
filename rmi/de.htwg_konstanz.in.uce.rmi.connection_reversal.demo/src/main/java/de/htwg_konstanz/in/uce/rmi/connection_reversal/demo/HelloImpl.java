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

package de.htwg_konstanz.in.uce.rmi.connection_reversal.demo;

import java.rmi.RemoteException;

/**
 * Sample implementation of {@link Hello}.
 * @author Daniel Maier
 *
 */
public class HelloImpl implements Hello {

    public String hello() throws RemoteException {
        return "hello world";
    }  
}
