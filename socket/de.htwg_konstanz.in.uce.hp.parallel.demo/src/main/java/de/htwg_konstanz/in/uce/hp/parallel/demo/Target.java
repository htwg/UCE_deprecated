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

package de.htwg_konstanz.in.uce.hp.parallel.demo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.htwg_konstanz.in.uce.hp.parallel.target.HolePunchingTarget;

public class Target {

    private static final String WRONG_ARGS = "Wrong arguments (MediatorAddress, " +
    		"MediatorPort and TargetID expected)";

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 3) {
            throw new IllegalArgumentException(WRONG_ARGS);
        }

        InetSocketAddress mediatorRegisterSocketAddress;
        String targetID = args[2];

        try {
            int mediatorPort = Integer.parseInt(args[1]);
            mediatorRegisterSocketAddress = new InetSocketAddress(args[0], mediatorPort);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(WRONG_ARGS, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(WRONG_ARGS, e);
        }

        HolePunchingTarget target = new HolePunchingTarget(mediatorRegisterSocketAddress, targetID);
        target.start();
     //   while(true) {
            Socket s;
            try {
                s = target.accept();
                System.out.println(s);  
                target.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
  //  }

}
