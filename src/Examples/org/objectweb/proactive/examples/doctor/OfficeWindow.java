/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.examples.doctor;

public class OfficeWindow extends javax.swing.JFrame implements java.awt.event.ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    DisplayPanel pan;
    javax.swing.JButton bLegend;
    javax.swing.JButton bExit;
    Legend legendDlg;

    public OfficeWindow() {
        java.awt.Container c = getContentPane();
        legendDlg = null;
        java.awt.GridBagLayout lay = new java.awt.GridBagLayout();
        java.awt.GridBagConstraints constr = new java.awt.GridBagConstraints();
        c.setLayout(lay);

        constr.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        constr.fill = java.awt.GridBagConstraints.BOTH;
        constr.weightx = 0.0;
        pan = new DisplayPanel();
        lay.setConstraints(pan, constr);
        c.add(pan);

        constr.gridwidth = 1;
        constr.weightx = 1.0;
        bLegend = new javax.swing.JButton("Legend");
        lay.setConstraints(bLegend, constr);
        c.add(bLegend);
        bLegend.addActionListener(this);

        constr.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        bExit = new javax.swing.JButton("Exit");
        lay.setConstraints(bExit, constr);
        c.add(bExit);
        bExit.addActionListener(this);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() == bExit) {
            System.exit(0);
        }
        if (e.getSource() == bLegend) {
            if (legendDlg == null) {
                legendDlg = new Legend(this, pan);
            }
            if (legendDlg.isVisible()) {
                legendDlg.setVisible(false);
            } else {
                legendDlg.setVisible(true);
            }
        }
    }

    public DisplayPanel getDisplay() {
        return pan;
    }
}
