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

import java.awt.Container;
import java.awt.FileDialog;
import java.awt.Frame;
import java.security.PrivilegedAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.sendItDirect.Configuration;

public class OpenFileDialog implements PrivilegedAction<Void> {
	
	private Logger logger;
	private Configuration config;
	
	public OpenFileDialog(Configuration config) {
		
		this.config = config;
		this.logger = LoggerFactory.getLogger(this.getClass());
		
		this.logger.info("open file dialog");
	}
	
	public Void run() {
		
		String fileSource = getFileSourceWithDialog();
		
		config.jsMethodCall.call(config.callbackFunction, fileSource);
		
		return null;
	}
	
	private String getFileSourceWithDialog() {

		Frame frame = findParentFrame();

		FileDialog fileDialog = new FileDialog(frame);
		fileDialog.setVisible(true);
		
		if (fileDialog.getFile() == null) return null;
		else return fileDialog.getDirectory() + fileDialog.getFile();
	}
	
	private Frame findParentFrame() {

		Container container = config.applet;

		while (container != null) {

			if (container instanceof Frame) return (Frame) container;

			container = container.getParent();
		}

		return (Frame) null;
	}
}
