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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.sendItDirect.Configuration;
import de.htwg_konstanz.in.uce.sendItDirect.HandleErrorMessages;

public class FileProvider extends Thread {
	
	private Logger logger;
	private Configuration config;
	private boolean isInterrupted;
	private InputStream inputStream;
	
	public FileProvider(Configuration config) {
		
		this.config = config;
		this.logger = LoggerFactory.getLogger(this.getClass());
	}
	
	public void run() {
		
		try {
			
			inputStream = config.senderSocket.getInputStream();
			
			String fileName = readNextLine();
			String fileSize = readNextLine();
			
			byte[] buffer = new byte[8192];
			int readCount = 0;
			
			ServerSocket serverSocket = new ServerSocket(0);
			
			int port = serverSocket.getLocalPort();
			URL url = new URL("http://localhost:" + port);
			
			logger.debug("Datei wird unter folgender Adresse bereitgestellt: " + url);
			
			config.jsMethodCall.call(config.callbackFunction, new String[] { "url", url.toString() });
			
			Socket browserSocket = serverSocket.accept();
			
			OutputStream browserOutputStream = browserSocket.getOutputStream();
			
			browserOutputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
			browserOutputStream.write(("Content-Length: " + String.valueOf(fileSize) + "\r\n").getBytes());
			browserOutputStream.write(("Content-disposition: attachment; filename=" + fileName + "\r\n\r\n").getBytes());
			
			browserOutputStream.flush();
			
			while (((readCount = inputStream.read(buffer, 0, 8192)) > 0) && !isInterrupted) {
				
				browserOutputStream.write(buffer, 0, readCount);
				browserOutputStream.flush();
			}
			
			browserOutputStream.close();
			browserSocket.close();
			
			config.senderSocket.shutdownInput();
			config.senderSocket.shutdownOutput();
			
			config.jsMethodCall.call(config.callbackFunction, new String[] { "finished", null });
			
			// Wait a few seconds for correct stream closing before close socket and thread exit
			try { Thread.sleep(8000); } catch (InterruptedException e) { logger.error("error while thread sleep "); }
			
			config.senderSocket.close();
		}
		catch (MalformedURLException e) {
			
			if (!isInterrupted) new HandleErrorMessages("malformedURLExceptionError", e, config, logger);
		}
		catch (IOException e) {
			
			if (!isInterrupted) new HandleErrorMessages("transferIOExceptionError", e, config, logger);
		}
	}
	
	private String readNextLine() {
		
		String lineValue = "";
		int[] lastTwoBytes = {'\0', '\0'};
		int buffer = 0;
		
		while (!(lastTwoBytes[0] == '\r' && lastTwoBytes[1] == '\n') && buffer >= 0 && !isInterrupted) {
			
			try {
				
				buffer = inputStream.read();
			}
			catch (IOException e) {
				
				if (!isInterrupted) new HandleErrorMessages("readFileInformationError", e, config, logger);
			}
			
			lastTwoBytes[0] = lastTwoBytes[1];
			lastTwoBytes[1] = buffer;
			
			if (buffer != '\r' && buffer != '\n') lineValue += (char)buffer;
		}
		
		return lineValue;
	}
	
	public void interrupt() {
		
		logger.debug("interrupt called");
		
		isInterrupted = true;
		
		super.interrupt();
		
		logger.debug("interrupt finished");
	}
}
