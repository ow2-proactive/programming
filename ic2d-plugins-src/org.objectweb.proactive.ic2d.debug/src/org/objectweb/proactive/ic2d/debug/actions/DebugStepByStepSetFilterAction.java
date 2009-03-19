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
package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Shell;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.debug.dialogs.StepByStepFilterDialog;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;


public class DebugStepByStepSetFilterAction extends AbstractStepByStepAction {

    public static final String SETFILTER = "DEBUG_R_STEPBYSTEP03_Set Breakpoint filter";

    //
    // -- CONSTRUCTOR ----------------------------------------------
    //
    public DebugStepByStepSetFilterAction() {
        super.setId(SETFILTER);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/filter.png"), null)));
        super.setText("Set Breakpoint filter");
        super.setToolTipText("Set Breakpoint filter");
        super.setEnabled(false);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    /**
     * Set the action's target and set the correct display Text of the contextual menu action.
     *
     * @param object - the action's target
     */
    public void setAbstractDataObject(AbstractData<?, ?> object) {
        if (object instanceof ActiveObject) {
            this.target = object;
            super.setText("Set Breakpoint filter for " + object.getName());
            super.setEnabled(true);
            return;
        }
        super.setEnabled(false);
    }

    /**
     * Show the dialog for setting the breakpoint filter for an Active Object.
     */
    @Override
    public void run() {
        executeAction((ActiveObject) target);
    }

    @Override
    protected void executeAction(ActiveObject obj) {
        new StepByStepFilterDialog(new Shell(), obj);
    }

}
