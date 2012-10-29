package de.htwg_konstanz.in.uce.dht.demo;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jmule.core.JMuleCoreException;
import org.jmule.core.configmanager.ConfigurationManagerException;

import de.htwg_konstanz.in.uce.dht.dht_access.UceDht;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtKadAdapter;

public final class TestKadDht {
	public static void main(String[] args) throws JMuleCoreException, ConfigurationManagerException, InterruptedException {
		long start;
		UceDht dht = new UceDhtKadAdapter(4665);
        
		start = System.currentTimeMillis();
        dht.bootstrap();
        System.out.println("Bootstrapped after " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " secs");
        
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
	}
}
