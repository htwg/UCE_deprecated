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

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.sendItDirect.Configuration;
import de.htwg_konstanz.in.uce.sendItDirect.HandleErrorMessages;

public class FileSender extends Thread {
	
	Logger logger;
	Configuration config;
	Timer callbackRefreshTimer;
	OutputStream outputStream;
	FileInputStream fileInputStream;
	String userId;
	
	public FileSender(String userId, Configuration config) {
		
		this.userId = userId;
		this.config = config;
		this.logger = LoggerFactory.getLogger(this.getClass());
		
		List<FileSender> fileSenderList = config.fileSenderListMap.get(config.fileId);
		
		if (fileSenderList == null) fileSenderList = new ArrayList<FileSender>();
		
		fileSenderList.add(this);
		
		config.fileSenderListMap.put(config.fileId, fileSenderList);
	}
	
	public void run() {
		
		sendFile();
	}
	
	private void sendFile() {
		
		ActionListener callbackRefresher = null;
		
		File file = new File(config.fileSource);
		
		String fileName = file.getName();
		long fileSize = file.length();
		
		try {
			
			fileInputStream = new FileInputStream(file);
			
			outputStream = config.receiverSocket.get(userId).getOutputStream();
			
			outputStream.write((fileName + "\r\n").getBytes());
			outputStream.write((fileSize + "\r\n").getBytes());
			outputStream.flush();
			
			long startTime = System.currentTimeMillis();
			byte[] buffer = new byte[8192];
			int readCount = 0;
			config.transferCount.put(userId, (long) 0);
			
			callbackRefresher = new CallbackRefresher(fileSize, startTime, userId, config);
			callbackRefreshTimer = new Timer(1000, callbackRefresher);
			
			callbackRefreshTimer.start();
			
			while (((readCount = fileInputStream.read(buffer, 0, 8192)) > 0) && !isInterrupted()) {
								
				outputStream.write(buffer, 0, readCount);
				outputStream.flush();
				
				config.transferCount.put(userId, config.transferCount.get(userId) + readCount);
			}
			
			callbackRefreshTimer.stop();
			fileInputStream.close();
			
			// last call for 100 percent
			callbackRefresher.actionPerformed(null);
			
			// Close output stream and wait until opponent closes his output stream
			config.receiverSocket.get(userId).shutdownOutput();
			config.receiverSocket.get(userId).getInputStream().read();

			// closes the socket after both streams are closed
			config.receiverSocket.get(userId).shutdownInput();
			config.receiverSocket.get(userId).close();
		}
		catch (FileNotFoundException e) {
			
			if (!isInterrupted()) new HandleErrorMessages("fileNotFoundExceptionError", e, config, logger);
		}
		catch (IOException e) {
			
			if (!isInterrupted()) new HandleErrorMessages("transferIOExceptionError", e, config, logger);
		}
		
		if (callbackRefreshTimer.isRunning()) callbackRefreshTimer.stop();
	}
	
	public void interrupt() {
		
		super.interrupt();
		
		callbackRefreshTimer.stop();
		
		try {
			
			outputStream.close();
			fileInputStream.close();
		}
		catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
