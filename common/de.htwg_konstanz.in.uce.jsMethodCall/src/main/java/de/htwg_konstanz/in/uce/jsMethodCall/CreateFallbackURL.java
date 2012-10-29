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

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivilegedAction;

/**
 * Klasse die eine gültige URL zurück liefert, mit der die
 * übergebene Methode mit den Parametern aufgerufen werden kann
 * 
 * @author Stefan Lohr
 *
 */
public class CreateFallbackURL implements PrivilegedAction<URL> {
	
	private String method = null;
	private String[] arguments = null;
	private JSURLStreamHandler jsurlStreamHandler;
	
	/**
	 * Erstellt eine URL die die übergeben Methode mit den übergebenen Argumenten aufruft
	 * @param method Name der gewünschten Methode
	 * @param arguments StringArray der gewünschten Argumente
	 */
	public CreateFallbackURL(String method, String[] arguments) {
		
		this.method = method;
		this.arguments = arguments;
		
		this.jsurlStreamHandler = new JSURLStreamHandler();
	}
	
	public URL run() {
		
		String parameter = implode(this.arguments, ", ");
		String call = method + "(" + parameter + ");";
		
		try {
			
			return new URL("javascript", "", -1, call, this.jsurlStreamHandler);
			
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
			
			return null;
		}
	}
	
	/**
	 * Methode um ein StringArray in einen String mit gegebenen delimiter zu konvertieren
	 * @param stringArray Array aus Strings
	 * @param delimiter Verbindungszeichen zwischen den Strings
	 * @return String (zusammengesetzt)
	 */
	private String implode(String[] stringArray, String delimiter) {
		
		String returnValue = "";
		
		for (int position = 0; position < stringArray.length; position++) {
			
			if (position != 0) returnValue += delimiter;
			
			returnValue += "'" + stringArray[position] + "'";
		}
		
		return returnValue;
	}
}
