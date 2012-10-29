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
 * Register message. It is sent from the target to the mediator to register itself.
 * For this it contains an ID under that the target should be registered. It also contains 
 * the private endpoint of the source.
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

 *</pre> 
 * 
 * @author Daniel Maier
 *
 */
public final class RegisterMessage implements Message {
    private final String id;
    private final InetAddress privateIp;
    private final int privatePort;
    
    /**
     * Creates a new RegisterMessage with the given ID.
     * @param id the ID under that the target gets registered.
     * @param privateIP the IP of the private endpoint of the target.
     * @param privatePort the port of the private endpoint of the target.
     */
    public RegisterMessage(String id, InetAddress privateIP, int privatePort) {
        this.id = id;
        this.privateIp = privateIP;
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
     * Returns the IP of the private endpoint of the target.
     * @return the IP of the private endpoint of the target.
     */
    public InetAddress getPrivateIP() {
        return privateIp;
    }

    /**
     * Returns the port of the private endpoint of the target.
     * @return the port of the private endpoint of the target.
     */
    public int getPrivatePort() {
        return privatePort;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((privateIp == null) ? 0 : privateIp.hashCode());
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
        if (!(obj instanceof RegisterMessage)) {
            return false;
        }
        RegisterMessage other = (RegisterMessage) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (privateIp == null) {
            if (other.privateIp != null) {
                return false;
            }
        } else if (!privateIp.equals(other.privateIp)) {
            return false;
        }
        if (privatePort != other.privatePort) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RegisterMessage [id=" + id + ", privateIp=" + privateIp + ", privatePort="
                + privatePort + "]";
    }
}
