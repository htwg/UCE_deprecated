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
import java.net.InetSocketAddress;
import java.security.PrivilegedAction;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.hp.parallel.target.HolePunchingTarget;
import de.htwg_konstanz.in.uce.sendItDirect.Configuration;
import de.htwg_konstanz.in.uce.sendItDirect.HandleErrorMessages;

public class ProvideFile implements PrivilegedAction<Void> {
	
	private Logger logger;
	private Configuration config;
	
	public ProvideFile(Configuration config) {
		
		this.config = config;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	public Void run() {
		
		config.fileId = UUID.randomUUID().toString();
		
		HolePunchingTarget holePunchingTarget = register();
		
		config.holePunchingTarget = holePunchingTarget;
		
		ListenerThread listenerThread = new ListenerThread(config);
		listenerThread.start();
		
		config.jsMethodCall.call(config.callbackFunction, config.fileId);
		
		return null;
	}
	
	private HolePunchingTarget register() {
		
		HolePunchingTarget holePunchingTarget = null;
		String mediatorIP = config.mediatorIP;
		int mediatorPort = config.mediatorPort;
		
		InetSocketAddress mrsa = new InetSocketAddress(mediatorIP, mediatorPort);
		holePunchingTarget = new HolePunchingTarget(mrsa, config.fileId);

		try {
			
			holePunchingTarget.start();
		}
		catch (IllegalStateException e) {

			new HandleErrorMessages("targetAlreadyRegisteredError", e, config, logger);
		}
		catch (IOException e) {
			
			new HandleErrorMessages("couldNotRegisterTargetError", e, config, logger);
		}
		
		return holePunchingTarget;
	}
}
