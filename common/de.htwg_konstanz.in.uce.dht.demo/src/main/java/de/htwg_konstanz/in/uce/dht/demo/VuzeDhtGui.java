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
package de.htwg_konstanz.in.uce.dht.demo;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.gudy.azureus2.core3.util.DisplayFormatters;

import com.aelitis.azureus.core.dht.DHT;
import com.aelitis.azureus.core.dht.control.DHTControlActivity;
import com.aelitis.azureus.core.dht.control.DHTControlListener;
import com.aelitis.azureus.core.dht.db.DHTDBStats;
import com.aelitis.azureus.core.dht.router.DHTRouterStats;
import com.aelitis.azureus.core.dht.transport.DHTTransportException;

import de.htwg_konstanz.in.uce.dht.dht_access.UceDhtVuzeAdapter;

public class VuzeDhtGui extends JFrame {

    private static final long serialVersionUID = -8922035767537827137L;
    private final UceDhtVuzeAdapter dhtAdapter;
    private final DHT dht;

    public VuzeDhtGui() throws DHTTransportException {
        dhtAdapter = new UceDhtVuzeAdapter(4567);
        dhtAdapter.bootstrap();
        dht = dhtAdapter.getDHT();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Vuze DHT Client");
        setLocationRelativeTo(null);
        JPanel dataPanel = createDataPanel();
        JPanel inputPanel = createInputPanel();
        JComponent activityPanel = createActivityPane();
        setSize(365, 320);
        getContentPane().setLayout(new FlowLayout());
        getContentPane().add(dataPanel, 0);
        getContentPane().add(inputPanel, 1);
        getContentPane().add(activityPanel, 2);
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {

            public void run() {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        getContentPane().remove(0);
                        getContentPane().add(createDataPanel(), 0);
                        getContentPane().validate();
                    }

                });

            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    private JPanel createDataPanel() {
        JPanel dataPanel = new JPanel();
        GridLayout gridLayout = new GridLayout(4, 3);
        dataPanel.setLayout(gridLayout);
        JLabel label = new JLabel();
        label.setText("Uptime: "
                + (TimeUnit.MILLISECONDS.toSeconds(dht.getControl().getStats().getRouterUptime()) / 60));
        dataPanel.add(label);

        label = new JLabel();
        label.setText("Users: " + dht.getControl().getStats().getEstimatedDHTSize());
        dataPanel.add(label);

        label = new JLabel();
        label.setText("Rendezvous: " + dht.getNATPuncher().operational());
        dataPanel.add(label);

        label = new JLabel();
        label.setText("Contacts: "
                + dht.getRouter().getStats().getStats()[DHTRouterStats.ST_CONTACTS]);
        dataPanel.add(label);

        label = new JLabel();
        label.setText("Replacements: "
                + dht.getRouter().getStats().getStats()[DHTRouterStats.ST_REPLACEMENTS]);
        dataPanel.add(label);

        label = new JLabel();
        label.setText("Live: "
                + dht.getRouter().getStats().getStats()[DHTRouterStats.ST_CONTACTS_LIVE]);
        dataPanel.add(label);

        DHTDBStats dbStats = dht.getDataBase().getStats();

        label = new JLabel();
        label.setText("Keys: " + dbStats.getKeyCount());
        dataPanel.add(label);

        label = new JLabel();
        label.setText("Values: " + dbStats.getValueCount());
        dataPanel.add(label);

        label = new JLabel();
        label.setText("Size: " + DisplayFormatters.formatByteCountToKiBEtc(dbStats.getSize()));
        dataPanel.add(label);

        int percent = dht.getTransport().getStats().getRouteablePercentage();
        label = new JLabel();
        label.setText("Reachable: " + (dht.getTransport().isReachable() ? "Yes" : "No")
                + (percent == -1 ? "" : (" " + percent + "%")));
        dataPanel.add(label);

        return dataPanel;
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel();
        GridBagLayout gbl = new GridBagLayout();
        inputPanel.setLayout(gbl);
        final JTextField keyField = new JTextField(25);
        final JTextField valueField = new JTextField(25);
        final JLabel timeLabel = new JLabel("###");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        final JButton getButton = new JButton("Get");
        final JButton putButton = new JButton("Put");
        // listeners for buttons
        getButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (keyField.getText().trim().equals("")) {
                    return;
                }
                keyField.setEnabled(false);
                valueField.setEnabled(false);
                getButton.setEnabled(false);
                putButton.setEnabled(false);

                new Thread() {
                    public void run() {
                        try {
                            final long start = System.currentTimeMillis();
                            final byte[] value = dhtAdapter.get(keyField.getText());
                            final long end = System.currentTimeMillis();
                            SwingUtilities.invokeAndWait(new Runnable() {

                                public void run() {
                                    timeLabel.setText(""
                                            + TimeUnit.MILLISECONDS.toSeconds(end - start));
                                    if (value == null) {
                                        valueField.setText(new String(""));
                                    } else {
                                        valueField.setText(new String(value));
                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } finally {
                            keyField.setEnabled(true);
                            valueField.setEnabled(true);
                            getButton.setEnabled(true);
                            putButton.setEnabled(true);
                        }
                    };
                }.start();
            }
        });

        putButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (keyField.getText().trim().equals("") || valueField.getText().trim().equals("")) {
                    return;
                }
                keyField.setEnabled(false);
                valueField.setEnabled(false);
                getButton.setEnabled(false);
                putButton.setEnabled(false);

                new Thread() {
                    public void run() {
                        try {
                            final long start = System.currentTimeMillis();
                            dhtAdapter.put(keyField.getText(), valueField.getText().getBytes());
                            final long end = System.currentTimeMillis();
                            SwingUtilities.invokeAndWait(new Runnable() {

                                public void run() {
                                    timeLabel.setText(""
                                            + TimeUnit.MILLISECONDS.toSeconds(end - start));
                                }
                            });
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } finally {
                            keyField.setEnabled(true);
                            valueField.setEnabled(true);
                            getButton.setEnabled(true);
                            putButton.setEnabled(true);
                        }
                    };
                }.start();
            }
        });

        buttonPanel.add(getButton);
        buttonPanel.add(putButton);
        // x y w h wx wy
        addComponent(inputPanel, gbl, new JLabel("Key:"), 0, 0, 1, 1, 0.0, 0.0);
        addComponent(inputPanel, gbl, new JLabel("Value:"), 0, 1, 1, 1, 0, 0.0);
        addComponent(inputPanel, gbl, new JLabel("Time:"), 0, 2, 1, 1, 0, 0);
        addComponent(inputPanel, gbl, keyField, 1, 0, 1, 1, 0.0, 0.0);
        addComponent(inputPanel, gbl, valueField, 1, 1, 1, 1, 0, 0.0);
        addComponent(inputPanel, gbl, timeLabel, 1, 2, 1, 1, 0, 0);
        addComponent(inputPanel, gbl, buttonPanel, 1, 3, 1, 1, 0.0, 0.0);
        return inputPanel;
    }

    private JComponent createActivityPane() {
        final String lineSeparator = System.getProperty("line.separator", "\n");
        JPanel activityPanel = new JPanel();
        final JTextArea activityTextArea = new JTextArea(5, 26);
        activityTextArea.setEditable(false);
        activityPanel.add(activityTextArea);
        dht.getControl().addListener(new DHTControlListener() {

            public void activityChanged(DHTControlActivity activity, int type) {
                // TODO status
                String typeString = "";
                switch (type) {
                case DHTControlActivity.AT_EXTERNAL_GET:
                    typeString = "External Get: ";
                    break;
                case DHTControlActivity.AT_EXTERNAL_PUT:
                    typeString = "External Put: ";
                    break;
                case DHTControlActivity.AT_INTERNAL_GET:
                    typeString = "Internal Get: ";
                    break;
                case DHTControlActivity.AT_INTERNAL_PUT:
                    typeString = "Internal Put: ";
                    break;

                }
                activityTextArea.append(typeString + activity.getDescription() + lineSeparator);
                // SwingUtilities.invokeLater(new Runnable() {
                // @Override
                // public void run() {
                // }
                // });
            }
        });

        final JScrollPane areaScrollPane = new JScrollPane(activityPanel);
        // autoscroll
        areaScrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                // e.getAdjustable().setValue(e.getAdjustable().getMaximum());
                activityTextArea.select(activityTextArea.getHeight() + 1000, 0);
            }
        });
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(350, 100));

        return areaScrollPane;
    }

    private void addComponent(Container cont, GridBagLayout gbl, Component c, int x, int y,
            int width, int height, double weightx, double weighty) {
        addComponent(cont, gbl, c, x, y, width, height, weightx, weighty, GridBagConstraints.CENTER);
    }

    private void addComponent(Container cont, GridBagLayout gbl, Component c, int x, int y,
            int width, int height, double weightx, double weighty, int anchor) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        gbc.gridheight = height;
        gbc.weightx = weightx;
        gbc.weighty = weighty;
        gbc.anchor = anchor;
        gbl.setConstraints(c, gbc);
        cont.add(c);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("java.security.policy",
                VuzeDhtGui.class.getClassLoader().getResource("client.policy").toExternalForm());

        System.setSecurityManager(new SecurityManager());
        System.setProperty("azureus.security.manager.permitexit", "1");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                VuzeDhtGui vg;
                try {
                    vg = new VuzeDhtGui();
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                    return;
                }
                vg.toFront();
                vg.setAlwaysOnTop(true);
                // vg.pack();
                vg.setResizable(false);
                vg.setVisible(true);
            }
        });

    }

}
