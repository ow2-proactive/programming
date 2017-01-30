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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroup;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroupSchedulerParser;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class GroupARCParser extends AbstractGroupSchedulerParser {
    private static final String XPATH_TRANSFER = "transfer";

    private static final String NODE_NAME_NOTIFY = "notify";

    private static final String NODE_NAME_MAX_TIME = "maxTime";

    private static final String NODE_NAME_STDIN = "stdin";

    private static final String NODE_NAME_STDERR = "stderr";

    private static final String NODE_NAME_STDOUT = "stdout";

    private static final String NODE_NAME_OUTPUT_FILES = "outputFiles";

    private static final String NODE_NAME_INPUT_FILES = "inputFiles";

    private static final String NODE_NAME_ARGUMENTS = "arguments";

    private static final String NODE_NAME_COUNT = "count";

    private static final String NODE_NAME = "arcGroup";

    private static final String ATTR_JOB_NAME = "jobName";

    @Override
    public AbstractGroup createGroup() {
        return new GroupARC();
    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        GroupARC arcGroup = (GroupARC) super.parseGroupNode(groupNode, xpath);

        try {
            String t = GCMParserHelper.getAttributeValue(groupNode, ATTR_JOB_NAME);
            arcGroup.setJobName(t);

            NodeList childNodes = groupNode.getChildNodes();
            for (int j = 0; j < childNodes.getLength(); ++j) {
                Node child = childNodes.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String nodeName = child.getNodeName();
                String nodeValue = GCMParserHelper.getElementValue(child);

                if (nodeName.equals(NODE_NAME_COUNT)) {
                    arcGroup.setCount(nodeValue);
                } else if (nodeName.equals(NODE_NAME_ARGUMENTS)) {
                    List<String> args = GCMParserHelper.parseArgumentListNode(xpath, child);
                    arcGroup.setArguments(args);
                } else if (nodeName.equals(NODE_NAME_INPUT_FILES)) {
                    List<FileTransfer> inputFilesList = parseFileTransferList(child, xpath);
                    arcGroup.setInputFiles(inputFilesList);
                } else if (nodeName.equals(NODE_NAME_OUTPUT_FILES)) {
                    List<FileTransfer> outputFilesList = parseFileTransferList(child, xpath);
                    arcGroup.setOutputFiles(outputFilesList);
                } else if (nodeName.equals(NODE_NAME_STDOUT)) {
                    arcGroup.setStdout(nodeValue);
                } else if (nodeName.equals(NODE_NAME_STDERR)) {
                    arcGroup.setStderr(nodeValue);
                } else if (nodeName.equals(NODE_NAME_STDIN)) {
                    arcGroup.setStdin(nodeValue);
                } else if (nodeName.equals(NODE_NAME_MAX_TIME)) {
                    arcGroup.setMaxTime(nodeValue);
                } else if (nodeName.equals(NODE_NAME_NOTIFY)) {
                    arcGroup.setNotify(nodeValue);
                }
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.fatal(e.getMessage());
        }

        return arcGroup;
    }

    static public class FileTransfer {
        private static final String ATTR_LOCATION = "location";

        private static final String ATTR_FILENAME = "filename";

        public String filename;

        public String location;

        FileTransfer(Node fileTransferNode) {
            filename = GCMParserHelper.getAttributeValue(fileTransferNode, ATTR_FILENAME);
            location = GCMParserHelper.getAttributeValue(fileTransferNode, ATTR_LOCATION);
        }
    }

    protected List<FileTransfer> parseFileTransferList(Node fileTransferListNode, XPath xpath)
            throws XPathExpressionException {
        List<FileTransfer> res = new ArrayList<FileTransfer>();

        NodeList ftNodes = (NodeList) xpath.evaluate(XPATH_TRANSFER, fileTransferListNode, XPathConstants.NODESET);

        for (int i = 0; i < ftNodes.getLength(); ++i) {
            Node ftNode = ftNodes.item(i);
            FileTransfer ft = new FileTransfer(ftNode);
            res.add(ft);
        }
        return res;
    }
}
