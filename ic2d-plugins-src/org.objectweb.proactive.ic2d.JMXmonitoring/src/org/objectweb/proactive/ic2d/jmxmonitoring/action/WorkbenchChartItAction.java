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
package org.objectweb.proactive.ic2d.jmxmonitoring.action;

import org.objectweb.proactive.ic2d.chartit.actions.AbstractWorkbenchChartItAction;
import org.objectweb.proactive.ic2d.chartit.data.resource.IResourceDescriptor;
import org.objectweb.proactive.ic2d.chartit.data.resource.ResourceDataBuilder;


/**
 * A concrete implementation of {@link org.objectweb.proactive.ic2d.chartit.actions.AbstractWorkbenchChartItAction} class
 * used to monitor JMXMonitoring plugin it self.
 * <p>
 * This action plugs into the general top level workbench panel (near File, Edit, Help).
 * 
 * @author <a href="mailto:support@activeeon.com">ActiveEon Team</a>.
 */
public class WorkbenchChartItAction extends AbstractWorkbenchChartItAction {

    @Override
    public IResourceDescriptor createResourceDescriptor() {
        // Here in the future vbodnart or other may submit a custom descriptor based on internal metrics
        // of JMXMonitoring plugin (for example nbEvent/s or nbMonitored entities ... )
        // For the moment only a default resource descriptor is used, it's based on a local RuntimeMXBean (provided by ChartIt plugin)		
        return ResourceDataBuilder.createResourceDescriptorForLocalRuntime();
    }

}
