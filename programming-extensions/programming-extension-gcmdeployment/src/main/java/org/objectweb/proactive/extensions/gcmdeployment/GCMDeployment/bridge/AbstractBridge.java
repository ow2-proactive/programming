/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;


public abstract class AbstractBridge implements Bridge {

    private static final long serialVersionUID = 62L;
    private String commandPath;
    private String env;
    private String hostname;
    private String username;
    private String id;

    public void setCommandPath(String commandPath) {
        this.commandPath = commandPath;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEnvironment(String env) {
        this.env = env;
    }

    protected String getCommandPath() {
        return commandPath;
    }

    protected String getHostname() {
        return hostname;
    }

    protected String getUsername() {
        return username;
    }

    protected String getEnv() {
        return env;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*
     * ------ Infrastructure tree operations & data
     */
    private List<Bridge> bridges = Collections.synchronizedList(new ArrayList<Bridge>());
    private List<Group> groups = Collections.synchronizedList(new ArrayList<Group>());
    private HostInfo hostInfo = null;

    public void addBridge(Bridge bridge) {
        bridges.add(bridge);
    }

    public void addGroup(Group group) {
        groups.add(group);
    }

    public List<Bridge> getBridges() {
        return bridges;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public HostInfo getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    public void check() throws IllegalStateException {
        if (hostname == null) {
            throw new IllegalStateException("hostname is not set in " + this);
        }

        if (id == null) {
            throw new IllegalStateException("id is not set in " + this);
        }

        for (Bridge bridge : bridges)
            bridge.check();

        for (Group group : groups)
            group.check();

        if (hostInfo == null) {
            throw new IllegalStateException("hostInfo is not set in " + this);
        }
        hostInfo.check();
    }

    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        List<String> commands = new ArrayList<String>();

        if (hostInfo != null) {
            commands.add(commandBuilder.buildCommand(hostInfo, gcma));
        }

        for (Group group : groups) {
            commands.addAll(group.buildCommands(commandBuilder, gcma));
        }

        for (Bridge bridge : bridges) {
            commands.addAll(bridge.buildCommands(commandBuilder, gcma));
        }

        // Prefix each command with this bridge
        List<String> ret = new ArrayList<String>();
        for (String command : commands) {
            ret.add(internalBuildCommand(commandBuilder) + " " + Helpers.escapeCommand(command));
        }

        return ret;
    }

    /**
     * Returns the command corresponding to this bridge
     *
     * This method is called by the generic AbstractBridge.buildCommand
     * method to prepend this bridge to retrieved command for children nodes
     * of the tree
     * @param commandBuilder TODO
     *
     * @return
     */
    abstract public String internalBuildCommand(CommandBuilder commandBuilder);
}
