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

import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;


public class BridgeSSH extends AbstractBridge {

    private static final long serialVersionUID = 61L;
    public final static String DEFAULT_SSHPATH = "ssh";
    private PathElement privateKey;
    private String commandOptions;

    public BridgeSSH() {
        setCommandPath(DEFAULT_SSHPATH);
        privateKey = null;
        commandOptions = null;
    }

    @Override
    public String internalBuildCommand(CommandBuilder commandBuilder) {
        StringBuilder command = new StringBuilder();
        command.append(getCommandPath());
        // append username
        if (getUsername() != null) {
            command.append(" -l ");
            command.append(getUsername());
        }

        if (privateKey != null) {
            command.append(" -i ");
            command.append(privateKey.getFullPath(getHostInfo(), commandBuilder));
            command.append(" ");
        }

        if (commandOptions != null) {
            command.append(" ");
            command.append(commandOptions);
            command.append(" ");
        }

        // append host
        command.append(" ");
        command.append(getHostname());
        command.append(" ");

        return command.toString();
    }

    public void setPrivateKey(PathElement privateKey) {
        this.privateKey = privateKey;
    }

    public String getCommandOptions() {
        return commandOptions;
    }

    public void setCommandOptions(String commandOptions) {
        this.commandOptions = commandOptions;
    }
}
