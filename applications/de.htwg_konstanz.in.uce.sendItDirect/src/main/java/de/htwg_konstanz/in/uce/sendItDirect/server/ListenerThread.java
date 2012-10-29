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

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.target.HolePunchingTarget;
import de.htwg_konstanz.in.uce.sendItDirect.Configuration;
import de.htwg_konstanz.in.uce.sendItDirect.HandleErrorMessages;

public class ListenerThread extends Thread {
	
	private Configuration config;
	private Logger logger;
	private HolePunchingTarget holePunchingTarget;
	private boolean isInterrupted;
	
	public ListenerThread(Configuration config) {
		
		this.config = config;
		this.holePunchingTarget = config.holePunchingTarget;
		this.logger = LoggerFactory.getLogger(this.getClass());
		
		config.listenerThreadMap.put(config.fileId, this);
	}
	
	public void run() {
		
		while (!isInterrupted) {
			
			String userId = UUID.randomUUID().toString();
			logger.debug("waiting for connection requests");
			
			try {
				
				config.receiverSocket.put(userId, holePunchingTarget.accept());
			}
			catch (IOException e) {
				
				if (!isInterrupted) new HandleErrorMessages("acceptIOExceptionError", e, config, logger);
				
				return;
			}
			catch (InterruptedException e) {
				
				logger.debug("Accept in ListenerThread interrupted");
				
				return;
			}
			
			logger.debug("handle incoming connection request in ReceiverThread");
			
			Thread fileSenderThread = new FileSender(userId, config);
			
			fileSenderThread.start();
		}
		
		logger.debug("while interrupted");
	}
	
	/**
	 * Methode die den Thread beendet und den accept() abbricht.
	 */
	public void interrupt() {
		
		logger.debug("call super.interrupt");
		
		super.interrupt();
		
		logger.debug("interrupt called");
		
		isInterrupted = true;
		
		try {
			
			logger.debug("calling hpt.stop");
			holePunchingTarget.stop();
			logger.debug("hpt.stop called");
		}
		catch (IOException e) {
			
			logger.debug("listenerSocket in interrupt() geschlossen -> execption");
		}
		
		logger.debug("interrupt finished");
	}
}
