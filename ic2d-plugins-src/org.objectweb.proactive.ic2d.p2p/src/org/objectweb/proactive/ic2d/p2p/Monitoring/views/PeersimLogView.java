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
package org.objectweb.proactive.ic2d.p2p.Monitoring.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener4;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.objectweb.proactive.ic2d.p2p.Monitoring.gui.PeersimGroup;


public class PeersimLogView extends ViewPart implements IPerspectiveListener4 {

    static public PeersimGroup group;

    public void createPartControl(Composite parent) {
        // Slider s = new Slider(parent, SWT.HORIZONTAL);
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(this);
        group = new PeersimGroup(parent, SWT.SHADOW_NONE);
    }

    public void perspectivePreDeactivate(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
    }

    public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

    }

    public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

    }

    public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

    }

    public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
            IPerspectiveDescriptor newPerspective) {

    }

    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
            IWorkbenchPartReference partRef, String changeId) {

    }

    public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {

    }

    public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {

    }

    public void setFocus() {

    }

}
