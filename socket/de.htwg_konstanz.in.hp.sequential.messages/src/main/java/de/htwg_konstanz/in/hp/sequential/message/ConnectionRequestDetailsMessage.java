package de.htwg_konstanz.in.hp.sequential.message;

import java.net.InetAddress;

/**
 * Connection request details message. It is sent from the mediator to the target to
 * forward the public endpoint of the source to the target. It also contains the 
 * information whether the target has to punch a hole or not and the IP address version 
 * of the public endpoint.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0           4  5     7  8                    15
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |    MAGIC     |  Type  |PH|     IP-Version     |
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
public final class ConnectionRequestDetailsMessage implements Message {
    private final InetAddress ip;
    private final int port;
    private final boolean punchHole;
    
    /**
     * Creates a new ConnectionRequestDetailsMessage.
     * @param ip the the IP of the public endpoint of the source.
     * @param port the port of the public endpoint of the source.
     * @param punchHole information whether the target needs to punch a hole or not.
     */
    public ConnectionRequestDetailsMessage(InetAddress ip, int port, boolean punchHole) {
        this.ip = ip;
        this.port = port;
        this.punchHole = punchHole;
    }

    /**
     * Returns the IP of the public endpoint of the source.
     * @return the IP of the public endpoint of the source.
     */
    public InetAddress getIP() {
        return ip;
    }

    /**
     * Returns the port of the public endpoint of the source.
     * @return the port of the public endpoint of the source.
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the information whether the target needs to punch a hole or not.
     * @return information whether the target needs to punch a hole or not.
     */
    public boolean isPunchHole() {
        return punchHole;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ip == null) ? 0 : ip.hashCode());
        result = prime * result + port;
        result = prime * result + (punchHole ? 1231 : 1237);
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
        if (!(obj instanceof ConnectionRequestDetailsMessage)) {
            return false;
        }
        ConnectionRequestDetailsMessage other = (ConnectionRequestDetailsMessage) obj;
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
        if (punchHole != other.punchHole) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConnectionRequestDetailsMessage [ip=" + ip + ", port=" + port + ", punchHole="
                + punchHole + "]";
    }
}
