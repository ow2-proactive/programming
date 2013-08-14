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
package functionalTests.filetransfer;

import static junit.framework.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.filetransfer.RemoteFile;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.utils.OperatingSystem;

import functionalTests.FunctionalTest;
import functionalTests.TestDisabler;


/**
 * Tests that both schemes work using the ProActive FileTransfer API
 */
public class TestDeployRetrieve extends FunctionalTest {
    private static Logger logger = ProActiveLogger.getLogger("functionalTests");
    private static File XML_LOCATION = null;
    static {
        try {
            XML_LOCATION = new File(TestAPI.class.getResource(
                    "/functionalTests/filetransfer/TestDeployRetrieve.xml").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    ProActiveDescriptor pad;
    File fileTest;
    File fileRetrieved;
    File fileDeployed;
    File fileRetrieved2;
    File fileDeployed2;
    static int testblocksize = org.objectweb.proactive.core.filetransfer.FileBlock.DEFAULT_BLOCK_SIZE;
    static int testflyingblocks = org.objectweb.proactive.core.filetransfer.FileTransferService.DEFAULT_MAX_SIMULTANEOUS_BLOCKS;
    static int filesize = 2;

    //Descriptor variables
    String jvmProcess = "localJVM";
    String hostName = "localhost";

    @BeforeClass
    public static void beforeClass() {

        // Disabled test deprecated API replaced by dataspaces
        TestDisabler.unsupportedOs(OperatingSystem.unix);
        // This test hangs on Windows because SSH processes are not killed
        TestDisabler.unsupportedOs(OperatingSystem.windows);
    }

    @Before
    public void initTest() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating " + filesize + "Mb random test file in /tmp");
        }

        fileTest = File.createTempFile("ProActiveTestFile", ".dat");
        fileRetrieved = File.createTempFile("ProActiveTestFileRetrieved", ".dat");
        fileDeployed = File.createTempFile("ProActiveTestFileDeployed", ".dat");
        fileRetrieved2 = File.createTempFile("ProActiveTestFileRetrieved2", ".dat");
        fileDeployed2 = File.createTempFile("ProActiveTestFileDeployed2", ".dat");

        //creates a new 2MB test file
        TestAPI.createRandomContentFile(fileTest, filesize);

        try {
            hostName = ProActiveInet.getInstance().getInetAddress().getHostName();
        } catch (Exception e) {
            hostName = "localhost";
        }
    }

    @After
    public void endTest() throws Exception {
        if (pad != null) {
            pad.killall(false);
        }

        cleanIfNecessary(this.fileTest);
        cleanIfNecessary(this.fileDeployed);
        cleanIfNecessary(this.fileDeployed2);
        cleanIfNecessary(this.fileRetrieved2);
        cleanIfNecessary(this.fileRetrieved);
    }

    @Test
    public void action() throws Exception {
        long fileTestSum = TestAPI.checkSum(fileTest);

        if (logger.isDebugEnabled()) {
            logger.debug("Loading descriptor from: " + XML_LOCATION);
        }

        // We save the current state of the schema validation and set it to false for this example
        boolean validatingProperyOld = CentralPAPropertyRepository.SCHEMA_VALIDATION.getValue();
        CentralPAPropertyRepository.SCHEMA_VALIDATION.setValue(false);

        VariableContractImpl vc = new VariableContractImpl();
        vc.setVariableFromProgram("HOST_NAME", hostName, VariableContractType.DescriptorDefaultVariable);

        pad = PADeployment.getProactiveDescriptor(XML_LOCATION.getCanonicalPath(), vc);

        // we restore the old state of the schema validation
        CentralPAPropertyRepository.SCHEMA_VALIDATION.setValue(validatingProperyOld);

        VirtualNode testVNode = pad.getVirtualNode("test");
        long initDeployment = System.currentTimeMillis();
        testVNode.activate();
        if (logger.isDebugEnabled()) {
            logger.debug("Getting the Node.");
        }

        Node[] node = testVNode.getNodes();
        long finitDeployment = System.currentTimeMillis();

        assertTrue(node.length > 0);
        if (logger.isDebugEnabled()) {
            logger.debug("Deployed " + node.length + " node from GCMVirtualNode " + testVNode.getName() +
                " in " + (finitDeployment - initDeployment) + "[ms]");
        }

        //Checking correct FileTransferDeploy
        if (logger.isDebugEnabled()) {
            logger.debug("Checking the integrity of the test file transfer at deployment time.");
        }
        long fileDeployedSum = TestAPI.checkSum(fileDeployed);
        assertTrue(fileTestSum == fileDeployedSum);

        //Checking correct FileTransferRetrieve
        if (logger.isDebugEnabled()) {
            logger.debug("Retrieving test files");
        }
        long initRetrieve = System.currentTimeMillis();

        List<RemoteFile> list = testVNode.getVirtualNodeInternal().fileTransferRetrieve(); //async
        for (RemoteFile rfile : list) {
            rfile.waitFor(); //sync here
        }

        long finitRetrieve = System.currentTimeMillis();

        if (logger.isDebugEnabled()) {
            logger.debug("Retrieved " + list.size() + " files from GCMVirtualNode " + testVNode.getName() +
                " in " + (finitRetrieve - initRetrieve) + "[ms]");
        }

        assertTrue(list.size() == 2);

        fileRetrieved = new File(fileRetrieved.getAbsoluteFile() + "-" +
            node[0].getNodeInformation().getName());
        fileRetrieved2 = new File(fileRetrieved2.getAbsoluteFile() + "-" +
            node[0].getNodeInformation().getName());

        long fileRetrievedSum = TestAPI.checkSum(fileRetrieved);

        if (logger.isDebugEnabled()) {
            logger.debug("CheckSum TestFile  =" + fileTestSum);
            logger.debug("CheckSum RetrieveFile=" + fileRetrievedSum);
            logger.debug("CheckSum Deploy=" + fileDeployedSum);
        }

        assertTrue(fileTestSum == fileRetrievedSum);
    }

    /**
     * Cleans test files
     */
    private void cleanIfNecessary(File f) {
        if (f.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Deleting old randomly generated file:" + f.getName());
            }
            f.delete();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 4) {
            filesize = Integer.parseInt(args[0]);
            testblocksize = Integer.parseInt(args[1]);
            testflyingblocks = Integer.parseInt(args[2]);
            XML_LOCATION = new File(args[3]);
        } else if (args.length != 0) {
            System.out
                    .println("Use with arguments: filesize[mb] fileblocksize[bytes] maxflyingblocks xmldescriptorpath");
        }

        TestDeployRetrieve test = new TestDeployRetrieve();
        test.jvmProcess = "remoteJVM";

        try {
            System.out.println("InitTest");
            test.initTest();
            System.out.println("Action");
            test.action();
            System.out.println("endTest");
            test.endTest();
            System.out.println("The end");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
