/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.registry.p2p.demo;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.common.HelloRemote.IHelloRemote;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtVuzeAdapter;
import de.htwg_konstanz.in.uce.rmi.registry.p2p.peer.P2pRmiPeer;

/**
 * @author zink
 *
 */
public class VuzePeerClient {	
	/**
	 * @param args
	 * @throws DHTTransportException 
	 * @throws NotBoundException 
	 * @throws RemoteException 
	 * @throws AccessException 
	 */
	public static void main(String[] args) throws DHTTransportException, AccessException, RemoteException, NotBoundException {
		System.setProperty("java.security.policy",
				VuzePeerClient.class.getClassLoader().getResource("client.policy").toExternalForm());

		long start, duration;
		
		// create the peer with DHT binding
		P2pRmiPeer peer = P2pRmiPeer.newInstance(new UceDhtVuzeAdapter(0));
		
		// lookup the stub and execute command
		String name = "HelloRemoteVuze";
		start = System.currentTimeMillis();
		IHelloRemote stub = (IHelloRemote) peer.registry().lookup(name);
		duration = System.currentTimeMillis() - start;
		System.out.printf("Received stub after %5d ms\n%s", duration, stub.message());
	}
}
