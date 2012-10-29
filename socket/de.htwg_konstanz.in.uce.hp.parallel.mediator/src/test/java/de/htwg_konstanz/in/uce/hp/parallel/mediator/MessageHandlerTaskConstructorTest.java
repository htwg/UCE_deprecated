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

package de.htwg_konstanz.in.uce.hp.parallel.mediator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Socket;

import org.junit.Before;
import org.junit.Test;

import de.htwg_konstanz.in.uce.hp.parallel.mediator.MessageHandlerTask;

public class MessageHandlerTaskConstructorTest {
	
	private Socket mockedSocket;
	
	@Before
	public void setUP() {
		mockedSocket = mock(Socket.class);
		when(mockedSocket.isClosed()).thenReturn(false);
		when(mockedSocket.isConnected()).thenReturn(true);
	}

	@Test ( expected = IllegalArgumentException.class )
	public void testConstructorNotConnected() {
		when(mockedSocket.isConnected()).thenReturn(false);
		new MessageHandlerTask(mockedSocket);
	}
	
	@Test ( expected = IllegalArgumentException.class )
	public void testConstructorClosed() {
		when(mockedSocket.isClosed()).thenReturn(true);
		new MessageHandlerTask(mockedSocket);
	}
	
	@Test
	public void testConstructorSuccess() {
		new MessageHandlerTask(mockedSocket);
	}
}
