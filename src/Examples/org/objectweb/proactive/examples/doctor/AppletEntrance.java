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
package org.objectweb.proactive.examples.doctor;

import org.objectweb.proactive.core.config.ProActiveConfiguration;


/**
 * <p>
 * This application simulates the behavior of an hospital. Sick
 * patients are waiting for a doctor to heal them, while doctors go
 * from patient to patient. This application illustrates
 * resource-sharing using ProActive
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public class AppletEntrance extends org.objectweb.proactive.examples.StandardFrame {

    private static final long serialVersionUID = 52;

    public AppletEntrance(String name, int width, int height) {
        super(name, width, height);
    }

    public static void main(String[] arg) {
        ProActiveConfiguration.load();
        new AppletEntrance("The salishan problems", 600, 300);
    }

    @Override
    public void start() {
        receiveMessage("Please wait while initializing remote objects");
        try {
            Office off = org.objectweb.proactive.api.PAActiveObject.newActive(Office.class,
                    new Object[] { Integer.valueOf(0) });
            Receptionnist recept = org.objectweb.proactive.api.PAActiveObject.newActive(Receptionnist.class,
                    new Object[] { off });
            receiveMessage("The doctors' office is open!");
            off.init(off, recept);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected javax.swing.JPanel createRootPanel() {
        javax.swing.JPanel rootPanel = new javax.swing.JPanel(new java.awt.BorderLayout());
        rootPanel.setBackground(java.awt.Color.white);
        rootPanel.setForeground(java.awt.Color.red);
        rootPanel.add(new javax.swing.JLabel("The salishan problems : Problem 3 - The Doctor's Office"),
                java.awt.BorderLayout.NORTH);
        //javax.swing.JPanel officePanel=new javax.swing.JPanel(new java.awt.GridLayout(2,1));
        return rootPanel;
    }
}
