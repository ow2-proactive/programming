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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.VMInformation;


@PublicAPI
public class GCMHost implements Serializable {
    protected String hostname;

    protected Map<VMInformation, GCMRuntime> runtimes;

    public GCMHost(String hostname, Set<Node> nodes) {
        super();
        this.hostname = hostname;
        this.runtimes = new HashMap<VMInformation, GCMRuntime>();

        update(nodes);
    }

    static private Map<VMInformation, Set<Node>> groupByRutnime(Set<Node> nodes) {
        Map<VMInformation, Set<Node>> ret = new HashMap<VMInformation, Set<Node>>();
        for (Node node : nodes) {
            VMInformation vmi = node.getVMInformation();
            if (ret.get(vmi) == null) {
                ret.put(vmi, new HashSet<Node>());
            }
            Set<Node> nodeSet = ret.get(vmi);
            nodeSet.add(node);
        }
        return ret;
    }

    public String getHostname() {
        return hostname;
    }

    public List<GCMRuntime> getRuntimes() {
        return new ArrayList<GCMRuntime>(runtimes.values());
    }

    public void update(Set<Node> nodes) {
        Map<VMInformation, Set<Node>> byRuntime = groupByRutnime(nodes);
        for (VMInformation vmi : byRuntime.keySet()) {
            if (runtimes.containsKey(vmi)) {
                runtimes.get(vmi).update(byRuntime.get(vmi));
            } else {
                GCMRuntime runtime = new GCMRuntime(vmi, byRuntime.get(vmi));
                runtimes.put(vmi, runtime);
            }
        }
    }
}
