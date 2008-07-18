/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.p2p.Monitoring.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.objectweb.proactive.extra.p2p.monitoring.color.NetworkColorScheme;


public class PeersimGroup {

    protected Group group;
    protected Label fileLabel;
    protected Label fileNameLabel;
    protected PeerAttributeColorComposite colorComposite;
    protected Composite peerComposite;

    public PeersimGroup(Composite parent, int style) {
        group = new Group(parent, style);
        group.setText("Peersim Log");
        RowLayout rowLayout = new RowLayout();
        rowLayout.type = SWT.VERTICAL;
        group.setLayout(rowLayout);
        //        GridLayout gridLayout = new GridLayout();
        //        gridLayout.numColumns = 2;
        //        group.setLayout(gridLayout);

        fileLabel = new Label(group, SWT.SHADOW_IN);
        fileNameLabel = new Label(group, SWT.SHADOW_IN);
        this.colorComposite = new PeerAttributeColorComposite(group, SWT.NONE);
        // peerComposite = .getComposite();
        // group.setLayout(new RowLayout(SWT.VERTICAL));

        this.test();
        group.pack();
    }

    protected void test() {
        this.fileLabel.setText("File : ");
        this.fileNameLabel.setText(" ");
    }

    public void setFileName(String s) {
        this.fileNameLabel.setText(s);
    }

    public Group getGroup() {
        return group;
    }

    public PeerAttributeColorComposite getAttributeColorComposite() {
        return this.colorComposite;
    }

    public void setColorScheme(NetworkColorScheme colorScheme) {
        this.colorComposite.setColorScheme(colorScheme);
    }

}
