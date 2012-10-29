package de.htwg_konstanz.in.hp.sequential.integration_test;

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

    @Override
    public void writeString(String s) throws IOException {
        dos.writeUTF(s);
    }

    @Override
    public String receiveString() throws IOException {
        return dis.readUTF();
    }

    @Override
    public void close() throws IOException {
        s.close();
    }

}
