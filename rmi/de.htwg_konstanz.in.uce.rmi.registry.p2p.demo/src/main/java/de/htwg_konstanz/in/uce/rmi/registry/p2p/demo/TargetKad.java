package de.htwg_konstanz.in.uce.rmi.registry.p2p.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.jmule.core.JMuleCoreException;
import org.jmule.core.configmanager.ConfigurationManagerException;

import de.htwg_konstanz.in.uce.common.HelloRemote.HelloRemote;
import de.htwg_konstanz.in.uce.common.HelloRemote.IHelloRemote;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDht;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtKadAdapter;
import de.htwg_konstanz.in.uce.rmi.hp.socket_factrories.HolePunchingRemoteObject;

public class TargetKad {
	
	private static String DEFAULT_MEDIATOR_IP = "141.37.121.124"; // "134.34.165.172"
	private static int DEFAULT_MEDIATOR_PORT = 10200; // 9090
	
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
     * @param args
     * @throws JMuleCoreException 
     * @throws IOException 
     * @throws InterruptedException 
     * @throws ConfigurationManagerException 
     */
    public static void main(String[] args) throws JMuleCoreException, IOException, InterruptedException, ConfigurationManagerException {
    	
    	/*
    	InetSocketAddress mediator = parseArgs(args);		
		long start, duration;
		// create the peer
		P2pRmiPeer peer = P2pRmiPeer.newInstance(new UceDhtKadAdapter(4683));
		
		// create remote stuff
		String name = "HelloRemoteKad";
		HelloRemote instance = new HelloRemote("Hello P2P RMI using Kad!");
		start = System.currentTimeMillis();
		IHelloRemote stub = peer.server().addService(name, instance, mediator);
		duration = System.currentTimeMillis() - start; 
				
        System.out.println("Serving stub after " + duration + " ms\n" + stub);
    	*/
    	
    	System.setProperty(
                "java.rmi.server.codebase",
                "http://ice.in.htwg-konstanz.de/downloads/demo/hello.jar "
                        + " http://ice.in.htwg-konstanz.de/downloads/socket_factories-0.1-jar-with-dependencies.jar");
                        
        HelloRemote impl = new HelloRemote();
        IHelloRemote server = (IHelloRemote) HolePunchingRemoteObject.exportObject(impl, 0,
                new InetSocketAddress("141.37.121.124", 10200));
        
        UceDht dht = new UceDhtKadAdapter(4683);
        long bootstrapStart = System.currentTimeMillis();
        dht.bootstrap();
        System.out.println("bootstrap duration: " + (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - bootstrapStart)));
        System.out.println("bootstraped");
        String key = "de.htwg_konstanz.in.uce.rmi.hp.demo.Hello2";
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(server);
        long putStart = System.currentTimeMillis();
        System.out.println(dht.put(key, bout.toByteArray()));
        System.out.println("put duration: " + (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - putStart)));
        Object lock = new Object();
        synchronized (lock) {
            lock.wait();
        }
        
    }

}
