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
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationParser;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationParserImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.TechnicalServicesProperties;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.AbstractApplicationParser;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderExecutable;
import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import org.w3c.dom.Node;

import functionalTests.FunctionalTest;


public class TestApplicationDescriptorParser extends FunctionalTest {
    final static URL TEST_APP_DIR = TestApplicationDescriptorParser.class.getClass()
                                                                         .getResource("/gcmdeployment/descriptorParser/testfiles/application");

    final static String[] skipDescriptors = { "script_ext.xml", "oldDescriptor.xml", "scriptInvalid.xml",
                                              "script6.xml" };

    @Test
    public void test() throws Exception {
        descloop: for (File descriptor : getApplicationDescriptors()) {
            for (String skipIt : skipDescriptors) {
                if (descriptor.toString().contains(skipIt))
                    continue descloop;
            }

            System.out.println("parsing " + descriptor.getCanonicalPath());
            GCMApplicationParserImpl parser = new GCMApplicationParserImpl(Helpers.fileToURL(descriptor), null);

            parser.getCommandBuilder();
            parser.getVirtualNodes();
            parser.getNodeProviders();
        }
    }

    /**
     * User application node parser used to demonstrate how to install custom app parsers
     * @author The ProActive Team
     *
     */
    protected static class UserApplicationNodeParser extends AbstractApplicationParser {
        @Override
        protected CommandBuilder createCommandBuilder() {
            return new CommandBuilderExecutable();
        }

        public String getNodeName() {
            return "paext:myapplication";
        }

        @Override
        public void parseApplicationNode(Node paNode, GCMApplicationParser applicationParser, XPath xpath)
                throws Exception {
            super.parseApplicationNode(paNode, applicationParser, xpath);

            System.out.println("User Application Parser - someattr value = " +
                               paNode.getAttributes().getNamedItem("someattr").getNodeValue());
        }

        public TechnicalServicesProperties getTechnicalServicesProperties() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    //    @Test
    public void userSchemaTest() throws Exception {
        for (File file : getApplicationDescriptors()) {
            if (!file.toString().contains("script_ext")) {
                continue;
            }
            System.out.println(file);

            URL userSchema = getClass().getResource("/gcmdeployment/descriptorParser/testfiles/application/SampleApplicationExtension.xsd");

            ArrayList<String> schemas = new ArrayList<String>();
            schemas.add(userSchema.toString());

            GCMApplicationParserImpl parser = new GCMApplicationParserImpl(Helpers.fileToURL(file), null, schemas);

            parser.registerApplicationParser(new UserApplicationNodeParser());

            parser.getCommandBuilder();
            parser.getVirtualNodes();
            parser.getNodeProviders();
        }
    }

    //    @Test
    public void doit() throws ProActiveException, FileNotFoundException, URISyntaxException {
        for (File file : getApplicationDescriptors()) {
            if (!file.toString().contains("scriptHostname") || file.toString().contains("Invalid") ||
                file.toString().contains("oldDesc")) {
                continue;
            }
            System.out.println(file);

            new GCMApplicationImpl(Helpers.fileToURL(file));
        }
    }

    private List<File> getApplicationDescriptors() throws URISyntaxException {
        List<File> ret = new ArrayList<File>();
        File dir = null;
        dir = new File(TEST_APP_DIR.toURI());

        for (String file : dir.list()) {
            if (file.endsWith(".xml")) {
                ret.add(new File(dir, file));
            }
        }
        return ret;
    }

    @Test(expected = Exception.class)
    public void validationTest() throws Exception {
        validationGenericTest("/gcmdeployment/descriptorParser/testfiles/application/scriptInvalid.xml");
    }

    @Test(expected = Exception.class)
    public void validationOldSchemaTest() throws Exception {
        validationGenericTest("/gcmdeployment/descriptorParser/testfiles/application/oldDescriptor.xml");
    }

    @Test(expected = Exception.class)
    public void validationBrokenXMLTest() throws Exception {
        validationGenericTest("/gcmdeployment/descriptorParser/testfiles/application/script6.xml");
    }

    protected void validationGenericTest(String desc) throws Exception {
        File descriptor = new File(this.getClass().getResource(desc).getFile());

        System.out.println("Parsing " + descriptor.getAbsolutePath());

        GCMApplicationParserImpl parser = new GCMApplicationParserImpl(Helpers.fileToURL(descriptor), null);
    }

}
