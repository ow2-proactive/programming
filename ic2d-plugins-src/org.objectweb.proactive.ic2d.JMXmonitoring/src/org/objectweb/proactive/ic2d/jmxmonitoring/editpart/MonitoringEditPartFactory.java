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
package org.objectweb.proactive.ic2d.jmxmonitoring.editpart;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.Communication;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.HostObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ProActiveNodeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.RuntimeObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.WorldObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.view.MonitoringView;


public class MonitoringEditPartFactory implements EditPartFactory {

    /** The monitoring view, where the figures will be drawn */
    private MonitoringView monitoringView;

    public MonitoringEditPartFactory(MonitoringView monitoringView) {
        this.monitoringView = monitoringView;
    }

    //
    // -- PUBLICS METHODS -----------------------------------------------
    //
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof WorldObject) {
            return new WorldEditPart((WorldObject) model, monitoringView);
        } else if (model instanceof HostObject) {
            return new HostEditPart((HostObject) model);
        } else if (model instanceof RuntimeObject) {
            return new RuntimeEditPart((RuntimeObject) model);
        } else if (model instanceof ProActiveNodeObject) {
            return new ProActiveNodeEditPart((ProActiveNodeObject) model);
        } else if (model instanceof ActiveObject) {
            ActiveObject ao = (ActiveObject) model;
            AOEditPart aoep = new AOEditPart(ao);
            //ao.canHandleCommunications = true;
            return aoep;
        } else if (model instanceof Communication) {
            return new CommunicationEditPart((Communication) model);
        } else {
            return null;
        }
    }
}
