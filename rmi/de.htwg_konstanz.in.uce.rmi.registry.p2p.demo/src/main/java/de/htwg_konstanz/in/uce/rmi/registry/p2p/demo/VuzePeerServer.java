package de.htwg_konstanz.in.uce.rmi.registry.p2p.demo;

import java.net.InetSocketAddress;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.common.HelloRemote.HelloRemote;
import de.htwg_konstanz.in.uce.common.HelloRemote.IHelloRemote;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtVuzeAdapter;
import de.htwg_konstanz.in.uce.rmi.registry.p2p.peer.P2pRmiPeer;

public class VuzePeerServer {
	private static String DEFAULT_MEDIATOR_IP = "141.37.121.124"; // "134.34.165.172"
	private static int DEFAULT_MEDIATOR_PORT = 10200; // 9090
	
	/**
	 * 
	 * @param args
	 * @return the mediator address
	 */
	private static InetSocketAddress parseArgs (String[] args) {
		String mediator_ip = DEFAULT_MEDIATOR_IP;
		int mediator_port = DEFAULT_MEDIATOR_PORT;
		if (args.length > 0 && args.length < 2) {
			System.err.print("Usage: VuzePeerServer [<mediator ip> <mediator port>]");
		} else if (args.length == 3) {
			mediator_ip = args[0];
			mediator_port = Integer.parseInt(args[1]);
		}
		return new InetSocketAddress(mediator_ip, mediator_port);
	}
	
	/**
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws RemoteException
	 * @throws DHTTransportException
	 * @throws NotBoundException
	 */
	public static void main (String[] args) throws InterruptedException, RemoteException, DHTTransportException, NotBoundException {
		// we do not really need this, only if we do not have all the definitions.
		/*System.setProperty(
				"java.rmi.server.codebase",
                "http://ice.in.htwg-konstanz.de/downloads/demo/hello.jar "
                + " http://ice.in.htwg-konstanz.de/downloads/socket_factories-0.1-jar-with-dependencies.jar");*/
		
		// check args and set mediator address
		InetSocketAddress mediator = parseArgs(args);		
		long start, duration;

		// create the peer
		P2pRmiPeer peer = P2pRmiPeer.newInstance(new UceDhtVuzeAdapter(0));
		
		// create remote stuff
		String name = "HelloRemoteVuze";
		HelloRemote instance = new HelloRemote("Hello P2P RMI using Vuze!");
		start = System.currentTimeMillis();
		IHelloRemote stub = peer.server().addService(name, instance, mediator);
		duration = System.currentTimeMillis() - start; 
				
        System.out.println("Serving stub after " + duration + " ms\n" + stub);
        // is waiting, service thread running
	}
}
