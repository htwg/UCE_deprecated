package de.htwg_konstanz.in.hp.sequential.mediator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.hp.sequential.message.RegisterMessage;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageEncoder;

public class RegistrationListenerThreadTest {
    
    private final static int LISTENER_PORT = 1234;
    
	@Before
	public void setUp() {
		Repository.INSTANCE.reset();
	}

    @Test
    public void testInterrupt() throws SocketException, InterruptedException {
        DatagramSocket registerSocket = new DatagramSocket();
        RegistrationListenerThread t = new RegistrationListenerThread(registerSocket);
        t.start();
        Thread.sleep(500);
        t.interrupt();
        t.join(10);
        Assert.assertFalse(t.isAlive());
    }
    
    @Test
    public void testRun() throws InterruptedException, IOException {
    	DatagramSocket registerSocket = null;
    	try {
    	   String id = "testID";
    	   registerSocket = new DatagramSocket(new InetSocketAddress("localhost", LISTENER_PORT));
    	   RegistrationListenerThread t = new RegistrationListenerThread(registerSocket);
    	   t.start();
    	   MessageEncoder encoder = new MessageEncoder();
    	   RegisterMessage m = new RegisterMessage(id);
    	   byte[] buf = encoder.encodeMessage(m);
    	   DatagramPacket p = new DatagramPacket(buf, buf.length);
    	   DatagramSocket s = new DatagramSocket();
    	   p.setSocketAddress(new InetSocketAddress("localhost", LISTENER_PORT));
    	   s.send(p);
    	   Thread.sleep(500);
    	   s.close();
    	   Assert.assertNotNull(Repository.INSTANCE.getTargetRegisterEndpoint(id));
    	   t.interrupt();   
       } finally {
    	   if(registerSocket != null) {
    		   registerSocket.close();    		   
    	   }
       }
        
    }
}
