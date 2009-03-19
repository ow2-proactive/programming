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

import java.util.List;

import org.eclipse.jface.action.Action;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.AbstractData;
import org.objectweb.proactive.ic2d.jmxmonitoring.data.ActiveObject;
import org.objectweb.proactive.ic2d.jmxmonitoring.extpoint.IActionExtPoint;


public abstract class AbstractStepByStepAction extends Action implements IActionExtPoint {

    /** The target data */
    protected AbstractData<?, ?> target;

    public AbstractStepByStepAction(String str, int opt) {
        super(str, opt);
    }

    public AbstractStepByStepAction() {
        super();
    }

    public void setActiveSelect(AbstractData<?, ?> ref) {
        // Nothing to do for the moment when the target get the focus
    }

    /**
     * Action to do when action invoked
     */
    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        if (target instanceof ActiveObject) {
            executeAction((ActiveObject) target);
        } else {
            explore((List<AbstractData>) target.getMonitoredChildrenAsList());
        }
    }

    /**
     * If target is a container, explore the child and enable the step by step mode for the active
     * object contained.
     */
    @SuppressWarnings("unchecked")
    protected void explore(List<AbstractData> exploreList) {
        if (exploreList != null && exploreList.size() > 0) {
            for (AbstractData obj : exploreList) {
                if (obj instanceof ActiveObject) {
                    executeAction((ActiveObject) obj);
                } else {
                    explore(obj.getMonitoredChildrenAsList());
                }
            }
        }
    }

    abstract protected void executeAction(ActiveObject obj);

}
