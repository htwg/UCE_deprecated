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

package de.htwg_konstanz.in.uce.socket.relay.messages;

import de.htwg_konstanz.in.uce.messages.UceMethod;
import de.htwg_konstanz.in.uce.messages.UceMethodDecoder;

/**
 * A {@link UceMethodDecoder} to decode specific relay uce methods.
 * @author Daniel Maier
 *
 */
final class RelayUceMethodDecoder implements UceMethodDecoder {

    public UceMethod decode(int encoded) {
        return RelayUceMethod.fromEncoded(encoded);
    }

}
