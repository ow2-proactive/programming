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
package org.objectweb.proactive.ic2d.debug.actions;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.debug.dialogs.StepByStepDelayDialog;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IWorkbenchWindowsActionExtPoint;


public class SetSlowMotionAction extends Action implements IWorkbenchWindowActionDelegate,
        IWorkbenchWindowsActionExtPoint {

    public static final boolean DEFAULT_DELAYING = false;
    public static final String SET_STEPBYSTEPDELAY = "DEBUG_STEPBYSTEP05_Set Step By Step Delay";

    private boolean stepdelaying = DEFAULT_DELAYING;
    private String enableMessage = "The step by step motion is Enabled";
    private String disableMessage = "The step by step motion is Disabled";

    /*  The world */
    private WorldObject world;

    public SetSlowMotionAction() {
        super("Enable Slow Motion", AS_CHECK_BOX);
        this.setId(SET_STEPBYSTEPDELAY);
        this.setText("Set Step By Step Delay");
        setToolTipText("Set Step By Step Delay");
        this.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                    "icons/clock.png"), null)));
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void dispose() { /* Do nothing */
    }

    public void init(IWorkbenchWindow window) { /* Do nothing */
    }

    public void run(IAction action) {
        this.run();
    }

    public void selectionChanged(IAction action, ISelection selection) { /* Do nothing */
    }

    @Override
    public void run() {
        updateStepDelayState();
        stepdelaying = !stepdelaying;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void updateStepDelayState() {
        if (stepdelaying) {
            this.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                    org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                        "icons/clock.png"), null)));
            Console.getInstance(Activator.CONSOLE_NAME).debug(disableMessage);
            this.setText("Set Step By Step Delay");
            setToolTipText("Set Step By Step Delay");
            for (ActiveObject a : world.getAOChildren()) {
                a.slowMotion(0);
            }
        } else {
            this.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                    org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                        "icons/clock_on.png"), null)));
            Console.getInstance(Activator.CONSOLE_NAME).debug(enableMessage);
            this.setText("Step By Step Slow Motion in progress...");
            setToolTipText("Step By Step Slow Motion in progress...");
            new StepByStepDelayDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), world);
        }
    }

    public void setWorldObject(WorldObject object) {
        this.world = object;
    }

}
