package de.htwg_konstanz.in.hp.sequential.integration_test;

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

import de.htwg_konstanz.in.connectivity_framework.AbstractSourceMock;
import de.htwg_konstanz.in.connectivity_framework.Connection;
import de.htwg_konstanz.in.hp.sequential.source.HolePunchingSource;

public class SourceMock extends AbstractSourceMock {

    private final HolePunchingSource source = new HolePunchingSource();
    private final String id;
    private final SocketAddress mediatorAddress;

    public SourceMock(String scenarioDescription, String id, SocketAddress mediatorAddress) {
        super(scenarioDescription);
        this.id = id;
        this.mediatorAddress = mediatorAddress;
    }

    @Override
    protected Connection connect() throws IOException {
        return new SocketConnection(source.getSocket(id, mediatorAddress));
    }

    /**
     * @param args
     */
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

        new SourceMock(description, targetId, mediatorSocketAddress).runTests(
                cmd.hasOption("simultaneousConnections"), cmd.hasOption("successiveConnections"),
                cmd.hasOption("realisticConnections"));

    }
}
