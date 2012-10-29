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
package de.htwg_konstanz.rmi.hp.mediator;


public class MediatorStarter {

	public static void main(String[] args) {
		int port = 8765;
		try {
			port = Integer.valueOf(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			// no port argument --> start with default port
		} catch (NumberFormatException e) {
			// wrong port format
			System.out.println("Unrecognized argument " + args[0]
					+ "; you can optionally specify a port number.");
			System.exit(0);
		}	
		new Mediator(port);

	}

}
