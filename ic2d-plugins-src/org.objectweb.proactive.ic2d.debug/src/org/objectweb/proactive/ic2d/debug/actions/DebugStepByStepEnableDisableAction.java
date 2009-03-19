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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.objectweb.proactive.ic2d.console.Console;
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IWorkbenchWindowsActionExtPoint;


public class DebugStepByStepEnableDisableAction extends Action implements IWorkbenchWindowActionDelegate,
        IWorkbenchWindowsActionExtPoint {

    public static final String ENABLE_DISABLE_STEPBYSTEP = "EnableDisbaleStepByStep";
    public static final boolean DEFAULT_IS_DISABLE = false;

    //private WorldObject world;
    private boolean stepByStep = DEFAULT_IS_DISABLE;
    private String enableMessage = "Step By Step is Enabled";
    private String disableMessage = "Step By Step is Disabled";
    private WorldObject worldObject;

    //
    // -- CONSTRUCTOR ----------------------------------------------
    //
    public DebugStepByStepEnableDisableAction() {
        super("Enable/Disable Step by Step", AS_CHECK_BOX);
        this.setId(ENABLE_DISABLE_STEPBYSTEP);
        this.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                    "icons/pause.gif"), null)));
        this.setToolTipText(disableMessage);
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    public void setWorldObject(WorldObject object) {
        this.worldObject = object;
    }

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
        if (worldObject != null) {
            updateStepByStepState();
        }
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    /*
     * Update all Active Object from the World to toggle their StepByStep state.
     */
    private void updateStepByStepState() {
        if (stepByStep) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                            org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                                "icons/pause.gif"), null)));
                    setToolTipText(disableMessage);
                    Console.getInstance(Activator.CONSOLE_NAME).debug(disableMessage);
                    for (ActiveObject a : worldObject.getAOChildren()) {
                        a.disableStepByStep();
                    }
                }
            });
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(
                            org.objectweb.proactive.ic2d.debug.Activator.getDefault().getBundle(), new Path(
                                "icons/resume.gif"), null)));
                    setToolTipText(enableMessage);
                    Console.getInstance(Activator.CONSOLE_NAME).debug(enableMessage);
                    for (ActiveObject a : worldObject.getAOChildren()) {
                        a.enableStepByStep();
                    }
                }
            });
        }
        stepByStep = !stepByStep;
    }

}
