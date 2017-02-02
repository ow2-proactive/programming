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

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;

import java.net.URL;
import java.util.List;

import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge.Bridge;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.Group;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfo;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;


public class GCMDeploymentDescriptorImpl implements GCMDeploymentDescriptor {
    private GCMDeploymentParser parser;

    private VariableContractImpl environment;

    private GCMDeploymentResources resources;

    private GCMDeploymentAcquisition acquisitions;

    public GCMDeploymentDescriptorImpl(URL descriptor, VariableContractImpl vContract) throws Exception {
        parser = new GCMDeploymentParserImpl(descriptor, vContract);
        environment = parser.getEnvironment();
        resources = parser.getResources();
        acquisitions = parser.getAcquisitions();
    }

    public void start(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        // Start Local JVMs
        startLocal(commandBuilder, gcma);

        startGroups(commandBuilder, gcma);
        startBridges(commandBuilder, gcma);
    }

    private void startLocal(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        HostInfo hostInfo = resources.getHostInfo();
        if (hostInfo != null) {
            // Something needs to be started on this host
            List<List<String>> command = commandBuilder.buildCommandLocal(hostInfo, gcma);

            GCMD_LOGGER.info("Starting a process on localhost");
            GCMD_LOGGER.debug("command= " + command);
            Executor.getExecutor().submit(command);
        }
    }

    private void startGroups(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        List<Group> groups = resources.getGroups();
        for (Group group : groups) {
            List<String> commands = group.buildCommands(commandBuilder, gcma);
            GCMD_LOGGER.info("Starting group id=" + group.getId() + " #commands=" + commands.size());

            for (String command : commands) {
                GCMD_LOGGER.debug("group id=" + group.getId() + " command= " + command);
                Executor.getExecutor().submit(command);
            }
        }
    }

    private void startBridges(CommandBuilder commandBuilder, GCMApplicationInternal gcma) {
        List<Bridge> bridges = resources.getBridges();
        for (Bridge bridge : bridges) {
            List<String> commands = bridge.buildCommands(commandBuilder, gcma);

            GCMD_LOGGER.info("Starting bridge id=" + bridge.getId() + " #commands=" + commands.size());

            for (String command : commands) {
                GCMD_LOGGER.debug("bridge id=" + bridge.getId() + " command= " + command);
                Executor.getExecutor().submit(command);
            }
        }
    }

    public VariableContractImpl getEnvironment() {
        return environment;
    }

    public GCMDeploymentResources getResources() {
        return resources;
    }

    public GCMDeploymentParser getParser() {
        return parser;
    }

    public GCMDeploymentAcquisition getAcquisitions() {
        return acquisitions;
    }

    public URL getDescriptorURL() {
        return parser.getDescriptorURL();
    }
}
