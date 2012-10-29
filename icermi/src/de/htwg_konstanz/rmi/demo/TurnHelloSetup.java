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
package de.htwg_konstanz.rmi.demo;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import de.htwg_konstanz.net.turn.server.TURNServer;
import de.htwg_konstanz.net.turn.socket.TurnRemoteObject;


public class TurnHelloSetup  {

	public static void main(String[] args) throws UnknownHostException, RemoteException, 
			MalformedURLException, NotBoundException, InterruptedException, AlreadyBoundException {

		System.setProperty("java.rmi.server.codebase", "http://141.37.121.130/~icermi/hello.jar " +
				"http://141.37.121.130/~icermi/TurnSocketFactory.jar");

		Hello stub = (Hello) TurnRemoteObject.exportObject(new HelloImpl(), 0,
				new InetSocketAddress("141.37.121.130", TURNServer.TURNSERVER_PORT));		

		Registry registry = LocateRegistry.getRegistry("141.37.121.130");
		registry.rebind("Turnhello" + InetAddress.getLocalHost(), stub);
		System.out.println("successfully registered.");
	}

}
