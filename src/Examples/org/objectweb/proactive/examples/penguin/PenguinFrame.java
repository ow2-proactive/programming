/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.penguin;

public class PenguinFrame extends javax.swing.JFrame {
    //The image panel
    private javax.swing.JPanel imagePanel;
    private javax.swing.JLabel imageLabel;

    public PenguinFrame(javax.swing.ImageIcon f, String location, int index) {
        super("Agent " + index);
        imagePanel = buildImagePanel(f);
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
        getContentPane().setLayout(new java.awt.BorderLayout());
        getContentPane().add(new javax.swing.JLabel(location), java.awt.BorderLayout.NORTH);
        getContentPane().add(imagePanel, java.awt.BorderLayout.CENTER);
        pack();
        toFront();
        setVisible(true);
    }

    /**
     *  Build the image panel
     * It is made of one pic
     */
    private javax.swing.JPanel buildImagePanel(javax.swing.ImageIcon i) {
        javax.swing.JPanel temp = new javax.swing.JPanel();
        imageLabel = new javax.swing.JLabel(i);
        temp.add(imageLabel);
        return temp;
    }
}
