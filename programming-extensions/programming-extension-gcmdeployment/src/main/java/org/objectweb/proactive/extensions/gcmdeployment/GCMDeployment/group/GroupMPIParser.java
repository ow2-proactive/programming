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

import javax.xml.xpath.XPath;

import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;


public class GroupMPIParser extends AbstractGroupParser {
    private static final String ATTR_COMMAND_OPTIONS = "commandOptions";

    private static final String ATTR_MACHINE_FILE = "machineFile";

    private static final String ATTR_EXEC_DIR = "execDir";

    private static final String ATTR_HOST_LIST = "hostList";

    private static final String ATTR_MPI_DISTRIBUTION_PATH = "distributionPath";

    static final String NODE_NAME = "mpiGroup";

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupMPI groupMPI = (GroupMPI) super.parseGroupNode(groupNode, xpath);

        // Mandatory attributes
        String hostList = GCMParserHelper.getAttributeValue(groupNode, ATTR_HOST_LIST);
        groupMPI.setHostList(hostList);

        String commandOptions = GCMParserHelper.getAttributeValue(groupNode, ATTR_COMMAND_OPTIONS);
        if (commandOptions != null) {
            groupMPI.setCommandOption(commandOptions);
        }

        String machineFile = GCMParserHelper.getAttributeValue(groupNode, ATTR_MACHINE_FILE);
        if (machineFile != null) {
            groupMPI.setMachineFile(machineFile);
        }

        String execDir = GCMParserHelper.getAttributeValue(groupNode, ATTR_EXEC_DIR);
        if (execDir != null) {
            groupMPI.setExecDir(execDir);
        }

        String mpiDistributionPath = GCMParserHelper.getAttributeValue(groupNode, ATTR_MPI_DISTRIBUTION_PATH);
        groupMPI.setMpiDistributionPath(mpiDistributionPath);

        return groupMPI;
    }

    @Override
    public AbstractGroup createGroup() {
        return new GroupMPI();
    }

    public String getNodeName() {
        return NODE_NAME;
    }
}
