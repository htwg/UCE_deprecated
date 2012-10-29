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

import java.util.Set;

/**
 * List response message. It is sent from the mediator as answer to a ListRequestMessage.
 * It contains all registered targets on the mediator.
 * 
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0        3  4        7  
 * +--+--+--+--+--+--+--+--+
 * |    MAGIC  |    Type   |    
 * +--+--+--+--+--+--+--+--+
 * |                       |
 * |   registered targets  |
 * .   (variable length)   .
 * .                       .
 * +--+--+--+--+--+--+--+--+ 
 * 
 *</pre>
 *
 * The IDs of the registered targets are separated by a binary 0. End of message is indicated by
 * two binary 0s.
 *                      
 * @author Daniel Maier
 *
 */
public final class ListResponseMessage implements Message {

    private final Set<String> registeredTargets;
    
    /**
     * Creates a new ListResponseMessage with the given registered targets.
     * @param registeredTargets set with IDs of all registered targets.
     */
    public ListResponseMessage(Set<String> registeredTargets) {
        this.registeredTargets = registeredTargets;
    }

    /**
     * Returns IDs of all registered targets.
     * @return set with IDs of all registered targets.
     */
    public Set<String> getRegisteredTargets() {
        return registeredTargets;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((registeredTargets == null) ? 0 : registeredTargets
						.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ListResponseMessage)) {
			return false;
		}
		ListResponseMessage other = (ListResponseMessage) obj;
		if (registeredTargets == null) {
			if (other.registeredTargets != null) {
				return false;
			}
		} else if (!registeredTargets.equals(other.registeredTargets)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ListResponseMessage [registeredTargets=" + registeredTargets
				+ "]";
	}

    
}
