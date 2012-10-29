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

package de.htwg_konstanz.in.uce.sendItDirect;

import java.security.AccessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.uce.jsMethodCall.IJSMethodCall;
import de.htwg_konstanz.in.uce.jsMethodCall.JSMethodCall;
import de.htwg_konstanz.in.uce.sendItDirect.client.StartFileTransfer;
import de.htwg_konstanz.in.uce.sendItDirect.server.AbortFileProviding;
import de.htwg_konstanz.in.uce.sendItDirect.server.OpenFileDialog;
import de.htwg_konstanz.in.uce.sendItDirect.server.ProvideFile;

/**
 * Main class of the JavaApplet.
 * This class will be called by Browser after loading the JavaApplet.
 * It contains the standard live cycle methods like init(), start(), stop() and destroy().
 * Additionally it provides all needed methods for the sendItDirect JavaScript application.
 * This means methods for sending and receiving files by a direct connection without changing firewall settings.
 * These functionality is realized by the TCP-Hole-Punching technic.
 * 
 * @author Stefan Lohr
 */
public class SendItDirectApplet extends java.applet.Applet {
	
	private static final long serialVersionUID = 7395560865918602390L;
	private Configuration config;
	private Logger logger;
	
	/**
	 * A LiveCycle method of the JavaApplet. It will be called after loading the JavaApplet.  
	 * In this method all parameters from JavaScript will be read and interpreted.
	 * All needed values, threads, classes, parameters will be initialized, started and set.
	 * At the end, the AppletReadyThread class will be initialized and started
	 * (This class tells the JavaScript in the Browser that the JavaApplet is ready for use).
	 */
	public void init() {
		
		// initialize all needed local variables
		String mediatorIP = null;
		Integer mediatorPort = null;
		String callbackFunction = null;
		String messageFunction = null;
		IJSMethodCall jsMethodCall = null;
		
		// generate a new logger class for logging information in this class
		logger = LoggerFactory.getLogger(SendItDirectApplet.class);
		
		// generate a new configuration class for all global settings in this application
		config = new Configuration();
		
		// generate a new jsMethodCall class for the communication with JavaScript
		jsMethodCall = new JSMethodCall(this);
		
		// get the callback function name which is set in JavaScript
		// this JavaScript function will be called after the JavaApplet is initialized
		callbackFunction = this.getParameter("callbackFunction");
		
		// get the message function name which is set in JavaScript
		// this JavaScript function will be called if an error occur or a message must be displayed
		messageFunction = this.getParameter("messageFunction");
		
		// if no callback function name is set, use a standard callback function name
		if (callbackFunction == null) callbackFunction = "sentItDirectAppletReady";
		
		// if no message function name is set, use a standard message function name
		if (messageFunction == null) messageFunction = "sentItDirectMessage";
		
		// set the message and callback function name in the configuration class
		config.messageFunction = messageFunction;
		config.callbackFunction = callbackFunction;
		
		// try to get the port and the ip address of the mediator
		try {
			
			// read the port and the ip address from parameters
			mediatorIP = this.getParameter("mediatorIP");
			String mediatorPortString = this.getParameter("mediatorPort");
			
			// if the ip address or the port number is not set, throw an exception
			if (mediatorIP == null) throw new InstantiationException("mediator ip not set");
			if (mediatorPortString == null) throw new InstantiationException("mediator port not set");
			
			// convert the string representation of the port into an integer representation
			mediatorPort = Integer.parseInt(mediatorPortString);
		}
		// if an error is thrown, catch it
		catch (Exception e) {
			
			// generate a new error which is send to the JavaScript
			new HandleErrorMessages("sendItDirectInitializationError", e, config, logger);
			
			// return premature
			return;
		}
		
		// sets the JavaApplet object in the configuration class for later use
		config.applet = this;
		
		// sets the jsMethodCall object in the configuration class for later use
		config.jsMethodCall = jsMethodCall;
		
		// sets the ip address and the port of the mediator in the configuration class
		config.mediatorIP = mediatorIP;
		config.mediatorPort = mediatorPort;
		
		// generate a new AppletReadyThread. This thread calls the callback function in
		// JavaScript and say the JavaScript with it, that the JavaApplet is ready.  
		AppletReadyThread appletReadyThread = new AppletReadyThread(config);
		
		// start the appletReadyThread
		appletReadyThread.start();
	}
	
	/**
	 * This method opens a file dialog window for file selection. It can be called from JavaScript.
	 * With the passed through callback function name, the selected file path will be set in JavaScript.
	 * The implementation of this method is in the class which is called by the same name.
	 * In this method, the class will be started as privileged, in case of security issues.
	 * 
	 * [send it direct - server sided method]
	 * 
	 * @param callbackFunction String which contains the JavaScript function name which will be called for setting file path
	 */
	public void openFileDialog(String callbackFunction) {
		
		// sets the callback function name in the config class
		config.callbackFunction = callbackFunction;
		
		// calls the 'OpenFileDialog' class with JavaApplet rights instead of JavaScript rights
		AccessController.doPrivileged(new OpenFileDialog(config));
	}
	
	/**
	 * This method is for providing the file from the first argument. It will be provided
	 * on the fileId, which is set in the function with the callback function name in the JavaScript.
	 * This method register the file on the Mediator and generates an unique fileId.
	 * Also a listener thread will be started which listens for incomming connection requests.
	 * 
	 * The implementation of this method is in the class which is called by the same name.
	 * In this method, the class will be started as privileged, in case of security issues.
	 * 
	 * [send it direct - server sided method]
	 * 
	 * @param fileSource String with the path of the file which sould be provided
	 * @param callbackFunction String with the name of the JavaScript callback function which will be called if the file is provided
	 */
	public void provideFile(String fileSource, String callbackFunction) {
		
		// sets the file source (file path) and the callback function name in the config class
		config.fileSource = fileSource;
		config.callbackFunction = callbackFunction;
		
		// calls the 'ProvideFile' class with JavaApplet rights instead of JavaScript rights
		AccessController.doPrivileged(new ProvideFile(config));
	}
	
	/**
	 * This method copies the the string of the arguments into the clipboard memory.
	 * It is called by JavaScript to copy the file providing URL into the clipboard memory.
	 * 
	 * The implementation of this method is in the class which is called by the same name.
	 * In this method, the class will be started as privileged, in case of security issues.
	 * 
	 * [send it direct - both sided method (but only used on server side)]
	 * 
	 * @param clipboardContent String with the text which should be copied to clipboard
	 */
	public void copyToClipboard(String clipboardContent) {
		
		// calls the 'CopyToClipboard' class with JavaApplet rights instead of JavaScript rights
		AccessController.doPrivileged(new CopyToClipboard(clipboardContent));
	}
	
	/**
	 * This method starts the download of a provided file. With the fileId the correct file
	 * can be selected and connected to the opponent. The callback function name is the
	 * JavaScript function which will be called for starting the download. This function
	 * is passed through the URL for download as parameter after the lokal server is started. 
	 * 
	 * The implementation of this method is in the class which is called by the same name.
	 * In this method, the class will be started as privileged, in case of security issues.
	 * 
	 * [send it direct - client sided method]
	 * 
	 * @param fileId String with the unique ID of the provided file
	 * @param callbackFunction String with the name of the JavaScript callback function
	 */
	public void startFileTransfer(String fileId, String callbackFunction) {
		
		// sets the fileId and the callback function name in the config class
		config.fileId = fileId;
		config.callbackFunction = callbackFunction;
		
		// calls the 'StartFileTransfer' class with JavaApplet rights instead of JavaScript rights
		AccessController.doPrivileged(new StartFileTransfer(config));
	}
	
	/**
	 * This method will unload all threads which are started for file providing.
	 * It is called, if the JavaApplet is unloaded or if a new file should be provided.
	 * 
	 * The implementation of this method is in the class which is called by the same name.
	 * In this method, the class will be started as privileged, in case of security issues.
	 * 
	 * [send it direct - server sided method]
	 */
	public void abortFileProviding() {
		
		// calls the 'AbortFileProviding' class with JavaApplet rights instead of JavaScript rights
		AccessController.doPrivileged(new AbortFileProviding(config));
	}
	
	/**
	 * This method will set a new callback function name.
	 * The callback function name is the name of the JavaScript function, which would be called for setting results.
	 * Especially this function is called by the JavaScript function 'fileIsProvided' to set a new callback function.
	 * 
	 * [send it direct - both sided method (but only used on client side)]
	 * 
	 * @param callbackFunction String with the name of the new JavaScript callback function
	 */
	public void setCallbackFunction(String callbackFunction) {
		
		// sets the new callback function name in the config class
		config.callbackFunction = callbackFunction;
	}
	
	/**
	 * A LiveCycle method of the JavaApplet.
	 * This method will be called on closing the browser windows / unloading of the JavaApplet.
	 * The implementation of this method is in the class called 'AbortFileProviding'.
	 * In this method, the class will be started as privileged, in case of security issues.
	 * The class will unload all threads which are started for file providing.
	 */
	public void destroy() {
		
		// calls the 'AbortFileProviding' class with JavaApplet rights instead of JavaScript rights
		AccessController.doPrivileged(new AbortFileProviding(config));
	}
	
	/**
	 * A LiveCycle method of the JavaApplet.
	 * Because of the different comportment of the different browsers, in this method nothing will be done.
	 * Normaly, this method will be called on visual loading of the JavaApplet.
	 */
	public void start() {
		
		// do nothing
	}
	
	/**
	 * A LiveCycle method of the JavaApplet.
	 * Because of the different comportment of the different browsers, in this method nothing will be done.
	 * Normaly, this method will be called on visual leaving of the JavaApplet.
	 */
	public void stop() {
		
		// do nothing
	}
}
