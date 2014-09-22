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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.ListGenerator;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;


public class GroupSSH extends AbstractGroup {

    private static final long serialVersionUID = 60L;
    public final static String DEFAULT_SSHPATH = "ssh";
    private String hostList;
    private String username;
    private String commandOptions;
    private PathElement privateKey;

    public GroupSSH() {
        setCommandPath(DEFAULT_SSHPATH);
        hostList = "";
        username = null;
        commandOptions = null;
        privateKey = null;
    }

    public GroupSSH(GroupSSH groupSSH) {
        super(groupSSH);
        this.hostList = groupSSH.hostList;
        this.username = groupSSH.username;
        this.commandOptions = groupSSH.commandOptions;
        this.privateKey = groupSSH.privateKey;
    }

    public String getCommandOption() {
        return commandOptions;
    }

    public void setCommandOption(String commandOption) {
        this.commandOptions = commandOption;
    }

    public void setHostList(String hostList) {
        this.hostList = hostList;
    }

    @Override
    public List<String> internalBuildCommands(CommandBuilder commandBuilder) {
        List<String> commands = new ArrayList<String>();

        for (String hostname : ListGenerator.generateNames(hostList)) {
            String command = makeSingleCommand(hostname, commandBuilder);
            commands.add(command);
        }

        return commands;
    }

    /**
     * return ssh command given the hostname, e.g. :
     *
     * ssh -l username hostname.domain
     *
     * @param hostname
     * @param commandBuilder 
     * @return
     */
    private String makeSingleCommand(String hostname, CommandBuilder commandBuilder) {
        StringBuilder res = new StringBuilder(getCommandPath());
        res.append(" ");

        if (username != null) {
            res.append("-l ").append(username);
            res.append(" ");
        }

        if (privateKey != null) {
            res.append(" -i ");
            res.append(privateKey.getFullPath(getHostInfo(), commandBuilder));
            res.append(" ");
        }

        if (commandOptions != null) {
            res.append(" ");
            res.append(commandOptions);
            res.append(" ");
        }

        res.append(hostname);

        return res.toString();
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPrivateKey(PathElement privateKey) {
        this.privateKey = privateKey;
    }
}
