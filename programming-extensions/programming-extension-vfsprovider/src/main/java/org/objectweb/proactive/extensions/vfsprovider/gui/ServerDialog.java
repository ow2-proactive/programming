/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.vfsprovider.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


class ServerDialog implements ActionListener {

    private JDialog frame;

    private JComboBox<String> rootCombo;
    private JComboBox<String> nameCombo;
    private JCheckBox rebindBox;
    private JCheckBox startBox;
    private JCheckBox protocolBox;
    private JLabel protocolLabel;
    private JComboBox<String> protocolCombo;
    private JTextArea errorArea;
    private JLabel errorLabel;
    private JPanel errorPanel;
    private JScrollPane scrollPane;
    private JPanel formPane;
    private JPanel waitPane;
    private JPanel donePane;
    private JLabel doneLabel;

    private JButton prevButton, nextButton, cancelButton, okButton;

    private String startServerName;

    ServerDialog(Frame parent, ActionListener actionListener) {
        build(parent, actionListener);
    }

    private void build(Frame parent, ActionListener actionListener) {
        JPanel contentPane = new JPanel(new BorderLayout());

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel form2Pane = new JPanel(layout);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.insets = new Insets(2, 2, 2, 2);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.LINE_START;
        JLabel l1 = new JLabel("<html><strong>Create a new Data Server</strong></html>");
        form2Pane.add(l1, c);

        c.gridy = 1;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        JLabel l2 = new JLabel("Root directory");
        form2Pane.add(l2, c);
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.rootCombo = new JComboBox<>(ServerBrowser.getHistory(ServerBrowser.rootHistoryFile));
        rootCombo.setPreferredSize(new Dimension(200, rootCombo.getPreferredSize().height));
        rootCombo.setEditable(true);
        rootCombo.setSelectedItem("");
        form2Pane.add(rootCombo, c);
        c.gridx = 2;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        JButton b2 = new JButton("Open");
        b2.addActionListener(this);
        b2.setActionCommand("open");
        form2Pane.add(b2, c);

        c.gridy = 2;
        c.gridx = 0;
        JLabel l3 = new JLabel("Name");
        form2Pane.add(l3, c);
        c.gridx = 1;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.nameCombo = new JComboBox<>(ServerBrowser.getHistory(ServerBrowser.nameHistoryFile));
        nameCombo.setEditable(true);
        nameCombo.setSelectedItem("");
        form2Pane.add(nameCombo, c);

        c.gridy = 3;
        c.gridx = 0;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        this.rebindBox = new JCheckBox("Rebind existing server");
        form2Pane.add(rebindBox, c);

        c.gridy = 4;
        this.startBox = new JCheckBox("Start server");
        startBox.setSelected(true);
        form2Pane.add(startBox, c);

        c.gridy = 5;
        this.protocolBox = new JCheckBox("Use default protocol (recommanded)");
        protocolBox.addActionListener(this);
        protocolBox.setActionCommand("protocol");
        protocolBox.setSelected(true);
        form2Pane.add(protocolBox, c);

        c.gridy = 6;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        this.protocolLabel = new JLabel("Protocol");
        protocolLabel.setEnabled(false);
        form2Pane.add(protocolLabel, c);
        c.gridx = 1;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        this.protocolCombo = new JComboBox<>(new String[] { "rmi", "rmissh", "http", "pnp", "pnps",
                "pamr" });
        protocolCombo.setEnabled(false);
        protocolCombo.setEditable(true);
        form2Pane.add(protocolCombo, c);

        this.errorArea = new JTextArea();
        this.errorLabel = new JLabel();
        this.errorPanel = new JPanel(new BorderLayout(5, 5));
        this.errorPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.errorPanel.add(this.errorLabel, BorderLayout.NORTH);
        this.errorPanel.add(this.errorArea, BorderLayout.CENTER);

        this.waitPane = new JPanel(new BorderLayout());
        JProgressBar progress = new JProgressBar(JProgressBar.HORIZONTAL);
        progress.setIndeterminate(true);
        this.waitPane.add(progress, BorderLayout.SOUTH);
        this.waitPane.add(new Label("Please wait..."), BorderLayout.CENTER);

        this.donePane = new JPanel(new BorderLayout());
        this.doneLabel = new JLabel();
        this.donePane.add(doneLabel, BorderLayout.CENTER);

        JPanel buttonPane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        nextButton = new JButton("Next >");
        nextButton.setActionCommand("next");
        nextButton.addActionListener(this);
        prevButton = new JButton("< Previous");
        prevButton.setActionCommand("previous");
        prevButton.addActionListener(this);
        prevButton.setEnabled(false);
        cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("serverDialog.cancel");
        cancelButton.addActionListener(actionListener);
        okButton = new JButton("Ok");
        okButton.setActionCommand("serverDialog.ok");
        okButton.addActionListener(actionListener);
        okButton.setEnabled(false);

        buttonPane.add(prevButton);
        buttonPane.add(nextButton);
        buttonPane.add(cancelButton);
        buttonPane.add(okButton);

        this.formPane = new JPanel(new BorderLayout());
        formPane.add(form2Pane, BorderLayout.NORTH);
        this.scrollPane = new JScrollPane(formPane);
        contentPane.add(scrollPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        this.frame = new JDialog(parent);
        frame.setContentPane(contentPane);
        frame.setMinimumSize(new Dimension(400, 300));
        frame.setPreferredSize(new Dimension(400, 300));
        frame.setModal(true);
        frame.setLocationRelativeTo(parent);
        frame.setTitle("New Data Server");
        frame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        frame.pack();
    }

    void show() {
        this.frame.setVisible(true);
    }

    void close() {
        this.frame.setVisible(false);
    }

    String getStartedServerName() {
        return this.startServerName;
    }

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equals("protocol")) {
            boolean sel = this.protocolBox.isSelected();
            this.protocolLabel.setEnabled(!sel);
            this.protocolCombo.setEnabled(!sel);
        } else if (cmd.equals("next")) {
            this.scrollPane.setViewportView(this.waitPane);
            this.frame.invalidate();
            this.frame.validate();
            this.frame.repaint();

            String _root = "";
            if (this.rootCombo.getSelectedItem() != null) {
                _root = this.rootCombo.getSelectedItem().toString();
            }
            final String root = _root;
            String _name = "";
            if (this.nameCombo.getSelectedItem() != null) {
                _name = this.nameCombo.getSelectedItem().toString();
            }
            final String name = _name;
            String _proto = null;
            if (!this.protocolBox.isSelected()) {
                _proto = this.protocolCombo.getSelectedItem().toString();
            }
            final String proto = _proto;

            final boolean start = this.startBox.isSelected();
            final boolean rebind = this.rebindBox.isSelected();

            new Thread(new Runnable() {
                public void run() {
                    try {
                        if (name.trim().length() == 0) {
                            throw new IllegalArgumentException("Data Server name cannot be empty.");
                        }
                        File f = new File(root);
                        if (!(f.exists() && f.isDirectory())) {
                            throw new IllegalArgumentException(
                                "Data Server root directory must be an existing directory");
                        }
                        DataServer.getInstance().addServer(root, name, rebind, start, proto);
                        ServerDialog.this.startServerName = DataServer.getInstance().getServer(name)
                                .getName();

                        String str = "Successfully created new Data Server <strong>" + name + "</strong><br>";
                        final String fstr = str;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ServerDialog.this.doneLabel
                                        .setText("<html>Successfully created Data Server " + name + "</html>");
                                ServerDialog.this.scrollPane.setViewportView(ServerDialog.this.donePane);
                                ServerDialog.this.nextButton.setEnabled(false);
                                ServerDialog.this.prevButton.setEnabled(false);
                                ServerDialog.this.cancelButton.setEnabled(false);
                                ServerDialog.this.okButton.setEnabled(true);
                            }
                        });

                    } catch (Throwable t) {
                        ServerDialog.this.startServerName = null;
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        PrintWriter pw = new PrintWriter(os);
                        t.printStackTrace(pw);
                        pw.close();

                        final String tmsg = t.getMessage();
                        final String trace = os.toString();

                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ServerDialog.this.errorLabel
                                        .setText("<html>Error while creating Data Server:<br><strong><font color='#dd0000'>" +
                                            tmsg + "</font></html>");
                                ServerDialog.this.errorArea.setText(trace);
                                ServerDialog.this.scrollPane.setViewportView(ServerDialog.this.errorPanel);
                                ServerDialog.this.prevButton.setEnabled(true);
                                ServerDialog.this.nextButton.setEnabled(false);
                            }
                        });
                    }
                }
            }).start();

        } else if (cmd.equals("previous")) {
            this.scrollPane.setViewportView(this.formPane);
            this.prevButton.setEnabled(false);
            this.nextButton.setEnabled(true);
            this.okButton.setEnabled(false);
        } else if (cmd.equals("open")) {
            JFileChooser jf = new JFileChooser();
            jf.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int ret = jf.showOpenDialog(this.frame);
            if (ret == JFileChooser.APPROVE_OPTION) {
                this.rootCombo.setSelectedItem(jf.getSelectedFile().getAbsolutePath());
            }
        }
    }
}
