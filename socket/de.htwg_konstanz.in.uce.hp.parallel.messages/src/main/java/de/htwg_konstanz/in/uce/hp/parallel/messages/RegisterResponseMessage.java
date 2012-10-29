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
 * Register response message. It is sent from the mediator as answer to a register message.
 * <br/><br/>
 * It has the following format on the wire.
 * <pre>
 * 
 *  0        3  4        7  
 * +--+--+--+--+--+--+--+--+
 * |    MAGIC  |    Type   |    
 * +--+--+--+--+--+--+--+--+
 * |        success?       |
 * +--+--+--+--+--+--+--+--+
 * @author Daniel Maier
 *
 */
public final class RegisterResponseMessage implements Message {
	private final boolean success;
	
	public RegisterResponseMessage(boolean success) {
		this.success = success;
	}

    /**
     * Indicates if the registration was successful or not.
     * @return true if the registration was successful, false otherwise.
     */
	public boolean isSuccess() {
		return success;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (success ? 1231 : 1237);
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
		if (!(obj instanceof RegisterResponseMessage)) {
			return false;
		}
		RegisterResponseMessage other = (RegisterResponseMessage) obj;
		if (success != other.success) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "RegisterResponseMessage [success=" + success + "]";
	}
}
