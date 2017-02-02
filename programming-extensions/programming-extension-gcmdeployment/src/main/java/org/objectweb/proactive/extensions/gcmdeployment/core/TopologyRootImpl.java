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
package org.objectweb.proactive.extensions.gcmdeployment.core;

import java.util.HashMap;
import java.util.Map;


public class TopologyRootImpl extends TopologyImpl {
    protected Map<Long, TopologyImpl> nodeMap;

    public TopologyRootImpl() {
        nodeMap = new HashMap<Long, TopologyImpl>();
    }

    public boolean isRoot() {
        return nodeMap != null;
    }

    public void addNode(TopologyImpl node, TopologyImpl parent) {
        parent.addChildren(node);
        nodeMap.put(node.getId(), node);
    }

    public TopologyImpl getNode(Long topologyId) {
        return nodeMap.get(topologyId);
    }
}
