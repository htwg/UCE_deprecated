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

package de.htwg_konstanz.in.uce.socket.relay.client;

/**
 * Class to define some global constants.
 * 
 * @author Daniel Maier
 * 
 */
public class Constants {
    /**
     * The desired lifetime of an allocation for this client in seconds.
     */
    public static final int ALLOCATION_LIFETIME = 2 * 60;
    /**
     * Number of seconds to send refresh request of allocation before lifetime
     * expires.
     */
    public static final int ALLOCATION_LIFETIME_ADVANCE = 60;
    /**
     * Minimum interval of allocation refresh requests in seconds (is used if
     * (lifetime granted by server - ALLOCATION_LIFETIME_ADVANCE) <
     * ALLOCATION_LIFETIME_MIN).
     */
    public static final int ALLOCATION_LIFETIME_MIN = 60;
}
