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
import java.util.UUID;

/**
 * Forward endpoints message. It is sent from the mediator to the target and source. 
 * It instructs them to start the hole punching process. It contains the private and 
 * public endpoint of the other peer. Additionally it contains a authentication token
 * that is used by the peers to authenticate each other.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0        3  4        7  8                    15
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |    MAGIC  |    Type   |      IP-Version       |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                Private  Port                  |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                Private IP (IPv4)              |
 * |                                               |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |                Public  Port                   |
 * +--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+--+
 * |      IP-Version       |                       |
 * +--+--+--+--+--+--+--+--+                       |
 * |                Public IP (IPv4)               |
 * |                       +--+--+--+--+--+--+--+--+
 * |                       |                       | 
 * +--+--+--+--+--+--+--+--+                       |
 * |        Authentication Token (16 Bytes)        |
 * .                                               .
 * .                       +--+--+--+--+--+--+--+--+                     
 * .                       |                       
 * +--+--+--+--+--+--+--+--+                      
 *
 *</pre> 
 * @author Daniel Maier
 */
public final class ForwardEndpointsMessage implements Message {
    private final InetAddress privateIp;
    private final int privatePort;
    private final InetAddress publicIp;
    private final int publicPort;
    private final UUID authenticationToken;
    
    /**
     * Creates a new ForwardEndpointsMessage.
     * @param privateIP the IP of the private endpoint of the destination peer.
     * @param privatePort the port of the private endpoint of the destination peer.
     * @param publicIP the IP of the public endpoint of the destination peer.
     * @param publicPort the port of the public endpoint of the destination peer.
     * @param authenticationToken the authentication token that is used by the peers
     * to authenticate each other.
     */
    public ForwardEndpointsMessage(InetAddress privateIP, int privatePort, InetAddress publicIP, 
            int publicPort, UUID authenticationToken) {
        this.privateIp = privateIP;
        this.privatePort = privatePort;
        this.publicIp = publicIP;
        this.publicPort = publicPort;
        this.authenticationToken = authenticationToken;
    }

    /**
     * Returns the IP of the private endpoint of the destination peer.
     * @return the IP of the private endpoint of the destination peer.
     */
    public InetAddress getPrivateIP() {
        return privateIp;
    }

    /**
     * Returns the port of the private endpoint of the destination peer.
     * @return the port of the private endpoint of the destination peer.
     */
    public int getPrivatePort() {
        return privatePort;
    }

    /**
     * Returns the IP of the public endpoint of the destination peer.
     * @return the IP of the public endpoint of the destination peer.
     */
    public InetAddress getPublicIP() {
        return publicIp;
    }

    /**
     * Returns the port of the public endpoint of the destination peer.
     * @return the port of the public endpoint of the destination peer.
     */
    public int getPublicPort() {
        return publicPort;
    }
    
    /**
     * Returns the the authentication token that is used by the peers
     * to authenticate each other.
     * @return the authentication token that is used by the peers
     * to authenticate each other.
     */
    public UUID getAuthenticationToken() {
        return authenticationToken;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((authenticationToken == null) ? 0 : authenticationToken.hashCode());
        result = prime * result + ((privateIp == null) ? 0 : privateIp.hashCode());
        result = prime * result + privatePort;
        result = prime * result + ((publicIp == null) ? 0 : publicIp.hashCode());
        result = prime * result + publicPort;
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
        if (!(obj instanceof ForwardEndpointsMessage)) {
            return false;
        }
        ForwardEndpointsMessage other = (ForwardEndpointsMessage) obj;
        if (authenticationToken == null) {
            if (other.authenticationToken != null) {
                return false;
            }
        } else if (!authenticationToken.equals(other.authenticationToken)) {
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
        if (publicIp == null) {
            if (other.publicIp != null) {
                return false;
            }
        } else if (!publicIp.equals(other.publicIp)) {
            return false;
        }
        if (publicPort != other.publicPort) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ForwardEndpointsMessage [privateIp=" + privateIp + ", privatePort=" + privatePort
                + ", publicIp=" + publicIp + ", publicPort=" + publicPort
                + ", authenticationToken=" + authenticationToken + "]";
    }  
}
