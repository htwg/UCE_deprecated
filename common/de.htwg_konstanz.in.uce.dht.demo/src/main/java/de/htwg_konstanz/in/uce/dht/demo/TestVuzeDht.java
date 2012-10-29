/**
 * 
 */
package de.htwg_konstanz.in.uce.dht.demo;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.dht.dht_access.UceDht;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtVuzeAdapter;

/**
 * @author tzink
 *
 */
public final class TestVuzeDht {
	public static void main(String[] args) throws DHTTransportException, InterruptedException {
		long start, runtime;
		UceDht dht = new UceDhtVuzeAdapter(6892);
        
		start = System.currentTimeMillis();
        dht.bootstrap();
        System.out.println("Bootstrapped after " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " secs");
        
        for (int i=0; i<100; i++) {
        	runtime = System.currentTimeMillis();
        	System.out.println("##### Loop " + i + " #####");
        String key = "TestVuzeDht";
        Random rnd = new Random();
        byte[] value = new byte[1024];
        rnd.nextBytes(value);
        
        start = System.currentTimeMillis();
        dht.put(key, value);
        System.out.println("Item put after " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " secs");

        start = System.currentTimeMillis();
        byte[] returned = dht.get(key);
        System.out.println("Item retreived after " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " secs");
        
        System.out.println("Items equal: " + Arrays.equals(value, returned));
        	System.out.println("##### Total loop runtime " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - runtime) + " secs #####");
        }
	}
}
