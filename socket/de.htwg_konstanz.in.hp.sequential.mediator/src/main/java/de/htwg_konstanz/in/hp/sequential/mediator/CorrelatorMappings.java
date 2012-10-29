package de.htwg_konstanz.in.hp.sequential.mediator;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

/**
 * Singleton to map correlators to the corresponding source socket and the
 * target endpoint.
 * 
 * @author Daniel Maier
 * 
 */
public enum CorrelatorMappings {
    INSTANCE;

    /**
     * Class to hold the value of the mapping. The values are the socket of the
     * source and the endpoint of the target.
     * 
     * @author Daniel Maier
     * 
     */
    private static class MappingValue {
        private Socket socket;
        private InetSocketAddress destinationAddress;

        private MappingValue(Socket socket) {
            this.socket = socket;
        }
    }

    private final Map<UUID, MappingValue> mapping = new Hashtable<UUID, MappingValue>();

    /**
     * Creates a new entry.
     * 
     * @param socket
     *            of the source.
     * @return the correlator.
     */
    public UUID createMapping(Socket s) {
        UUID correlator = UUID.randomUUID();
        mapping.put(correlator, new MappingValue(s));
        return correlator;
    }

    /**
     * Removes the mapping of the given correlator.
     * 
     * @param correlator
     *            the correlator of the mapping that should be removed.
     */
    public void removeMapping(UUID correlator) {
        mapping.remove(correlator);
    }

    /**
     * Returns the source socket that is mapped to the given correlator.
     * 
     * @param correlator
     *            the correlator of the mapping its socket is requested.
     * @return the source socket.
     */
    public Socket getSourceSocket(UUID correlator) {
        return mapping.get(correlator).socket;
    }

    /**
     * Adds the target endpoint to the given mapping.
     * 
     * @param correlator
     *            the correlator of the mapping.
     * @param socketAddress
     *            the endpoint of the target.
     */
    public void addTargetEndpointToMapping(UUID correlator, InetSocketAddress socketAddress) {
        mapping.get(correlator).destinationAddress = socketAddress;
    }

    /**
     * Returns the target endpoint of the desired mapping.
     * 
     * @param correlator
     *            the correlator of the mapping its target endpoint is desired.
     * @return the endpoint of the target.
     */
    public InetSocketAddress getTargetEndpoint(UUID correlator) {
        return mapping.get(correlator).destinationAddress;
    }
}
