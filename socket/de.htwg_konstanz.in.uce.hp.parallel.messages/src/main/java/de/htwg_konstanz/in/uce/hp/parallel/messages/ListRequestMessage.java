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
 * List request message. It is sent to the mediator to retrieve a list of registered targets.
 * The list of targets is sent in a ListResponseMessage.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0        3  4        7  
 * +--+--+--+--+--+--+--+--+
 * |    MAGIC  |    Type   |    
 * +--+--+--+--+--+--+--+--+
 *
 * @author Daniel Maier
 *
 */
public final class ListRequestMessage implements Message {

    @Override
    public String toString() {
        return "ListRequestMessage []";
    }
}
