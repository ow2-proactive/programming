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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.core.ApplicationScratchSpace;
import org.objectweb.proactive.extensions.dataspaces.core.BaseScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.NodeScratchSpace;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSNodeScratchSpaceImpl;

import dataspaces.mock.MOCKNode;


/**
 * Test for {@link VFSNodeScratchSpaceImpl} class, uses view MOCK Objects for imitating integration
 * with ProActive (obtaining ID's of a node, runtime).
 */
public class VFSNodeScratchSpaceImplTest {

    private static final String NODE_ID_2 = "second_node";

    private static final String NODE_ID = "node_id";

    private static final String RUNTIME_ID = "rt_id";

    private static final String SCRATCH_URL = "/";

    private static final String APP_ID = "0";

    private static final String TEST_FILE_CONTENT = "qwerty";

    private static DefaultFileSystemManager fileSystemManager;

    private File testDir;

    private MOCKNode node;

    private NodeScratchSpace nodeScratchSpace;

    private String testDirPath;

    private boolean configured;

    private File partialDSDummyFile;

    private File dsDummyFile;

    private BaseScratchSpaceConfiguration localAccessConfig;

    private NodeScratchSpace nodeScratchSpace2;

    private boolean configured2;

    @BeforeClass
    static public void init() throws FileSystemException {
        fileSystemManager = VFSFactory.createDefaultFileSystemManager();
    }

    @AfterClass
    static public void close() {
        fileSystemManager.close();
    }

    @Before
    public void setUp() throws ConfigurationException, IOException {
        testDir = new File(System.getProperty("java.io.tmpdir"), "ProActiveVFSNodeScratchSpaceImplTest");
        assertTrue(testDir.mkdir());
        testDirPath = testDir.getCanonicalPath();
        localAccessConfig = new BaseScratchSpaceConfiguration(SCRATCH_URL, testDirPath);

        node = new MOCKNode(RUNTIME_ID, NODE_ID);
        nodeScratchSpace = new VFSNodeScratchSpaceImpl();
        configured = false;
        configured2 = false;
    }

    @After
    public void tearDown() throws FileSystemException, IllegalStateException {
        try {
            if (nodeScratchSpace != null && configured) {
                nodeScratchSpace.close();
            }
        } finally {
            try {
                if (nodeScratchSpace2 != null && configured2) {
                    nodeScratchSpace2.close();
                }
            } finally {
                doCleanup();
            }
        }
    }

    private void doCleanup() {
        if (partialDSDummyFile != null) {
            File nodeDir = partialDSDummyFile.getParentFile();
            File rtDir = nodeDir.getParentFile();
            assertTrue(partialDSDummyFile.delete());
            assertTrue(nodeDir.delete());
            assertTrue(rtDir.delete());
            partialDSDummyFile = null;
        }

        if (dsDummyFile != null) {
            File appDir = dsDummyFile.getParentFile();
            File nodeDir = appDir.getParentFile();
            File rtDir = nodeDir.getParentFile();
            assertTrue(dsDummyFile.delete());
            assertTrue(appDir.delete());
            assertTrue(nodeDir.delete());
            assertTrue(rtDir.delete());
            dsDummyFile = null;
        }

        if (testDir != null) {
            assertTrue(testDir.delete());
            testDir = null;
        }
    }

    /**
     * Check if files are being created.
     */
    @Test
    public void testInitNSS() throws Exception {

        nodeScratchSpace.init(node, localAccessConfig);
        configured = true;
        String path = Utils.appendSubDirs(testDirPath, RUNTIME_ID, NODE_ID);
        assertIsExistingEmptyDirectory(path);
    }

    /**
     * Check if existing files are being removed.
     * 
     * @throws ConfigurationException
     * @throws IllegalStateException
     * @throws IOException
     */
    @Test
    public void testInitNSS2() throws ConfigurationException, IllegalStateException, IOException {
        final String partialDS = Utils.appendSubDirs(testDirPath, RUNTIME_ID, NODE_ID);
        final File dir = new File(partialDS);

        dir.mkdirs();
        partialDSDummyFile = new File(dir, "test.txt");
        final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(partialDSDummyFile));
        osw.write(TEST_FILE_CONTENT);
        osw.close();

        nodeScratchSpace.init(node, localAccessConfig);
        configured = true;
        assertIsExistingEmptyDirectory(partialDS);
        partialDSDummyFile = null;
    }

    /**
     * Double initialization case checking.
     */
    @Test
    public void testInitNSSIllegalState() throws Exception {

        nodeScratchSpace.init(node, localAccessConfig);
        configured = true;
        try {
            nodeScratchSpace.init(node, localAccessConfig);
            fail("Exception expected");
        } catch (IllegalStateException e) {
        } catch (Exception e) {
            fail("Wrong exception");
        }
    }

    /**
     * Initialization after close method call.
     */
    @Test
    public void testInitNSSIllegalState2() throws Exception {

        nodeScratchSpace.init(node, localAccessConfig);
        configured = true;
        nodeScratchSpace.close();
        configured = false;

        try {
            nodeScratchSpace.init(node, localAccessConfig);
            fail("Exception expected");
        } catch (IllegalStateException e) {
        } catch (Exception e) {
            fail("Wrong exception");
        }
    }

    /**
     * Check if files are being created and not null instance returned.
     */
    @Test
    public void testInitForApplication() throws Exception {

        nodeScratchSpace.init(node, localAccessConfig);
        configured = true;
        checkInitForApplication();
    }

    /**
     * InitForApplication without former node initialization.
     */
    @Test
    public void testInitForApplicationIllegalState() throws Exception {
        try {
            nodeScratchSpace.initForApplication(APP_ID);
            fail("Exception expected");
        } catch (IllegalStateException e) {
        } catch (Exception e) {
            fail("Wrong exception");
        }

        nodeScratchSpace.init(node, localAccessConfig);
        configured = true;
        checkInitForApplication();
    }

    /**
     * Passing configuration without remote access defined.
     */
    @Test(expected = ConfigurationException.class)
    public void testInitConfigurationException() throws Exception {

        BaseScratchSpaceConfiguration conf = new BaseScratchSpaceConfiguration((String) null, testDirPath);
        nodeScratchSpace = new VFSNodeScratchSpaceImpl();
        nodeScratchSpace.init(node, conf);
    }

    /**
     * Check if only one data space is being removed. Note that closing is also tested on each
     * {@link #tearDown()} method call.
     */
    @Test
    public void testClose() throws Exception {
        final String path1 = Utils.appendSubDirs(testDirPath, RUNTIME_ID, NODE_ID);
        final String path2 = Utils.appendSubDirs(testDirPath, RUNTIME_ID, NODE_ID_2);
        final Node node2 = new MOCKNode(RUNTIME_ID, NODE_ID_2);
        nodeScratchSpace2 = new VFSNodeScratchSpaceImpl();

        nodeScratchSpace.init(node, localAccessConfig);
        configured = true;
        nodeScratchSpace2.init(node2, localAccessConfig);
        configured2 = true;

        assertIsExistingEmptyDirectory(path1);
        assertIsExistingEmptyDirectory(path2);
        nodeScratchSpace.close();
        assertIsExistingEmptyDirectory(path2);
    }

    private void assertIsExistingEmptyDirectory(String path) throws FileSystemException {
        FileObject fPartialDS = fileSystemManager.resolveFile(path);

        assertTrue(fPartialDS.exists());
        assertEquals(FileType.FOLDER, fPartialDS.getType());
        assertEquals(0, fPartialDS.getChildren().length);
    }

    private void checkInitForApplication() throws Exception {
        final String dataSpacePath = Utils.appendSubDirs(testDirPath, RUNTIME_ID, NODE_ID, APP_ID);
        final ApplicationScratchSpace app = nodeScratchSpace.initForApplication(APP_ID);
        assertNotNull(app);
        assertIsExistingEmptyDirectory(dataSpacePath);
    }

}
