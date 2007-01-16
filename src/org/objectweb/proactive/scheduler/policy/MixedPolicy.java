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
package org.objectweb.proactive.scheduler.policy;

import java.util.Vector;

import org.objectweb.proactive.scheduler.GenericJob;
import org.objectweb.proactive.scheduler.RessourceManager;


/**
 * This is the mixed policy class that takes, while instanciating, the policy names and
 * creates a vector of the policies for the only purpose of using its comparor
 * @author cjarjouh
 *
 */
public class MixedPolicy extends AbstractPolicy {
    private Vector policies;

    public MixedPolicy() {
        // TODO Auto-generated constructor stub
    }

    /**
     * Here we create a mixed policy job manager by submitting the ressource manager
     * object and a vector of the policies that shall form this policy.
     * @param ressourceManager the reference to the ressource manager active object
     *                 responsible of the ressourc allocation
     * @param policyNames a vector of the policies names that form this policy
     */
    public MixedPolicy(RessourceManager ressourceManager, Vector policyNames) {
        super(ressourceManager);
        this.policies = new Vector();
        for (int i = 0; i < policyNames.size(); ++i) {
            String policyName = (String) policyNames.get(i);
            try {
                policies.add(Class.forName(policyName).newInstance());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns true if job1 is to be served before job2 according to the policy.
     * @param job1
     * @param job2
     * @return true if job1 is to be served before job2.
     */
    public boolean isToBeServed(GenericJob job1, GenericJob job2) {
        // TODO Auto-generated method stub
        for (int i = 0; i < this.policies.size(); ++i) {
            AbstractPolicy policy = (AbstractPolicy) this.policies.get(i);
            if (policy.isToBeServed(job2, job1)) {
                return false;
            }
        }

        return true;
    }
}
