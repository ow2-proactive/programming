/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.ic2d.debug.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.ic2d.debug.actions.DebugSocketConnection;


/**
 *
 * Create a waiting feedback dialog for the time to
 * establish the tunnel for debug connection
 *
 */
public class TunnelingCreationWaitingDialog extends Dialog {

    private Shell shell = null;
    private Label titleLabel = null;
    private final Display display;

    public TunnelingCreationWaitingDialog(Shell parent) {
        super(parent, SWT.APPLICATION_MODAL);

        /* Init the display */
        display = getParent().getDisplay();
        shell = new Shell(getParent(), SWT.APPLICATION_MODAL);

        // Get the resolution
        Rectangle pDisplayBounds = getParent().getBounds();
        int nMinWidth = 60;
        int nMinHeight = 20;
        // This formulae calculate the shell's Left ant Top
        int nLeft = (pDisplayBounds.width - nMinWidth) / 2;
        int nTop = (pDisplayBounds.height - nMinHeight) / 2;
        // Set shell bounds,
        shell.setBounds(nLeft, nTop, nMinWidth, nMinHeight);

        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        titleLabel = new Label(shell, SWT.NONE);
        titleLabel.setText("Establishing connection... Please wait...");
        //        FormData titleLabelFormData = new FormData();
        //        titleLabelFormData.left = new FormAttachment(0, 5);
        //        titleLabel.setLayoutData(titleLabelFormData);

        shell.pack();
        shell.open();

    }

    public void labelUp(int cpt) {
        titleLabel.setText("Establishing connection... try: " + cpt + "/" + DebugSocketConnection.NB_TRIES);
        titleLabel.update();
    }

    public void showError() {
        shell.setSize(300, 300);
        FontDialog dlg = new FontDialog(shell);
        RGB rgb = new RGB(255, 0, 0);
        dlg.setRGB(rgb);
        Color color = new Color(shell.getDisplay(), dlg.getRGB());
        titleLabel.setForeground(color);
        titleLabel.setText("Connection failed, try again!");
        titleLabel.update();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        shell.close();
    }

}
