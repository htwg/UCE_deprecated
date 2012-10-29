package de.htwg_konstanz.in.uce.hp.parallel.mediator_browser.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.htwg_konstanz.in.uce.hp.parallel.mediator_browser.MediatorBrowser;

public class MediatorBrowserFrame extends JFrame {

    private static final long serialVersionUID = 1L;
    private final MediatorBrowser mb;
    private JPanel jContentPane = null;
    private JScrollPane listScroller = null;
    private JList targetList = null;

    /**
     * This is the default constructor
     * 
     * @throws IOException
     */
    public MediatorBrowserFrame(MediatorBrowser mb) throws IOException {
        super("Mediator Browser");
        this.mb = mb;
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     * @throws IOException
     */
    private void initialize() throws IOException {
        this.setSize(210, 200);
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     * @throws IOException
     */
    private JPanel getJContentPane() throws IOException {
        if (jContentPane == null) {
            FlowLayout layout = new FlowLayout();
            final JScrollPane listScroller = getRegisteredTargetsScrollPane();
            JButton refreshButton = new JButton("Refresh");
            refreshButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ((DefaultListModel) targetList.getModel()).removeAllElements();
                    try {
                        for (String target : mb.getSetOfRegisteredTargets()) {
                            ((DefaultListModel) targetList.getModel()).addElement(target);
                        }
                    } catch (IOException e1) {
                        JOptionPane.showMessageDialog(MediatorBrowserFrame.this, e1.getClass()
                                .getCanonicalName() + " occured", "Error",
                                JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                    }
                }
            });
            jContentPane = new JPanel();
            jContentPane.setLayout(layout);
            jContentPane.add(listScroller);
            jContentPane.add(refreshButton);
        }
        return jContentPane;
    }

    /**
     * This method initializes registeredTargetsList
     * 
     * @return javax.swing.JList
     * @throws IOException
     */
    private JScrollPane getRegisteredTargetsScrollPane() throws IOException {
        if (listScroller == null) {
            DefaultListModel model = new DefaultListModel();
            targetList = new JList(model);
            targetList.setFixedCellWidth(200);
            for (String target : mb.getSetOfRegisteredTargets()) {
                model.addElement(target);
            }
            listScroller = new JScrollPane(targetList);
        }
        return listScroller;
    }

}
