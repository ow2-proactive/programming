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

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.ScratchSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceType;
import org.objectweb.proactive.extensions.dataspaces.core.SpacesMountManager;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectoryImpl;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.objectweb.proactive.extensions.dataspaces.vfs.DataSpacesLimitingFileObject;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSSpacesMountManagerImpl;
import org.objectweb.proactive.extensions.dataspaces.vfs.adapter.VFSFileObjectAdapter;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * This test is actually not a pure unit test run in high isolation. It depends on correct behavior
 * of {@link SpacesDirectoryImpl}, {@link VFSFactory}, {@link VFSFileObjectAdapter} together with
 * {@link DataSpacesLimitingFileObject} and basic classes - {@link SpaceInstanceInfo}/
 * {@link DataSpacesURI}.
 */
public class VFSSpacesMountManagerImplTest {

    private static final String INPUT_FILE = "file.txt";

    private static final String INPUT_FILE_CONTENT = "test";

    private static final String NONEXISTING_FILE = "got_you_i_do_not_exist.txt";

    private static final String SCRATCH_ACTIVE_OBJECT_ID = "777";

    private static final String WRONG_DS_SERVER_URL = "pappnp://welcome.to.proactive:5461/inputserver?proactive_vfs_provider_path=/";

    private static final DataSpacesURI NONEXISTING_SPACE = DataSpacesURI.createInOutSpaceURI("123",
                                                                                             SpaceType.OUTPUT,
                                                                                             "dummy");

    private File write_p;

    private static void closeFileObject(final DataSpacesFileObject file) {
        if (file != null) {
            try {
                file.close();
            } catch (FileSystemException x) {
                System.err.println("Could not close file object: " + x);
            }
        }
    }

    private SpacesMountManager manager;

    private SpacesDirectory directory;

    private File spacesDir;

    private File inputSpaceDir;

    private File outputSpaceDir;

    private DataSpacesURI inputUri;

    private DataSpacesURI outputUri;

    private DataSpacesURI scratchUri;

    private DataSpacesFileObject fileObject;

    private static String inputSpaceFileUrl;

    private static String outputSpaceFileUrl;

    private static String scratchSpaceFileUrl;

    private static FileSystemServerDeployer serverInput;

    private static FileSystemServerDeployer serverOutput;

    private static FileSystemServerDeployer serverScratch;

    private static DefaultFileSystemManager fileSystemManager;

    @BeforeClass
    static public void init() throws org.apache.commons.vfs2.FileSystemException {
        fileSystemManager = VFSFactory.createDefaultFileSystemManager();
    }

    @AfterClass
    static public void close() {
        fileSystemManager.close();
    }

    @Before
    public void setUp() throws Exception {

        ProActiveLogger.getLogger(Loggers.DATASPACES).setLevel(Level.DEBUG);
        spacesDir = new File(System.getProperty("java.io.tmpdir"), "ProActive SpaceMountManagerTest");
        if (spacesDir.exists()) {
            if (write_p != null && write_p.exists()) {
                write_p.setWritable(true);
            }
            FileUtils.forceDelete(spacesDir);
        }

        // input space
        inputSpaceDir = new File(spacesDir, "input");
        assertTrue(inputSpaceDir.mkdirs());
        if (serverInput == null) {
            serverInput = new FileSystemServerDeployer("inputserver", inputSpaceDir.toString(), true, true);
            System.out.println("Started Input File Server at " + Arrays.toString(serverInput.getVFSRootURLs()));
        }

        final File inputSpaceFile = new File(inputSpaceDir, INPUT_FILE);
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(inputSpaceFile))) {
            osw.write(INPUT_FILE_CONTENT);
        }
        inputSpaceFileUrl = inputSpaceDir.toURI().toURL().toString();
        ArrayList<String> rootinputuris = new ArrayList<String>();
        rootinputuris.add(inputSpaceFileUrl);
        rootinputuris.add(serverInput.getVFSRootURL());

        final InputOutputSpaceConfiguration inputSpaceConf = InputOutputSpaceConfiguration.createInputSpaceConfiguration(rootinputuris,
                                                                                                                         null,
                                                                                                                         null,
                                                                                                                         "read_only_space");
        final SpaceInstanceInfo inputSpaceInfo = new SpaceInstanceInfo("123", inputSpaceConf);
        inputUri = inputSpaceInfo.getMountingPoint();

        // output space
        outputSpaceDir = new File(spacesDir, "output");
        assertTrue(outputSpaceDir.mkdirs());

        if (serverOutput == null) {
            serverOutput = new FileSystemServerDeployer("outputserver", outputSpaceDir.toString(), true, true);
            System.out.println("Started Output File Server at " + Arrays.toString(serverOutput.getVFSRootURLs()));
        }

        outputSpaceFileUrl = outputSpaceDir.toURI().toURL().toString();
        ArrayList<String> rootoutputuris = new ArrayList<String>();
        rootoutputuris.add(outputSpaceFileUrl);
        rootoutputuris.add(serverOutput.getVFSRootURL());

        final InputOutputSpaceConfiguration outputSpaceConf = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(rootoutputuris,
                                                                                                                           null,
                                                                                                                           null,
                                                                                                                           "read_write_space");
        final SpaceInstanceInfo outputSpaceInfo = new SpaceInstanceInfo("123", outputSpaceConf);
        outputUri = outputSpaceInfo.getMountingPoint();

        // scratch space
        final File scratchSpaceDir = new File(spacesDir, "scratch");
        assertTrue(scratchSpaceDir.mkdirs());

        if (serverScratch == null) {
            serverScratch = new FileSystemServerDeployer("scratchserver", scratchSpaceDir.toString(), true, true);
            System.out.println("Started Scratch File Server at " + Arrays.toString(serverScratch.getVFSRootURLs()));
        }

        final File scratchSpaceSubdir = new File(scratchSpaceDir, SCRATCH_ACTIVE_OBJECT_ID);
        scratchSpaceSubdir.mkdir();

        scratchSpaceFileUrl = scratchSpaceDir.toURI().toURL().toString();
        ArrayList<String> rootscratchuris = new ArrayList<String>();
        rootscratchuris.add(scratchSpaceFileUrl);
        rootscratchuris.add(serverScratch.getVFSRootURL());

        final ScratchSpaceConfiguration scratchSpaceConf = new ScratchSpaceConfiguration(rootscratchuris, null, null);
        final SpaceInstanceInfo scratchSpaceInfo = new SpaceInstanceInfo("123", "runtimeA", "nodeB", scratchSpaceConf);
        scratchUri = scratchSpaceInfo.getMountingPoint();

        // directory and finally manager
        directory = new SpacesDirectoryImpl();
        directory.register(inputSpaceInfo);
        directory.register(outputSpaceInfo);
        directory.register(scratchSpaceInfo);

        manager = new VFSSpacesMountManagerImpl(directory);

        fileSystemManager = VFSFactory.createDefaultFileSystemManager();

    }

    @After
    public void tearDown() throws ProActiveException, IOException {
        closeFileObject(fileObject);
        fileObject = null;

        if (manager != null) {
            manager.close();
            manager = null;
        }

        if (spacesDir != null && spacesDir.exists()) {
            if (write_p != null && write_p.exists()) {
                write_p.setWritable(true);
            }
            FileUtils.forceDelete(spacesDir);
            spacesDir = null;
        }

    }

    @AfterClass
    public static void lastTearDown() throws ProActiveException {
        if (serverInput != null) {
            serverInput.terminate();
        }
        if (serverOutput != null) {
            serverOutput.terminate();
        }
        if (serverScratch != null) {
            serverScratch.terminate();
        }
    }

    @Test
    public void testResolveFileForInputSpace() throws IOException, SpaceNotFoundException {
        fileObject = manager.resolveFile(inputUri, null, null);
        assertIsWorkingInputSpaceDir(fileObject);
    }

    @Test
    public void testResolveFileForInputSpaceAlreadyMounted1() throws IOException, SpaceNotFoundException {
        testResolveFileForInputSpace();
        testResolveFileForInputSpace();
    }

    @Test
    public void testResolveFileForInputSpaceAlreadyMounted2() throws SpaceNotFoundException, IOException {
        testResolveFileForFileInInputSpace();
        testResolveFileForInputSpace();
    }

    @Test
    public void testResolveFileForOutputSpace() throws IOException, SpaceNotFoundException {
        fileObject = manager.resolveFile(outputUri, null, null);
        assertIsWorkingOutputSpaceDir(fileObject);
    }

    @Test
    public void testResolveFilesNotSharedFileObject() throws IOException, SpaceNotFoundException {
        final DataSpacesFileObject fileObject1 = manager.resolveFile(inputUri, null, null);
        final DataSpacesFileObject fileObject2 = manager.resolveFile(inputUri, null, null);

        assertNotSame(fileObject1, fileObject2);
    }

    @Test
    public void testResolveFileForUnexistingSpace() throws SpaceNotFoundException, IOException {
        try {
            manager.resolveFile(NONEXISTING_SPACE, null, null);
            fail("Exception expected");
        } catch (SpaceNotFoundException x) {
        }
    }

    @Test
    public void testResolveFileForSpacePartNotFullyDefined() throws SpaceNotFoundException, IOException {
        final DataSpacesURI uri = DataSpacesURI.createURI(inputUri.getAppId());
        assertFalse(uri.isSpacePartFullyDefined());
        try {
            manager.resolveFile(uri, null, null);
            fail("Exception expected");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testResolveFileForNotSuitableForUserPath() throws SpaceNotFoundException, IOException {
        assertFalse(scratchUri.isSuitableForUserPath());
        try {
            manager.resolveFile(scratchUri, null, null);
            fail("Exception expected");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testEnsureExistingOrSwitch() throws Exception {
        String wrongpath = createWrongFileUri();
        String writeProtected = createWriteProtectedFile();

        ArrayList<String> rootUrisWithWrongFileUri = new ArrayList<String>();
        rootUrisWithWrongFileUri.add(wrongpath);
        rootUrisWithWrongFileUri.add(writeProtected);
        rootUrisWithWrongFileUri.add(serverOutput.getVFSRootURL());

        VFSSpacesMountManagerImpl manager2 = createManagerForEnsureExistingTest(wrongpath);

        FileObject fileObjectWithWrongFileUri = fileSystemManager.resolveFile(wrongpath);

        // create Adapter
        DataSpacesFileObject dsFileObjectWithWrongFileUri = new VFSFileObjectAdapter(fileObjectWithWrongFileUri,
                                                                                     outputUri,
                                                                                     fileObjectWithWrongFileUri.getName(),
                                                                                     rootUrisWithWrongFileUri,
                                                                                     wrongpath,
                                                                                     manager2,
                                                                                     null);

        // switch to existing
        DataSpacesFileObject newfo = dsFileObjectWithWrongFileUri.ensureExistingOrSwitch(true);
        new File(writeProtected).setWritable(true);
        assertEquals(serverOutput.getVFSRootURL(), newfo.getSpaceRootURI());

    }

    private String createWrongFileUri() throws org.apache.commons.vfs2.FileSystemException {
        String wrongpath;
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            wrongpath = "file:///C:/path/which/does/not/exist";

        } else {
            wrongpath = "file:/path/which/does/not/exist";
        }
        return wrongpath;
    }

    private String createWriteProtectedFile() throws IOException {
        write_p = new File(outputSpaceDir, "write_p.txt");

        // we create a file instead of a directory (as setWritable fails on windows for directories)
        write_p.createNewFile();
        assertTrue(write_p.setWritable(false));

        assumeFalse("Probably running the test as root as we cannot make the file read only (JDK-6931128)",
                    write_p.canWrite());

        return write_p.toURI().toURL().toString();
    }

    private VFSSpacesMountManagerImpl createManagerForEnsureExistingTest(String wrongpath) throws Exception {

        // create output space configuration with wrong file uri
        File outputSpaceDir = new File(spacesDir, "output");
        outputSpaceFileUrl = outputSpaceDir.toURI().toURL().toString();
        ArrayList<String> rootoutputuris = new ArrayList<String>();
        rootoutputuris.add(wrongpath);
        // add as well wrong server uri
        rootoutputuris.add(WRONG_DS_SERVER_URL);
        rootoutputuris.add(serverOutput.getVFSRootURL());

        InputOutputSpaceConfiguration outputSpaceConf = InputOutputSpaceConfiguration.createOutputSpaceConfiguration(rootoutputuris,
                                                                                                                     null,
                                                                                                                     null,
                                                                                                                     "read_write_space");
        SpaceInstanceInfo outputSpaceInfo = new SpaceInstanceInfo("123", outputSpaceConf);

        // create manager
        SpacesDirectory directory2 = new SpacesDirectoryImpl();
        directory2.register(outputSpaceInfo);
        VFSSpacesMountManagerImpl manager2 = new VFSSpacesMountManagerImpl(directory2);
        return manager2;

    }

    private void assertIsWorkingInputSpaceDir(final DataSpacesFileObject fo)
            throws FileSystemException, SpaceNotFoundException {
        assertTrue(fo.exists());

        ArrayList<String> expectedRootList = new ArrayList<String>();
        expectedRootList.add(inputSpaceFileUrl);
        expectedRootList.add(serverInput.getVFSRootURL());
        assertEquals(expectedRootList, fo.getAllSpaceRootURIs());

        // is it that directory?
        final DataSpacesFileObject child = fo.getChild(INPUT_FILE);
        assertNotNull(child);
        assertTrue(child.exists());
        assertEquals(inputUri.toString(), fo.getVirtualURI());

        ArrayList<String> expectedUriList = new ArrayList<String>();
        expectedUriList.add(inputSpaceFileUrl + (inputSpaceFileUrl.endsWith("/") ? "" : "/") + INPUT_FILE);
        expectedUriList.add(serverInput.getVFSRootURL() + INPUT_FILE);

        assertEquals(expectedUriList, child.getAllRealURIs());

        DataSpacesFileObject foTest = fo.switchToSpaceRoot(serverInput.getVFSRootURL());
        final DataSpacesFileObject childTest = foTest.getChild(INPUT_FILE);
        DataSpacesFileObject finalChild = childTest.switchToSpaceRoot(inputSpaceFileUrl);
        assertEquals(child.getRealURI(), finalChild.getRealURI());

        // check with all available protocols
        for (String rooturi : fo.getAllSpaceRootURIs()) {
            DataSpacesFileObject fo2 = fo.switchToSpaceRoot(rooturi);
            DataSpacesFileObject child2 = child.switchToSpaceRoot(rooturi);

            // check if write access restrictions are computed correctly - this should be denied
            try {
                child2.delete();
                fail("Expected exception - should not have right to write to input space");
            } catch (FileSystemException x) {
                assertTrue(child2.exists());
            }

            // check if access restrictions are computed correctly - these 2 should be denied 
            try {
                fo2.getParent();
                fail("Expected exception - should not have access to parent file of space dir");
            } catch (FileSystemException x) {
            }
            try {
                fo2.resolveFile("../");
                fail("Expected exception - should not have access to parent file of space dir");
            } catch (FileSystemException x) {
            }

            // check if access restrictions are computed correctly - this should be allowed
            assertNotNull(child.getParent());
            // this not
            try {
                child2.resolveFile("../..");
                fail("Expected exception - should not have access to parent file of space dir");
            } catch (FileSystemException x) {
            }
        }
    }

    private void assertIsWorkingOutputSpaceDir(DataSpacesFileObject fo)
            throws FileSystemException, SpaceNotFoundException {
        assertTrue(fo.exists());

        ArrayList<String> expectedRootList = new ArrayList<String>();
        expectedRootList.add(outputSpaceFileUrl);
        expectedRootList.add(serverOutput.getVFSRootURL());
        assertEquals(expectedRootList, fo.getAllSpaceRootURIs());

        assertEquals(outputUri.toString(), fo.getVirtualURI());
        final DataSpacesFileObject child = fo.resolveFile("new_file");

        // check if write access restrictions are computed correctly - this should be allowed
        child.createFile();
        assertTrue(child.exists());

        ArrayList<String> expectedUriList = new ArrayList<String>();
        expectedUriList.add(outputSpaceFileUrl + (outputSpaceFileUrl.endsWith("/") ? "" : "/") + "new_file");
        expectedUriList.add(serverOutput.getVFSRootURL() + "new_file");

        assertEquals(expectedUriList, child.getAllRealURIs());

        DataSpacesFileObject foTest = fo.switchToSpaceRoot(serverOutput.getVFSRootURL());
        final DataSpacesFileObject childTest = foTest.getChild("new_file");
        DataSpacesFileObject finalChild = childTest.switchToSpaceRoot(outputSpaceFileUrl);
        assertEquals(child.getRealURI(), finalChild.getRealURI());
    }

    @Test
    public void testResolveFileForFileInInputSpace() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = inputUri.withUserPath(INPUT_FILE);
        fileObject = manager.resolveFile(fileUri, null, null);

        assertTrue(fileObject.exists());
        // is it that file?
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fileObject.getContent()
                                                                                        .getInputStream()))) {
            line = reader.readLine();
        }
        assertEquals(INPUT_FILE_CONTENT, line);
        assertEquals(fileUri.toString(), fileObject.getVirtualURI());
    }

    @Test
    public void testResolveFileForFileInInputSpaceAlreadyMounted1() throws SpaceNotFoundException, IOException {
        testResolveFileForFileInInputSpace();
        testResolveFileForFileInInputSpace();
    }

    @Test
    public void testResolveFileForFileInInputSpaceAlreadyMounted2() throws SpaceNotFoundException, IOException {
        testResolveFileForInputSpace();
        testResolveFileForFileInInputSpace();
    }

    @Test
    public void testResolveFileForFileInScratchSpaceForOwner() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = scratchUri.withActiveObjectId(SCRATCH_ACTIVE_OBJECT_ID);
        fileObject = manager.resolveFile(fileUri, SCRATCH_ACTIVE_OBJECT_ID, null);
        assertIsWorkingScratchForAODir(fileObject, fileUri, true);
    }

    @Test
    public void testResolveFileForFileInScratchSpaceForOtherAO() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = scratchUri.withActiveObjectId(SCRATCH_ACTIVE_OBJECT_ID);
        final String nonexistingActiveObjectId = SCRATCH_ACTIVE_OBJECT_ID + "toto";
        fileObject = manager.resolveFile(fileUri, nonexistingActiveObjectId, null);
        assertIsWorkingScratchForAODir(fileObject, fileUri, false);
    }

    @Test
    public void testResolveFileForFileInScratchSpaceForAnonymousOwner() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = scratchUri.withActiveObjectId(SCRATCH_ACTIVE_OBJECT_ID);
        fileObject = manager.resolveFile(fileUri, null, null);
        assertIsWorkingScratchForAODir(fileObject, fileUri, false);
    }

    private void assertIsWorkingScratchForAODir(final DataSpacesFileObject fo, final DataSpacesURI fileUri,
            final boolean owner) throws FileSystemException, IOException {
        assertTrue(fo.exists());
        assertEquals(fileUri.toString(), fo.getVirtualURI());
        final DataSpacesFileObject child = fo.resolveFile("new_file");

        if (owner) {
            // check if write access restrictions are computed correctly - this should be allowed
            child.createFile();
            assertTrue(child.exists());
        } else {
            try {
                child.createFile();
                fail("Expected exception - should not have right to write to other AO's scratch");
            } catch (FileSystemException x) {
            }
        }

        // check if access restrictions are computed correctly - these 2 should be denied 
        try {
            fo.getParent();
            fail("Expected exception - should not have access to parent file of scratch for AO");
        } catch (FileSystemException x) {
        }
        try {
            fo.resolveFile("../");
            fail("Expected exception - should not have access to parent file of scratch for AO");
        } catch (FileSystemException x) {
        }

        // check if access restrictions are computed correctly - this should be allowed
        assertNotNull(child.getParent());
        // this not
        try {
            child.resolveFile("../..");
            fail("Expected exception - should not have access to parent file of scratch for AO");
        } catch (FileSystemException x) {
        }
    }

    @Test
    public void testResolveFileForUnexistingFileInSpace() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = inputUri.withUserPath(NONEXISTING_FILE);
        fileObject = manager.resolveFile(fileUri, null, null);
        assertFalse(fileObject.exists());
    }

    @Test
    public void testResolveFileForUnexistingFileInInputSpaceAlreadyMounted1()
            throws SpaceNotFoundException, IOException {
        testResolveFileForFileInInputSpace();
        testResolveFileForUnexistingFileInSpace();
    }

    @Test
    public void testResolveFileForUnexistingFileInInputSpaceAlreadyMounted2()
            throws SpaceNotFoundException, IOException {
        testResolveFileForInputSpace();
        testResolveFileForUnexistingFileInSpace();
    }

    @Test
    public void testResolveFileForFileInNonexistingSpace() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = NONEXISTING_SPACE.withUserPath(NONEXISTING_FILE);
        try {
            manager.resolveFile(fileUri, null, null);
            fail("Exception expected");
        } catch (SpaceNotFoundException x) {
        }
    }

    @Test
    public void testResolveSpaces() throws Exception {
        final DataSpacesURI queryUri = DataSpacesURI.createURI(inputUri.getAppId(), inputUri.getSpaceType());
        final Map<DataSpacesURI, ? extends DataSpacesFileObject> spaces = manager.resolveSpaces(queryUri, null, null);
        assertEquals(1, spaces.size());

        fileObject = spaces.get(inputUri);
        assertNotNull(fileObject);
        assertIsWorkingInputSpaceDir(fileObject);
    }

    @Test
    public void testResolveSpacesAlreadyMounted1() throws Exception {
        testResolveSpaces();
        testResolveSpaces();
    }

    @Test
    public void testResolveSpacesAlreadyMounted2() throws Exception {
        testResolveFileForFileInInputSpace();
        testResolveSpaces();
    }

    @Test
    public void testResolveSpacesNonexisting() throws SpaceNotFoundException, IOException {
        final String nonexistingRuntimeId = scratchUri.getRuntimeId() + "toto";
        final DataSpacesURI queryUri = DataSpacesURI.createScratchSpaceURI(scratchUri.getAppId(), nonexistingRuntimeId);
        assertEquals(0, manager.resolveSpaces(queryUri, null, null).size());
    }

    @Test
    public void testResolveSpacesNotSharedFileObject() throws IOException {
        final DataSpacesURI queryUri = DataSpacesURI.createURI(inputUri.getAppId(), inputUri.getSpaceType());

        final Map<DataSpacesURI, ? extends DataSpacesFileObject> spaces1 = manager.resolveSpaces(queryUri, null, null);
        assertEquals(1, spaces1.size());
        final DataSpacesFileObject fileObject1 = spaces1.get(inputUri);

        final Map<DataSpacesURI, ? extends DataSpacesFileObject> spaces2 = manager.resolveSpaces(queryUri, null, null);
        assertEquals(1, spaces2.size());
        final DataSpacesFileObject fileObject2 = spaces2.get(inputUri);
        assertNotSame(fileObject1, fileObject2);
    }

    @Test
    public void testResolveSpacesForSpacePartFullyDefined() throws SpaceNotFoundException, IOException {
        try {
            manager.resolveSpaces(inputUri, null, null);
            fail("Exception expected");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testResolveSpacesForNotSuitableForUserPath() throws SpaceNotFoundException, IOException {
        final DataSpacesURI uri = DataSpacesURI.createScratchSpaceURI(scratchUri.getAppId(), scratchUri.getRuntimeId());
        try {
            manager.resolveSpaces(uri, null, null);
            fail("Exception expected");
        } catch (IllegalArgumentException x) {
        }
    }
}
