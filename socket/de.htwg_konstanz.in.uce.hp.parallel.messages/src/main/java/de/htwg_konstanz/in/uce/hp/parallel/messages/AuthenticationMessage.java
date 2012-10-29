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

import java.util.UUID;

/**
 * Authentication message. It is sent from the source to the target to initiate the authentication
 * process. Therefore it contains the shared authentication token.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0        3  4        7  
 * +--+--+--+--+--+--+--+--+
 * |    MAGIC  |    Type   |    
 * +--+--+--+--+--+--+--+--+
 * |    Authentication     |
 * .  Token (16 Bytes)     |
 * .                       .
 * .                       .
 * .                       .
 * +--+--+--+--+--+--+--+--+
 *
 *</pre> 
 * @author Daniel Maier
 *
 */
public final class AuthenticationMessage implements Message {
    private final UUID authenticationToken;
    
    /**
     * Creates a new AuthenticationMessage with the given authentication token.
     * @param authenticationToken the authentication token.
     */
    public AuthenticationMessage(UUID authenticationToken) {
        this.authenticationToken = authenticationToken;
    }

    /**
     * Returns the authentication token.
     * @return the authentication token.
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
        if (!(obj instanceof AuthenticationMessage)) {
            return false;
        }
        AuthenticationMessage other = (AuthenticationMessage) obj;
        if (authenticationToken == null) {
            if (other.authenticationToken != null) {
                return false;
            }
        } else if (!authenticationToken.equals(other.authenticationToken)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AuthenticationMessage [authenticationToken=" + authenticationToken + "]";
    }  
}
