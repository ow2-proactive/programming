/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package org.objectweb.proactive.core.descriptor.data;

import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.process.ExternalProcess;


/**
 * A <code>VirtualMachine</code> is a conceptual entity that represents
 * a JVM running a ProActiveRuntime
 *
 * @author The ProActive Team
 * @version 1.0,  2002/09/20
 * @since   ProActive 0.9.4
 *
 */

public class VirtualMachineImpl implements VirtualMachine, java.io.Serializable {

    private static final long serialVersionUID = 60L;
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //

    /** the name of this VirtualMachine */
    private String name;

    /** number of nodes that will be deployed on this VM. One node is the default */
    private int nbNodes = 1;

    /** indiquates if this machine results from a lookup or not  */
    private boolean hasProcess = true;

    /** the process to start in order to create the JVM */
    private ExternalProcess process;

    /** the service to start in order to acquire the JVM */
    private UniversalService service;

    /** The name of the VirtualNode that created this VirtualMachine */
    private String creatorId = null;

    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //

    /**
     * Contructs a new intance of VirtualNode
     */
    VirtualMachineImpl() {
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    public void setNbNodes(int nbNodes) {
        this.nbNodes = nbNodes;
    }

    public int getNbNodesOnCreatedVMs() {
        return this.nbNodes;
    }

    public void setName(String s) {
        name = s;
    }

    public String getName() {
        return name;
    }

    public void setProcess(ExternalProcess p) {
        process = p;
    }

    public ExternalProcess getProcess() {
        return process;
    }

    /**
     * Returns the name of the machine where the process mapped to this virtual machine
     * was launched.
     * @return String
     */
    public String getHostName() {
        String hostName = process.getHostname();
        if (hostName == null) {
            hostName = "localhost";
        }
        return hostName;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorId() {
        return this.creatorId;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.VirtualMachine#hasProcess()
     */
    public boolean hasProcess() {
        return hasProcess;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.VirtualMachine#setService(org.objectweb.proactive.core.descriptor.services.UniversalService)
     */
    public void setService(UniversalService service) {
        hasProcess = false;
        this.service = service;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.ServiceUser#getUserClass()
     */
    public String getUserClass() {
        return this.getClass().getName();
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.data.VirtualMachine#getService()
     */
    public UniversalService getService() {
        return service;
    }
}
