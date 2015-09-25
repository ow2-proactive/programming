/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
package functionalTests.multiprotocol;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import functionalTests.FunctionalTest;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * TestVFSProviderMultiProtocol tests that when Multi-Protocol is used, A FileSystemServer is deployed using all used protocols
 *
 * The test tries then to use the server via the different protocols
 *
 * @author The ProActive Team
 */
public class TestVFSProviderMultiProtocol extends FunctionalTest {

    // remote protocols that will be used, the local protocol will always be the protocol used in the test suite
    ArrayList<String> protocolsToTest = new ArrayList<String>(Arrays.asList(new String[] { "rmi", "pnp",
            "pamr" }));

    // pamr router
    static Router router;

    static File SERVER_PATH = new File(System.getProperty("java.io.tmpdir"), "data server");

    @BeforeClass
    static public void prepareForTest() throws Exception {
        ProActiveLogger.getLogger(Loggers.REMOTEOBJECT).setLevel(Level.DEBUG);
        ProActiveLogger.getLogger(Loggers.PAPROXY).setLevel(Level.DEBUG);
        PAMRConfig.PA_NET_ROUTER_ADDRESS.setValue("localhost");
        PAMRConfig.PA_NET_ROUTER_PORT.setValue(0);
        FunctionalTest.prepareForTest();
    }

    private static DefaultFileSystemManager fileSystemManager;

    public TestVFSProviderMultiProtocol() {
    }

    @BeforeClass
    public static void prepare() throws Exception {
        RouterConfig config = new RouterConfig();
        config.setPort(PAMRConfig.PA_NET_ROUTER_PORT.getValue());
        router = Router.createAndStart(config);
        fileSystemManager = VFSFactory.createDefaultFileSystemManager();
    }

    /**
     * Testing the File server deployment using multi-protocol
     * @throws Exception
     */
    @Test
    public void testVFSProviderMP() throws Exception {

        logger.info("**************** Testing deploying dataspace server with protocol list : " +
            protocolsToTest);

        CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.setValue(protocolsToTest.get(0));
        String add_str = protocolsToTest.get(1);
        for (int i = 2; i < protocolsToTest.size(); i++) {
            add_str += "," + protocolsToTest.get(i);
        }
        CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.setValue(add_str);

        FileSystemServerDeployer deployer = new FileSystemServerDeployer("space name",
            SERVER_PATH.getAbsolutePath(), true);

        try {

            String[] urls = deployer.getVFSRootURLs();
            logger.info("Received urls :" + Arrays.asList(urls));
            Assert.assertEquals(
                    "Number of urls of the FileSystemServerDeployer should match the number of protocols + the file protocol",
                    protocolsToTest.size() + 1, urls.length);

            // check the file server uris

            URI receiveduri = new URI(urls[0]);
            Assert.assertEquals("protocol of first uri " + receiveduri + " should be file", "file",
                    receiveduri.getScheme());

            for (int i = 1; i < urls.length; i++) {
                receiveduri = new URI(urls[i]);
                Assert.assertEquals("protocol of uri " + urls[i] + " should match the expected protocol",
                        "pap" + protocolsToTest.get(i - 1), receiveduri.getScheme());
            }

            // use the file server
            for (int i = 0; i < urls.length; i++) {
                File f = new File(System.getProperty("java.io.tmpdir"), "testfile_" + i);
                f.createNewFile();
                logger.info("Trying to use : " + urls[i]);
                FileObject source = fileSystemManager.resolveFile(f.toURI().toURL().toExternalForm());
                FileObject dest = fileSystemManager.resolveFile(urls[i] + "/" + f.getName());
                dest.copyFrom(source, Selectors.SELECT_SELF);
                Assert.assertTrue("Copy successful of " + source.getURL() + " to " + dest.getURL(),
                        dest.exists());

            }

        } finally {
            deployer.terminate();
        }

    }

    @AfterClass
    public static void clean() throws Exception {
        router.stop();
        fileSystemManager.close();
    }

}
