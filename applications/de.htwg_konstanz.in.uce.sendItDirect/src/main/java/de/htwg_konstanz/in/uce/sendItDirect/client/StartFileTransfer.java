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

package de.htwg_konstanz.in.uce.sendItDirect.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.PrivilegedAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.source.HolePunchingSource;
import de.htwg_konstanz.in.uce.hp.parallel.source.TargetNotRegisteredException;
import de.htwg_konstanz.in.uce.sendItDirect.Configuration;
import de.htwg_konstanz.in.uce.sendItDirect.HandleErrorMessages;

public class StartFileTransfer implements PrivilegedAction<Void> {
	
	Logger logger;
	Configuration config;
	
	public StartFileTransfer(Configuration config) {
		
		this.config = config;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	public Void run() {
		
		InetSocketAddress mediatorSocketAddress;
		HolePunchingSource holePunchingSource;
		
		mediatorSocketAddress = new InetSocketAddress(config.mediatorIP, config.mediatorPort);
		
		holePunchingSource = new HolePunchingSource();
		
		try {
			
			config.senderSocket = holePunchingSource.getSocket(config.fileId, mediatorSocketAddress);
			
			FileProvider fileProvider = new FileProvider(config);
			
			fileProvider.start();
		}
		catch (IOException e) {
			
			new HandleErrorMessages("socketIOExceptionError", e, config, logger);
			
		} catch (TargetNotRegisteredException e) {
			
			new HandleErrorMessages("targetNotRegisteredError", e, config, logger);
		}
		
		return null;
	}
}
