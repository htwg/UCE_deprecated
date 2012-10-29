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

package de.htwg_konstanz.in.uce.hp.parallel.mediator_browser;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.junit.Test;

import de.htwg_konstanz.in.uce.hp.parallel.mediator_browser.MediatorBrowser;

public class MediatorBrowserConstructorAndSetterTest {

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorPortOutOfRangeToHigh() throws UnknownHostException {
        new MediatorBrowser(InetAddress.getByName("192.168.7.4"), 65536);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorPortOutOfRangeNegative() throws UnknownHostException {
        new MediatorBrowser(InetAddress.getByName("192.168.7.4"), -1);
    }

    @Test
    public void testConstructor() throws UnknownHostException {
        new MediatorBrowser(InetAddress.getByName("192.168.7.4"), 1234);
    }

    @Test
    public void testConstructor1() throws UnknownHostException {
        new MediatorBrowser(new InetSocketAddress(InetAddress.getByName("192.168.7.4"), 1234));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor1MediatorAddressIsNull() throws UnknownHostException {
        new MediatorBrowser(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetSocketFactoryNull() throws UnknownHostException {
        MediatorBrowser browser = new MediatorBrowser(InetAddress.getByName("192.168.7.4"), 1234);
        browser.setSocketFactory(null);
    }

    @Test
    public void testSetSocketFactory() throws UnknownHostException {
        MediatorBrowser browser = new MediatorBrowser(InetAddress.getByName("192.168.7.4"), 1234);
        browser.setSocketFactory(SocketFactory.getDefault());
    }
}
