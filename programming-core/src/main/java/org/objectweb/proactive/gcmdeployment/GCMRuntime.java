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
package org.objectweb.proactive.gcmdeployment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.VMInformation;


@PublicAPI
public class GCMRuntime implements Serializable {
    protected VMInformation vmInfo;

    protected List<Node> nodes;

    public GCMRuntime(VMInformation vmInfo, Set<Node> nodes) {
        super();
        this.vmInfo = vmInfo;
        this.nodes = new ArrayList<Node>(nodes);
    }

    public String getName() {
        return vmInfo.getName();
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void update(Set<Node> nodes) {
        for (Node node : nodes) {
            if (!this.nodes.contains(node)) {
                this.nodes.add(node);
            }
        }
    }
}
