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

package de.htwg_konstanz.in.uce.rmi.hp.demo;

import java.net.InetSocketAddress;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.htwg_konstanz.in.uce.rmi.hp.socket_factrories.HolePunchingRemoteObject;

/**
 * Demo usage of hole punching RMI on server side.
 * 
 * @author Daniel Maier
 * 
 */
public class Target {

    public static void main(String[] args) throws RemoteException {
        System.setProperty(
                "java.rmi.server.codebase",
                "http://ice.in.htwg-konstanz.de/downloads/demo/hello.jar "
                        + " http://ice.in.htwg-konstanz.de/downloads/socket_factories-0.1-jar-with-dependencies.jar");

        Hello stub = (Hello) HolePunchingRemoteObject.exportObject(new HelloImpl(), 0,
                new InetSocketAddress("141.37.121.124", 10200));

        Registry registry = LocateRegistry.getRegistry("141.37.121.124");
        registry.rebind("hello", stub);
    }
}
