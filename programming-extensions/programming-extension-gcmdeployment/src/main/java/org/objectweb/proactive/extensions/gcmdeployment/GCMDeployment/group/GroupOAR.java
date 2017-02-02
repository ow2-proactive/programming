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


public class GroupOAR extends AbstractGroup {
    final static public int BEST = -1;

    protected static final String DEFAULT_HOSTS_NUMBER = "1";

    protected String hostNumber = DEFAULT_HOSTS_NUMBER;

    protected String OARSUB = "oarsub";

    protected boolean interactive = false;

    protected String queueName;

    private PathElement scriptLocation = new PathElement("dist/scripts/gcmdeployment/oar2.sh",
                                                         PathElement.PathBase.PROACTIVE);

    private String directory;

    private String stdout;

    private String stderr;

    private String type = null;

    private String resources = null;

    private String wallTime = null;

    private int nodes = 0;

    private int cpu = 0;

    private int core = 0;

    @Override
    public List<String> buildCommands(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        StringBuilder command = new StringBuilder();

        // OARSUB parameters
        command.append(buildOARSub());

        // Script
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

        List<String> ret = new ArrayList<String>();
        ret.add(command.toString());
        return ret;
    }

    @Override
    public List<String> internalBuildCommands(CommandBuilder commandBuilder) {
        return null;
    }

    public String buildOARSub() {
        StringBuilder commandBuf = new StringBuilder();
        if (getCommandPath() != null) {
            commandBuf.append(getCommandPath());
        } else {
            commandBuf.append(OARSUB);
        }
        commandBuf.append(" ");

        commandBuf.append(" ");
        if (type != null) {
            commandBuf.append(" --type ");
            commandBuf.append(type);
            commandBuf.append(" ");
        }

        if (interactive) {
            commandBuf.append(" --interactive");
            commandBuf.append(" ");
        }

        if (queueName != null) {
            commandBuf.append(" --queue=");
            commandBuf.append(queueName);
            commandBuf.append(" ");
        }

        commandBuf.append(" -l ");
        if (resources != null) {
            commandBuf.append(resources);
        } else {
            if (nodes != 0) {
                commandBuf.append("/nodes=" + nodes);
            }
            if (cpu != 0) {
                commandBuf.append("/cpu=" + cpu);
            }
            if (core != 0) {
                commandBuf.append("/core=" + core);
            }
            if (wallTime != null) {
                commandBuf.append(",walltime=" + wallTime);
            }

            // Remove extra ','
            if (commandBuf.charAt(commandBuf.length() - 1) == ',') {
                commandBuf.setCharAt(commandBuf.length() - 1, ' ');
            }
        }

        if (directory != null) {
            commandBuf.append(" --directory=");
            commandBuf.append(directory);
            commandBuf.append(" ");
        }

        if (stdout != null) {
            commandBuf.append(" --stdout=");
            commandBuf.append(stdout);
            commandBuf.append(" ");
        }

        if (stderr != null) {
            commandBuf.append(" --stderr=");
            commandBuf.append(stderr);
            commandBuf.append(" ");
        }

        // argument - must be last append
        commandBuf.append(" ");
        return commandBuf.toString();
    }

    public void setInteractive(String interactive) {
        this.interactive = Boolean.parseBoolean(interactive);
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setScriptLocation(PathElement location) {
        this.scriptLocation = location;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public void setCore(int core) {
        this.core = core;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    protected void setResourcesString() {
        resources = "";
    }

    public void setWallTime(String nodeValue) {
        this.wallTime = nodeValue;
    }
}
