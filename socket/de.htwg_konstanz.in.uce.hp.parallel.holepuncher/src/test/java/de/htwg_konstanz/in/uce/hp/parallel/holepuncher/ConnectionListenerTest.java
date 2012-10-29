/**
 * Copyright (C) 2011 Daniel Maier
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.htwg_konstanz.in.uce.hp.parallel.holepuncher;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.htwg_konstanz.in.test.helper.concurrency.MultiThreadedExceptionsRunner;
import de.htwg_konstanz.in.uce.hp.parallel.holepuncher.ConnectionListener;

@RunWith(MultiThreadedExceptionsRunner.class)
public class ConnectionListenerTest {

    private int listenerPort;
    
    
    @Before
    public void setUp() throws IOException {
        ServerSocket dummy = new ServerSocket(0);
        listenerPort = dummy.getLocalPort();
        dummy.close();
    }

    /**
     * Tests only if ServerSocket is closed and not if worker thread is
     * terminated.
     * 
     * @throws IOException
     * @throws UnknownHostException
     * @throws InterruptedException
     */
    private void testStartStopMultiThreaded() throws UnknownHostException, IOException,
            InterruptedException {
        final ConnectionListener cl = new ConnectionListener(InetAddress.getLocalHost(),
                listenerPort);
        Socket connector = new Socket();
        final Object monitor = new Object();
        Thread starter = new Thread() {
            public void run() {
                try {
                    cl.start();
                    synchronized (monitor) {
                        monitor.notify();                        
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        };
        starter.start();
        synchronized (monitor) {
            monitor.wait(100);            
        }
        cl.stop();
        try {
            connector.connect(new InetSocketAddress(listenerPort));
            Assert.fail("Got Connection, but ConnectionListener was stopped.");
        } catch (Exception e) {
            //ignore
        } finally {
            cl.shutdown();            
        }
    }

    @Test
    public void testStartStopMultiThreadedRepeated() throws UnknownHostException, IOException,
            InterruptedException {
        for (int i = 0; i < 1000; i++) {
            try {
                testStartStopMultiThreaded();                
            } catch(AssertionFailedError e) {
                Assert.fail("Failed in Run #" + i + " : " + e.getMessage());
            }
        }
        Thread.sleep(100);
        int threads = Thread.currentThread().getThreadGroup().activeCount();
        Assert.assertTrue("Expected all threads terminated, but count is: " + threads, 
                 threads == 1);
    }
    
    @Test
    public void testRegister() throws IOException, InterruptedException {
        ConnectionListener cl = new ConnectionListener(InetAddress.getLocalHost(),
                listenerPort);
        cl.start();
        Socket connector = new Socket();
        BlockingQueue<Socket> queue = new LinkedBlockingQueue<Socket>();
        ServerSocket dummy = new ServerSocket(0);
        int port = dummy.getLocalPort();
        dummy.close();
        InetSocketAddress binding = new InetSocketAddress(InetAddress.getLocalHost(), port);
        connector.bind(binding);
        cl.registerForOriginator(binding, queue);
        Assert.assertTrue(queue.isEmpty());
        connector.connect(new InetSocketAddress(InetAddress.getLocalHost(), listenerPort));
        Thread.sleep(200);
        Assert.assertFalse(queue.isEmpty());
        Socket accepted = queue.take();
        Assert.assertEquals(binding, accepted.getRemoteSocketAddress());
        accepted.close();
        connector.close();
        Assert.assertTrue(queue.isEmpty());
        cl.stop();
        cl.shutdown();
    }
    
    /**
     * In old implementation this test leads to a socket is closed exception
     * in start() method.
     * @throws IOException
     */
    @Test
    public void multipleFastStartStop() throws IOException {
        ConnectionListener cl = new ConnectionListener(InetAddress.getLocalHost(),
                listenerPort);
        for (int i = 0; i < 10000; i++) {
            try {
                cl.start();                
            } catch(IOException e) {
                Assert.fail("Failed in Run #" + i + " : " + e.getMessage());
            }
            cl.stop();
        }
        cl.shutdown();
    }
    
    @Test( expected = IllegalStateException.class )
    public void testShutdownStart() throws IllegalStateException, IOException {
        ConnectionListener cl = new ConnectionListener(InetAddress.getLocalHost(),
                listenerPort);
        cl.start();
        cl.shutdown();
        cl.start();  
    }
    
    @Test( expected = IllegalStateException.class )
    public void testShutdownShutdown() throws IllegalStateException, IOException {
        ConnectionListener cl = new ConnectionListener(InetAddress.getLocalHost(),
                listenerPort);
        cl.shutdown(); 
        cl.shutdown();
    }
    
    @Test
    public void testUnnormalButPossibleStates() throws IllegalStateException, IOException {
        ConnectionListener cl = new ConnectionListener(InetAddress.getLocalHost(),
                listenerPort);
        cl.start();
        cl.stop();
        cl.stop();
        cl.start();
        cl.start();
        cl.shutdown();
        cl.stop();
    }
}
