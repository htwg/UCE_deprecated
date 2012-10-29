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

package de.htwg_konstanz.in.uce.hp.parallel.messages;

import java.net.InetAddress;

/**
 * Connection request  message.  It is sent form the source to start the 
 * hole punching process with the desired target. To identify the target
 * it contains the ID of it. It also contains the private endpoint of the 
 * target.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0        3  4        7  8                    15
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |    MAGIC  |    Type   |        Length         |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |             ID (Maximum 255 Bytes)            |
 * |                                               |
 * .                                               .
 * .                                               .
 * .                                               .
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                Private  Port                  |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |      IP-Version       |                       |
 * +--+--+--+--+--+--+--+--+                       |
 * |                Private IP (IPv4)              |
 * |                       +--+--+--+--+--+--+--+--+
 * |                       |                      
 * +--+--+--+--+--+--+--+--+ 
 *
 *</pre> 
 * 
 * @author Daniel Maier
 *
 */
public final class ConnectionRequestMessage implements Message {
    private final String id;
    private final InetAddress privateIP;
    private final int privatePort;
    

    /**
     * Creates a new ConnectionRequestMessage with the given ID.
     * @param id id the ID of the target.
     * @param privateIP the IP of the private endpoint of the source.
     * @param privatePort the IP of the private endpoint of the source.
     */
    public ConnectionRequestMessage(String id, InetAddress privateIP, int privatePort) {
        this.id = id;
        this.privateIP = privateIP;
        this.privatePort = privatePort;
    }

    /**
     * Returns the ID of the target.
     * @return the ID of the target.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the IP of the private endpoint of the source.
     * @return the IP of the private endpoint of the source.
     */
    public InetAddress getPrivateIP() {
        return privateIP;
    }

    /**
     * Returns the port of the private endpoint of the source.
     * @return the port of the private endpoint of the source.
     */
    public int getPrivatePort() {
        return privatePort;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((privateIP == null) ? 0 : privateIP.hashCode());
        result = prime * result + privatePort;
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
        if (!(obj instanceof ConnectionRequestMessage)) {
            return false;
        }
        ConnectionRequestMessage other = (ConnectionRequestMessage) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (privateIP == null) {
            if (other.privateIP != null) {
                return false;
            }
        } else if (!privateIP.equals(other.privateIP)) {
            return false;
        }
        if (privatePort != other.privatePort) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ConnectionRequestMessage [id=" + id + ", ip=" + privateIP + ", port="
                + privatePort + "]";
    }
}
