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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge;

import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;


public class BridgeSSH extends AbstractBridge {
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
