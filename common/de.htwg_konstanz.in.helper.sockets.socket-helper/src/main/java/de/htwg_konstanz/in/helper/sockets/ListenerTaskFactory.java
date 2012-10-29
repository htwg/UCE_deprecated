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

import java.io.IOException;
import java.net.Socket;

/**
 * A {@link ListenerTaskFactory} gets handed over to a {@link ListenerThread}.
 * The {@link ListenerThread} uses the factory to create tasks to handle new
 * accepted socket connections.
 * 
 * @author Daniel Maier
 * 
 */
public interface ListenerTaskFactory {
    /**
     * Returns a task to handle the new connection.
     * 
     * @param s
     *            the socket of the new connection
     * @return a task to handle the new connection
     * @throws IOException
     *             if an I/O error occurs
     */
    Runnable getTask(Socket s) throws IOException;
}
