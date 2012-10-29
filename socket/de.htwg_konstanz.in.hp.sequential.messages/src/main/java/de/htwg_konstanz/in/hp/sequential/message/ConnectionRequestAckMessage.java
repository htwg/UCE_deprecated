package de.htwg_konstanz.in.hp.sequential.message;

import java.net.InetAddress;
import java.util.UUID;
/**
 * Conection request ack message. It is sent from the target to the mediator to 
 * acknowledge the connection request. It contains the version of the private IP
 * address of the target followed by the IP address and port of the private endpoint
 * of the target. To match this message to the connection request it contains a correlator.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 *  0           4  5     7  8                    15
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |    MAGIC     |  Type  |--|     IP-Version     |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                  IP (IPv4)                    |
 * |                                               |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                    Port                       |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |             Correlator (16 Bytes)             |
 * |                                               |
 * .                                               .
 * .                                               .
 * .                                               .
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * </pre>
 * 
 * @author Daniel Maier
 *
 */
public final class ConnectionRequestAckMessage implements Message {
    private final UUID correlator;
    private final InetAddress ip;
    private final int port;

    /**
     * Creates a new ConnectionRequestAckMessage.
     * @param correlator the correlator of the message.
     * @param ip the IP address of the private endpoint of the target.
     * @param port the port of the private endpoint of the target.
     */
    public ConnectionRequestAckMessage(UUID correlator, InetAddress ip, int port) {
        this.correlator = correlator;
        this.ip = ip;
        this.port = port;
    }

    /**
     * Returns the correlator of the message.
     * @return the correlator of the message.
     */
    public UUID getCorrelator() {
        return correlator;
    }

    /**
     * Returns the IP of the private endpoint of the target.
     * @return the IP of the private endpoint of the target.
     */
    public InetAddress getIP() {
        return ip;
    }

    /**
     * Returns the port of the private endpoint of the target.
     * @return the port of the private endpoint of the target.
     */
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((correlator == null) ? 0 : correlator.hashCode());
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ConnectionRequestAckMessage)) {
            return false;
        }
        ConnectionRequestAckMessage other = (ConnectionRequestAckMessage) obj;
        if (correlator == null) {
            if (other.correlator != null) {
                return false;
            }
        } else if (!correlator.equals(other.correlator)) {
            return false;
        }
        if (ip == null) {
            if (other.ip != null) {
                return false;
            }
        } else if (!ip.equals(other.ip)) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConnectionRequestAckMessage [correlator=" + correlator + ", ip=" + ip + ", port="
                + port + "]";
    }
}
