package de.htwg_konstanz.in.hp.sequential.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.htwg_konstanz.in.hp.sequential.source.HolePunchingSource;

public class Source {
    
    private static final String WRONG_ARGS = "Wrong arguments (MediatorAddress, " +
    		"MediatorConnectionHandlePort and TargetID expected)";

	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
	    if (args.length != 3) {
            throw new IllegalArgumentException(WRONG_ARGS);
        }

	    InetSocketAddress mediatorSocketAddress;
        String targetID = args[2];

        try {
            int mediatorPort = Integer.parseInt(args[1]);
            mediatorSocketAddress = new InetSocketAddress(args[0], mediatorPort);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_ARGS, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(WRONG_ARGS, e);
        }
		HolePunchingSource source = new HolePunchingSource();
		Socket s = source.getSocket(targetID, mediatorSocketAddress);
		System.out.println("Got socket:" + s);
	}

}
