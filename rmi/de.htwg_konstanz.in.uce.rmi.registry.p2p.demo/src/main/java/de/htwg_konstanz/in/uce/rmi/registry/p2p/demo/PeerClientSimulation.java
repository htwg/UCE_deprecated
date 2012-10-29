/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.registry.p2p.demo;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.jmule.core.JMuleCoreException;
import org.jmule.core.configmanager.ConfigurationManagerException;

import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.common.HelloRemote.IHelloRemote;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtKadAdapter;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtVuzeAdapter;
import de.htwg_konstanz.in.uce.rmi.registry.p2p.peer.P2pRmiPeer;

/**
 * @author zink
 */
public class PeerClientSimulation {
	 
	private VuzeThread vuze;
	private KadThread kad;
	
	private abstract class DhtThread extends Thread {
		private P2pRmiPeer peer;
		private ArrayList<Long> times = new ArrayList<Long>();
		private int n;
		private String key;
		
		public DhtThread(int niterations, final P2pRmiPeer peer, final String key) {
			this.n = niterations;
			this.peer = peer;
			this.key = key;
		}
		
		public void run() {
			int i = 0;
			long start, duration;
			while(i++ < n) {
				start = System.currentTimeMillis();
				IHelloRemote stub;
				try {
					stub = (IHelloRemote) peer.registry().lookup(key);
					duration = System.currentTimeMillis() - start;
					times.add(duration);
					System.out.printf("Received stub after %5d ms\n%s", duration, stub.message());
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				}
			}
			System.out.printf("%s exiting. bye.", this.getClass().toString());
		}
	}
	
	private class VuzeThread extends DhtThread {
		public VuzeThread(final int niterations) throws RemoteException, DHTTransportException {
			super(niterations, P2pRmiPeer.newInstance(new UceDhtVuzeAdapter(0)), "HelloRemoteVuze");
		}
	}
	
	private class KadThread extends DhtThread {
		public KadThread(final int niterations) throws RemoteException, JMuleCoreException, ConfigurationManagerException {
			super(niterations, P2pRmiPeer.newInstance(new UceDhtKadAdapter(4684)), "HelloRemoteKad");
		}
	}
	
	public static void main(String[] args) throws RemoteException, DHTTransportException, JMuleCoreException, ConfigurationManagerException {
		System.setProperty("java.security.policy",
				VuzePeerClient.class.getClassLoader().getResource("client.policy").toExternalForm());
		
		int n = Integer.parseInt(args[0]);
		PeerClientSimulation sim = new PeerClientSimulation();
		sim.vuze = sim.new VuzeThread(n);
		sim.kad = sim.new KadThread(n);
		
		sim.vuze.start();
		sim.kad.start();
	}
}
