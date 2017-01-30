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
package org.objectweb.proactive.core.descriptor.data;

import java.io.IOException;

import org.objectweb.proactive.core.descriptor.services.ServiceUser;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.process.ExternalProcess;


/**
 * A <code>VirtualMachine</code> is a conceptual entity that represents
 * a JVM running a ProActiveRuntime
 *
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.3
 * @see ProActiveDescriptorInternal
 * @see VirtualNodeInternal
 */
public interface VirtualMachine extends ServiceUser {

    /**
     * Sets the number of nodes that will be created on this VirtualMachine.
     * @param nodeNumber
     * @throws IOException
     */
    public void setNbNodes(int nodeNumber) throws java.io.IOException;

    /**
     * Returns the number of nodes that will be created for each of the virtual machines
     * associated to this VirtualMachine object
     * @return String
     */
    public int getNbNodesOnCreatedVMs();

    /**
     * Sets the name of this VirtualMachine
     * @param s
     */
    public void setName(String s);

    /**
     * Returns the name of this VirtualMachine
     * @return String
     */
    public String getName();

    //    /**
    //     * Sets the acquisitionMethod field to the given value
    //     * @param s
    //     */
    //    public void setAcquisitionMethod(String s);
    //
    //    /**
    //     * Returns the AcquisitionMethod value
    //     * @return String
    //     */
    //    public String getAcquisitionMethod();
    //    /**
    //     * Sets the port number of the acquisition process
    //     * @param s
    //     */
    //    public void setPortNumber(String s);
    //
    //    /**
    //     * Return the Acquisition port number
    //     * @param s
    //     */
    //    public String getPortNumber();

    /**
     * Sets the process mapped to this VirtualMachine to the given process
     * @param p
     */
    public void setProcess(ExternalProcess p);

    /**
     * Returns the process mapped to this VirtualMachine
     * @return ExternalProcess
     */
    public ExternalProcess getProcess();

    /**
     * Sets the service mapped to this VirtualMachine to the given service
     * @param service
     */
    public void setService(UniversalService service);

    /**
     * Returns the service mapped to this VirtualMachine
     * @return the service mapped to this VirtualMachine
     */
    public UniversalService getService();

    /**
     * Returns the name of the machine where the process mapped to this virtual machine
     * was launched.
     * @return String
     */
    public String getHostName();

    /**
     * Sets the creatorId field to the given value
     * @param creatorId The Id of the VirtualNode that created this VirtualMachine
     */
    public void setCreatorId(String creatorId);

    /**
     * Returns the value of creatorId field.
     * @return String The Id of the VirtualNode that created this VirtualMachine
     */
    public String getCreatorId();

    /**
     * Returns true if this machine his mapped onto a process false if mapped
     * onto a service
     * @return boolean if the machine result of a lookup
     */
    public boolean hasProcess();
}
