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

package de.htwg_konstanz.in.uce.hp.parallel.integration_test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import de.htwg_konstanz.in.connectivity_framework.Connection;

public class SocketConnection implements Connection {
 
    private final Socket s;
    private final DataOutputStream dos;
    private final DataInputStream dis;

    public SocketConnection(Socket s) throws IOException {
        this.s = s;
        this.dos = new DataOutputStream(s.getOutputStream());
        this.dis = new DataInputStream(s.getInputStream());
    }

  //  @Override
    public void writeString(String s) throws IOException {
        dos.writeUTF(s);
    }

    //@Override
    public String receiveString() throws IOException {
        return dis.readUTF();
    }

    //@Override
    public void close() throws IOException {
        s.close();
    }

}
