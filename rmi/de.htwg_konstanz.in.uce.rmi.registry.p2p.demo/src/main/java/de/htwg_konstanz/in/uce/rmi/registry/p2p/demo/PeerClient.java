/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.registry.p2p.demo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.jmule.core.JMuleCoreException;
import org.jmule.core.configmanager.ConfigurationManagerException;

import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.common.HelloRemote.IHelloRemote;
import de.htwg_konstanz.in.uce.rmi.registry.p2p.peer.P2pRmiPeer;

/**
 * @author zink
 *
 */
public class PeerClient {
	public static void main(String[] args) throws DHTTransportException, AccessException, RemoteException, NotBoundException, JMuleCoreException, ConfigurationManagerException {
		System.setProperty("java.security.policy",
				VuzePeerClient.class.getClassLoader().getResource("client.policy").toExternalForm());
		
		PeerCmd cmd = PeerCmd.parseArgs(args);
		
		// output file (this could go to cmd parser)
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
		
		// stopwatch
		long start, duration;
		
		// create remote stuff
		String name = "HelloRemote" + cmd.type;
		
		for (int i=0; i<=cmd.n-1; i++) {
			// create the peer
			start = System.currentTimeMillis();
			P2pRmiPeer peer = P2pRmiPeer.newInstance(cmd.adapter);
			duration = System.currentTimeMillis() - start;
			System.out.printf("[PeerClient.main] bootstrap duration %6d ms\n", duration);
			long bootstrap = duration;
			//PeerCmd.writeToFile(out, "bootstrap " + duration + "\n");
			
			// lookup
			start = System.currentTimeMillis();
			IHelloRemote stub = (IHelloRemote) peer.registry().lookup(name);
			duration = System.currentTimeMillis() - start;
			System.out.printf("[PeerClient.main][%d / %d] lookup duration %6d ms\n", (i+1), cmd.n, duration);
			long lookup = duration;
			//PeerCmd.writeToFile(out, "lookup " + duration + "\n");
			
			// invoke
			boolean success = false;
			boolean fail = false;
			start = System.currentTimeMillis();
			while (!success && !fail) {
				try {
					System.out.println(stub.message());
					success = true;
				} catch (NullPointerException npx) {
					System.err.println("Exception caught at method invokation:");
					npx.printStackTrace();
					fail = true;
				} catch (Exception e) {
					System.err.println("Exception caught at method invokation:");
					e.printStackTrace();
				}
			}
			duration = System.currentTimeMillis() - start;
			System.out.printf("[PeerClient.main][%d / %d] invoke duration %6d ms\n", (i+1), cmd.n, duration);
			long invoke = duration;
			
			PeerCmd.writeToFile(out, "bootstrap " + bootstrap + "\n");
			PeerCmd.writeToFile(out, "lookup " + lookup + "\n");
			
			if (success) {
				PeerCmd.writeToFile(out, "invoke " + invoke + "\n");
			} else if (fail) {
				PeerCmd.writeToFile(out, "invoke NAN\n");
			}
			
			/*
			try {
				start = System.currentTimeMillis();
				System.out.println(stub.message());
				duration = System.currentTimeMillis() - start;
				System.out.printf("[PeerClient.main][%d / %d] invoke duration %6d ms\n", (i+1), cmd.n, duration);
				long invoke = duration;
				
				// write to file if everything's right
				PeerCmd.writeToFile(out, "bootstrap " + bootstrap + "\n");
				PeerCmd.writeToFile(out, "lookup " + lookup + "\n");
				PeerCmd.writeToFile(out, "invoke " + invoke + "\n");
			} catch (Exception e) {
				e.printStackTrace();
			}*/
			
			// destroy
			peer.dht().destroy();
		}
		
		System.out.println("BYE");
	}

}
