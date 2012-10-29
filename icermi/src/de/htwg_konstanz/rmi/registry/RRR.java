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
package de.htwg_konstanz.rmi.registry;

import java.io.IOException;
import java.rmi.registry.Registry;

/**
* @author Oliver Haase, HTWG Konstanz
*/

public class RRR  {
	private static String rrrFile = ".rrrbindings"; 
	
	@SuppressWarnings("unused")
	private static Registry registry;
	

	public static void main(String[] args) throws InterruptedException, 
		IOException, ClassNotFoundException {

		System.setProperty("java.security.policy", RRR.class
				.getClassLoader().getResource("resources/rrr.policy")
				.toExternalForm());

		System.setSecurityManager(new SecurityManager());

		int port = Registry.REGISTRY_PORT;

		try {
			port = Integer.valueOf(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			// no port argument --> stick with Registry.REGISTRY_PORT
		} catch (NumberFormatException e) {
			// wrong port format
			System.out.println("Unrecognized argument " + args[0]
					+ "; you can optionally specify a port number.");
			System.exit(0);
		}
		
		registry = new RemoteRegistry(port, rrrFile);
		System.out.println("remote registry up and listening on port " + port);
	}
}
