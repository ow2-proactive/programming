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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication;

import java.rmi.AlreadyBoundException;
import java.util.List;

import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeInternal;


public class FakeNode {
    private GCMApplicationInternal gcma;
    private ProActiveRuntime part;
    private boolean created;

    public FakeNode(GCMApplicationInternal gcma, ProActiveRuntime part) {
        this.part = part;
        this.gcma = gcma;

        created = false;
    }

    public ProActiveRuntime getProActiveRuntime() {
        return part;
    }

    public String getRuntimeURL() {
        return part.getURL();
    }

    public long getCapacity() {
        return part.getVMInformation().getCapacity();
    }

    public Node create(GCMVirtualNodeInternal vn, List<TechnicalService> tsList) throws NodeException {

        Node node = null;
        if (!created) {
            String jobIb = new Long(gcma.getDeploymentId()).toString();

            try {

                //create the node
                ProActiveSecurityManager siblingPSM = null;
                if (this.gcma.getProActiveApplicationSecurityManager() != null) {
                    siblingPSM = this.gcma.getProActiveApplicationSecurityManager()
                            .generateSiblingCertificate(EntityType.NODE, vn.getName());
                }

                node = part.createGCMNode(siblingPSM, vn.getName(), jobIb, tsList);
                gcma.addNode(node);
            } catch (AlreadyBoundException e) {
                new NodeException(e);
            }
        }
        return node;
    }

}
