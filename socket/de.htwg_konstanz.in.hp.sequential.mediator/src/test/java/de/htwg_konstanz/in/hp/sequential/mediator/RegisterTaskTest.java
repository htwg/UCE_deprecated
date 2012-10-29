package de.htwg_konstanz.in.hp.sequential.mediator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.hp.sequential.message.RegisterMessage;
import de.htwg_konstanz.in.hp.sequential.message.coder.MessageEncoder;

public class RegisterTaskTest {
	
	@Before
	public void setUp() {
		Repository.INSTANCE.reset();
	}

    @Test
    public void testRegisterTask() throws IOException {
        String id = "registerID";
        MessageEncoder encoder = new MessageEncoder();
        RegisterMessage m = new RegisterMessage(id);
        byte[] buf = encoder.encodeMessage(m);
        DatagramPacket p = new DatagramPacket(buf, buf.length);
        p.setPort(1234);
        p.setAddress(InetAddress.getLocalHost());
        RegisterTask t = new RegisterTask(p);
        t.run();
        Assert.assertNotNull(Repository.INSTANCE.getTargetRegisterEndpoint(id));
        Assert.assertEquals(p.getSocketAddress(), Repository.INSTANCE.getTargetRegisterEndpoint(id));
    }
}
