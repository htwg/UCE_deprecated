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

package de.htwg_konstanz.in.uce.socket.relay.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.htwg_konstanz.in.uce.socket.relay.client.RelayClient;

/**
 * Demo client for relaying. Creates a new relay allocation on the relay server
 * and prints out the allocation endpoint. After that it waits for a relay
 * connection of a peer to that allocation. Then it prints out all the data that
 * is sent from the peer to this client through the relay server.
 * 
 * @author Daniel Maier
 * 
 */
public class DemoRelayClient {

    private static final String IP = "localhost";
    private static final int PORT = 10300;

    public static void main(String[] args) throws IOException, ClassNotFoundException,
            InterruptedException {
        RelayClient client = new RelayClient(new InetSocketAddress(IP, PORT));
        System.out.println(client.createAllocation());
        Socket s = client.accept();
        System.out.println(s);
        int r;
        while ((r = s.getInputStream().read()) > -1) {
            System.out.print((char) r);
        }
        client.discardAllocation();
        Thread.sleep(1000);
    }

}
