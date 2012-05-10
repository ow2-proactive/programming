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
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder;

import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.NodeProvider;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.TechnicalServicesProperties;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ApplicationParserExecutableMPI extends AbstractApplicationParser {

    private static final String XPATH_NODE_PROVIDER = "app:nodeProvider";
    private static final String XPATH_PATH = "app:path";
    private static final String XPATH_COMMAND = "app:command";
    private static final String XPATH_ARG = "app:arg";

    protected static final String NODE_NAME = "mpi";
    private TechnicalServicesProperties applicationTechnicalServices;

    @Override
    protected CommandBuilder createCommandBuilder() {
        return new CommandBuilderExecutableMPI();

    }

    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public void parseApplicationNode(Node appNode, GCMApplicationParser applicationParser, XPath xpath)
            throws Exception {
        super.parseApplicationNode(appNode, applicationParser, xpath);

        CommandBuilderExecutableMPI commandBuilderMPI = (CommandBuilderExecutableMPI) commandBuilder;

        Node techServicesNode = (Node) xpath.evaluate(XPATH_TECHNICAL_SERVICES, appNode, XPathConstants.NODE);
        if (techServicesNode != null) {
            applicationTechnicalServices = GCMParserHelper
                    .parseTechnicalServicesNode(xpath, techServicesNode);
        } else {
            applicationTechnicalServices = new TechnicalServicesProperties();
        }

        NodeList nodeProviderNodes;
        nodeProviderNodes = (NodeList) xpath.evaluate(XPATH_NODE_PROVIDER, appNode, XPathConstants.NODESET);
        Map<String, NodeProvider> nodeProvidersMap = applicationParser.getNodeProviders();

        // resource providers
        //
        for (int i = 0; i < nodeProviderNodes.getLength(); ++i) {
            Node rpNode = nodeProviderNodes.item(i);
            String refid = GCMParserHelper.getAttributeValue(rpNode, "refid");
            NodeProvider nodeProvider = nodeProvidersMap.get(refid);
            if (nodeProvider != null) {
                commandBuilderMPI.addDescriptor(nodeProvider);
            } else {
                // TODO - log warning
            }
        }

        Node pathNode = (Node) xpath.evaluate(XPATH_PATH, appNode, XPathConstants.NODE);
        if (pathNode != null) {
            // path tag is optional
            commandBuilderMPI.setPath(GCMParserHelper.parsePathElementNode(pathNode));
        }

        Node commandNode = (Node) xpath.evaluate(XPATH_COMMAND, appNode, XPathConstants.NODE);
        String name = GCMParserHelper.getAttributeValue(commandNode, "name");
        commandBuilderMPI.setCommand(name);

        // command args
        //
        NodeList argNodes = (NodeList) xpath.evaluate(XPATH_ARG, commandNode, XPathConstants.NODESET);

        for (int i = 0; i < argNodes.getLength(); ++i) {
            Node argNode = argNodes.item(i);
            String argVal = argNode.getFirstChild().getNodeValue();
            commandBuilderMPI.addArg(argVal);
        }
    }

    public TechnicalServicesProperties getTechnicalServicesProperties() {
        return applicationTechnicalServices;
    }
}