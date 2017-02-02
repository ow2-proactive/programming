/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.runtime;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <p>
 * A class implementing this interface provides information about the VM it is attached to.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
@PublicAPI
public interface VMInformation extends java.io.Serializable {

    /**
     * Returns the VMID of the VM the node belongs to
     * @return the VMID of the VM the node belongs to
     */
    public java.rmi.dgc.VMID getVMID();

    /**
     * Returns the InetAddress of the host where is the VM located
     * @return the InetAddress of the host where is the VM located
     */
    public java.net.InetAddress getInetAddress();

    /**
     * Returns the given name (identifier) of this VM.
     *
     * @return the given name (identifier) of this VM
     */
    public String getName();

    /**
     * Returns the host where the vm is located
     * @return the host where the vm is located
     */
    public String getHostName();

    /**
     * Gives the name of the java virtual machine, that was used to create this node.
     * The name corresponds to the name specified in the descriptor file:
     *  <pre>
     *  ...
     *  &lt;deployment&gt;
     *  ...
     *          &lt;jvms&gt;
     *                 ...
     *                &lt;jvm name="The Name"&gt;...&lt;/jvm&gt;
     *                ...
     *                &lt;/jvms&gt;
     *  ...
     *  &lt;/deployment&gt;
     *  </pre>
     * @return The java virtual machine name if created with a descriptor file, null otherwise.
     *
     */
    public String getDescriptorVMName();

    /**
     * Returns the capacity of the ProActive Runtime
     *
     * Capacity corresponds to the number of node created by default on the runtime.
     * Usually capacity corresponds to the number of cores on which the JVM runs.
     */
    public long getCapacity();

    public long getDeploymentId();

    public long getTopologyId();
}
