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

import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;


public class GroupGridEngine extends AbstractGroup {

    private static final long serialVersionUID = 62L;
    private String resources = null;
    private String wallTime;
    private String parallelEnvironment;
    private String nodes; // String since n, -n, n-m, n- should be allowed

    private String queueName;
    private String jobName;

    private String stdout;
    private String stderr;
    private String directory;

    private PathElement scriptLocation = new PathElement("dist/scripts/gcmdeployment/gridEngine.sh",
        PathElement.PathBase.PROACTIVE);

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        StringBuilder command = new StringBuilder();

        // ProActive script and parameters are read from STDIN
        // echo "oar2.sh paCommand bookedNodeAcces hostcapacity" | qsub ...
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

        // Ressources
        if (resources != null) {
            commandBuf.append(resources);
        } else {

            // build resources 
            if (wallTime != null) {
                commandBuf.append("-l h_rt=");
                commandBuf.append(wallTime);
                commandBuf.append(" ");
            }

            if (parallelEnvironment != null && nodes != null) {
                commandBuf.append("-pe ");
                commandBuf.append(parallelEnvironment);
                commandBuf.append(" ");
                commandBuf.append(nodes);
                commandBuf.append(" ");
            }
        }

        // argument - must be last append
        commandBuf.append(" ");
        return commandBuf.toString();
    }

    public void setQueue(String queue) {
        this.queueName = queue;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     *  Set the booking duration of the cluster's nodes. The default is 00:01:00
     * @param d duration
     */
    public void setWallTime(String d) {
        this.wallTime = d;
    }

    /**
     * Sets the number of nodes requested when running the job
     * @param nodes the number of nodes requested when running the job
     */
    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    /**
     * Sets the parallel environment for this GridEngineSubProcess
     * @param p the parallel environment to use
     */
    public void setParallelEnvironment(String p) {
        this.parallelEnvironment = p;
    }

    /**
     * Returns the parallel environment for this GridEngineSubProcess
     * @return the parallel environment for this GridEngineSubProcess
     */
    public String getParallelEnvironment() {
        return this.parallelEnvironment;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}
