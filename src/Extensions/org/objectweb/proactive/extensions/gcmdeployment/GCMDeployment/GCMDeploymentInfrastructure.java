/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm.GCMVirtualMachineManager;


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
    private Map<String, GCMVirtualMachineManager> vmms;

    public GCMDeploymentInfrastructure() {
        groups = new HashMap<String, Group>();
        bridges = new HashMap<String, Bridge>();
        hosts = new HashMap<String, HostInfo>();
        vmms = new HashMap<String, GCMVirtualMachineManager>();
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

    public Map<String, GCMVirtualMachineManager> getVMM() {
        return vmms;
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

    public void addVMM(GCMVirtualMachineManager vmm) {
        vmms.put(vmm.getId(), vmm);
    }
}
