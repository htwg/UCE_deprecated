/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.registry.p2p.peer;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.htwg_konstanz.in.uce.rmi.hp.socket_factrories.HolePunchingRemoteObject;

/**
 * The P2P RMI Server takes any Remote Object, creates the Stub and
 * registers it using a P2P RMI Registry. As long as services are registered
 * the server class keeps a service thread running to prevent premature shutdown.
 * To add objects as servers, currently a mediator address is required. Note that
 * this can be different for each serving object.
 * 
 * Actually, the name P2pRmiServer is misleading. It can potentially take any
 * Registry reference and provide server functionality in any environment. So it is
 * not bound to an actual P2P or DHT environment.
 * 
 * TODO get rid of mediator requirement, use a UCE manager instead
 * TODO To object management / instantiation / destruction here
 * TODO allow any object to be added as a service, Cajo inspired 
 *   
 * @author thomas zink
 */
public class P2pRmiServer implements Serializable {
	/*
	 * Sample solutions:
	 * - simple: server takes remote object instance, provides bind / unbind and passes that to p2p rmi registry
	 * - complicated: server takes any remote object, possible arguments to constructor (maybe use reflection or similar), automatically locates
	 *   a registry using somehow obtained global information (maybe lookup sth in the dht itself)
	 *   
	 * Another possible solution would be to utilize java.rmi.actvation stuff, might be worth a look
	 */ 

	private static final long serialVersionUID = -2204307689815542860L;
	
	/**
	 * starts the security manager, if not yet available
	 * TODO check if security manager is really always needed
	 */
	/*static {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
	}*/
	
	/**
	 * An RMI Registry reference
	 */
	private final Registry registry;
	
	/**
	 * Number of registered services
	 */
	private int numServices = 0;
	
	/**
	 * Service Thread executor
	 */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();
	
	/**
	 * Service thread
	 */
	private final Thread serve = new Thread() {	
		public void run() {
			while(true);
		}
	};
	
	/**
	 * Constructs a new P2pRmiServer instance.
	 * @param registry the registry reference
	 */
	private P2pRmiServer (final Registry registry) {
		this.registry = registry;
	}
	
	
	public static P2pRmiServer newInstance (final Registry registry) {
		return new P2pRmiServer(registry);
	}
	
	public final synchronized int numServices() {
		return this.numServices;
	}
		
	private final synchronized void addService() {
		this.numServices += 1;
		if (numServices()<=0) executor.execute(serve);
	}
	
	private final synchronized void removeService() {
		this.numServices -= 1;
		if (numServices()<=0) executor.shutdown();
	}
	
	/**
	 * Start serving a Remote instance. Stub is generated and registered.
	 * 
	 * @param instance the Remote object to serve 
	 * @param address the public reachable address on which object is served
	 * @return the registered stub
	 * @throws RemoteException
	 */
	public <T extends Remote> T addService (String name, T instance, InetSocketAddress address) throws RemoteException {
		String key = name;
		@SuppressWarnings("unchecked")
		T stub = (T) HolePunchingRemoteObject.exportObject(instance, 0, address);
		this.registry.rebind(key, stub);
		// TODO: addService only, if not already serving remote object!
		this.addService();
		return stub;
	}
	
	// we don't actually need the interface.
	/*public <T extends Remote> T addService (String name, T instance, Class<T> iface, InetSocketAddress address) throws RemoteException {
		String key = name;
		if (key == null || key.equals("")) key = iface.getName();
		T stub = iface.cast(HolePunchingRemoteObject.exportObject(instance, 0, address));
		this.registry.rebind(key, iface.cast(stub)); // cast not needed
		this.addService();
		return stub;
	}*/
	
	/**
	 * Removes the Stub from the registry and decreases service count.
	 * 
	 * @param name the name under which the stub has been bound.
	 * @throws AccessException
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void removeService (String name) throws AccessException, RemoteException, NotBoundException {
		registry.unbind(name);
		this.removeService();
	}
}
