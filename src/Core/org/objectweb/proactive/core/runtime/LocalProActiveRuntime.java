/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.runtime;

import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;


/**
 * This interface provides a local view of the ProActiveRuntime. It contains methods that can only
 * be called locally.
 * @author The ProActive Team
 * @version 1.0
 * @since   ProActive 3.0
 */
public interface LocalProActiveRuntime {

    /**
     * Register the given VirtualNode on this local ProActiveRuntime.
     * @param vn the virtualnode to register
     * @param vnName the name of the VirtualNode to register
     */
    public void registerLocalVirtualNode(VirtualNodeInternal vn, String vnName);

    /**
     * This method adds a reference to the runtime that created this runtime.
     * It is called when a new runtime is created from another one.
     * @param parentPARuntime the creator of this runtime
     */
    public void setParent(ProActiveRuntime parentPARuntime);

    /**
     * Set the capacity of the local ProActive Runtime and create
     * as many local nodes.
     *
     * Capacity can only be set once.
     *
     * @param capacity An long strictly greater than 0.
     * @throws IllegalStateException if called twice
     * @throws IllegalArgumentException if capacity is not strictly positive
     * @return URLs of the created nodes
     */
    public void setCapacity(long capacity);

    public void setDeploymentId(long deploymentId);

    public void setTopologyId(long topologyId);

    public void setVMName(String vmName);

    public void addDeployment(long deploymentId);
}
