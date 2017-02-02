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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;


public class GCMDeploymentResources {
    private List<Group> groups = Collections.synchronizedList(new ArrayList<Group>());

    private List<Bridge> bridges = Collections.synchronizedList(new ArrayList<Bridge>());

    private HostInfo hostInfo;

    public List<Group> getGroups() {
        return groups;
    }

    public List<Bridge> getBridges() {
        return bridges;
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public void addBridge(Bridge bridge) {
        bridges.add(bridge);
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    protected void setHostInfo(HostInfo hostInfo) {
        assert (this.hostInfo == null);
        this.hostInfo = hostInfo;
    }

    public void check() throws IllegalStateException {
        for (Group group : groups)
            group.check();

        for (Bridge bridge : bridges)
            bridge.check();

        hostInfo.check();
    }
}
