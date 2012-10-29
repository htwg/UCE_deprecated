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

package de.htwg_konstanz.in.uce.rmi.registry.p2p.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.dht.dht_access.UceDht;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtVuzeAdapter;

/**
 * Implements a P2P RMI registry.
 * This registry implementation is deprecated and incomplete. Use P2pRmiRegistry in
 * package de.htwg_konstanz.in.uce.rmi.registry.p2p.peer instead.
 * 
 * @author dmaier
 */
@Deprecated
public class P2PRegistry implements Registry {
    
    private final UceDht dht;
    
    public P2PRegistry() throws IOException {
        try {
            dht = new UceDhtVuzeAdapter(0);
            dht.bootstrap();
        } catch (DHTTransportException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    public Remote lookup(String name) throws RemoteException, NotBoundException, AccessException {
        byte[] returnValue;
        try {
            returnValue = dht.get(name);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // TODO was werfen???
            throw new RemoteException("Lookup got interrupted", e);
        }
        if(returnValue == null) {
            throw new NotBoundException();
        }
        ByteArrayInputStream bin = new ByteArrayInputStream(returnValue);
        ObjectInputStream oin;
        Remote stub;
        try {
            oin = new ObjectInputStream(bin);
            stub = (Remote) oin.readObject();
        } catch (IOException e) {
            throw new RemoteException("", e);
        } catch (ClassNotFoundException e) {
            throw new RemoteException("", e);
        }
        return stub;
    }

    public void bind(String name, Remote obj) throws RemoteException, AlreadyBoundException,
            AccessException {
        throw new UnsupportedOperationException();
    }

    public void unbind(String name) throws RemoteException, NotBoundException, AccessException {
        throw new UnsupportedOperationException();
    }

    public void rebind(String name, Remote obj) throws RemoteException, AccessException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout;
        try {
            oout = new ObjectOutputStream(bout);
            oout.writeObject(obj);
        } catch (IOException e) {
            throw new RemoteException("", e);
        }
        try {
            dht.put(name, bout.toByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public String[] list() throws RemoteException, AccessException {
        throw new UnsupportedOperationException();
    }

}
