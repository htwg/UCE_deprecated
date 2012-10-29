package de.htwg_konstanz.in.hp.sequential.mediator;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.NoSuchElementException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class RepositoryTest {
	
	@Before
	public void setUp() {
		Repository.INSTANCE.reset();
	}

    @Test
    public void testInsertIDAndGetEndpoint() {
        String id = "testID";
        SocketAddress remoteAddress = new InetSocketAddress(1234);
        Repository.INSTANCE.insertOrUpdateID(id, remoteAddress);
        SocketAddress got = Repository.INSTANCE.getTargetRegisterEndpoint(id);
        Assert.assertEquals(remoteAddress, got);
        try {
        	Repository.INSTANCE.getTargetRegisterEndpoint("testID2");   
        	Assert.fail("Expected NoSuchElementException");
        } catch(NoSuchElementException e) {
        	
        }
    }
    
    @Test(expected = NoSuchElementException.class)
    public void testGetEndpointWithUnkownID() {
        Repository.INSTANCE.getTargetRegisterEndpoint("testID");
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSocketIllegalState() {
        Repository.INSTANCE.getRegisterSocket();
    }
    
    @Test
    public void testSetAndGetRegisterSocket() throws SocketException {
        DatagramSocket registerSocket = new DatagramSocket();
        Repository.INSTANCE.setRegisterSocket(registerSocket);
        DatagramSocket got = Repository.INSTANCE.getRegisterSocket();
        Assert.assertEquals(registerSocket, got);
    }
    
}
