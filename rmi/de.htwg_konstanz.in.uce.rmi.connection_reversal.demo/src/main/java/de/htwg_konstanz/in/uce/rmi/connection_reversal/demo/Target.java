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

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.htwg_konstanz.in.uce.rmi.connection_reversal.socket_factories.ConnectionReversalRemoteObject;

/**
 * Demo usage of hole punching RMI on server side.
 * 
 * @author Daniel Maier
 * 
 */
public class Target {
	
    public static void main(String[] args) throws RemoteException {
    	String ip_reversal = args[0]; // "localhost"
    	int port_reversal = Integer.parseInt(args[1]); // 10300
    	
    	String ip_registry = args[2]; // 141.37.121.124
    	int port_registry = Integer.parseInt(args[3]); // 1099 
    	
        System.setProperty(
                "java.rmi.server.codebase",
                "http://ice.in.htwg-konstanz.de/downloads/demo/connection_reversal/hello.jar "
                        + " http://ice.in.htwg-konstanz.de/downloads/demo/connection_reversal/socket_factories-0.1-jar-with-dependencies.jar");

        Hello impl = new HelloImpl();
        
        Hello stub = (Hello) ConnectionReversalRemoteObject.exportObject(impl, 0,
                new InetSocketAddress(ip_reversal, port_reversal));

        Registry registry = LocateRegistry.getRegistry(ip_registry, port_registry);
        registry.rebind("hello", stub);
    }
}
