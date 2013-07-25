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
package unitTests.dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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

import unitTests.vfsprovider.AbstractIOOperationsBase;


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

    private static final DataSpacesURI NONEXISTING_SPACE = DataSpacesURI.createInOutSpaceURI(123,
            SpaceType.OUTPUT, "dummy");

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
    private DataSpacesURI inputUri;
    private DataSpacesURI outputUri;
    private DataSpacesURI scratchUri;
    private DataSpacesFileObject fileObject;
    private static String fakeFileUri = "file:///Z:/toto/tata";
    private static String inputSpaceFileUrl;
    private static String outputSpaceFileUrl;
    private static String scratchSpaceFileUrl;
    private static FileSystemServerDeployer serverInput;
    private static FileSystemServerDeployer serverOutput;
    private static FileSystemServerDeployer serverScratch;

    private String fakeUri = "ftp://fake";

    @Before
    public void setUp() throws Exception {

        ProActiveLogger.getLogger(Loggers.DATASPACES).setLevel(Level.DEBUG);
        spacesDir = new File(System.getProperty("java.io.tmpdir"), "ProActive-SpaceMountManagerTest");

        // input space
        final File inputSpaceDir = new File(spacesDir, "input");
        assertTrue(inputSpaceDir.mkdirs());
        if (serverInput == null) {
            serverInput = new FileSystemServerDeployer("inputserver", inputSpaceDir.toString(), true, true);
            System.out.println("Started File Server at " + serverInput.getVFSRootURL());
        }

        final File inputSpaceFile = new File(inputSpaceDir, INPUT_FILE);
        final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(inputSpaceFile));
        osw.write(INPUT_FILE_CONTENT);
        osw.close();
        inputSpaceFileUrl = inputSpaceDir.toURI().toURL().toString();
        ArrayList<String> rootinputuris = new ArrayList<String>();
        rootinputuris.add(fakeFileUri);
        rootinputuris.add(inputSpaceFileUrl);
        rootinputuris.add(fakeUri);
        rootinputuris.add(serverInput.getVFSRootURL());

        final InputOutputSpaceConfiguration inputSpaceConf = InputOutputSpaceConfiguration
                .createInputSpaceConfiguration(rootinputuris, null, null, "read_only_space");
        final SpaceInstanceInfo inputSpaceInfo = new SpaceInstanceInfo(123, inputSpaceConf);
        inputUri = inputSpaceInfo.getMountingPoint();

        // output space
        final File outputSpaceDir = new File(spacesDir, "output");
        assertTrue(outputSpaceDir.mkdirs());

        if (serverOutput == null) {
            serverOutput = new FileSystemServerDeployer("outputserver", outputSpaceDir.toString(), true, true);
            System.out.println("Started File Server at " + serverOutput.getVFSRootURL());
        }

        outputSpaceFileUrl = outputSpaceDir.toURI().toURL().toString();
        ArrayList<String> rootoutputuris = new ArrayList<String>();
        rootoutputuris.add(outputSpaceFileUrl);
        rootoutputuris.add(fakeUri);
        rootoutputuris.add(serverOutput.getVFSRootURL());

        final InputOutputSpaceConfiguration outputSpaceConf = InputOutputSpaceConfiguration
                .createOutputSpaceConfiguration(rootoutputuris, null, null, "read_write_space");
        final SpaceInstanceInfo outputSpaceInfo = new SpaceInstanceInfo(123, outputSpaceConf);
        outputUri = outputSpaceInfo.getMountingPoint();

        // scratch space
        final File scratchSpaceDir = new File(spacesDir, "scratch");
        assertTrue(scratchSpaceDir.mkdirs());

        if (serverScratch == null) {
            serverScratch = new FileSystemServerDeployer("scratchserver", scratchSpaceDir.toString(), true,
                true);
            System.out.println("Started File Server at " + serverScratch.getVFSRootURL());
        }

        final File scratchSpaceSubdir = new File(scratchSpaceDir, SCRATCH_ACTIVE_OBJECT_ID);
        scratchSpaceSubdir.mkdir();

        scratchSpaceFileUrl = scratchSpaceDir.toURI().toURL().toString();
        ArrayList<String> rootscratchuris = new ArrayList<String>();
        rootscratchuris.add(scratchSpaceFileUrl);
        rootscratchuris.add(fakeUri);
        rootscratchuris.add(serverScratch.getVFSRootURL());

        final ScratchSpaceConfiguration scratchSpaceConf = new ScratchSpaceConfiguration(rootscratchuris,
            null, null);
        final SpaceInstanceInfo scratchSpaceInfo = new SpaceInstanceInfo(123, "runtimeA", "nodeB",
            scratchSpaceConf);
        scratchUri = scratchSpaceInfo.getMountingPoint();

        // directory and finally manager
        directory = new SpacesDirectoryImpl();
        directory.register(inputSpaceInfo);
        directory.register(outputSpaceInfo);
        directory.register(scratchSpaceInfo);

        manager = new VFSSpacesMountManagerImpl(directory);
    }

    @After
    public void tearDown() throws ProActiveException {
        closeFileObject(fileObject);
        fileObject = null;

        if (manager != null) {
            manager.close();
            manager = null;
        }

        if (spacesDir != null && spacesDir.exists()) {
            assertTrue(AbstractIOOperationsBase.deleteRecursively(spacesDir));
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
        fileObject = manager.resolveFile(inputUri, null);
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
        fileObject = manager.resolveFile(outputUri, null);
        assertIsWorkingOutputSpaceDir(fileObject);
    }

    @Test
    public void testResolveFilesNotSharedFileObject() throws IOException, SpaceNotFoundException {
        final DataSpacesFileObject fileObject1 = manager.resolveFile(inputUri, null);
        final DataSpacesFileObject fileObject2 = manager.resolveFile(inputUri, null);

        assertNotSame(fileObject1, fileObject2);
    }

    @Test
    public void testResolveFileForUnexistingSpace() throws SpaceNotFoundException, IOException {
        try {
            manager.resolveFile(NONEXISTING_SPACE, null);
            fail("Exception expected");
        } catch (SpaceNotFoundException x) {
        }
    }

    @Test
    public void testResolveFileForSpacePartNotFullyDefined() throws SpaceNotFoundException, IOException {
        final DataSpacesURI uri = DataSpacesURI.createURI(inputUri.getAppId());
        assertFalse(uri.isSpacePartFullyDefined());
        try {
            manager.resolveFile(uri, null);
            fail("Exception expected");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testResolveFileForNotSuitableForUserPath() throws SpaceNotFoundException, IOException {
        assertFalse(scratchUri.isSuitableForUserPath());
        try {
            manager.resolveFile(scratchUri, null);
            fail("Exception expected");
        } catch (IllegalArgumentException x) {
        }
    }

    private void assertIsWorkingInputSpaceDir(final DataSpacesFileObject fo) throws FileSystemException,
            SpaceNotFoundException {
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

    private void assertIsWorkingOutputSpaceDir(DataSpacesFileObject fo) throws FileSystemException,
            SpaceNotFoundException {
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
        fileObject = manager.resolveFile(fileUri, null);

        assertTrue(fileObject.exists());
        // is it that file?
        final InputStream io = fileObject.getContent().getInputStream();
        final BufferedReader reader = new BufferedReader(new InputStreamReader(io));
        assertEquals(INPUT_FILE_CONTENT, reader.readLine());
        assertEquals(fileUri.toString(), fileObject.getVirtualURI());
    }

    @Test
    public void testResolveFileForFileInInputSpaceAlreadyMounted1() throws SpaceNotFoundException,
            IOException {
        testResolveFileForFileInInputSpace();
        testResolveFileForFileInInputSpace();
    }

    @Test
    public void testResolveFileForFileInInputSpaceAlreadyMounted2() throws SpaceNotFoundException,
            IOException {
        testResolveFileForInputSpace();
        testResolveFileForFileInInputSpace();
    }

    @Test
    public void testResolveFileForFileInScratchSpaceForOwner() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = scratchUri.withActiveObjectId(SCRATCH_ACTIVE_OBJECT_ID);
        fileObject = manager.resolveFile(fileUri, SCRATCH_ACTIVE_OBJECT_ID);
        assertIsWorkingScratchForAODir(fileObject, fileUri, true);
    }

    @Test
    public void testResolveFileForFileInScratchSpaceForOtherAO() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = scratchUri.withActiveObjectId(SCRATCH_ACTIVE_OBJECT_ID);
        final String nonexistingActiveObjectId = SCRATCH_ACTIVE_OBJECT_ID + "toto";
        fileObject = manager.resolveFile(fileUri, nonexistingActiveObjectId);
        assertIsWorkingScratchForAODir(fileObject, fileUri, false);
    }

    @Test
    public void testResolveFileForFileInScratchSpaceForAnonymousOwner() throws SpaceNotFoundException,
            IOException {
        final DataSpacesURI fileUri = scratchUri.withActiveObjectId(SCRATCH_ACTIVE_OBJECT_ID);
        fileObject = manager.resolveFile(fileUri, null);
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
        fileObject = manager.resolveFile(fileUri, null);
        assertFalse(fileObject.exists());
    }

    @Test
    public void testResolveFileForUnexistingFileInInputSpaceAlreadyMounted1() throws SpaceNotFoundException,
            IOException {
        testResolveFileForFileInInputSpace();
        testResolveFileForUnexistingFileInSpace();
    }

    @Test
    public void testResolveFileForUnexistingFileInInputSpaceAlreadyMounted2() throws SpaceNotFoundException,
            IOException {
        testResolveFileForInputSpace();
        testResolveFileForUnexistingFileInSpace();
    }

    @Test
    public void testResolveFileForFileInNonexistingSpace() throws SpaceNotFoundException, IOException {
        final DataSpacesURI fileUri = NONEXISTING_SPACE.withUserPath(NONEXISTING_FILE);
        try {
            manager.resolveFile(fileUri, null);
            fail("Exception expected");
        } catch (SpaceNotFoundException x) {
        }
    }

    @Test
    public void testResolveSpaces() throws Exception {
        final DataSpacesURI queryUri = DataSpacesURI.createURI(inputUri.getAppId(), inputUri.getSpaceType());
        final Map<DataSpacesURI, ? extends DataSpacesFileObject> spaces = manager.resolveSpaces(queryUri,
                null);
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
        final DataSpacesURI queryUri = DataSpacesURI.createScratchSpaceURI(scratchUri.getAppId(),
                nonexistingRuntimeId);
        assertEquals(0, manager.resolveSpaces(queryUri, null).size());
    }

    @Test
    public void testResolveSpacesNotSharedFileObject() throws IOException {
        final DataSpacesURI queryUri = DataSpacesURI.createURI(inputUri.getAppId(), inputUri.getSpaceType());

        final Map<DataSpacesURI, ? extends DataSpacesFileObject> spaces1 = manager.resolveSpaces(queryUri,
                null);
        assertEquals(1, spaces1.size());
        final DataSpacesFileObject fileObject1 = spaces1.get(inputUri);

        final Map<DataSpacesURI, ? extends DataSpacesFileObject> spaces2 = manager.resolveSpaces(queryUri,
                null);
        assertEquals(1, spaces2.size());
        final DataSpacesFileObject fileObject2 = spaces2.get(inputUri);
        assertNotSame(fileObject1, fileObject2);
    }

    @Test
    public void testResolveSpacesForSpacePartFullyDefined() throws SpaceNotFoundException, IOException {
        try {
            manager.resolveSpaces(inputUri, null);
            fail("Exception expected");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testResolveSpacesForNotSuitableForUserPath() throws SpaceNotFoundException, IOException {
        final DataSpacesURI uri = DataSpacesURI.createScratchSpaceURI(scratchUri.getAppId(), scratchUri
                .getRuntimeId());
        try {
            manager.resolveSpaces(uri, null);
            fail("Exception expected");
        } catch (IllegalArgumentException x) {
        }
    }
}
