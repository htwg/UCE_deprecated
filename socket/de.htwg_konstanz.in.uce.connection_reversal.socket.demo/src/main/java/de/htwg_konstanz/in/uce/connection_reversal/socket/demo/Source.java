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

import de.htwg_konstanz.in.uce.connection_reversal.socket.source.ConnectionReversalSource;

/**
 * Class which uses the ConnectionReversal class for testing.
 * 
 * @author Stefan Lohr, Thomas Zink
 */
public class Source {

	public static void main(String[] args) throws Exception {
		
		Logger logger = LoggerFactory.getLogger("Source");
		
		if (args.length != 3) {
			
			logger.error("Illegal count of arguments, exact three arguments expected");
			logger.error("targetName, mediatorIP, mediatorPort");
			System.exit(1);
		}
		int mediatorPort;
		
		String uniqueUserName = args[0];
		String mediatorIP = args[1];
		
		try {
			
			mediatorPort = Integer.parseInt(args[2]);
		}
		catch (Exception e) {
			
			logger.error("Illegal 3th argument, number expected");
			System.exit(2);
			return;
		}
		
		ConnectionReversalSource.Builder crsBuilder = new ConnectionReversalSource.Builder();
		
		crsBuilder.setMediatorIP(mediatorIP);
		crsBuilder.setMediatorPort(mediatorPort);
		crsBuilder.setUniqueUserName(uniqueUserName);
		
		ConnectionReversalSource crs = crsBuilder.build();
		
		Socket socket = crs.connect();
		System.out.println(socket);
	}
}
