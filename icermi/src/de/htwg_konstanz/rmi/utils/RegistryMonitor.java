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
package de.htwg_konstanz.rmi.utils;

import java.lang.reflect.Method;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RegistryMonitor {
	private static final int PERIOD = 60000;
	private String host;
	
	private RegistryMonitor(String host) {
		this.host = host;
	}
	
	private void run() throws RemoteException, NotBoundException, InterruptedException {
		Registry registry = LocateRegistry.getRegistry(host);
		
		while ( true ) {	
			String[] names = registry.list();
		
			for ( String name : names ) {
				Remote server = registry.lookup(name);
				Class<?> c = server.getClass();

				System.out.println(name + ":");

				Class<?>[] interfaces = c.getInterfaces();
				for ( Class<?> iface : interfaces ) {
					System.out.println("   " + iface);
					Method[] methods = iface.getMethods();
					for ( Method m : methods ) {
						System.out.println("      " + m);
					}		
				}	
			}
			System.out.println();
			System.out.println("----------------------");
			System.out.println();

			Thread.sleep(PERIOD);
		}
	}
		
	
	public static void main(String[] args) throws RemoteException, 
			NotBoundException, InterruptedException {
		
		System.setProperty("java.security.policy", RegistryMonitor.class
				.getClassLoader().getResource("resources/client.policy")
				.toExternalForm());

		System.setSecurityManager(new SecurityManager());
	
		String host = ( args.length == 0 ) ? "141.37.121.130" : args[0];
		
		new RegistryMonitor(host).run();
	}
	
}
