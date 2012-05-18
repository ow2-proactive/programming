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
package org.objectweb.proactive.examples.webservices.c3dWS.gui;

import java.awt.Button;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A Dialog box, with a title, displaying two lines of text.
 * Nothing fancy at all. Used to display the ProActive "about" window
 */
public class DialogBox extends Dialog implements ActionListener, java.io.Serializable {

    private static final long serialVersionUID = 52;
    public DialogBox(Frame parent, String frametitle, String line1, String line2) {
        super(parent, frametitle, true);

        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gb);

        // line 1
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        Label line1Label = new Label(line1, Label.CENTER);
        gb.setConstraints(line1Label, c);
        line1Label.setForeground(Color.blue);
        line1Label.setFont(new Font("arial", Font.BOLD | Font.ITALIC, 16));
        add(line1Label);

        // line 2
        c.gridy = 1;
        Label line2Label = new Label(line2, Label.CENTER);
        gb.setConstraints(line2Label, c);
        add(line2Label);

        //Button
        c.gridy = 2;
        c.fill = GridBagConstraints.NONE;
        Button okButton = new Button("OK");
        gb.setConstraints(okButton, c);
        okButton.addActionListener(this);
        add(okButton);

        setLocation(400, 200);
        pack();
        setVisible(true);
        toFront();
    }

    public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
    }
}
