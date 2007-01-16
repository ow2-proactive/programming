/* 
 * ################################################################
 * 
 * ProActive: The Java(TM) library for Parallel, Distributed, 
 *            Concurrent computing with Security and Mobility
 * 
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *  
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s): 
 * 
 * ################################################################
 */ 
package org.objectweb.proactive.core.descriptor.services;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.scheduler.Scheduler;
import org.objectweb.proactive.scheduler.SchedulerConstants;


/**
 * This class represents a service to acquire the nodes of a given Job
 * from the scheduler service
 *
 * This service can be defined and used transparently when using XML Deployment descriptor
 *
 * @author cjarjouh
 *
 */
public class SchedulerLookupService implements UniversalService,
    SchedulerConstants {
    protected static String serviceName = SCHEDULER_NODE_NAME;
    protected Scheduler scheduler;
    protected ProActiveRuntime[] runtimeArray;
    protected int askedNodes = 0;
    protected int minAskedNodes = 0;

    public SchedulerLookupService(String schedulerURL) {
        scheduler = Scheduler.connectTo(schedulerURL + "/" + serviceName);
        this.runtimeArray = null;
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#startService()
     */
    public ProActiveRuntime[] startService() throws ProActiveException {
        return null;
    }
    
    /**
     * This is the method to get nodes form the scheduler ressource manager.
     */
    public Node [] getNodes() {
    	String jobId = System.getProperty(JOB_ID);
        
    	return scheduler.getReservedNodes(jobId, askedNodes);
    }

    /**
     * @see org.objectweb.proactive.core.descriptor.services.UniversalService#getServiceName()
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @return the associated scheduler service.
     */
    public Scheduler getSchedulerService() {
        return this.scheduler;
    }
    
    /**
     * @return the askedNodes.
     */
    public int getNodeNumber() {
        return askedNodes;
    }

    /**
     * Sets the number of nodes to be acquired with this Scheduler service
     * @param nodeNumber The askedNodes to set.
     */
    public void setNodeNumber(int nodeNumber) {
        this.askedNodes = nodeNumber;
    }
    
    /**
     * @return Returns the min askedNodes number.
     */
    public int getMinNodeNumber() {
        return minAskedNodes;
    }

    /**
     * Sets the min number of nodes to be acquired with this Scheduler service.
     * By minimum we mark that if the right policy is selected this number would
     * be judged as suffisant to start the application.
     * @param nodeNumber The askedNodes to set.
     */
    public void setMinNodeNumber(int nodeNumber) {
        this.minAskedNodes = nodeNumber;
    }
}
