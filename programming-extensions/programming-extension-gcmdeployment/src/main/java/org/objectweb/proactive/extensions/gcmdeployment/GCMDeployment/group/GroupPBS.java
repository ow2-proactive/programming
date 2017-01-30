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


public class GroupPBS extends AbstractGroup {
    private String resources = null;

    private String wallTime;

    private int nodes;

    private int ppn;

    private String jobName;

    private String queueName;

    private String interactive;

    private String stdout;

    private String stderr;

    private String mailWhen;

    private String mailTo;

    private String joinOutput;

    private PathElement scriptLocation = new PathElement("dist/scripts/gcmdeployment/pbs.sh",
                                                         PathElement.PathBase.PROACTIVE);

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        StringBuilder command = new StringBuilder();

        // ProActive script and parameters are read from STDIN
        // echo "oar2.sh paCommand bookedNodeAcces hostcapacity ppn" | qsub ...
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
        command.append(" ");

        command.append(ppn);

        command.append('"');

        command.append(" | ");

        command.append(buildQsub());

        // Script

        List<String> ret = new ArrayList<String>();
        ret.add(command.toString());
        return ret;
    }

    @Override
    public List<String> internalBuildCommands(CommandBuilder commandBuilder) {
        return null;
    }

    private String buildQsub() {
        StringBuilder commandBuf = new StringBuilder();
        if (getCommandPath() != null) {
            commandBuf.append(getCommandPath());
        } else {
            commandBuf.append("qsub");
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
            commandBuf.append(" -N ");
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

        if (mailWhen != null) {
            commandBuf.append(" -m ");
            commandBuf.append(mailWhen);
            commandBuf.append(" ");
        }

        if (mailTo != null) {
            commandBuf.append(" -M ");
            commandBuf.append(mailTo);
            commandBuf.append(" ");
        }

        if (joinOutput != null) {
            commandBuf.append(" -j ");
            commandBuf.append(joinOutput);
            commandBuf.append(" ");
        }

        // Ressources
        commandBuf.append(" -l ");
        if (resources != null) {
            commandBuf.append(resources);
        } else {
            // build resources 
            if (wallTime != null) {
                commandBuf.append("walltime=");
                commandBuf.append(wallTime);
                commandBuf.append(",");
            }
            if (nodes != 0) {
                commandBuf.append("nodes=");
                commandBuf.append(nodes);
                if (ppn != 0) {
                    commandBuf.append(":ppn=");
                    commandBuf.append(ppn);
                }
                commandBuf.append(",");
            }
            // remove the extra ','
            commandBuf.setCharAt(commandBuf.length() - 1, ' ');
        }

        // argument - must be last append
        commandBuf.append(" ");
        return commandBuf.toString();
    }

    public void setInteractive(String interactive) {
        this.interactive = interactive;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public void setPPN(int processorPerNode) {
        this.ppn = processorPerNode;
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

    public void setMailWhen(String mailWhen) {
        this.mailWhen = mailWhen;
    }

    public void setMailTo(String mailTo) {
        this.mailTo = mailTo;
    }

    public void setJoinOutput(String nodeValue) {
        if ((nodeValue != null) && nodeValue.equals("true")) {
            this.joinOutput = "oe";
        } else {
            this.joinOutput = null;
        }
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}
