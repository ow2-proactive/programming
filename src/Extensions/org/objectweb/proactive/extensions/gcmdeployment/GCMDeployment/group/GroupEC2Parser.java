package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GroupEC2Parser extends AbstractGroupParser {

    private static final String NODE_NAME = "ec2Group";

    private static final String XPATH_PRIVATEKEY = "dep:privateKey";
    private static final String XPATH_CERTIFICATION = "dep:certification";

    @Override
    public AbstractGroup createGroup() {
        return new GroupEC2();
    }

    @Override
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {

        GroupEC2 ec2Group = (GroupEC2) super.parseGroupNode(groupNode, xpath);

        try {

            Node privateKey = (Node) xpath.evaluate(XPATH_PRIVATEKEY, groupNode, XPathConstants.NODE);
            Node certification = (Node) xpath.evaluate(XPATH_CERTIFICATION, groupNode, XPathConstants.NODE);

            ec2Group.setPrivateKey(GCMParserHelper.parsePathElementNode(privateKey));
            ec2Group.setCertification(GCMParserHelper.parsePathElementNode(certification));

        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }


        return ec2Group;
    }

}
