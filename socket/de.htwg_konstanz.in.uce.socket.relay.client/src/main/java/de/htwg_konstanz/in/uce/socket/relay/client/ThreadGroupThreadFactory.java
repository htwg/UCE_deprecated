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

package de.htwg_konstanz.in.uce.socket.relay.client;

import java.util.concurrent.ThreadFactory;

/**
 * Implementation of {@link ThreadFactory} that creates daemon threads.
 * Additionally all created threads belong to the same thread group. The used
 * thread group is the one to that the thread belongs that used this class for
 * the first time.
 * 
 * @author Daniel Maier
 * 
 */
public class ThreadGroupThreadFactory implements ThreadFactory {

    private static final ThreadGroup userThreadGroup = Thread.currentThread().getThreadGroup();

    public Thread newThread(Runnable r) {
        Thread thread = new Thread(userThreadGroup, r);
        thread.setDaemon(true);
        return thread;
    }

}
