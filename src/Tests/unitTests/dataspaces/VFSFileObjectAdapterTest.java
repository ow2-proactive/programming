package unitTests.dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.MalformedURIException;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.extensions.dataspaces.vfs.adapter.VFSFileObjectAdapter;

import unitTests.vfsprovider.AbstractIOOperationsBase;


// TODO: some adapted functionality smoke tests - an abstract test for DataSpacesFileObject?
public class VFSFileObjectAdapterTest {

    private static final long appId = 1;
    private static final String runtimeId = "rt1";
    private static final String nodeId = "node1";
    private static final String activeObjectId = "ao1";
    private static final String path = "dir/file.txt";
    private static final DataSpacesURI spaceURI = DataSpacesURI.createScratchSpaceURI(appId, runtimeId,
            nodeId);
    private static final DataSpacesURI fileURI = spaceURI.withActiveObjectId(activeObjectId).withUserPath(
            path);

    private static DefaultFileSystemManager fileSystemManager;
    private DataSpacesFileObject dsFileObject;
    private FileObject adaptee;
    private File testDir;
    private String differentDirPath;
    private String rootDirPath;

    @BeforeClass
    static public void init() throws org.apache.commons.vfs.FileSystemException {
        fileSystemManager = VFSFactory.createDefaultFileSystemManager();
    }

    @AfterClass
    static public void close() {
        fileSystemManager.close();
    }

    @Before
    public void setUp() throws IOException, MalformedURIException {

        testDir = new File(System.getProperty("java.io.tmpdir"), "ProActive-VFSFileObjectAdapterTest");
        final File differentDir = new File(testDir, "different");
        final File rootDir = new File(testDir, "root");
        final File aoDir = new File(rootDir, "ao1");
        final File someDir = new File(aoDir, "dir");
        final File someFile = new File(someDir, "file.txt");
        assertTrue(someDir.mkdirs());
        assertTrue(differentDir.mkdir());
        assertTrue(someFile.createNewFile());

        rootDirPath = rootDir.getCanonicalPath();
        differentDirPath = differentDir.getCanonicalPath();

        final FileObject rootFileObject = fileSystemManager.resolveFile("file://" + rootDirPath);
        final FileName mountintPointFileName = rootFileObject.getName();
        adaptee = rootFileObject.resolveFile(fileURI.getRelativeToSpace());

        dsFileObject = new VFSFileObjectAdapter(adaptee, spaceURI, mountintPointFileName);
    }

    @After
    public void tearDown() {
        if (testDir != null && testDir.exists()) {
            assertTrue(AbstractIOOperationsBase.deleteRecursively(testDir));
            testDir = null;
        }

        adaptee = null;
        dsFileObject = null;
    }

    @Test
    public void testGetURI1() throws org.apache.commons.vfs.FileSystemException, MalformedURIException,
            FileSystemException {
        final FileObject rootFileObject = fileSystemManager.resolveFile("file://" + rootDirPath);
        final FileName mountintPointFileName = rootFileObject.getName();
        final FileObject rootAdaptee = rootFileObject;
        final DataSpacesFileObject fo = new VFSFileObjectAdapter(rootAdaptee, spaceURI, mountintPointFileName);

        assertEquals(spaceURI.toString(), fo.getURI());
    }

    @Test
    public void testGetURI2() throws FileSystemException {
        assertEquals(fileURI.toString(), dsFileObject.getURI());
    }

    @Test
    public void testGetParent() throws FileSystemException {
        DataSpacesFileObject parent = dsFileObject.getParent();
        assertIsSomeDir(parent);
    }

    @Test
    public void testResolveParent() throws FileSystemException {
        DataSpacesFileObject parent = dsFileObject.resolveFile("..");
        assertIsSomeDir(parent);
    }

    @Test(expected = FileSystemException.class)
    public void testResolveExceedsRoot() throws FileSystemException {
        dsFileObject.resolveFile("../../../../");
    }

    @Test(expected = FileSystemException.class)
    public void testGetParentExceedsRoot() throws FileSystemException {
        DataSpacesFileObject grandGrandParent = dsFileObject.getParent().getParent().getParent();
        grandGrandParent.getParent();
    }

    @Test(expected = FileSystemException.class)
    public void testGetParentOnFilesystemRoot() throws org.apache.commons.vfs.FileSystemException,
            FileSystemException {
        final FileObject rootFileObject = fileSystemManager.resolveFile("file:///");
        final FileName mountintPointFileName = rootFileObject.getName();
        final FileObject rootAdaptee = rootFileObject;
        final DataSpacesFileObject fo = new VFSFileObjectAdapter(rootAdaptee, spaceURI, mountintPointFileName);
        fo.getParent();
    }

    @Test(expected = FileSystemException.class)
    public void testResolveAbsolutePath() throws FileSystemException {
        dsFileObject.resolveFile("/absolute/path");
    }

    @Test(expected = FileSystemException.class)
    public void testMismatchedRoot() throws FileSystemException, org.apache.commons.vfs.FileSystemException {
        final FileName diffName;
        diffName = fileSystemManager.resolveFile(differentDirPath).getName();
        new VFSFileObjectAdapter(adaptee, spaceURI, diffName);
    }

    private void assertIsSomeDir(DataSpacesFileObject parent) throws FileSystemException {
        assertEquals(spaceURI.withActiveObjectId(activeObjectId).withUserPath("dir").toString(), parent
                .getURI());
        final List<DataSpacesFileObject> desc = parent.getChildren();
        assertEquals(1, desc.size());
        assertTrue(desc.contains(dsFileObject));
        assertEquals(dsFileObject, parent.getChild("file.txt"));
    }
}
