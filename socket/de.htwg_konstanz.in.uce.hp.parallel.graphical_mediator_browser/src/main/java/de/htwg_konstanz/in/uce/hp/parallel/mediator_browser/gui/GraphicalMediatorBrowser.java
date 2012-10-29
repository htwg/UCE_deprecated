package de.htwg_konstanz.in.uce.hp.parallel.mediator_browser.gui;

import java.net.InetSocketAddress;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import de.htwg_konstanz.in.uce.hp.parallel.mediator_browser.MediatorBrowser;

public class GraphicalMediatorBrowser {

    public static void main(String[] args) throws Exception {
        String mediatorEndpoint = JOptionPane.showInputDialog("IP:Port of mediator",
                "141.37.121.124:10200");
        try {
            String[] tokens = mediatorEndpoint.split(":");
            MediatorBrowser mb = new MediatorBrowser(new InetSocketAddress(tokens[0],
                    Integer.valueOf(tokens[1])));
            MediatorBrowserFrame gmb = new MediatorBrowserFrame(mb);
            gmb.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            gmb.setLocationRelativeTo(gmb);
            gmb.setVisible(true);            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getClass().getCanonicalName() + " occured", "Error", JOptionPane.ERROR_MESSAGE);
            throw e;
        }
    }

}
