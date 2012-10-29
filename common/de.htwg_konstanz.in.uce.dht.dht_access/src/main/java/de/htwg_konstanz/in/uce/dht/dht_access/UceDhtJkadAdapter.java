/**
 * Copyright (C) 2011 Thomas Zink
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

package de.htwg_konstanz.in.uce.dht.dht_access;

import java.net.InetSocketAddress;

import de.uka.tm.jkad.framework.Hash;
import de.uka.tm.jkad.framework.JKad;
import de.uka.tm.jkad.framework.exceptions.HashLengthException;
import de.uka.tm.jkad.framework.exceptions.JKadException;
import de.uka.tm.jkad.mainline.MainLineBTPeer;


/**
 * @author tzink
 *
 */
public class UceDhtJkadAdapter implements UceDht {

	private final JKad jkad;
	private final MainLineBTPeer peer;
	
	public UceDhtJkadAdapter(int port) throws JKadException {
		jkad = JKad.getInstance();
		peer = jkad.createKademliaPeer(port);
		peer.start();
		peer.bootstrap(new InetSocketAddress("dht.transmissionbt.com", 6881), 10000);
	}


	/* (non-Javadoc)
	 * @see de.htwg_konstanz.in.uce.dht.dht_access.UceDht#put(java.lang.String, byte[])
	 */
	public boolean put(final String key, final byte[] value) throws InterruptedException {
		try {
			Hash hashkey = new Hash(key);
		} catch (HashLengthException e) {
			throw new InterruptedException(e.getMessage());
		}
		// TODO: check splitting
		// jkad / mainline dht only support writing int. need splits as in vuze adapter, don't know if possible though
		//GetClosestNodesTransactionResult rc = peer.getClosestNodesSync(hashkey, value);
		//peer.storeSync(destination, hashkey, value, token, timeout);
		return false;
	}

	/* (non-Javadoc)
	 * @see de.htwg_konstanz.in.uce.dht.dht_access.UceDht#get(java.lang.String)
	 */
	public byte[] get(final String key) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.htwg_konstanz.in.uce.dht.dht_access.UceDht#remove(java.lang.String)
	 */
	public byte[] remove(final String key) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see de.htwg_konstanz.in.uce.dht.dht_access.UceDht#bootstrap()
	 */
	public void bootstrap() {
		// TODO Auto-generated method stub

	}
	
	public void destroy() {}

}
