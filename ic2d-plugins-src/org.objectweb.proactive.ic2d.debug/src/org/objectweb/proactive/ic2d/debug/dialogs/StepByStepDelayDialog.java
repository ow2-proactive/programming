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
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;


public class StepByStepDelayDialog extends Dialog {

    private Shell shell = null;
    private Scale scale;
    private Text text;
    private Button okButton;
    private Button cancelButton;
    private WorldObject world;

    //
    // -- CONSTRUCTOR ----------------------------------------------
    //
    /**
     * Constructor which create the dialog
     */
    public StepByStepDelayDialog(Shell parent, WorldObject world) {
        // Pass the default styles here
        super(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

        this.world = world;

        /* Init the display */
        Display display = getParent().getDisplay();

        /* Init the shell */
        shell = new Shell(getParent(), SWT.BORDER | SWT.CLOSE);
        shell.setText("Set Step by Step motion Delay");
        FormLayout layout = new FormLayout();
        layout.marginHeight = 5;
        layout.marginWidth = 5;
        shell.setLayout(layout);

        Label titleLabel = new Label(shell, SWT.NONE);
        titleLabel.setText("Select the delay for each step by step action in ms.");
        FormData titleLabelFormData = new FormData();
        titleLabelFormData.left = new FormAttachment(0, 0);
        titleLabel.setLayoutData(titleLabelFormData);

        this.scale = new Scale(shell, SWT.HORIZONTAL);
        scale.setMinimum(0);
        scale.setMaximum((world.getStepByStepDelay() == 0) ? 2500 : world.getStepByStepDelay() +
            world.getStepByStepDelay());
        scale.setSelection((world.getStepByStepDelay() == 0) ? 0 : world.getStepByStepDelay());
        scale.setIncrement(5);
        scale.addSelectionListener(new StepByStepDelayListener());
        FormData scaleData = new FormData();
        scaleData.top = new FormAttachment(titleLabel, 10);
        scaleData.left = new FormAttachment(10, 10);
        scaleData.right = new FormAttachment(75, -20);
        scale.setLayoutData(scaleData);

        this.text = new Text(shell, SWT.BORDER);
        text.setText(Integer.toString(world.getStepByStepDelay()));
        FormData textFormData = new FormData();
        textFormData.top = new FormAttachment(titleLabel, 7);
        textFormData.left = new FormAttachment(scale, 10);
        textFormData.right = new FormAttachment(90, 0);
        text.setLayoutData(textFormData);

        // button "OK"
        this.okButton = new Button(shell, SWT.NONE);
        okButton.setText("OK");
        okButton.addSelectionListener(new StepByStepDelayListener());
        FormData okFormData = new FormData();
        okFormData.top = new FormAttachment(scale, 20);
        okFormData.left = new FormAttachment(25, 20);
        okFormData.right = new FormAttachment(50, -10);
        okButton.setLayoutData(okFormData);
        shell.setDefaultButton(okButton);

        // button "CANCEL"
        this.cancelButton = new Button(shell, SWT.NONE);
        cancelButton.setText("Cancel");
        cancelButton.addSelectionListener(new StepByStepDelayListener());
        FormData cancelFormData = new FormData();
        cancelFormData.top = new FormAttachment(scale, 20);
        cancelFormData.left = new FormAttachment(50, 10);
        cancelFormData.right = new FormAttachment(75, -20);
        cancelButton.setLayoutData(cancelFormData);

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
                world.setStepByStepDelay(Integer.parseInt(text.getText()));
                for (ActiveObject a : world.getAOChildren()) {
                    a.slowMotion(Integer.parseInt(text.getText()));
                }
                shell.close();
            }
            if (e.widget == cancelButton) {
                shell.close();
            }
            if (e.widget == scale) {
                if (scale.getSelection() > ((scale.getMaximum() / 100) * 90)) {
                    scale.setMaximum(scale.getMaximum() + ((scale.getMaximum() / 100) * 2));
                }
                text.setText(Integer.toString(scale.getSelection()));
            }
        }

    }

}
