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

package de.htwg_konstanz.in.uce.hp.parallel.mediator_browser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Set;

import javax.net.SocketFactory;

import de.htwg_konstanz.in.uce.hp.parallel.messages.ExceptionMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListRequestMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.ListResponseMessage;
import de.htwg_konstanz.in.uce.hp.parallel.messages.Message;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageDecoder;
import de.htwg_konstanz.in.uce.hp.parallel.messages.coder.MessageEncoder;

/**
 * Class to retrieve list of all registered targets of a mediator.
 * 
 * @author Daniel Maier
 * 
 */
public final class MediatorBrowser {

    private final SocketAddress mediatorAddress;
    private SocketFactory sf;

    /**
     * Creates a new {@link MediatorBrowser} with the given mediatorAddress.
     * 
     * @param mediatorAddress
     */
    public MediatorBrowser(SocketAddress mediatorAddress) {
        if (mediatorAddress == null) {
            throw new NullPointerException("mediatorAddress must not be null");
        }
        this.mediatorAddress = mediatorAddress;
        sf = SocketFactory.getDefault();
    }

    /**
     * Creates a new {@link MediatorBrowser} with the given IP address and port
     * of a mediator.
     * 
     * @param ip
     *            the IP address of the desired mediator
     * @param port
     *            the port of the desired mediator
     * @throws IllegalArgumentException
     *             if the port parameter is outside the range of valid port
     *             values
     */
    public MediatorBrowser(InetAddress ip, int port) {
        this(new InetSocketAddress(ip, port));
    }

    /**
     * Establishes a new connection to the mediator and retrieves a set of the
     * registered target IDs.
     * 
     * @return a set of the registered target IDs
     * @throws IOException
     *             if an I/O occurs
     */
    public Set<String> getSetOfRegisteredTargets() throws IOException {
        Socket s = sf.createSocket();
        s.connect(mediatorAddress);
        ListRequestMessage request = new ListRequestMessage();
        MessageEncoder mec = new MessageEncoder();
        s.getOutputStream().write(mec.encodeMessage(request));
        MessageDecoder med = new MessageDecoder(s.getInputStream());
        Message response = med.decodeMessage();
        s.close();
        if (response instanceof ListResponseMessage) {
            return ((ListResponseMessage) response).getRegisteredTargets();
        } else if (response instanceof ExceptionMessage) {
            throw new IOException(((ExceptionMessage) response).getErrorText());
        } else {
            throw new IOException("Received unknown message type from mediator.");
        }
    }

    /**
     * Sets the {@link SocketFactory} that gets used to create socket for the
     * connection to the mediator. If socket factory is not set the default
     * {@link SocketFactory} is used.
     * 
     * @param sf
     *            the {@link SocketFactory} to be used
     */
    public void setSocketFactory(SocketFactory sf) {
        if (sf == null) {
            throw new IllegalArgumentException("socket factory must not be null");
        }
        this.sf = sf;
    }

}
