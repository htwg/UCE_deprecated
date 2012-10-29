/**
 * Copyright (C) 2011 Daniel Maier
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

package de.htwg_konstanz.in.uce.hp.parallel.source;

/**
 * Thrown to indicate that the desired target was not registered at the mediator.
 * @author Daniel Maier
 *
 */
public class TargetNotRegisteredException extends Exception {
	private static final long serialVersionUID = -5858672311200878838L;
	
	public TargetNotRegisteredException(String text) {
		super(text);
	}
}
