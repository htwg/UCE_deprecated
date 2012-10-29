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

package de.htwg_konstanz.in.uce.hp.parallel.integration_test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.htwg_konstanz.in.connectivity_framework.AbstractTargetMock;
import de.htwg_konstanz.in.connectivity_framework.Connection;
import de.htwg_konstanz.in.uce.hp.parallel.target.HolePunchingTarget;

public class TargetMock extends AbstractTargetMock {

    private static final Logger logger = LoggerFactory.getLogger(TargetMock.class);
    private final HolePunchingTarget target;
    private volatile Thread acceptingThread;

    public TargetMock(SocketAddress mediatorAddress, String regKey) throws IOException {
        target = new HolePunchingTarget(mediatorAddress, regKey);
        try {
            target.start();
        } catch (IllegalStateException e) {
            logger.error("IllegalStateException: {}", e);
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            logger.error("IOException: {}", e);
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected Connection acceptConnection() throws IOException {
        try {
            acceptingThread = Thread.currentThread();
            return new SocketConnection(target.accept());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    protected void stopAccept() throws IOException {
        target.stop();
        if (acceptingThread != null) {
            acceptingThread.interrupt();
        }
    }

    public static void main(String[] args) throws IOException {
        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        Option o = new Option("m", "mediatorIP", true, "mediator ip");
        o.setRequired(true);
        options.addOption(o);
        o = new Option("p", "mediatorPort", true, "mediator port");
        o.setRequired(true);
        options.addOption(o);
        o = new Option("t", "targetId", true, "target ID");
        o.setRequired(true);
        options.addOption(o);

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("TargetMock", options);
            return;
        }

        String mediatorIP = cmd.getOptionValue("mediatorIP");
        String mediatorPort = cmd.getOptionValue("mediatorPort");
        String targetId = cmd.getOptionValue("targetId");

        InetSocketAddress mediatorSocketAddress;

        try {
            int port = Integer.parseInt(mediatorPort);
            mediatorSocketAddress = new InetSocketAddress(mediatorIP, port);
        } catch (NumberFormatException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SourceMock", options);
            return;
        } catch (IllegalArgumentException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SourceMock", options);
            return;
        }

        new TargetMock(mediatorSocketAddress, targetId).start();
    }
}
