/**
 * Copyright (C) 2011 Stefan Lohr
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

package de.htwg_konstanz.in.uce.connection_reversal.mediator;

import java.net.InetSocketAddress;

/**
 * Class which contains all relevant and necessary data of one user
 * 
 * @author Stefan Lohr
 */
public class UserData {
	
	private String userName;
	private long timeStamp;
	private InetSocketAddress inetSocketAddress;
	
	/**
	 * Constructor, initializes a new user
	 * with unique name and InetSocketAddress
	 * 
	 * @param userName Unique name of the user
	 * @param inetSocketAddress InetSocketAddress of the user
	 */
	public UserData(String userName, InetSocketAddress inetSocketAddress) {
		
		this.userName = userName;
		this.inetSocketAddress = inetSocketAddress;
		
		timeStamp = System.currentTimeMillis();
	}
	
	/**
	 * Method to get the unique name of the user
	 * 
	 * @return String with unique name of the user
	 */
	public synchronized String getUserName() {
		
		return userName;
	}
	
	/**
	 * Method to get the port number of the user
	 * 
	 * @return int with the port number of the user
	 */
	public synchronized int getPort() {
		
		return inetSocketAddress.getPort();
	}
	
	/**
	 * Method to get the HostName (IP-Address) of the user
	 * 
	 * @return String with the HostName (IP-Address) of the user 
	 */
	public synchronized String getHostName() {
		
		return inetSocketAddress.getHostName();
	}
	
	/**
	 * Method to get the InetSocketAddress of the user
	 * 
	 * @return InetSocketAddress of the user 
	 */
	public synchronized InetSocketAddress getInetSocketAddress() {
		
		return inetSocketAddress;
	}
	
	/**
	 * Method to get the current TimeStamp (milliseconds since 1970-01-01) of the user
	 * 
	 * @return long with the current TimeStamp (milliseconds since 1970-01-01) of the user 
	 */
	public synchronized long getTimeStamp() {
		
		return timeStamp;
	}
	
	/**
	 * Method which refresh the TimeStamp of the user
	 * with the current TimeStamp (milliseconds since 1970-01-01)
	 */
	public synchronized void refreshTimeStamp() {
		
		timeStamp = System.currentTimeMillis();
	}
}
