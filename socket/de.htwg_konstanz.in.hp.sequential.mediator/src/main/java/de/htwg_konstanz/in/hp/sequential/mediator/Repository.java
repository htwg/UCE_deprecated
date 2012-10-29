package de.htwg_konstanz.in.hp.sequential.mediator;

import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Repository for registered targets implemented as a singleton.
 * 
 * @author Daniel Maier
 * 
 */
public enum Repository {
    INSTANCE;

    private final Map<String, SocketAddress> repo = new HashMap<String, SocketAddress>();
    private DatagramSocket registerSocket;

    /**
     * Inserts or updates a new entry for the given ID.
     * 
     * @param id
     *            the ID under that the target should get registered.
     * @param remoteAddress
     *            the public endpoint of the registering target.
     */
    public void insertOrUpdateID(String id, SocketAddress remoteAddress) {
        repo.put(id, remoteAddress);
    }

    /**
     * Gets the public endpoint of the target with the given ID.
     * 
     * @param id
     *            the ID of the target.
     * @return the public endpoint of the desired target.
     * @throws NoSuchElementException
     *             if no entry is found for the given ID.
     */
    public SocketAddress getTargetRegisterEndpoint(String id) throws NoSuchElementException {
        SocketAddress address = repo.get(id);
        if (address == null) {
            throw new NoSuchElementException();
        }
        return address;
    }

    /**
     * Sets the register socket.
     * @param registerSocket the socket that receives the register messages.
     */
    public void setRegisterSocket(DatagramSocket registerSocket) {
        this.registerSocket = registerSocket;
    }

    /**
     * Returns the register socket.
     * @return the register socket.
     * @throws IllegalStateException if no register socket has been set before.
     */
    public DatagramSocket getRegisterSocket() throws IllegalStateException {
        if (registerSocket == null) {
            throw new IllegalStateException(
                    "No register socket set. You have to set a register socket before "
                            + "you can invoke this method.");
        }
        return registerSocket;
    }
    
    /**
     * Turns the singleton object in original state. 
     */
    protected void reset() {
    	this.registerSocket = null;
    	this.repo.clear();
    }

    // TODO remove -> deregistreiren (intervall wenn kein register ankommt oder
    // explizit)
}
