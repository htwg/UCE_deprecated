/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.registry.p2p.demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.jmule.core.JMuleCoreException;
import org.jmule.core.configmanager.ConfigurationManagerException;

import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.common.HelloRemote.HelloRemote;
import de.htwg_konstanz.in.uce.common.HelloRemote.IHelloRemote;
import de.htwg_konstanz.in.uce.rmi.hp.socket_factrories.HolePunchingRemoteObject;
import de.htwg_konstanz.in.uce.rmi.registry.p2p.peer.P2pRmiPeer;

/**
 * @author zink
 *
 */
public class PeerServer {
	
	public static void main(String[] args) throws JMuleCoreException, ConfigurationManagerException, DHTTransportException, RemoteException, NotBoundException, InterruptedException {
		PeerCmd cmd = PeerCmd.parseArgs(args);
		FileWriter fstream = null;
		try {
			fstream = new FileWriter(cmd.file,true);
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(fstream);
		} catch (NullPointerException e) {
			System.err.println(e.getMessage());
		}
		
		// performance testing
		long start, duration;

		// create remote instance
		String name = "HelloRemote" + cmd.type;
		//HelloRemote instance = new HelloRemote("Hello P2P RMI using " + cmd.type + "!");

		if (cmd.n == 1) {
			// just serve
			HelloRemote instance = new HelloRemote("Hello P2P RMI using " + cmd.type + "!");

			// create and bootstrap peer
			start = System.currentTimeMillis();
			P2pRmiPeer peer = P2pRmiPeer.newInstance(cmd.adapter);
			duration = System.currentTimeMillis() - start;
			System.out.printf("[PeerServer.main] bootstrap duration %6d ms\n", duration);
			PeerCmd.writeToFile(out, "bootstrap " + duration + "\n");
			
			// add service
			start = System.currentTimeMillis();
			IHelloRemote stub = peer.server().addService(name, instance, cmd.mediator);
			duration = System.currentTimeMillis() - start;
			System.out.println("[PeerServer.main] addService duration " + duration + " ms\n" + stub);
			
			// serve
			Object lock = new Object();
	        synchronized (lock) {
	            lock.wait();
	        }
		} else {
			// do the simulation loop
			for (int i=0; i<=cmd.n-1; i++) {
				// try with different names for each cycle
				name = name + i;
				// create and bootstrap the peer
				start = System.currentTimeMillis();
				P2pRmiPeer peer = P2pRmiPeer.newInstance(cmd.adapter);
				duration = System.currentTimeMillis() - start;
				System.out.printf("[PeerServer.main][%d / %d] bootstrap duration %6d ms\n", (i+1), cmd.n, duration);
				PeerCmd.writeToFile(out, "bootstrap " + duration + "\n");
				
				// register object
				HelloRemote instance = new HelloRemote("Hello P2P RMI using " + cmd.type + "!");
				start = System.currentTimeMillis();
				IHelloRemote stub = (IHelloRemote) HolePunchingRemoteObject.exportObject(instance, 0, cmd.mediator);
				duration = System.currentTimeMillis() - start;
				System.out.printf("[PeerServer.main][%d / %d] export duration %6d ms\n%s\n", (i+1), cmd.n, duration, stub);
				PeerCmd.writeToFile(out, "export " + duration + "\n");
				
				// (re)bind
				start = System.currentTimeMillis();
				peer.registry().rebind(name, stub);
				duration = System.currentTimeMillis() - start;
				System.out.printf("[PeerServer.main][%d / %d] rebind duration %6d ms\n", (i+1), cmd.n, duration);
				PeerCmd.writeToFile(out, "rebind " + duration + "\n");
				
				// unbind
				start = System.currentTimeMillis();
				peer.registry().unbind(name);
				duration = System.currentTimeMillis() - start;
				System.out.printf("[PeerServer.main][%d / %d] unbind duration %5d ms\n", (i+1), cmd.n, duration);
				PeerCmd.writeToFile(out, "unbind " + duration + "\n");
				
				// destroy
				peer.dht().destroy();
			}
		}

		if (out != null) {
			try {
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
