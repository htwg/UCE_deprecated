package de.htwg_konstanz.in.hp.sequential.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.htwg_konstanz.in.hp.sequential.target.HolePunchingTarget;

public class Target {

    private static final String WRONG_ARGS = "Wrong arguments (MediatorAddress, " +
    		"MediatorRegisterPort, MediatorConnectionHandlePort, RegisterIntervall and TargetID " +
    		"expected)";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            throw new IllegalArgumentException(WRONG_ARGS);
        }

        InetSocketAddress mediatorRegisterSocketAddress;
        InetSocketAddress mediatorConnectionHandleSocketAddress;
        long registerIntervall;
        String targetID = args[4];

        try {
            int mediatorRegisterPort = Integer.parseInt(args[1]);
            int mediatorConnectionHandlePort = Integer.parseInt(args[2]);
            registerIntervall = Long.parseLong(args[3]);
            mediatorRegisterSocketAddress = new InetSocketAddress(args[0], mediatorRegisterPort);
            mediatorConnectionHandleSocketAddress = new InetSocketAddress(args[0], 
                    mediatorConnectionHandlePort);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_ARGS, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(WRONG_ARGS, e);
        }

        HolePunchingTarget target = new HolePunchingTarget(mediatorRegisterSocketAddress, 
                mediatorConnectionHandleSocketAddress, targetID, registerIntervall);
        target.start();
        while(true) {
            Socket s = target.accept();
            System.out.println(s);            
        }
    }

}
