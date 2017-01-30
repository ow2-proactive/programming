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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo;

import java.io.Serializable;
import java.util.Set;

import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 *
 * @author The ProActive Team
 *
 */
public interface HostInfo extends Serializable {

    /**
     * Returns the Id of this of set
     * @return the Id as declared by the host's id attribute
     */
    public String getId();

    public void setId(String id);

    /**
     * Returns the username associated to this set of hosts
     * @return the username it is present inside the GCM Deployment Descriptor. If
     * not null is returned
     */
    public String getUsername();

    /**
     * Returns the homeDirectory associated to this set of hosts
     *
     * @return the home directory as an platform dependent absolute path.
     */
    public String getHomeDirectory();

    /**
     * Returns the Operating System associated to this set of hosts
     *
     * @return the Operating System
     */
    public OperatingSystem getOS();

    /**
     * Returns the set of available tools on this set of hosts
     *
     * @return A set of available tools as declared inside the GCM Deployment Descriptor
     */
    public Set<Tool> getTools();

    /**
     * Returns the Tool identified by this id
     *
     * @param id the identifier of this tool
     * @return If declared the Tool is returned. Otherwise null is returned
     */
    public Tool getTool(final String id);

    /**
     * Returns the capacity of this host
     *
     * @return the capacity of this host
     */
    public int getHostCapacity();

    /**
     * Returns the VM capacity of this host
     *
     * @return the VM capacity of this host
     */
    public int getVmCapacity();

    public boolean isCapacitiyValid();

    /**
     * @return Data Spaces scratch space access URL
     */
    public String getDataSpacesScratchURL();

    /**
     * @return Data Spaces scratch space local path;
     */
    public PathElement getDataSpacesScratchPath();

    /**
     * Check that this bridge is in a consistent state and is ready to be
     * used.
     *
     * @throws IllegalStateException thrown if anything is wrong
     */
    public void check() throws IllegalStateException;

    /**
     * Return the deployment id of this HostInfo.
     *
     * A Deployment ID is an uniq ID associated to an HostInfo at runtime. This
     * ID can be used to discover topology and manage the application. This ID is a link
     * between GCM descriptors and middlewares/runtime.
     *
     * @return the deployment ID
     */
    public long getToplogyId();

    /**
     * Set the deployment id of this HostInfo
     */
    public void setTopologyId(long topologyId);

    public void setNetworkInterface(String inet);

    public String getNetworkInterface();
}
