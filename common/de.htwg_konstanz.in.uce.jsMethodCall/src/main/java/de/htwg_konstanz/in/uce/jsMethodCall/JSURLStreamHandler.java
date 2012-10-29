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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Klasse die es ermöglicht eine gültige URL mit JavaScript Aufruf zu erstellen
 * Enthält eigentlich keine Implementierung, nur eine URLConnection die nichts macht
 * das reicht um eine JavaScript Funktion aufzurufen
 * 
 * @author Stefan Lohr
 *
 */
public class JSURLStreamHandler extends URLStreamHandler {
	
	protected URLConnection openConnection(URL url) throws IOException {
		
		URLConnection urlConnection = new URLConnection(url) {
			
			public void connect() throws IOException {}
		};
		
		return urlConnection;
	}
}
