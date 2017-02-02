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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;


/**
 *
 * TODO (long term) Transform this class into a real tree.
 *         This implementation duplicates Bridge and Tree like in the previous
 *  ProActive deployment framework.
 *
 * TODO Allow to start a command on intermediate Bridges
 *
 */
public class GCMDeploymentInfrastructure {
    private Map<String, Group> groups;

    private Map<String, Bridge> bridges;

    private Map<String, HostInfo> hosts;

    public GCMDeploymentInfrastructure() {
        groups = new HashMap<String, Group>();
        bridges = new HashMap<String, Bridge>();
        hosts = new HashMap<String, HostInfo>();
    }

    public Map<String, Group> getGroups() {
        return groups;
    }

    public Map<String, Bridge> getBridges() {
        return bridges;
    }

    public Map<String, HostInfo> getHosts() {
        return hosts;
    }

    public void addGroup(Group group) {
        groups.put(group.getId(), group);
    }

    public void addBrige(Bridge bridge) {
        bridges.put(bridge.getId(), bridge);
    }

    public void addHost(HostInfo host) {
        hosts.put(host.getId(), host);
    }
}
