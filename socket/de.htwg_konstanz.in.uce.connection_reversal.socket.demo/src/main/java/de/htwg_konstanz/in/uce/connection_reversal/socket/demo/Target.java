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

package de.htwg_konstanz.in.uce.connection_reversal.socket.demo;

import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.connection_reversal.socket.target.ConnectionReversalTarget;

/**
 *  Class which uses the ConnectionReversal class for testing.
 * 
 * @author Stefan Lohr
 */
public class Target {
	
	public static void main(String[] args) throws Exception {
		
		Logger logger = LoggerFactory.getLogger("Target");
		
		if (args.length != 4) {
			
			logger.error("Illegal count of arguments, exact four arguments expected");
			logger.error("targetName, iterationTime, mediatorIP, mediatorPort");
			System.exit(1);
		}
		
		String uniqueUserName = args[0];
		String mediatorIP = args[2];
		int mediatorPort, iterationTime;
		
		try {
			
			iterationTime = Integer.parseInt(args[1]);
			mediatorPort = Integer.parseInt(args[3]);
		}
		catch (Exception e) {
			
			logger.error("Illegal argument, number expected");
			System.exit(2); return;
		}
		
		ConnectionReversalTarget.Builder crtBuilder = new ConnectionReversalTarget.Builder();
		
		crtBuilder.setIterationTime(iterationTime);
		crtBuilder.setMediatorIP(mediatorIP);
		crtBuilder.setMediatorPort(mediatorPort);
		crtBuilder.setUniqueUserName(uniqueUserName);
		
		ConnectionReversalTarget crt = crtBuilder.build();
		
		crt.register();
		Socket socket = crt.accept();
		System.out.println(socket);
		
		crt.deregister();
	}
}
