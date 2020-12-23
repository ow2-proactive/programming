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
package dataspaces;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.UserCredentials;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSMountManagerHelper;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.router.Router;
import org.objectweb.proactive.extensions.pamr.router.RouterConfig;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;

import vfsprovider.AbstractIOOperationsBase;


/**
 * VFSMountManagerHelperTest
 *
 * This tests various scenario usage of the VFSMountManagerHelper
 *
 * @author The ProActive Team
 **/
public class VFSMountManagerHelperTest {

    static final public Logger logger = Logger.getLogger("testsuite");

    private static File spacesDir;

    private static FileSystemServerDeployer server;

    // remote protocols that will be used, the local protocol will always be the protocol used in the test suite
    static HashSet<String> protocolsToTest = new LinkedHashSet<String>(Arrays.asList("rmi", "pnp", "pamr"));

    // a list of fake urls, for file urls, use the opposite operating system file type of url to launch FileSystemExceptions
    private List<String> fakeUrls = Arrays.asList("ftp://a/b",
                                                  "pappnp://welcome.to.proactive:5461/inputserver?proactive_vfs_provider_path=/",
                                                  "sftp://itmysite/fake");

    private List<String> fakeFileUrls = Arrays.asList("");

    // pamr router
    static Router router;

    static {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
        PAMRConfig.PA_NET_ROUTER_ADDRESS.setValue("localhost");
        PAMRConfig.PA_NET_ROUTER_PORT.setValue(0);
        ProActiveLogger.getLogger(Loggers.DATASPACES).setLevel(Level.DEBUG);
    }

    /**
     * Start a PAMR router and a file system server
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {

        RouterConfig config = new RouterConfig();
        config.setPort(PAMRConfig.PA_NET_ROUTER_PORT.getValue());
        router = Router.createAndStart(config);

        protocolsToTest.remove(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());

        StringBuilder apstring = new StringBuilder();
        for (String p : protocolsToTest) {
            apstring.append(p + ",");
        }
        apstring.deleteCharAt(apstring.length() - 1);

        CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS.setValue(apstring.toString());

        spacesDir = new File(System.getProperty("java.io.tmpdir"), "ProActive SpaceMountManagerTest");

        if (server == null) {
            server = new FileSystemServerDeployer("inputserver", spacesDir.toString(), true, true);
            System.out.println("Started File Server at " + Arrays.toString(server.getVFSRootURLs()));
        }
    }

    /**
     * - Insert a valid file vfs root and a valid proactive vfs root in the list of fake uris
     * - verifies that mountAny returns the file system corresponding to the valid uri
     * - do that for all valid uris of the file system server
     * @throws Exception
     */
    @Test
    public void testMountAnyOk() throws Exception {
        logger.info("*************** testMountAnyOk");
        String[] validUris = server.getVFSRootURLs();

        for (String validUrl : validUris) {
            ConcurrentHashMap<String, FileObject> fileSystems = new ConcurrentHashMap<String, FileObject>();
            ArrayList<String> uriToMount = new ArrayList<String>(fakeFileUrls);
            uriToMount.add(spacesDir.toURI().toString()); // adds a valid file uri
            uriToMount.addAll(fakeUrls);
            uriToMount.add((int) Math.floor(Math.random() * uriToMount.size()), validUrl);
            VFSMountManagerHelper.mountAny(new UserCredentials(), uriToMount, fileSystems);
            logger.info("Content of map : " + fileSystems.toString());
            Assert.assertTrue("map contains valid Url", fileSystems.containsKey(validUrl));
        }
    }

    /**
     * when only fake uris are provided to mountAny, verify that an exception is received
     * @throws Exception
     */
    @Test(expected = FileSystemException.class)
    public void testMountAnyKo() throws Exception {
        logger.info("*************** testMountAnyKo");
        ArrayList<String> urlsToMount = new ArrayList<String>(fakeFileUrls);
        urlsToMount.addAll(fakeUrls);
        ConcurrentHashMap<String, FileObject> fileSystems = new ConcurrentHashMap<String, FileObject>();
        VFSMountManagerHelper.mountAny(new UserCredentials(), urlsToMount, fileSystems);
    }

    /**
     * Tests mounting only one valid FileSystem
     * @throws Exception
     */
    @Test
    public void testMountOk() throws Exception {
        logger.info("*************** testMountOk");
        String[] validUrls = server.getVFSRootURLs();
        for (String validUrl : validUrls) {
            FileObject mounted = VFSMountManagerHelper.mount(new UserCredentials(), validUrl);
            Assert.assertTrue(mounted.exists());
        }
    }

    /**
     * Tests mounting only one invalid FileSystem
     * @throws Exception
     */
    @Test(expected = FileSystemException.class)
    public void testMountKo() throws Exception {
        logger.info("*************** testMountKo");
        for (String fakeUrl : fakeUrls) {
            FileObject mounted = VFSMountManagerHelper.mount(new UserCredentials(), fakeUrl);
        }
    }

    /**
     * Tests closing all FileSystems
     * @throws Exception
     */
    @Ignore("vfs close file system doesn't seem to work properly")
    @Test
    public void testCloseFileSystems() throws Exception {
        logger.info("*************** testCloseFileSystems");
        String[] validUrls = server.getVFSRootURLs();
        ArrayList<FileObject> fos = new ArrayList<FileObject>();
        for (String validUrl : validUrls) {
            FileObject mounted = VFSMountManagerHelper.mount(new UserCredentials(), validUrl);
            Assert.assertTrue(mounted.exists());
            fos.add(mounted);
        }

        VFSMountManagerHelper.closeFileSystems(new UserCredentials(), Arrays.asList(validUrls));

        boolean onlyExceptions = true;
        for (FileObject closedFo : fos) {
            try {
                FileObject toto = closedFo.resolveFile("toto");
                toto.createFile();
                onlyExceptions = false;
                logger.error(toto.getURL() + " exists : " + toto.exists());
            } catch (FileSystemException e) {
                // this should occur
            }
        }
        Assert.assertTrue("Only Exceptions received", onlyExceptions);
    }

    @AfterClass
    public static void tearDown() throws ProActiveException {
        server.terminate();

        router.stop();

        VFSMountManagerHelper.terminate();

        if (spacesDir != null && spacesDir.exists()) {
            assertTrue(AbstractIOOperationsBase.deleteRecursively(spacesDir));
            spacesDir = null;
        }
    }
}
