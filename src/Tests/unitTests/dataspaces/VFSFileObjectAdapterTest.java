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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
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
    static public void init() throws org.apache.commons.vfs2.FileSystemException {
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
    public void testGetURI1() throws org.apache.commons.vfs2.FileSystemException, MalformedURIException,
            FileSystemException {
        final FileObject rootFileObject = fileSystemManager.resolveFile("file://" + rootDirPath);
        final FileName mountintPointFileName = rootFileObject.getName();
        final FileObject rootAdaptee = rootFileObject;
        final DataSpacesFileObject fo = new VFSFileObjectAdapter(rootAdaptee, spaceURI, mountintPointFileName);

        assertEquals(spaceURI.toString(), fo.getVirtualURI());
    }

    @Test
    public void testGetURI2() throws FileSystemException {
        assertEquals(fileURI.toString(), dsFileObject.getVirtualURI());
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
    public void testGetParentOnFilesystemRoot() throws org.apache.commons.vfs2.FileSystemException,
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
    public void testMismatchedRoot() throws FileSystemException, org.apache.commons.vfs2.FileSystemException {
        final FileName diffName;
        diffName = fileSystemManager.resolveFile(differentDirPath).getName();
        new VFSFileObjectAdapter(adaptee, spaceURI, diffName);
    }

    private void assertIsSomeDir(DataSpacesFileObject parent) throws FileSystemException {
        assertEquals(spaceURI.withActiveObjectId(activeObjectId).withUserPath("dir").toString(), parent
                .getVirtualURI());
        final List<DataSpacesFileObject> desc = parent.getChildren();
        assertEquals(1, desc.size());
        assertTrue(desc.contains(dsFileObject));
        assertEquals(dsFileObject, parent.getChild("file.txt"));
    }
}
