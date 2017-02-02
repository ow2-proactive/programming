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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;


public class GroupLSF extends AbstractGroup {
    private String resources = null;

    private String wallTime;

    private int processorNumber;

    private String stdout;

    private String stderr;

    private String interactive;

    private String jobName;

    private String queueName;

    private PathElement scriptLocation = new PathElement("dist/scripts/gcmdeployment/lsf.sh",
                                                         PathElement.PathBase.PROACTIVE);

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        StringBuilder command = new StringBuilder();

        // BSUB parameters
        command.append("echo ");
        command.append('"');
        command.append(scriptLocation.getFullPath(hostInfo, commandBuilder));
        command.append(" ");

        String cbCommand = commandBuilder.buildCommand(hostInfo, gcma);
        cbCommand = Helpers.escapeCommand(cbCommand);
        command.append(cbCommand);
        command.append(" ");

        command.append(getBookedNodesAccess());
        command.append(" ");

        command.append(hostInfo.getHostCapacity());

        command.append('"');

        command.append(" | ");

        command.append(buildBsub());

        // Script

        List<String> ret = new ArrayList<String>();
        ret.add(command.toString());
        return ret;
    }

    private String buildBsub() {
        StringBuilder commandBuf = new StringBuilder();
        if (getCommandPath() != null) {
            commandBuf.append(getCommandPath());
        } else {
            commandBuf.append("bsub");
        }
        commandBuf.append(" ");

        if (queueName != null) {
            commandBuf.append(" -q ");
            commandBuf.append(queueName);
            commandBuf.append(" ");
        }

        if (interactive != null) {
            commandBuf.append(" -I ");
        }

        if (jobName != null) {
            commandBuf.append(" -J ");
            commandBuf.append(jobName);
            commandBuf.append(" ");
        }

        if (stdout != null) {
            commandBuf.append(" -o ");
            commandBuf.append(stdout);
            commandBuf.append(" ");
        }

        if (stderr != null) {
            commandBuf.append(" -e ");
            commandBuf.append(stderr);
            commandBuf.append(" ");
        }

        // Ressources
        if (resources != null) {
            commandBuf.append(resources);
        } else {
            if (processorNumber != 0) {
                commandBuf.append(" -n ");
                commandBuf.append(processorNumber);
                commandBuf.append(" ");
            }

            // build resources 
            if (wallTime != null) {
                commandBuf.append(" -c ");
                commandBuf.append(wallTime);
                commandBuf.append(" ");
            }
        }

        return commandBuf.toString();
    }

    @Override
    public List<String> internalBuildCommands(CommandBuilder commandBuilder) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setProcessorNumber(int processorNumber) {
        this.processorNumber = processorNumber;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setWallTime(String wallTime) {
        this.wallTime = wallTime;
    }

    public void setStdout(String outputFile) {
        this.stdout = outputFile;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }
}
