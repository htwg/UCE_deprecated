/**
 * Copyright (C) 2012 HTWG Konstanz, Oliver Haase
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
package de.htwg_konstanz.rmi.utils;

import java.awt.HeadlessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;


public class RegistryBrowser  {
	
	private Registry registry;
	
	public RegistryBrowser(String host) throws RemoteException {
		registry = LocateRegistry.getRegistry(host);		
	}
	
	private Remote chooseObj() throws RemoteException, HeadlessException, NotBoundException {		
		String[] names = registry.list();	

		if (names.length == 0) {
			JOptionPane.showMessageDialog(null, "no objects listed.");
			return null;
		}
		
		String name = (String) JOptionPane.showInputDialog(null, 
				"select RMI server object", 
				"Server Object Selection",
				JOptionPane.INFORMATION_MESSAGE, 
				null, 
				names, 
				null);
		
		if ( name == null) {
			return null;
		}

		return registry.lookup(name);
		
	}
	
	
	private Method chooseMethod(Remote obj) throws AccessException, RemoteException, NotBoundException {
		Class<?> c = obj.getClass();
		Class<?>[] interfaces = c.getInterfaces();
		List<Method> methods = new Vector<Method>();
		for ( Class<?> iface : interfaces ) {
			methods.addAll(Arrays.asList(iface.getMethods()));
		}
		return (Method) JOptionPane.showInputDialog(null, 
				"select method", 
				"Method Selection",
				JOptionPane.INFORMATION_MESSAGE, 
				null, 
				methods.toArray(), 
				null);
	}
	
	private void invokeMethod(Remote obj, Method m) throws IllegalArgumentException, 
			IllegalAccessException, InvocationTargetException {
		Class<?>[] paramTypes = m.getParameterTypes();
		Object[] params = new Object[paramTypes.length];
		
		JOptionPane.showMessageDialog(null, m.invoke(obj, params));
	}
	
	
	public void run() throws RemoteException, NotBoundException, IllegalArgumentException, 
			IllegalAccessException, InvocationTargetException {
		Remote obj = chooseObj();
		System.out.println(obj);
		if ( obj != null ) {
			Method m = chooseMethod(obj);
			if ( m != null ) {
				invokeMethod(obj, m);
			}
		}
	}
		
	
	public static void main(String[] args) throws RemoteException, NotBoundException, 
			IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		System.setProperty("java.security.policy", RegistryBrowser.class
		.getClassLoader().getResource("resources/client.policy")
		.toExternalForm());
		
		System.setSecurityManager(new SecurityManager());
		
		String host = ( args.length == 0 ) ? "141.37.121.130" : args[0];
		
		RegistryBrowser browser = new RegistryBrowser(host);
		browser.run();

	}
	
}
