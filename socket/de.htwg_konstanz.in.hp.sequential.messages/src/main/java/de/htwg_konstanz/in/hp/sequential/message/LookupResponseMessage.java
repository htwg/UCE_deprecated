package de.htwg_konstanz.in.hp.sequential.message;

import java.net.InetAddress;

/**
 * Lookup response message. It is sent from the mediator to the source. It contains
 * the public endpoint of the target and the used IP address version.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0           4  5     7  8                    15
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |    MAGIC     |  Type  |--|     IP-Version     |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                  IP (IPv4)                    |
 * |                                               |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                    Port                       |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * </pre>
 * @author Daniel Maier
 *
 */
public final class LookupResponseMessage implements Message {
    private final InetAddress ip;
    private final int port;
    
    /**
     * Creates a new LookupResponseMessage.
     * @param ip the IP of the public endpoint of the target.
     * @param port the port of the public endpoint of the target.
     */
    public LookupResponseMessage(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * Returns the IP of the public endpoint of the target.
     * @return the IP of the public endpoint of the target.
     */
    public InetAddress getIP() {
        return ip;
    }

    /**
     * Returns the port of the public endpoint of the target.
     * @return the port of the public endpoint of the target.
     */
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        if (!(obj instanceof LookupResponseMessage)) {
            return false;
        }
        LookupResponseMessage other = (LookupResponseMessage) obj;
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
        return "LookupResponseMessage [ip=" + ip + ", port=" + port + "]";
    }
}
