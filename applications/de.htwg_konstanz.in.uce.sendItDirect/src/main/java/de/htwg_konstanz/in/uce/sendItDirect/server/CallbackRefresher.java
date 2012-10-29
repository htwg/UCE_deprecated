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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import de.htwg_konstanz.in.uce.sendItDirect.Configuration;

public class CallbackRefresher implements ActionListener {

	private long fileSize = 0;
	private long startTime = 0;
	private String userId = "";
	private Configuration config = null;
	
	public CallbackRefresher(long fileSize, long startTime, String userId, Configuration config) {
		
		this.fileSize = fileSize;
		this.startTime = startTime;
		this.userId = userId;
		this.config = config;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		long millis = 0, time = 0;
		float speed = 0, percent = 0, left = 0;
		long currentTime = System.currentTimeMillis();
		DecimalFormat decimalFormat = new DecimalFormat("#.0");
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		
		time = (currentTime - startTime) / 1000;
		left = (((float)fileSize) - ((float)config.transferCount.get(userId)))  / 1024f;
		
		percent = (((float)config.transferCount.get(userId)) / ((float)fileSize)) * 100f;
		speed = (((float)config.transferCount.get(userId)) / ((float)time)) / 1024f;
		millis = Math.round((left / ((float)speed)) * 1000);
		
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
		
		String _percent = decimalFormat.format(percent);
		String _date = dateFormat.format(new Date(millis));
		String _speed = decimalFormat.format(speed);
		
		String[] params = { userId, _percent, _date, _speed };
		
		config.jsMethodCall.call(config.callbackFunction, params);
	}
}
