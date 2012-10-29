/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.registry.p2p.peer;

import java.rmi.RemoteException;

import de.htwg_konstanz.in.uce.dht.dht_access.UceDht;

/**
 * The peer unifies all P2P RMI functionality. 
 * @author tzink
 */
public class P2pRmiPeer {
	private final UceDht dht;
	private final P2pRmiRegistry registry;
	private final P2pRmiServer server;
	
	private P2pRmiPeer (UceDht dht) throws RemoteException {
		this.dht = dht;
		this.dht.bootstrap();
		this.registry = P2pRmiRegistry.getRegistry(this.dht);
		this.server = P2pRmiServer.newInstance(this.registry);
	}
	
	public static P2pRmiPeer newInstance (UceDht dht) throws RemoteException {
		return new P2pRmiPeer(dht);
	}
	
	public P2pRmiRegistry registry() {
		return this.registry;
	}
	
	public P2pRmiServer server() {
		return this.server;
	}
	
	public UceDht dht() {
		return this.dht;
	}
}
