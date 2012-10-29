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

package de.htwg_konstanz.in.uce.sendItDirect;

import java.applet.Applet;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import de.htwg_konstanz.in.uce.hp.parallel.target.HolePunchingTarget;
import de.htwg_konstanz.in.uce.jsMethodCall.IJSMethodCall;
import de.htwg_konstanz.in.uce.sendItDirect.server.FileSender;
import de.htwg_konstanz.in.uce.sendItDirect.server.ListenerThread;

public class Configuration {
	
	public Socket senderSocket = null;
	public IJSMethodCall jsMethodCall = null;
	public HolePunchingTarget holePunchingTarget = null;
	public String mediatorIP;
	public int mediatorPort;
	public String callbackFunction;
	public String messageFunction;
	public Applet applet;
	public String fileSource;
	public String fileId;
	public ConcurrentMap<String, Long> transferCount;
	public Map<String, Socket> receiverSocket;
	public Map<String, ListenerThread> listenerThreadMap;
	public Map<String, List<FileSender>> fileSenderListMap;
	
	public Configuration() {
		
		listenerThreadMap = new HashMap<String, ListenerThread>();
		fileSenderListMap = new HashMap<String, List<FileSender>>();
		receiverSocket = new HashMap<String, Socket>();
		transferCount = new ConcurrentHashMap<String, Long>(); 
	}
}
