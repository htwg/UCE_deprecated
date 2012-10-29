/**
 * 
 */
package de.htwg_konstanz.in.uce.rmi.registry.p2p.demo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.jmule.core.JMuleCoreException;
import org.jmule.core.configmanager.ConfigurationManagerException;

import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.dht.dht_access.UceDht;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtKadAdapter;
import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtVuzeAdapter;

/**
 * @author zink
 *
 */
public class PeerCmd {
	private static String DEFAULT_ADAPTER = "vuze";
	private static String DEFAULT_MEDIATOR_IP = "134.34.165.172"; //"141.37.121.124"; // "134.34.165.172"
	private static int DEFAULT_MEDIATOR_PORT = 9090; // 10200; // 9090;
	private static int DEFAULT_VUZE_PORT = 0;
	private static int DEFAULT_KAD_PORT = 4683;
	
	String type = DEFAULT_ADAPTER;
	int port = DEFAULT_VUZE_PORT;
	UceDht adapter;
	InetSocketAddress mediator;
	int n = 1;
	String file;
	
	private PeerCmd (String adapter, int adapter_port, String ip, int port, int n, String file) throws JMuleCoreException, ConfigurationManagerException, DHTTransportException {
		this.type = adapter;
		this.port = adapter_port;
		this.adapter = selectAdapter(adapter, adapter_port);
		this.mediator = new InetSocketAddress(ip,port);
		this.n = n;
		this.file = file;
	}
	
	private static void usage() {
		System.err.println("options: [-a {vuze, kad}] [-p port] [-m <mediator ip>:<mediator port>] [-n niterations]");
		System.err.println("default: " + DEFAULT_ADAPTER + " " + DEFAULT_VUZE_PORT + " " + DEFAULT_MEDIATOR_IP + ":" + DEFAULT_MEDIATOR_PORT);
		System.exit(0);
	}
	
	public static PeerCmd parseArgs (String[] args) throws JMuleCoreException, ConfigurationManagerException, DHTTransportException {
		String adapter = DEFAULT_ADAPTER;
		int adapter_port = -1;
		String mediator_ip = DEFAULT_MEDIATOR_IP;
		int mediator_port = DEFAULT_MEDIATOR_PORT;
		int n = 1;
		String file = "";
		
		int i=0;
		String arg = "";
		while (i < args.length && args[i].startsWith("-")) {
            arg = args[i++];
            if (arg.equals("--help") || arg.equals("-h")) {
                usage();
            } else if (arg.equals("-a")) /* adapter */ {
            	if (i < args.length) adapter = args[i++];
            	else usage();
            } else if (arg.equals("-p")) /* adapter port */ {
            	if (i < args.length) adapter_port = Integer.parseInt(args[i++]);
            	else usage();
            } else if (arg.equals("-m")) /* mediator */ {
            	if (i < args.length) {
            		String[] tmp = args[i++].split(":");
            		if (tmp.length != 2) usage();
            		mediator_ip = tmp[0];
            		mediator_port = Integer.parseInt(tmp[1]);
            	} else usage();
            } else if (arg.equals("-n")) {
            	if (i < args.length) n = Integer.parseInt(args[i++]);
            	else usage();
            } else if (arg.equals("-f")) {
            	file = args[i++];
            }
		}
		if (adapter_port == -1) {
			if (adapter.equals("vuze")) adapter_port = DEFAULT_VUZE_PORT;
			if (adapter.equals("kad")) adapter_port = DEFAULT_KAD_PORT;
		}

		return new PeerCmd(adapter, adapter_port, mediator_ip, mediator_port, n, file);
	}
	
	private static UceDht selectAdapter (String s, int p) throws DHTTransportException, JMuleCoreException, ConfigurationManagerException {
		UceDht adapter = null;
		if(s.equals("vuze")) {
			adapter = new UceDhtVuzeAdapter(p);
		} else if (s.equals("kad")) {
			adapter = new UceDhtKadAdapter(p);
		}
		return adapter;
	}
	
	public UceDht adapter() throws JMuleCoreException, ConfigurationManagerException, DHTTransportException {
		return selectAdapter(this.type, this.port);
	}
	
	public static boolean writeToFile(BufferedWriter out, String str) {
		if (out == null) return false;
		try {
			out.write(str);
			out.flush();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
}
