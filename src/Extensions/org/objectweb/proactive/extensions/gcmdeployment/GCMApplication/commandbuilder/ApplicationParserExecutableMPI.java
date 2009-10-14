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