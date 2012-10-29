/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.registry.p2p.peer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;

import de.htwg_konstanz.in.uce.dht.dht_access.UceDht;

/**
 * Implements an RMI Registry that binds to a DHT back-end.
 * This is no remote RMI Registry service (yet), it only provides a local
 * registry binding to the DHT. So any server / client that wants to access
 * the DHT needs to use P2pRmiRegistry.
 *  
 * TODO the registry should also listen to requests, like the rrr
 * 
 * @author thomas zink, daniel maier
 */
public final class P2pRmiRegistry extends RemoteServer implements Registry {
	/**
	 * starts the security manager, if not yet available
	 * TODO check if security manager is really always needed
	 */
	/*static {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
	}*/
	
	private static final long serialVersionUID = 6308383134884477405L;
	
	/**
	 * the DHT adapter
	 */
	private final UceDht dht;
	
	/**
	 * Creates a new P2P RMI Registry using the specified DHT adapter
	 * @param dht the dht adapter
	 */
	private P2pRmiRegistry (final UceDht dht) {
		this.dht = dht; 
	}
	
	/*private P2pRmiRegistry (final UceDht dht, final int port, final InetSocketAddress address) throws RemoteException {
		this.dht = dht;
		HolePunchingRmiSocketFactory hprsf = new HolePunchingRmiSocketFactory(address);
		LiveRef ref = new LiveRef(port, hprsf, hprsf);
		new UnicastServerRef(ref).exportObject(this, null); // <- throws an exception right now, obviously requires a stub ... 
	}*/
	
	/**
	 * Factory for P2pRmiRegistry.
	 * @param dht the dht adapter
	 * @return a new P2pRmiRegistry instance
	 */
	public static P2pRmiRegistry getRegistry (final UceDht dht) {
		return new P2pRmiRegistry(dht);
	}
	
	/* (non-Javadoc)
	 * @see java.rmi.registry.Registry#lookup(java.lang.String)
	 */
	public Remote lookup(String name) throws RemoteException, NotBoundException {
        byte[] returnValue;
        // retrieve from dht
        try {
        	returnValue = dht.get(name);
        } catch (InterruptedException e) {
        	Thread.currentThread().interrupt();
            throw new RemoteException("Lookup interrupted", e);
        } 
        
        if(returnValue == null) throw new NotBoundException("Value could not be retreived.");
        
        ByteArrayInputStream bin = new ByteArrayInputStream(returnValue);
        ObjectInputStream oin;

        // create the stub
        Remote stub = null;
        try {
            oin = new ObjectInputStream(bin);
            stub = (Remote) oin.readObject();
        } catch (IOException e) {
            // throw new RemoteException("", e);
        } catch (ClassNotFoundException e) {
            // throw new RemoteException("", e);
        }
        return stub;
	}

	/* (non-Javadoc)
	 * @see java.rmi.registry.Registry#bind(java.lang.String, java.rmi.Remote)
	 */
	public void bind(String name, Remote obj) throws RemoteException,
			AlreadyBoundException {
		try {
			lookup(name);
			throw new AlreadyBoundException(name);
		} catch (NotBoundException e) {
			rebind(name, obj);
		}
	}

	/* (non-Javadoc)
	 * @see java.rmi.registry.Registry#unbind(java.lang.String)
	 */
	public void unbind(String name) throws RemoteException, NotBoundException,
			AccessException {
		try {
			dht.remove(name);
		} catch (InterruptedException e) {
			throw new RemoteException("",e);
		}
	}

	/* (non-Javadoc)
	 * @see java.rmi.registry.Registry#rebind(java.lang.String, java.rmi.Remote)
	 */
	public void rebind(String name, Remote obj) throws RemoteException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new RemoteException("", e);
        }
        try {
            dht.put(name, baos.toByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
	}

	/* (non-Javadoc)
	 * @see java.rmi.registry.Registry#list()
	 */
	public String[] list() throws RemoteException {
		// TODO: we could keep a list of stubs we registered and return that ...
		throw new RemoteException("Cannot retrieve list.");
	}

}
