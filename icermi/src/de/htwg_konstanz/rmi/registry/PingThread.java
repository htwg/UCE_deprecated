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

import java.rmi.ConnectException;
import java.rmi.Remote;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.util.Map;

import sun.rmi.server.UnicastRef;

public class PingThread implements Runnable {
	
	private boolean toBeStopped = false;
	private String name;
	private RemoteObjectInvocationHandler ih;
	private Map<String, Remote> map;
	private Object monitor = new Object();

	public PingThread(String name, RemoteObjectInvocationHandler ih, Map<String, Remote> map) {
		this.name = name;
		this.ih = ih;
		this.map = map;
	}
	
	public void stop() {
		synchronized ( monitor ) {
			toBeStopped = true;	
		}
	}
	
	public void run() {
		
		System.out.println(name + "-thread started.");
		
		while ( !toBeStopped ) {
			
			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {}
			
			synchronized ( monitor ) {				
				try {
					((UnicastRef) ih.getRef()).invoke(null, null, null, 0);
				}
				catch ( ConnectException e) {
					System.out.println(name + " to be removed");
					map.remove(name);
					toBeStopped = true;
				}
				catch ( Exception e) {
					System.out.println(name + " on the safe side");
				}
			}
		}
		System.out.println(name + "-thread stopped.");

		
	}

}
