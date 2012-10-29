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

/**
 * Unregister message. It is sent to the mediator to unregister a target with the containing ID.
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
 *
 *</pre> 
 * 
 * @author Daniel Maier
 *
 */
public final class UnregisterMessage implements Message {
    
    private final String id;

    /**
     * Creates a new UnregisterMessage with the given ID.
     * @param id the ID of the target that should get unregistered.
     */
    public UnregisterMessage(String id) {
        this.id = id;
    }

    /**
     * Returns the ID of the target that should get unregistered.
     * @return the ID of the target.
     */
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        if (!(obj instanceof UnregisterMessage)) {
            return false;
        }
        UnregisterMessage other = (UnregisterMessage) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UnregisterMessage [id=" + id + "]";
    }
}
