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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.security.PrivilegedAction;

public class CopyToClipboard implements PrivilegedAction<Void>, ClipboardOwner {

	private String stringContent;
	
	public CopyToClipboard(String stringContent) {
		
		this.stringContent = stringContent;
	}
	
	public Void run() {
		
		StringSelection clipboardContent = new StringSelection(stringContent);
		
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		clipboard.setContents(clipboardContent, this);
		
		return null;
	}
	
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		
	}
}
