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
package gcmdeployment.virtualnode;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.NodeProvider;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;


public class GCMApplicationDescriptorMockup implements GCMApplicationInternal {
    public long deploymentId;

    public GCMApplicationDescriptorMockup() {
        deploymentId = ProActiveRandom.nextInt();
    }

    public List<Node> getAllCurrentNodes() {
        throw new RuntimeException("Not implemented");
    }

    public Topology getTopology() {
        throw new RuntimeException("Not implemented");
    }

    public List<Node> getAllNodes() {
        throw new RuntimeException("Not implemented");
    }

    public long getDeploymentId() {
        return deploymentId;
    }

    public VariableContractImpl getVariableContract() {
        throw new RuntimeException("Not implemented");
    }

    public URL getDescriptorURL() {
        throw new RuntimeException("Not implemented");
    }

    public GCMVirtualNode getVirtualNode(String vnName) {
        throw new RuntimeException("Not implemented");
    }

    public Map<String, GCMVirtualNode> getVirtualNodes() {
        throw new RuntimeException("Not implemented");
    }

    public boolean isStarted() {
        throw new RuntimeException("Not implemented");
    }

    public void kill() {
        throw new RuntimeException("Not implemented");
    }

    public void startDeployment() {
        throw new RuntimeException("Not implemented");
    }

    public void updateTopology(Topology topology) {
        throw new RuntimeException("Not implemented");
    }

    public void addNode(Node node) {
        // Do nothing
    }

    public NodeProvider getNodeProviderFromTopologyId(Long topologyId) {
        throw new RuntimeException("Not implemented");
    }

    public String getDebugInformation() {
        throw new RuntimeException("Not implemented");
    }

    public long getNbUnmappedNodes() {
        throw new RuntimeException("Not implemented");
    }

    public void waitReady() {
        throw new RuntimeException("Not implemented");
    }

    public Set<String> getVirtualNodeNames() {
        throw new RuntimeException("Not implemented");
    }

    public void addDeployedRuntime(ProActiveRuntime part) {
        // Do nothing
    }

    public void waitReady(long timeout) throws ProActiveTimeoutException {
        // Do nothing
    }

    public String getLogCollectorUrl() {
        throw new RuntimeException("Not implemented");
    }
}
