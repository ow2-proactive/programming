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
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication;

import java.util.List;

import org.objectweb.proactive.core.descriptor.services.TechnicalService;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
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
            //            String jobIb = new Long(gcma.getDeploymentId()).toString();

            try {
                node = part.createGCMNode(vn.getName(), tsList);
                if (node == null) {
                    // Remote Object failed to contact the Runtime and returned null
                    // instead of throwing an exception
                    throw new NodeException("Failed to create a GCM node, node is null");
                }
                gcma.addNode(node);
            } catch (NodeException e) {
                throw e;
            } catch (Exception e) {
                throw new NodeException(e);
            }
        }
        return node;
    }

}
