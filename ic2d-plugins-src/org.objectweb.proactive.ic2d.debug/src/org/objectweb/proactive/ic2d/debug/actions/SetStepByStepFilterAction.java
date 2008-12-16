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
import org.objectweb.proactive.ic2d.debug.dialogs.StepByStepFilterDialog;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IWorkbenchWindowsActionExtPoint;


public class SetStepByStepFilterAction extends Action implements IWorkbenchWindowActionDelegate,
        IWorkbenchWindowsActionExtPoint {

    public static final boolean DEFAULT_FILTERING = false;
    public static final String SET_STEPBYSTEPFILER = "DEBUG_STEPBYSTEP04_Set Step By Step Breakpoint Filter";

    private boolean filtering = DEFAULT_FILTERING;
    private String enableMessage = "Step By Step Filters are set.";
    private String disableMessage = "Step By Step Filters are unset.";

    /** The world */
    private WorldObject world;

    public SetStepByStepFilterAction() {
        super("Enable Slow Motion", AS_CHECK_BOX);
        this.setId(SET_STEPBYSTEPFILER);
        this.setText("Set Step By Step Breakpoint Filters");
        setToolTipText("Set Step By Step Breakpoint Filters");
        this.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/filter.png"), null)));
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
        filtering = !filtering;
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void updateStepDelayState() {
        if (filtering) {
            Console.getInstance(Activator.CONSOLE_NAME).debug(enableMessage);
            for (ActiveObject a : world.getAOChildren()) {
                a.initBreakpointTypes();
            }
        } else {
            Console.getInstance(Activator.CONSOLE_NAME).debug(disableMessage);
            new StepByStepFilterDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), world);

        }
    }

    public void setWorldObject(WorldObject object) {
        this.world = object;
    }

}
