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
package dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
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
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.objectweb.proactive.extensions.vfsprovider.util.URIHelper;
import org.objectweb.proactive.utils.OperatingSystem;


// TODO: some adapted functionality smoke tests - an abstract test for DataSpacesFileObject?
public class VFSFileObjectAdapterTest {

    private static final String appId = "1";
    private static final String runtimeId = "rt1";
    private static final String nodeId = "node1";
    private static final String activeObjectId = "ao1";
    private static final String path = "dir/file.txt";
    private static final DataSpacesURI spaceURI = DataSpacesURI.createScratchSpaceURI(appId, runtimeId,
            nodeId);
    private static final DataSpacesURI fileURI = spaceURI.withActiveObjectId(activeObjectId).withUserPath(
            path);

    private String vfsServerUrl;

    private static DefaultFileSystemManager fileSystemManager;
    private DataSpacesFileObject dsFileObject;

    private static FileSystemServerDeployer server;
    private FileObject adaptee;
    private File testDir;
    private String differentDirPath;
    private String rootDirPath;

    private ArrayList<String> rootUris;

    private String rootFileUri;

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
        final File rootDir = new File(testDir, "root space");
        final File aoDir = new File(rootDir, "ao1");
        final File someDir = new File(aoDir, "dir");
        final File someFile = new File(someDir, "file.txt");
        AbstractIOOperationsBase.deleteRecursively(testDir);
        assertTrue(someDir.mkdirs());
        assertTrue(differentDir.mkdir());
        assertTrue(someFile.createNewFile());

        rootDirPath = rootDir.getCanonicalPath();
        rootFileUri = rootDir.toURI().toURL().toExternalForm();
        differentDirPath = differentDir.getCanonicalPath();

        final FileObject rootFileObject = fileSystemManager.resolveFile(rootFileUri);
        if (server == null) {
            server = new FileSystemServerDeployer(rootDirPath, false);
        }
        vfsServerUrl = server.getVFSRootURL();

        final FileName mountintPointFileName = rootFileObject.getName();
        adaptee = rootFileObject.resolveFile(fileURI.getRelativeToSpace());

        rootUris = new ArrayList<String>();
        rootUris.add(rootFileUri);
        rootUris.add(vfsServerUrl);

        dsFileObject = new VFSFileObjectAdapter(adaptee, spaceURI, mountintPointFileName, rootUris,
            rootFileUri);
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
        final FileObject rootFileObject = fileSystemManager.resolveFile(rootFileUri);
        final FileName mountintPointFileName = rootFileObject.getName();
        final FileObject rootAdaptee = rootFileObject;
        final DataSpacesFileObject fo = new VFSFileObjectAdapter(rootAdaptee, spaceURI,
            mountintPointFileName, rootUris, rootFileUri);

        assertEquals(spaceURI.toString(), fo.getVirtualURI());
    }

    @Test
    public void testGetURI2() throws FileSystemException {
        assertEquals(fileURI.toString(), dsFileObject.getVirtualURI());
    }

    @Test
    public void testGetRealURI_WithSpecialChars() throws Exception {
        // because of PROACTIVE-1314, uri string returned by getRealURI() can escape characters and thus be different than the original URL
        assertEquals(URIHelper.convertToEncodedURIString(adaptee.getURL().toString()),
                dsFileObject.getRealURI());
        assertEquals(rootUris, dsFileObject.getAllSpaceRootURIs());

        // testing that all are valid uris
        new URI(dsFileObject.getRealURI());
        new URI(dsFileObject.getSpaceRootURI());
        for (String uri : dsFileObject.getAllSpaceRootURIs()) {
            new URI(uri);
        }
        for (String uri : dsFileObject.getAllRealURIs()) {
            new URI(uri);
        }

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
            FileSystemException, MalformedURLException {
        FileObject rootFileObject;
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.windows) {
            rootFileObject = fileSystemManager.resolveFile(new File("c:\\").toURI().toURL().toString());
        } else {
            rootFileObject = fileSystemManager.resolveFile(new File("/").toURI().toURL().toString());
        }
        final FileName mountintPointFileName = rootFileObject.getName();
        final FileObject rootAdaptee = rootFileObject;
        ArrayList<String> fos = new ArrayList<String>();
        fos.add("file:///");
        final DataSpacesFileObject fo = new VFSFileObjectAdapter(rootAdaptee, spaceURI,
            mountintPointFileName, fos, "file:///");
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

        new VFSFileObjectAdapter(adaptee, spaceURI, diffName, rootUris, rootUris.get(0));
    }

    private void assertIsSomeDir(DataSpacesFileObject parent) throws FileSystemException {
        assertEquals(spaceURI.withActiveObjectId(activeObjectId).withUserPath("dir").toString(),
                parent.getVirtualURI());
        final List<DataSpacesFileObject> desc = parent.getChildren();
        assertEquals(1, desc.size());
        assertTrue(desc.contains(dsFileObject));
        assertEquals(dsFileObject, parent.getChild("file.txt"));
    }
}
