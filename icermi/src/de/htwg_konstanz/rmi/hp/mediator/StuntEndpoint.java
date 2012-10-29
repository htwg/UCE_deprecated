/**
 * Copyright (C) 2012 HTWG Konstanz, Oliver Haase
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
package de.htwg_konstanz.rmi.hp.mediator;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.UUID;

public class StuntEndpoint implements Serializable {

	private static final long serialVersionUID = -7812416225983761805L;

	private InetSocketAddress localInetSocketAddress;

	private InetSocketAddress remoteInetSocketAddress;

	private UUID id;

	public StuntEndpoint(UUID id, InetSocketAddress localInetSocketAddress,
			InetSocketAddress remoteInetSocketAddress) {
		this.id = id;
		this.localInetSocketAddress = localInetSocketAddress;
		this.remoteInetSocketAddress = remoteInetSocketAddress;
	}

	public StuntEndpoint(UUID id, InetSocketAddress localInetSocketAdress) {
		this.id = id;
		this.localInetSocketAddress = localInetSocketAdress;
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof StuntEndpoint)) {
			return false;
		}
		StuntEndpoint p = (StuntEndpoint) o;
		if (p.getId() == null) {
			return false;
		}
		return p.getId().equals(this.getId());
	}

	public String toString() {
		return id + " " + localInetSocketAddress + "##" + remoteInetSocketAddress;
	}

	public InetSocketAddress getRemoteInetSocketAddress() {
		return remoteInetSocketAddress;
	}

	public void setRemoteInetSocketAddress(
			InetSocketAddress remoteInetSocketAddress) {
		this.remoteInetSocketAddress = remoteInetSocketAddress;
	}

	public InetSocketAddress getLocalInetSocketAddress() {
		return localInetSocketAddress;
	}

	public UUID getId() {
		return id;
	}

}
