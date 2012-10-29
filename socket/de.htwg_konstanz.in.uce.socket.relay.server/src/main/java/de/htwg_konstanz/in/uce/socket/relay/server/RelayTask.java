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

package de.htwg_konstanz.in.uce.socket.relay.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that relays data between one client and one peers. One instance of this task transports
 * data in one direction. Another instance is needed to handle the other
 * direction.
 * 
 * @author Daniel Maier
 * 
 */
public class RelayTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RelayTask.class);
    private static final int BUFFER_SIZE = 768;
    private final Socket in;
    private final Socket out;

    /**
     * Creates a new {@link RelayTask}.
     * 
     * @param in
     *            the socket from that the data gets read from
     * @param out
     *            the socket to that the data gets written to
     */
    public RelayTask(Socket in, Socket out) {
        this.in = in;
        this.out = out;
    }

    /**
     * Relays data from in one direction from one client to one peer.
     * 
     */
    public void run() {
        BufferedInputStream bufferedIn;
        BufferedOutputStream bufferedOut;
        try {
            bufferedIn = new BufferedInputStream(in.getInputStream());
            bufferedOut = new BufferedOutputStream(out.getOutputStream());
        } catch (IOException e) {
            logger.error("IOException while getting streams for relaying relaying: {}", e);
            return;
        }

        byte[] buf = new byte[BUFFER_SIZE];
        int len = 0;

        try {
            while ((len = bufferedIn.read(buf, 0, BUFFER_SIZE)) > -1) {
                bufferedOut.write(buf, 0, len);
                bufferedOut.flush();
            }
            in.shutdownInput();
            out.shutdownOutput();
        } catch (IOException e) {
            logger.error("IOException while relaying: {}", e);
        }
    }
}
