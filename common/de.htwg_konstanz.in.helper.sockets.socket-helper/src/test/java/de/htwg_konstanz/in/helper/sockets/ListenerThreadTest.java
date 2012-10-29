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

package de.htwg_konstanz.in.helper.sockets;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;

import javax.net.ServerSocketFactory;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import de.htwg_konstanz.in.test.helper.concurrency.MultiThreadedExceptionsRunner;

@RunWith(value = MultiThreadedExceptionsRunner.class)
public class ListenerThreadTest {

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArgs() throws IOException {
        new ListenerThread(0, ServerSocketFactory.getDefault(), null, new ListenerTaskFactory() {
            public Runnable getTask(Socket s) {
                return null;
            }
        });
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArgs1() throws IOException {
        new ListenerThread(0, ServerSocketFactory.getDefault(), Executors.newCachedThreadPool(),
                null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArgs2() throws IOException {
        new ListenerThread(0, ServerSocketFactory.getDefault(), null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullArgs3() throws IOException {
        new ListenerThread(0, null, null, null);
    }

    @Test
    public void testRun() throws IOException, InterruptedException {
        // try to get get free port
        ServerSocket dummy = new ServerSocket(0);
        int listenerPort = dummy.getLocalPort();
        dummy.close();
        ListenerTaskFactory tf = mock(ListenerTaskFactory.class);
        Runnable task = mock(Runnable.class);
        when(tf.getTask(isA(Socket.class))).thenReturn(task);
        ListenerThread lt = new ListenerThread(listenerPort, ServerSocketFactory.getDefault(),
                Executors.newCachedThreadPool(), tf);
        lt.start();

        Socket s = new Socket();
        s.connect(new InetSocketAddress(listenerPort));
        Thread.sleep(200);
        lt.interrupt();
        lt.join();
        verify(task).run();
    }

    @Test
    public void testInterrupt() throws IOException, InterruptedException {
        // try to get get free port
        ServerSocket dummy = new ServerSocket(0);
        int listenerPort = dummy.getLocalPort();
        dummy.close();
        ListenerThread lt = new ListenerThread(listenerPort, ServerSocketFactory.getDefault(),
                Executors.newCachedThreadPool(), new ListenerTaskFactory() {
                    public Runnable getTask(Socket s) {
                        return null;
                    }
                });
        lt.start();
        Thread.sleep(200);
        lt.interrupt();
        lt.join();

        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(listenerPort));
            Assert.fail("Got connection, but listener should be terminated");
        } catch (IOException e) {
            // thread is terminated.
        }
    }
}
