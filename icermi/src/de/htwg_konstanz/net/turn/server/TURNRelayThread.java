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
package de.htwg_konstanz.net.turn.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A TURNRelayThread relays data between a RMI-Client and a RMI-Server.
 * One instance of this class transports RMI-Data in one direction.
 * Another instance handles the other direction.
 * 
 * @author Andre Erb
 * 
 */
public class TURNRelayThread extends Thread {
	private static final int BUFFER_SIZE = 768;
	private InputStream in = null;
	private OutputStream out = null;
	
	/**
	 * Creates a TURNRelayThread.
	 * 
	 * @param inputStream
	 * 		The input stream from communication partner 1.
	 * 
	 * @param outputStream
	 * 		The output stream to communication partner 2.
	 * 
	 */
	public TURNRelayThread(InputStream inputStream, OutputStream outputStream)
	{		
		in = inputStream;
		out = outputStream;
	}
	
	/**
	 * Inherited from Java Thread. This implementation relays RMI-Data from communication partner 1 to 2.
	 * 
	 */
	public void run ()
	{
		BufferedInputStream bufferedIn   = new BufferedInputStream (in);
		BufferedOutputStream bufferedOut = new BufferedOutputStream(out);		
		
		byte[] buf = new byte[BUFFER_SIZE];
		int len = 0;
		
		try { 
			while ( (len = bufferedIn.read(buf, 0, BUFFER_SIZE)) > 0 )
			{	
				bufferedOut.write(buf, 0, len);
				bufferedOut.flush();
			}
		}
		catch (IOException e) 
		{
			System.out.println("The connection between the RMI-Client and the RMI-Server was closed!");
		}
	}
}
