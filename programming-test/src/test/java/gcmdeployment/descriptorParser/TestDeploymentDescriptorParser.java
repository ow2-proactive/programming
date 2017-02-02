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
package gcmdeployment.descriptorParser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.GCMDeploymentParserImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge.AbstractBridge;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.bridge.AbstractBridgeParser;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroup;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group.AbstractGroupParser;
import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * Add -Djaxp.debug=1 for, well, JAXP debugging
 * 
 * @author The ProActive Team
 * 
 */
public class TestDeploymentDescriptorParser {

    @Test
    public void test() throws Exception {
        File descriptor = new File(this.getClass().getResource("testfiles/deployment.xml").toURI());

        System.out.println("Parsing " + descriptor.getAbsolutePath());
        GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(Helpers.fileToURL(descriptor), null);

        parser.parseInfrastructure();
        parser.parseResources();
    }

    @Test
    public void allGroupsTest() throws Exception {
        File descriptor = new File(this.getClass().getResource("testfiles/deployment/allGroupsExample.xml").toURI());

        System.out.println("Parsing " + descriptor.getAbsolutePath());
        GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(Helpers.fileToURL(descriptor), null);

        parser.parseInfrastructure();
        parser.parseResources();
    }

    //
    // Examples of custom group & bridge parsers
    //

    protected static class UserGroup extends AbstractGroup {
        @Override
        public List<String> internalBuildCommands(CommandBuilder commandBuilder) {
            return new ArrayList<String>();
        }
    }

    protected static class UserBridge extends AbstractBridge {
        @Override
        public String internalBuildCommand(CommandBuilder commandBuilder) {
            return "";
        }
    }

    protected static class UserGroupParser extends AbstractGroupParser {
        @Override
        public AbstractGroup createGroup() {
            return new UserGroup();
        }

        public String getNodeName() {
            return "pauext:myGroup";
        }

        @Override
        public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
            AbstractGroup group = super.parseGroupNode(groupNode, xpath);

            System.out.println("User Group Parser - someattr value = " +
                               groupNode.getAttributes().getNamedItem("someattr").getNodeValue());

            return group;
        }
    }

    protected static class UserBridgeParser extends AbstractBridgeParser {
        @Override
        public AbstractBridge createBridge() {
            return new UserBridge();
        }

        @Override
        public String getNodeName() {
            return "pauext:myBridge";
        }

        @Override
        public AbstractBridge parseBridgeNode(Node bridgeNode, XPath xpath) {
            AbstractBridge bridge = super.parseBridgeNode(bridgeNode, xpath);
            System.out.println("User Bridge Parser - someattr value = " +
                               bridgeNode.getAttributes().getNamedItem("someattr").getNodeValue());

            return bridge;
        }
    }

    @Test
    public void userSchemaTest() throws Exception {
        File descriptor = new File(getClass().getResource("testfiles/deployment/group_bridge_ext.xml").toURI());

        URL userSchema = getClass().getResource("testfiles/deployment/SampleDeploymentExtension.xsd");

        ArrayList<String> schemas = new ArrayList<String>();
        schemas.add(userSchema.toString());

        System.out.println("Parsing " + descriptor.getAbsolutePath() + " with custom schema " + userSchema);
        GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(Helpers.fileToURL(descriptor), null, schemas);

        parser.registerGroupParser(new UserGroupParser());
        parser.registerBridgeParser(new UserBridgeParser());

        parser.parseInfrastructure();
        parser.parseResources();
    }

    protected void idConstraintTest(String descriptorLocation) throws Exception {
        File descriptor = new File(this.getClass().getResource(descriptorLocation).toURI());

        System.out.println("Parsing " + descriptor.getAbsolutePath());
        boolean gotException = false;

        try {
            GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(Helpers.fileToURL(descriptor), null);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
            final String errMsg = "Duplicate key value";
            gotException = e.getMessage().contains(errMsg) || e.getException().getMessage().contains(errMsg);
        }

        Assert.assertTrue(descriptor.getAbsolutePath(), gotException);
    }

    @Test
    public void hostIdConstraintTest() throws Exception {
        idConstraintTest("testfiles/deployment/duplicateHostId.xml");
    }

    @Test
    public void groupIdConstraintTest() throws Exception {
        idConstraintTest("testfiles/deployment/duplicateGroupId.xml");
    }

    @Test
    public void bridgeIdConstraintTest() throws Exception {
        idConstraintTest("testfiles/deployment/duplicateBridgeId.xml");
    }

    protected void refConstraintTest(String descriptorLocation) throws Exception {
        File descriptor = new File(this.getClass().getResource(descriptorLocation).toURI());

        System.out.println("Parsing " + descriptor.getAbsolutePath());
        boolean gotException = false;

        try {
            GCMDeploymentParserImpl parser = new GCMDeploymentParserImpl(Helpers.fileToURL(descriptor), null);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        } catch (SAXException e) {
            e.printStackTrace();
            final String errMsg = "not found for identity constraint";
            gotException = e.getMessage().contains(errMsg) || e.getException().getMessage().contains(errMsg);
        }

        Assert.assertTrue(gotException);
    }

    @Test
    public void hostRefIdConstraintTest() throws Exception {
        refConstraintTest("testfiles/deployment/missingHostId.xml");
    }

    @Test
    public void groupRefIdConstraintTest() throws Exception {
        refConstraintTest("testfiles/deployment/missingGroupId.xml");
    }

    @Test
    public void groupHostRefIdConstraintTest() throws Exception {
        refConstraintTest("testfiles/deployment/missingGroupHostId.xml");
    }

    @Test
    public void bridgeRefIdConstraintTest() throws Exception {
        refConstraintTest("testfiles/deployment/missingBridgeId.xml");
    }
}
