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
import org.objectweb.proactive.ic2d.debug.Activator;
import org.objectweb.proactive.ic2d.debug.views.AOInformationView;


/**
 * Clear the {@link AOInformationView} Tree by removing all Active Object from it
 */
public class ExpandAllAoInformationViewAction extends Action implements IWorkbenchWindowActionDelegate {

    public static final String DEBUGEXPAND = "DEBUGVIEW_01_Expand";

    private AOInformationView view;

    public ExpandAllAoInformationViewAction(AOInformationView view) {
        super.setId(DEBUGEXPAND);
        super.setImageDescriptor(ImageDescriptor.createFromURL(FileLocator.find(Activator.getDefault()
                .getBundle(), new Path("icons/expand_all.png"), null)));
        super.setText("Expand all Active Objects");
        super.setToolTipText("Expand all Active Objects");
        this.view = view;
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
        view.expandAll();
    }
}
