package de.htwg_konstanz.in.uce.rmi.relay.demo;

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

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.htwg_konstanz.in.uce.rmi.relay.socket_factories.RelayRemoteObject;

/**
 * Demo usage of relay RMI on server side.
 * 
 * @author Daniel Maier
 * 
 */
public class Target {
	
    public static void main(String[] args) throws RemoteException {
    	String ip_relay = args[0]; // "localhost"
    	int port_relay = Integer.parseInt(args[1]); // 10300
    	
    	String ip_registry = args[2]; // 141.37.121.124
    	int port_registry = Integer.parseInt(args[3]); // 1099 
    	
        System.setProperty(
                "java.rmi.server.codebase",
                "http://ice.in.htwg-konstanz.de/downloads/demo/relay/hello.jar "
                        + " http://ice.in.htwg-konstanz.de/downloads/relay/socket_factories-0.1-jar-with-dependencies.jar");

        Hello impl = new HelloImpl();
        
        Hello stub = (Hello) RelayRemoteObject.exportObject(impl, 0,
                new InetSocketAddress(ip_relay, port_relay));

        Registry registry = LocateRegistry.getRegistry(ip_registry, port_registry);
        registry.rebind("hello", stub);
    }
}
