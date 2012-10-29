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

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Demo usage of hole punching RMI on client side.
 * 
 * @author Daniel Maier
 * 
 */
public class Source {

    public static void main(String[] args) throws RemoteException, NotBoundException,
            InterruptedException {
        System.setProperty("java.security.policy",
                Source.class.getClassLoader().getResource("client.policy").toExternalForm());

        System.setSecurityManager(new SecurityManager());
        Registry registry = LocateRegistry.getRegistry("141.37.121.124");
        Hello server = (Hello) registry.lookup("hello");
        System.out.println(server.hello());
    }
}
