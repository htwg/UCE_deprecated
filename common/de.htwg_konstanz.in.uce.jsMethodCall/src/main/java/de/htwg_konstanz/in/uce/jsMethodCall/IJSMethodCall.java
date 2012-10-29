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

import java.net.URL;

/**
 * Interface von JSMethodCall
 * 
 * @author Stefan Lohr
 *
 */
public interface IJSMethodCall {
	
	public void showDocument(URL url, String target);
	public Object call(String method);
	public Object call(String method, String argument);
	public Object call(String method, String[] arguments);
}
