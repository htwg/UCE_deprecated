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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.ConnectException;
import java.rmi.MarshalledObject;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import sun.rmi.server.UnicastRef;
import sun.rmi.server.UnicastServerRef;
import sun.rmi.transport.LiveRef;

/**
* @author Oliver Haase, HTWG Konstanz
*/

/**
* This class provides an implementation of the {@link java.rmi.registry.Registry} interface.
* In contrast to the standard <code>rmiregistry</code> tool, an instance of this class allows 
* invocations of the server side methods {@link #bind}, {@link #rebind}, and {@link #unbind}
* from  objects that  run on remote machines. Also, bindings are stored persistently to allow
* for lossless registry reboots.
* 
* <p>
* 
* RMI servers and clients can obtain a reference to a <code>RemoteRegistry</code> object through
* invocation of the {@link java.rmi.registry.LocateRegistry#getRegistry()} method, as if 
* they dealt with the standard <code>rmiregistry</code>.
*/

public class RemoteRegistry extends RemoteServer implements Registry {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("serial")
	private static class ValuePair implements Serializable {
		public Remote stub;
		public String host;

		public ValuePair(Remote stub, String host) {
			this.stub = stub;
			this.host = host;
		}
	}
	
	private String rrrFile; 
	private Map<String, ValuePair> map;
	
	private class PingThread implements Runnable {
		private static final int SLEEPTIME = 120000; // 2 minutes
		
		private String name;

		public PingThread(String name) {
			this.name = name;
		}
						
		private boolean isAlive() {
			try {
				((UnicastRef) 
					((RemoteObjectInvocationHandler) 
						Proxy.getInvocationHandler(map.get(name).stub)).getRef()).
							invoke(null, null, null, 0);
			}
			catch ( ConnectException e) {
				return false;
			}
			catch ( Exception e) {
				return true;
			}
			// this line is never executed. The method, however, needs a 
			// return value...
			return true; 
		}
		
		public void run() {	
			System.out.println(name + "-thread started.");
			boolean done = false;
			while ( !done ) {
				try {
					Thread.sleep(SLEEPTIME);
				} catch (InterruptedException e) {
					throw new RuntimeException("interrupted");
				}
					
				if (isAlive()) {
					System.out.println(name + " on the safe side");
				} else {
					System.out.println(name + " to be removed");
					if ( Thread.currentThread().isInterrupted() ) {
						throw new RuntimeException("interrupted");
					}
					removeBinding(name);
					done = true;
				}
			}
			System.out.println(name + "-thread stopped.");
		}
	}
	
	private Map<String, Thread> pingers;
	

	/**
	 * Constructs a remote registry that listens on the given port and 
	 * stores bindings in the given file.
	 * 
	 * When created, the <code>RemoteRegistry</code> instance reads the previously 
	 * stored bindings from file <code>rrrFile</code>, if such a file exists. Otherwise,
	 * an empty collection of bindings is created. Each time the bindings 
	 * are modified through a call of {@link #bind}, {@link #rebind}, or {@link #unbind}, 
	 * <code>rrrFile</code> is 
	 * updated.  
	 * 
	 * @param port port that the remote registry listens on
	 * @param rrrFile name of the file to be used for persistent storage
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public RemoteRegistry(int port, String rrrFile) throws ClassNotFoundException, IOException {
		pingers = new HashMap<String, Thread>();

		try {
			this.rrrFile = rrrFile;
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(rrrFile));
			System.out.println("reloading stored bindings...");
			// use MarshalledObject for deserialization in order not to
			// lose codebase settings of the stubs
			MarshalledObject mo = (MarshalledObject) ois.readObject();
			map = (HashMap<String, ValuePair>) mo.get();
			ois.close();
			
			// create a ping thread per binding
			Set<String> names = map.keySet();
			for ( String name : names ) {
				Thread pinger = new Thread(new PingThread(name));
				pingers.put(name, pinger);
				pinger.start();
			}
			
		} catch ( FileNotFoundException e ) {
			// first run, no persistent bindings yet
			System.out.println("initializing empty bindings...");
			map = new HashMap<String, ValuePair>();
		}
	
		LiveRef lref = new LiveRef(new ObjID(ObjID.REGISTRY_ID), port);
		new UnicastServerRef(lref).exportObject(this, null);
	}
	
	private synchronized void removeBinding(String name) {
		map.remove(name);
		writeBindings();
	}
	
	
	private synchronized void addBinding(String name, ValuePair pair) {
		map.put(name, pair);
		writeBindings();
	}
	
	private void writeBindings() {
		try {
			// use MarshalledObject for serialization in order not to
			// lose codebase settings of the stubs
			ObjectOutputStream oos = new ObjectOutputStream(
					new FileOutputStream(rrrFile));
			oos.writeObject(new MarshalledObject(map));
			oos.close();
			System.out.println("persistent bindings updated");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getHost() throws RemoteException {
		try {
			return java.rmi.server.RemoteServer.getClientHost();
		} catch (ServerNotActiveException e) {
			e.printStackTrace();
			throw new RemoteException(e.getMessage());
		}
	}

	private void checkArgsNotNull(Object... args) {
		for (Object arg : args) {
			if (arg == null) {
				throw new NullPointerException();
			}
		}
	}

	private void checkMapContainsName(String name) throws NotBoundException {
		if (!map.containsKey(name)) {
			System.out.println(name + " not bound");
			throw new NotBoundException(name);
		}
	}

	private void checkMapDoesntContainName(String name) throws AlreadyBoundException {
		if (map.containsKey(name)) {
			System.out.println(name + " already bound");
			throw new AlreadyBoundException(name);
		}
	}
	
	private void checkSameHost(String name, String host, String command) throws AccessException {		
		if (map.containsKey(name) && !host.equals(map.get(name).host)) {
			throw new AccessException(command + " attempt by foreign host");
		}	
	}
	
	protected void finalize() {
		System.out.println("remote registry is being garbage collected, bye.");
		System.exit(0);
	}

	// *************************************************************
	// start of Registry methods
	// *************************************************************

	public String[] list() throws RemoteException {
		System.out.println("list");
		return map.keySet().toArray(new String[map.size()]);
	}

	public Remote lookup(String name) throws RemoteException, NotBoundException {
		System.out.println("lookup " + name);

		checkArgsNotNull(name);
		checkMapContainsName(name);

		return map.get(name).stub;
	}
	
	/**
	* In contrast to the reference implementation, this method can be called
	* both locally and remotely.
	*/
	public void bind(String name, Remote obj) throws RemoteException,
			AlreadyBoundException, AccessException {
		String host = getHost();
		System.out.println("bind " + name + " by " + host);

		checkArgsNotNull(name, obj);
		checkMapDoesntContainName(name);
		addBinding(name, new ValuePair(obj, host));	
		
		Thread pinger = new Thread(new PingThread(name));
		pingers.put(name, pinger);
		pinger.start();	
	}

	/**
	* In contrast to the reference implementation, this method can be called
	* both locally and remotely.
	*/
	public void rebind(String name, Remote obj) throws RemoteException,
			AccessException {
		String host = getHost();
		System.out.println("rebind " + name + " by " + host);
		
		checkArgsNotNull(name, obj);
		checkSameHost(name, host, "rebind");
		
		if ( !map.containsKey(name) ) {
			addBinding(name, new ValuePair(obj, host));	
			
			Thread pinger = new Thread(new PingThread(name));
			pingers.put(name, pinger);
			pinger.start();
		}
		else {
			addBinding(name, new ValuePair(obj, host));	
		}
	}

	/**
	* In contrast to the reference implementation, this method can be called
	* both locally and remotely.
	*/
	public void unbind(String name) throws RemoteException, NotBoundException,
			AccessException {
		String host = getHost();
		System.out.println("unbind " + name + " by " + host);

		checkArgsNotNull(name);
		checkMapContainsName(name);
		checkSameHost(name, host, "unbind");

		Thread pinger = pingers.get(name);
		pinger.interrupt();
		pingers.remove(name);
		removeBinding(name);
	}

}
