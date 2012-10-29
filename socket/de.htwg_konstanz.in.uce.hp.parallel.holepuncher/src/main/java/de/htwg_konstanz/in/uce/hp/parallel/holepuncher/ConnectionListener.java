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
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to listen for incoming connections. Listener can be stopped and restarted.
 * To get a established connection you have to register for a specific remote endpoint.
 * @author Daniel Maier
 *
 */
public final class ConnectionListener {

    private static Logger logger = LoggerFactory.getLogger(ConnectionListener.class);
    private final Map<SocketAddress, BlockingQueue<Socket>> exchangers;
    private final InetAddress bindingAddress;
    private final int bindingPort;
    private final ExecutorService executor;
    private boolean running = false;
    private boolean shutdown = false;
    private ConnectionListenerTask currentTask;

   /**
    * Task that waits for incoming connections until it gets stopped.
    * If there is a registration for the established connection the corresponding socket
    * will be put in the exchanger object.
    * @author Daniel Maier
    *
    */
    private class ConnectionListenerTask implements Runnable {
        
        private final ServerSocket ss;
        private volatile boolean stop = false;

        private ConnectionListenerTask(ServerSocket ss) {
            this.ss = ss;
        }
        
        private void stop() {
            try {
                stop = true;
                ss.close();
            } catch (IOException ignore) {
                logger.error("IOException while close socket");
            }           
        }

        /**
         * Listens for incoming connections until it gets stopped. If there is a registration for 
         * the established connection the corresponding socket will be put in the exchanger object.
         */
    	public void run() {
            try {
                while (!stop) {
                    // important to bind in loop, so it gets tried again while
                    // the client starts connecting
                    try {
                        logger.info("Try to bind to: {}:{}", bindingAddress, bindingPort);
                        ss.bind(new InetSocketAddress(bindingAddress, bindingPort));
                    } catch (IOException ignore) {
                        logger.info("IOException while bind: {}", ignore.getMessage());
                        // only continue on bind exception and not when socket
                        // is closed
                        if (ignore instanceof BindException) {
                            logger.info("Try again ...");
                            continue;
                        }
                    }
                    logger.info("Bound successful");
                    while (!stop) {
                        logger.info("Waiting for accept..");
                        Socket s = ss.accept();
                        logger.info("Accepted socket: {}", s);
                        BlockingQueue<Socket> exchanger = exchangers
                                .get(s.getRemoteSocketAddress());
                        if (exchanger != null) {
                            exchanger.add(s);
                        } else {
                            logger.error("No one registered for socket: {}", s);
                            try {
                                s.close();
                            } catch (IOException ignore) {
                            }
                        }
                    }
                }
            } catch (IOException e) {
                logger.info("IOException in ConnectionListenerTask: {}. Terminating Task.", 
                        e.getMessage());
            }

        }

    }

    /**
     * Creates a new ConnectionListener.
     * @param bindingAddress local address of the listener
     * @param bindingPort local port of the listener
     */
    public ConnectionListener(InetAddress bindingAddress, int bindingPort) {
        this.bindingAddress = bindingAddress;
        this.bindingPort = bindingPort;
        exchangers = new Hashtable<SocketAddress, BlockingQueue<Socket>>();
        this.executor = Executors.newSingleThreadExecutor(new ThreadGroupThreadFactory());
    }

    /**
     * Register for a specific established connection. If the connection gets established
     * the corresponding socket will be put in the given exchanger object.
     * @param originator the endpoint of the desired connection
     * @param exchanger exchanger object in that the connected socket will be put
     */
    public synchronized void registerForOriginator(SocketAddress originator,
            BlockingQueue<Socket> exchanger) {
        logger.info("New registration for: {}", originator);
        exchangers.put(originator, exchanger);
    }
    
    /**
     * Remove registration for a specific originator. Assumes a registration of the desired
     * originator via the {@link de.htwg_konstanz.in.uce.hp.parallel.holepuncher.ConnectionListener#
     * registerForOriginator(SocketAddress, BlockingQueue) registerForOriginator} method before. 
     * If no registration is present this method does nothing. 
     * @param originator the endpoint of the originator its registration should be removed
     */
    public synchronized void deregisterForOriginator(SocketAddress originator) {
        logger.info("Deregistration for: {}", originator);
        exchangers.remove(originator);
    }

    /**
     * Stops the ConnectionListener. Strictly speaking it closes the underlying server socket.
     */
    public synchronized void stop() {
        logger.info("Stop ConnectionListener...");
        if(running) {
            currentTask.stop();
            running = false;
            logger.info("Stop completed");     
        } else {
            logger.info("Stop completed (was not started)");
        }
    }

    /**
     * Starts the ConnectionListener. It can be started as often as you want.
     * @throws IOException if the required server socket could not be created.
     * @throws IllegalStateException if ConnectionListener is shutdown.
     */
    public synchronized void start() throws IOException, IllegalStateException {
        logger.info("Start...");
        if(shutdown) {
            throw new IllegalStateException("ConnectionListener is shutdown");
        }
        if (!running) {
            ServerSocket ss = new ServerSocket();
            ss.setReuseAddress(true);
            ConnectionListenerTask task = new ConnectionListenerTask(ss);
            executor.execute(task);
            currentTask = task;
            running = true;
            logger.info("Started.");
        }
    }
    
    /**
     * Attempts to stop the needed worker thread.
     * @throws IllegalStateException if the ConnectionListener was already shutdown.
     */
    public synchronized void shutdown() throws IllegalStateException {
        logger.info("Shutdown...");
        if(shutdown) {
            throw new IllegalStateException("ConnectionListener was already shutdown");
        }
        executor.shutdownNow();
        if(currentTask != null) {
            currentTask.stop();            
        }
        shutdown = true;
    }
}
