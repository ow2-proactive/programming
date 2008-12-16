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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class DebuggerBranchInfoDialog extends Dialog {

    private Shell shell = null;
    private Button okButton;

    //
    // -- CONSTRUCTOR ----------------------------------------------
    //
    /**
     * Constructor which create the dialog
     */
    public DebuggerBranchInfoDialog(Shell parent, int port) {
        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        /* Init the display */
        Display display = getParent().getDisplay();

        /* Init the shell */
        shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
        shell.setText("Debugger Tunneling informations");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        Label titleLabel = new Label(shell, SWT.NONE);
        titleLabel
                .setText("A debugging tunnel was successfully created,\nyou can now connect your debugger with the\nfollowing informations :");
        FormData titleLabelFormData = new FormData();
        titleLabelFormData.left = new FormAttachment(0, 0);
        titleLabel.setLayoutData(titleLabelFormData);

        // Host info
        Label hostlabel = new Label(shell, SWT.NONE);
        hostlabel.setText("Host : ");
        FormData hostlabelFormData = new FormData();
        hostlabelFormData.top = new FormAttachment(titleLabel, 10);
        hostlabelFormData.left = new FormAttachment(0, 10);
        hostlabel.setLayoutData(hostlabelFormData);

        Text hosttext = new Text(shell, SWT.BORDER);
        hosttext.setText("localhost");
        FormData hosttextFormData = new FormData();
        hosttextFormData.top = new FormAttachment(titleLabel, 7);
        hosttextFormData.left = new FormAttachment(hostlabel, 15);
        hosttextFormData.right = new FormAttachment(95, 0);
        hosttext.setLayoutData(hosttextFormData);

        // Port info
        Label portlabel = new Label(shell, SWT.NONE);
        portlabel.setText("Port : ");
        FormData portlabelFormData = new FormData();
        portlabelFormData.top = new FormAttachment(hostlabel, 15);
        portlabelFormData.left = new FormAttachment(0, 10);
        portlabel.setLayoutData(portlabelFormData);

        Text porttext = new Text(shell, SWT.BORDER);
        porttext.setText(port + "");
        FormData porttextFormData = new FormData();
        porttextFormData.top = new FormAttachment(hostlabel, 12);
        porttextFormData.left = new FormAttachment(portlabel, 18);
        porttextFormData.right = new FormAttachment(95, 0);
        porttext.setLayoutData(porttextFormData);

        // button "OK"
        this.okButton = new Button(shell, SWT.PUSH);
        okButton.setText("Ok");
        okButton.addSelectionListener(new StepByStepDelayListener());
        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(portlabel, 20);
        okFormData.left = new FormAttachment(30, 0);
        okFormData.right = new FormAttachment(70, 0);
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        shell.pack();
        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    //
    // -- INNER CLASS -----------------------------------------------
    //
    private class StepByStepDelayListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent e) {
            if (e.widget == okButton) {
                shell.close();
            }

        }

    }

}
