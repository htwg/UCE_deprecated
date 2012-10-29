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
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import de.htwg_konstanz.in.connectivity_framework.AbstractSourceMock;
import de.htwg_konstanz.in.connectivity_framework.Connection;
import de.htwg_konstanz.in.uce.hp.parallel.mediator_browser.MediatorBrowser;
import de.htwg_konstanz.in.uce.hp.parallel.source.HolePunchingSource;
import de.htwg_konstanz.in.uce.hp.parallel.source.TargetNotRegisteredException;

public class SourceMock extends AbstractSourceMock {

    private static final long WAIT_TIME_FOR_TARGET = 300;
    private final HolePunchingSource source = new HolePunchingSource();
    private final String id;
    private final SocketAddress mediatorAddress;

    public SourceMock(String scenarioDescription, String id, SocketAddress mediatorAddress) {
        super(scenarioDescription);
        this.id = id;
        this.mediatorAddress = mediatorAddress;
    }

    public SourceMock(String scenarioDescription, String id, SocketAddress mediatorAddress,
            int simultaneousConnections) {
        super(scenarioDescription, simultaneousConnections);
        this.id = id;
        this.mediatorAddress = mediatorAddress;
    }

    @Override
    protected boolean waitForTarget() {
        MediatorBrowser browser = new MediatorBrowser(mediatorAddress);
        long start = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime());
        try {
            while ((TimeUnit.NANOSECONDS.toSeconds(System.nanoTime()) - start) <= WAIT_TIME_FOR_TARGET
                    && !browser.getSetOfRegisteredTargets().contains(id)) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return browser.getSetOfRegisteredTargets().contains(id);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected Connection connect() throws IOException {
        Socket s;
        try {
            s = source.getSocket(id, mediatorAddress);
        } catch (TargetNotRegisteredException e) {
            throw new IOException(e);
        }
        return new SocketConnection(s);
    }

    public static void main(String[] args) {
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
        o = new Option("d", "description", true, "scenario description");
        o.setRequired(true);
        options.addOption(o);
        o = new Option("sin", "numberOfsimultaneousConnections", true, "number of simultaneous "
                + "connections");
        o.setRequired(false);
        options.addOption(o);
        o = new Option("si", "simultaneousConnections", false, "simultaneous connections test");
        o.setRequired(false);
        options.addOption(o);
        o = new Option("su", "successiveConnections", false, "successive connections test");
        o.setRequired(false);
        options.addOption(o);
        o = new Option("r", "realisticConnections", false, "realistic connections test");
        o.setRequired(false);
        options.addOption(o);

        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("SourceMock", options);
            return;
        }

        String mediatorIP = cmd.getOptionValue("mediatorIP");
        String mediatorPort = cmd.getOptionValue("mediatorPort");
        String description = cmd.getOptionValue("description");
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

        if (cmd.hasOption("numberOfsimultaneousConnections")) {
            int simConnections = Integer.parseInt(cmd
                    .getOptionValue("numberOfsimultaneousConnections"));
            new SourceMock(description, targetId, mediatorSocketAddress, simConnections).runTests(
                    cmd.hasOption("simultaneousConnections"),
                    cmd.hasOption("successiveConnections"), cmd.hasOption("realisticConnections"));
        } else {
            new SourceMock(description, targetId, mediatorSocketAddress).runTests(
                    cmd.hasOption("simultaneousConnections"),
                    cmd.hasOption("successiveConnections"), cmd.hasOption("realisticConnections"));
        }
    }
}
