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
package de.htwg_konstanz.in.uce.sendItDirect.server;

import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.sendItDirect.Configuration;

public class AbortFileProviding implements PrivilegedAction<Void> {
	
	private Logger logger;
	private Configuration config;
	
	public AbortFileProviding(Configuration config) {
		
		this.config = config;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	public Void run() {
		
		logger.debug("Interrupting all FileSender-Threads");
		
		synchronized (config.fileSenderListMap) {
			
			for (Map.Entry<String, List<FileSender>> fileSenderList : config.fileSenderListMap.entrySet()) {
				
				for (FileSender fileSender : fileSenderList.getValue()) {
					
					fileSender.interrupt();
				}
			}
			
			config.fileSenderListMap.clear();
		}
		
		logger.debug("All FileSender-Threads are interrupted");
		logger.debug("Interrupting all ListenerThread-Threads");
		
		synchronized (config.listenerThreadMap) {
			
			for (Map.Entry<String, ListenerThread> listenerThreadMap : config.listenerThreadMap.entrySet()) {
				
				listenerThreadMap.getValue().interrupt();
			}
			
			config.listenerThreadMap.clear();
		}
		
		logger.debug("All ListenerThread-Threads are interrupted");
		
		return null;
	}
}
