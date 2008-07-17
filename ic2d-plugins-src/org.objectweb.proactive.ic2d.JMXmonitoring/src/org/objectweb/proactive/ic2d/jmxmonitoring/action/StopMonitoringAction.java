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
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;


public class StopMonitoringAction extends Action {
    public static final String STOP_MONITORING = "Set/Unset Monitoring";
    private AbstractData<?, ?> object;

    public StopMonitoringAction() {
        this.setId(STOP_MONITORING);
        this.setImageDescriptor(ImageDescriptor.createFromFile(this.getClass(), "stop_monitoring.gif"));
    }

    public void setObject(AbstractData<?, ?> object) {
        this.object = object;
        String msg;
        // The user can set to NOT MONITORED object
        if (this.object.isMonitored()) {
            msg = "Stop monitoring this " + object.getType();
        } else { // If not monitored the user can set to MONITORED object
            msg = "Monitor this " + object.getType();
        }
        this.setText(msg);
        this.setToolTipText(msg);
    }

    @Override
    public void run() {
        // The user can set to NOT MONITORED object
        if (this.object.isMonitored()) {
            this.object.setNotMonitored();
        } else { // If not monitored the user can set to MONITORED object
            this.object.setMonitored();
        }
    }
}
