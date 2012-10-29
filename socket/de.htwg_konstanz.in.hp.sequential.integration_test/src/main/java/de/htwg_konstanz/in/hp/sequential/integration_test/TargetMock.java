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

import de.htwg_konstanz.in.connectivity_framework.AbstractTargetMock;
import de.htwg_konstanz.in.connectivity_framework.Connection;
import de.htwg_konstanz.in.hp.sequential.target.HolePunchingTarget;

public class TargetMock extends AbstractTargetMock {

    private final HolePunchingTarget target;
    private volatile Thread acceptingThread;

    public TargetMock(SocketAddress mediatorRegisterAddress,
            SocketAddress mediatorConnectionRequestAddress, String regKey, long regIntervall)
            throws IOException {
        target = new HolePunchingTarget(mediatorRegisterAddress, mediatorConnectionRequestAddress,
                regKey, regIntervall);
        target.start();
    }

    @Override
    protected Connection acceptConnection() throws IOException {
        acceptingThread = Thread.currentThread();
        return new SocketConnection(target.accept());
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
        o = new Option("r", "mediatorRegisterPort", true, "mediator register port");
        o.setRequired(true);
        options.addOption(o);
        o = new Option("c", "mediatorConReqPort", true, "mediator connection request port");
        o.setRequired(true);
        options.addOption(o);
        o = new Option("i", "registerInterval", true, "register interval");
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
        String mediatorRegisterPort = cmd.getOptionValue("mediatorRegisterPort");
        String mediatorConReqPort = cmd.getOptionValue("mediatorConReqPort");
        String registerInterval = cmd.getOptionValue("registerInterval");
        String targetId = cmd.getOptionValue("targetId");

        InetSocketAddress mediatorRegisterSocketAddress;
        InetSocketAddress mediatorConnectionHandleSocketAddress;
        long interval;

        try {
            int registerPort = Integer.parseInt(mediatorRegisterPort);
            int connectionHandlePort = Integer.parseInt(mediatorConReqPort);
            interval = Long.parseLong(registerInterval);
            mediatorRegisterSocketAddress = new InetSocketAddress(mediatorIP, registerPort);
            mediatorConnectionHandleSocketAddress = new InetSocketAddress(mediatorIP,
                    connectionHandlePort);
        } catch (NumberFormatException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("TargetMock", options);
            return;
        } catch (IllegalArgumentException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("TargetMock", options);
            return;
        }

        new TargetMock(mediatorRegisterSocketAddress, mediatorConnectionHandleSocketAddress,
                targetId, interval).start();
    }
}
