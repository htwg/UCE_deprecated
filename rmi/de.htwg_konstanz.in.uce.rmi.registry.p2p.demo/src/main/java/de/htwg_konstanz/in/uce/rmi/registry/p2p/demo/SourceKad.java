package de.htwg_konstanz.in.uce.rmi.registry.p2p.demo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.rmi.NotBoundException;
import java.util.concurrent.TimeUnit;

import org.jmule.core.JMuleCoreException;
import org.jmule.core.configmanager.ConfigurationManagerException;

import de.htwg_konstanz.in.uce.common.HelloRemote.IHelloRemote;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDht;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtKadAdapter;

public class SourceKad {

    /**
     * @param args
     * @throws IOException 
     * @throws ClassNotFoundException 
     * @throws JMuleCoreException 
     * @throws InterruptedException 
     * @throws ConfigurationManagerException 
     * @throws NotBoundException 
     */
    public static void main(String[] args) throws IOException, ClassNotFoundException, JMuleCoreException, InterruptedException, ConfigurationManagerException, NotBoundException {
        System.setProperty("java.security.policy",
                SourceKad.class.getClassLoader().getResource("client.policy").toExternalForm());
        System.setSecurityManager(new SecurityManager());
        
        // create peer
        /*long start = System.currentTimeMillis();
        P2pRmiPeer peer = P2pRmiPeer.newInstance(new UceDhtKadAdapter(4684));
        System.out.println("Bootstrapped after " + (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start)));
        
        String name = "de.htwg_konstanz.in.uce.rmi.hp.demo.Hello2";
        
        // get stub and print message
        start = System.currentTimeMillis();
        IHelloRemote stub = (IHelloRemote) peer.registry().lookup(name);
		System.out.printf("Received stub after %5d ms\n%s", TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start), stub.message());
		*/
        
        String key = "de.htwg_konstanz.in.uce.rmi.hp.demo.Hello2";
		UceDht dht = new UceDhtKadAdapter(4684);
		long bootstrapStart = System.currentTimeMillis();
        dht.bootstrap();
        long getStart = System.currentTimeMillis();
        System.out.println("bootstrapped after: " + (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - bootstrapStart)));
        byte[] returnValue = dht.get(key);
        System.out.println("get duration: " + (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - getStart)));
        System.out.println(returnValue.length);
        ByteArrayInputStream bin = new ByteArrayInputStream(returnValue);
        ObjectInputStream oin = new ObjectInputStream(bin);
        //Hello server = (Hello) oin.readObject();
        IHelloRemote server = (IHelloRemote) oin.readObject();
        System.out.println(server.message());

    }

}
