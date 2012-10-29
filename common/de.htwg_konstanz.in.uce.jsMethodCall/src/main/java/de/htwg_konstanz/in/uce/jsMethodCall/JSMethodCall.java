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

package de.htwg_konstanz.in.uce.jsMethodCall;

import java.applet.Applet;
import java.applet.AppletContext;
import java.net.URL;
import java.security.AccessController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 * Komminukationsklasse, die alle Methode zum Funktionsaufruf
 * von JavaScript Funktionen im Browser bereit stellle.
 * Wir von WHPApplet und vielen unterklassen verwendet.
 * 
 * @author Stefan Lohr
 *
 */
public class JSMethodCall implements IJSMethodCall {
	
	private Applet applet;
	private AppletContext appletContext;
	private JSObject jsObject = null;
	private Logger logger;
	
	/**
	 * Konstrukor: Applet instanz muss übergeben werden,
	 * damit der zugriff auf den browser möglich ist
	 * 
	 * @param applet Applet Instanz von WHPApplet
	 */
	public JSMethodCall(Applet applet) {
		
		this.applet = applet;
		this.appletContext = applet.getAppletContext();
		this.logger = LoggerFactory.getLogger(JSMethodCall.class);
		
		generateJSObject();
	}

	/**
	 * Methode stößt das Laden einer neuen URL
	 * in einem bestimmten Zielframe im Browser an
	 * 
	 * @param url URL die geladen werden soll
	 * @param target Zielframe
	 */
	public void showDocument(URL url, String target) {
		
		this.call("showDocument", new String[] {url.toString(), target});
		
		//appletContext.showDocument(url, target);
	}
	
	/**
	 * Methode um eine Funktion im JavaScript im Browser aufzurufen
	 * Vorsicht: Rückgabe von Werten funktioniert nur mit JSObject (jsoCall)
	 * Bei der alternativen Aufrufmethode mit urlCall gibt es keine Rückgabewerte
	 * 
	 * @param method Funktion die aufgerufen werden soll
	 * @return Object Rückgabewert der Funtkion
	 */
	public Object call(String method) {
		
		return this.call(method, new String[] {});
	}
	
	/**
	 * Methode um eine Funktion mit einem Parameter im JavaScript im Browser aufzurufen
	 * Vorsicht: Rückgabe von Werten funktioniert nur mit JSObject (jsoCall)
	 * Bei der alternativen Aufrufmethode mit urlCall gibt es keine Rückgabewerte
	 * 
	 * @param method Funktion die aufgerufen werden soll
	 * @param argument Parameter der Funktion
	 * @return Object Rückgabewert der Funtkion
	 */
	public Object call(String method, String argument) {
		
		return this.call(method, new String[] { argument });
	}
	
	/**
	 * Methode um eine Funktion mit mehreren Parameter im JavaScript im Browser aufzurufen
	 * Vorsicht: Rückgabe von Werten funktioniert nur mit JSObject (jsoCall)
	 * Bei der alternativen Aufrufmethode mit urlCall gibt es keine Rückgabewerte
	 * 
	 * @param method Funktion die aufgerufen werden soll
	 * @param arguments mehrere Parameter der Funktion
	 * @return Object Rückgabewert der Funtkion
	 */
	public Object call(String method, String[] arguments) {
		
		Object response = null;
		
		if (jsObject == null) {
			
			generateJSObject();
			
			if (jsObject == null) urlCall(method, arguments);
			else response = jsoCall(method, arguments);
		}
		else response = jsoCall(method, arguments);
		
		return response;
	}
	
	/**
	 * Interne Methode um eine Funktion mit Parameter im JavaScript im Browser aufzurufen.
	 * Bei dieser Variante sind Rückgabewerte möglich, bei urlCall() nicht.
	 * 
	 * @param method Funktion die aufgerufen werden soll
	 * @param arguments mehrere Parameter der Funktion
	 * @return Object Rückgabewert der Funtkion
	 */
	private Object jsoCall(String method, String[] arguments) {
		
		Object response = null;
		
		try {
			
			URL url = AccessController.doPrivileged(new CreateFallbackURL(method, arguments));
			
			response = jsObject.call(method, arguments);
			
			logger.debug("jsoCall: " + url.toString());
		}
		catch (JSException jse) {
			
			jse.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * Interne Methode um eine Funktion mit Parameter im JavaScript im Browser aufzurufen.
	 * Bei dieser Variante sind keine Rückgabewerte möglich, bei jsoCall() schon.
	 * 
	 * @param method Funktion die aufgerufen werden soll
	 * @param arguments mehrere Parameter der Funktion
	 */
	private void urlCall(String method, String[] arguments) {
		
		URL url = AccessController.doPrivileged(new CreateFallbackURL(method, arguments));
		
		// damit keine nachrichten verloren gehen, geht auf schnellen macs auch mit 1er ms
		try { Thread.sleep(25); }
		catch (InterruptedException e) { e.printStackTrace(); }
		
		appletContext.showDocument(url, "_self");
		
		logger.debug("urlCall: " + url.toString());
	}
	
	/**
	 * Interne Methode die Versucht ein JSObject für den Methodenaufruf zu erstellen
	 */
	private void generateJSObject() {
		
		try {
			
			jsObject = JSObject.getWindow(applet);
		}
		catch (JSException jse) {}
	}
}
