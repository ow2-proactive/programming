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
package org.objectweb.proactive.scheduler;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * Class that contains the description of the job and the reference to an
 * agent that keeps track of the job evolution. This agent helps setting
 * the main JVM system properties as well a simple method to make sure
 * that this node stays alive.
 * @author cjarjouh
 *
 */
public class DeployedTask {
    private GenericJob jobDescription;
    private Agent agent;

    public DeployedTask(GenericJob jobDescription, Agent agent) {
        this.setAgent(agent);
        this.setTaskDescription(jobDescription);

        // TODO Auto-generated constructor stub
    }

    public void setTaskDescription(GenericJob jobDescription) {
        this.jobDescription = jobDescription;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public GenericJob getTaskDescription() {
        return this.jobDescription;
    }

    /**
     * this method is a sort of pinging method. It gives the status
     * of the main node (alive or dead)
     * @return true if the main node is alive, false otherwise
     */
    public BooleanWrapper isAlive() {
        if (agent == null) {
            return new BooleanWrapper(true);
        }

        try {
            this.agent.ping();

            return new BooleanWrapper(true);
        } catch (Exception e) {
            return new BooleanWrapper(false);
        }
    }
}
