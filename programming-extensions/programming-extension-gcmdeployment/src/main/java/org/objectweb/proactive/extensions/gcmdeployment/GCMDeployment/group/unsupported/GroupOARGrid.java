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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.unsupported;

import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroup;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;


public class GroupOARGrid extends AbstractGroup {
    private String queueName;

    private String accessProtocol;

    private String wallTime;

    private String resources;

    private PathElement scriptLocation;

    @Override
    public List<String> internalBuildCommands(CommandBuilder commandBuilder) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setAccessProtocol(String accessProtocol) {
        this.accessProtocol = accessProtocol;
    }

    public void setScriptLocation(PathElement path) {
        this.scriptLocation = path;
    }

    public void setWallTime(String wallTime) {
        this.wallTime = wallTime;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}
