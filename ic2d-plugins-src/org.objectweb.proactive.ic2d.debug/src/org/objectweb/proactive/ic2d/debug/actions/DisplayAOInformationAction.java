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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;
import org.objectweb.proactive.ic2d.debug.views.AOInformationView;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;


public class DisplayAOInformationAction extends Action implements IActionExtPoint {

    public static final String DISAPLAYAOINFORMATIONS = "STEPBYSTEP03_Display AO Informations";

    public DisplayAOInformationAction() {
        super.setId(DISAPLAYAOINFORMATIONS);
        super.setEnabled(false);
    }

    public void setAbstractDataObject(AbstractData<?, ?> object) {
    }

    public void setActiveSelect(AbstractData<?, ?> ref) {
        if (ref instanceof ActiveObject) {
            AOInformationView aoinfInformationView;
            aoinfInformationView = (AOInformationView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().findView(AOInformationView.ID);
            if (aoinfInformationView != null) {
                aoinfInformationView.selectItem((ActiveObject) ref);
            }
        }
    }
}
