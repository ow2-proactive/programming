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

import java.io.IOException;
import java.util.ArrayList;

import org.objectweb.proactive.extensions.dataspaces.vfs.AbstractLimitingFileObject;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.temp.TemporaryFileProvider;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Test for general access and write access limiting and keeping (Abstract)FileObject behavior
 * (which is not so obvious, especially regarding unusual behavior like non-existing file, null
 * array etc.).
 */
public class AbstractLimitingFileObjectTest {
    private static final String CHILD_NAME = "abc";
    private FileObject realFile;
    private FileObject readOnlyFile;
    private FileObject readWriteFile;
    private FileObject ancestorLimitedFile;
    private FileObject anotherFile;
    private DefaultFileSystemManager manager;

    @Before
    public void setUp() throws Exception {
        manager = VFSFactory.createDefaultFileSystemManager();
        manager.addProvider("tmpfs", new TemporaryFileProvider());

        realFile = manager.resolveFile("tmpfs:///test1/test2");

        readWriteFile = new ConstantlyLimitingFileObject(realFile, false, true);
        readOnlyFile = new ConstantlyLimitingFileObject(realFile, true, true);
        ancestorLimitedFile = new ConstantlyLimitingFileObject(realFile, false, false);

        anotherFile = manager.resolveFile("tmpfs:///test2");
        anotherFile.createFile();

        realFile.delete();

        assertFalse(readOnlyFile.exists());
        assertFalse(readWriteFile.exists());
        assertTrue(anotherFile.exists());
    }

    private void createRealFile() throws FileSystemException {
        realFile.createFile();
        assertTrue(readOnlyFile.exists());
    }

    private void createRealFolder() throws FileSystemException {
        realFile.createFolder();
        assertTrue(readOnlyFile.exists());
    }

    private void createRealFileChild() throws FileSystemException {
        realFile.createFolder();
        final FileObject childFile = realFile.resolveFile(CHILD_NAME);
        childFile.createFile();
        assertTrue(childFile.exists());
    }

    @After
    public void tearDown() throws Exception {
        if (realFile != null) {
            realFile.close();
            realFile = null;
        }
        if (manager != null) {
            manager.close();
            manager = null;
        }
    }

    @Test
    public void testReadOnlyCreateFile() throws FileSystemException {
        try {
            readOnlyFile.createFile();
            fail("Expected exception");
        } catch (FileSystemException e) {
        }
        assertFalse(readOnlyFile.exists());
    }

    @Test
    public void testReadOnlyCreateFolder() throws FileSystemException {
        try {
            readOnlyFile.createFolder();
            fail("Expected exception");
        } catch (FileSystemException e) {
        }
        assertFalse(readOnlyFile.exists());
    }

    @Test
    public void testReadOnlyDelete() throws FileSystemException {
        createRealFile();

        try {
            readOnlyFile.delete();
            fail("Expected exception");
        } catch (FileSystemException e) {
        }
        assertTrue(readOnlyFile.exists());
    }

    @Test
    public void testReadOnlyDeleteFileSelector() throws FileSystemException {
        createRealFile();

        try {
            readOnlyFile.delete(Selectors.SELECT_ALL);
            fail("Expected exception");
        } catch (FileSystemException e) {
        }
        assertTrue(readOnlyFile.exists());
    }

    @Test
    public void testReadOnlyIsWriteable() throws FileSystemException {
        assertFalse(readOnlyFile.isWriteable());
    }

    @Test
    public void testReadOnlyCanRenameToFileObjectSource() {
        assertFalse(readOnlyFile.canRenameTo(anotherFile));
    }

    // limitation of AbstractLimitingFileObject
    @Ignore
    @Test
    public void testReadOnlyCanRenameToFileObjectDesination() {
        assertFalse(anotherFile.canRenameTo(readOnlyFile));
    }

    @Test
    public void testReadOnlyCopyFromFileObjectFileSelector() throws FileSystemException {
        try {
            readOnlyFile.copyFrom(anotherFile, Selectors.SELECT_ALL);
            fail("Expected exception");
        } catch (FileSystemException e) {
        }
        assertFalse(readOnlyFile.exists());
    }

    @Test
    public void testReadOnlyMoveToFileObjectSource() throws FileSystemException {
        createRealFile();

        try {
            readOnlyFile.moveTo(anotherFile);
            fail("Expected exception");
        } catch (FileSystemException e) {
        }
        assertTrue(readOnlyFile.exists());
    }

    // limitation of AbstractLimitingFileObject (rely on rename operation internally)
    @Ignore
    @Test
    public void testReadOnlyMoveToFileObjectDestination() throws FileSystemException {
        createRealFile();
        try {
            anotherFile.moveTo(readOnlyFile);
            fail("Expected exception");
        } catch (FileSystemException e) {
        }
        assertTrue(anotherFile.exists());
    }

    @Test
    public void testReadOnlyFindFilesFileSelector() throws FileSystemException {
        createRealFileChild();

        final FileObject result[] = readOnlyFile.findFiles(Selectors.SELECT_CHILDREN);
        assertEquals(1, result.length);
        assertFalse(result[0].isWriteable());
    }

    @Test
    public void testReadOnlyFindFilesFileSelectorNonExisting() throws FileSystemException {
        final FileObject result[] = readOnlyFile.findFiles(Selectors.SELECT_CHILDREN);
        assertNull(result);
    }

    @Test
    public void testReadOnlyFindFilesFileSelectorBooleanList() throws FileSystemException {
        createRealFileChild();

        final ArrayList<FileObject> result = new ArrayList<FileObject>();
        readOnlyFile.findFiles(Selectors.SELECT_CHILDREN, true, result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isWriteable());
    }

    @Test
    public void testReadOnlyResolveFileString() throws FileSystemException {
        createRealFileChild();

        final FileObject childFile = readOnlyFile.resolveFile(CHILD_NAME);
        assertNotNull(childFile);
        assertFalse(childFile.isWriteable());
    }

    @Test
    public void testReadOnlyResolveFileStringNameScope() throws FileSystemException {
        createRealFileChild();

        final FileObject childFile = readOnlyFile.resolveFile(CHILD_NAME, NameScope.CHILD);
        assertNotNull(childFile);
        assertFalse(childFile.isWriteable());
    }

    @Test
    public void testReadOnlyGetChildStringExisting() throws FileSystemException {
        createRealFileChild();

        final FileObject childFile = readOnlyFile.getChild(CHILD_NAME);
        assertNotNull(childFile);
        assertFalse(childFile.isWriteable());
    }

    @Test
    public void testReadOnlyGetChildStringNonExisting() throws FileSystemException {
        createRealFolder();

        final FileObject childFile = readOnlyFile.getChild(CHILD_NAME);
        assertNull(childFile);
    }

    @Test
    public void testReadOnlyGetChildrenExisting() throws FileSystemException {
        createRealFileChild();

        final FileObject childrenFiles[] = readOnlyFile.getChildren();
        assertNotNull(childrenFiles);
        assertEquals(1, childrenFiles.length);
        assertFalse(childrenFiles[0].isWriteable());
    }

    @Test
    public void testReadOnlyGetChildrenNonExisting() throws FileSystemException {
        createRealFolder();

        final FileObject childrenFiles[] = readOnlyFile.getChildren();
        assertNotNull(childrenFiles);
        assertEquals(0, childrenFiles.length);
    }

    @Test
    public void testReadOnlyGetParent() throws FileSystemException {
        final FileObject parent = readOnlyFile.getParent();
        assertNotNull(parent);
        assertFalse(parent.isWriteable());
    }

    @Test
    public void testReadOnlyGetParentForRoot() throws FileSystemException {
        final FileObject rawRoot = readOnlyFile.getFileSystem().getRoot();
        final ConstantlyLimitingFileObject root = new ConstantlyLimitingFileObject(rawRoot, true, true);
        final FileObject parent = root.getParent();

        assertNull(parent);
    }

    @Test
    public void testReadOnlyGetContentInputStream() throws IOException {
        createRealFile();

        final FileContent content = readOnlyFile.getContent();
        try {
            content.getInputStream().close();
        } finally {
            content.close();
        }
    }

    @Test
    public void testReadOnlyGetContentOutputStream() throws IOException {
        createRealFile();

        final FileContent content = readOnlyFile.getContent();
        try {
            content.getOutputStream();
            fail("Expected exception");
        } catch (FileSystemException x) {
        } finally {
            content.close();
        }
    }

    @Test
    public void testReadOnlyGetContentRandomInputStream() throws IOException {
        createRealFile();

        final FileContent content = readOnlyFile.getContent();
        try {
            content.getRandomAccessContent(RandomAccessMode.READ).close();
        } finally {
            content.close();
        }
    }

    @Test
    public void testReadOnlyGetContentRandomOutputStream() throws IOException {
        createRealFile();

        final FileContent content = readOnlyFile.getContent();
        try {
            content.getRandomAccessContent(RandomAccessMode.READWRITE).close();
            fail("Expected exception");
        } catch (FileSystemException x) {
        } finally {
            content.close();
        }
    }

    //FIXME: depends on VFS-259, fixed in VFS fork
    @Test
    public void testReadOnlyGetContentGetFile() throws FileSystemException {
        final FileObject sameFile = readOnlyFile.getContent().getFile();
        assertFalse(sameFile.isWriteable());
    }

    @Test
    public void testReadWriteCreateFile() throws FileSystemException {
        readWriteFile.createFile();
        assertTrue(readWriteFile.exists());
    }

    @Test
    public void testReadWriteCreateFolder() throws FileSystemException {
        readWriteFile.createFolder();
        assertTrue(readWriteFile.exists());
    }

    @Test
    public void testReadWriteDelete() throws FileSystemException {
        createRealFile();
        readWriteFile.delete();
        assertFalse(readWriteFile.exists());
    }

    @Test
    public void testReadWriteDeleteFileSelector() throws FileSystemException {
        createRealFile();
        readWriteFile.delete(Selectors.SELECT_ALL);
        assertFalse(readWriteFile.exists());
    }

    @Test
    public void testReadWriteIsWriteable() throws FileSystemException {
        createRealFile();
        assertTrue(readWriteFile.isWriteable());
    }

    @Test
    public void testReadWriteCanRenameToFileObjectSource() {
        assertTrue(readWriteFile.canRenameTo(anotherFile));
    }

    @Test
    public void testReadWriteCanRenameToFileObjectDesination() {
        assertTrue(anotherFile.canRenameTo(readWriteFile));
    }

    @Test
    public void testReadWriteCopyFromFileObjectFileSelector() throws FileSystemException {
        readWriteFile.copyFrom(anotherFile, Selectors.SELECT_ALL);
        assertTrue(readWriteFile.exists());
    }

    @Test
    public void testReadWriteMoveToFileObjectSource() throws FileSystemException {
        createRealFile();

        readWriteFile.moveTo(anotherFile);
        assertFalse(readWriteFile.exists());
    }

    @Test
    public void testReadWriteMoveToFileObjectDestination() throws FileSystemException {
        createRealFile();
        anotherFile.moveTo(readWriteFile);
        assertFalse(anotherFile.exists());
    }

    @Test
    public void testReadWriteFindFilesFileSelector() throws FileSystemException {
        createRealFileChild();

        final FileObject result[] = readWriteFile.findFiles(Selectors.SELECT_CHILDREN);
        assertEquals(1, result.length);
        assertTrue(result[0].isWriteable());
    }

    @Test
    public void testReadWriteFindFilesFileSelectorNonExisting() throws FileSystemException {
        final FileObject result[] = readWriteFile.findFiles(Selectors.SELECT_CHILDREN);
        assertNull(result);
    }

    @Test
    public void testReadWriteFindFilesFileSelectorBooleanList() throws FileSystemException {
        createRealFileChild();

        final ArrayList<FileObject> result = new ArrayList<FileObject>();
        readWriteFile.findFiles(Selectors.SELECT_CHILDREN, true, result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isWriteable());
    }

    @Test
    public void testReadWriteResolveFileString() throws FileSystemException {
        createRealFileChild();

        final FileObject childFile = readWriteFile.resolveFile(CHILD_NAME);
        assertNotNull(childFile);
        assertTrue(childFile.isWriteable());
    }

    @Test
    public void testReadWriteResolveFileStringNameScope() throws FileSystemException {
        createRealFileChild();

        final FileObject childFile = readWriteFile.resolveFile(CHILD_NAME, NameScope.CHILD);
        assertNotNull(childFile);
        assertTrue(childFile.isWriteable());
    }

    @Test
    public void testReadWriteGetChildStringExisting() throws FileSystemException {
        createRealFileChild();

        final FileObject childFile = readWriteFile.getChild(CHILD_NAME);
        assertNotNull(childFile);
        assertTrue(childFile.isWriteable());
    }

    @Test
    public void testReadWriteGetChildStringNonExisting() throws FileSystemException {
        createRealFolder();

        final FileObject childFile = readWriteFile.getChild(CHILD_NAME);
        assertNull(childFile);
    }

    @Test
    public void testReadWriteGetChildrenExisting() throws FileSystemException {
        createRealFileChild();

        final FileObject childrenFiles[] = readWriteFile.getChildren();
        assertNotNull(childrenFiles);
        assertEquals(1, childrenFiles.length);
        assertTrue(childrenFiles[0].isWriteable());
    }

    @Test
    public void testReadWriteGetChildrenNonExisting() throws FileSystemException {
        createRealFolder();

        final FileObject childrenFiles[] = readWriteFile.getChildren();
        assertNotNull(childrenFiles);
        assertEquals(0, childrenFiles.length);
    }

    @Test
    public void testWriteOnlyGetParent() throws FileSystemException {
        final FileObject parent = readWriteFile.getParent();
        assertNotNull(parent);
        assertTrue(parent.isWriteable());
    }

    @Test
    public void testReadWriteGetParentForRoot() throws FileSystemException {
        final FileObject rawRoot = readWriteFile.getFileSystem().getRoot();
        final ConstantlyLimitingFileObject root = new ConstantlyLimitingFileObject(rawRoot, true, true);
        final FileObject parent = root.getParent();

        assertNull(parent);
    }

    @Test
    public void testReadWriteGetContentInputStream() throws IOException {
        createRealFile();

        final FileContent content = readWriteFile.getContent();
        try {
            content.getInputStream().close();
        } finally {
            content.close();
        }
    }

    @Test
    public void testReadWriteGetContentOutputStream() throws IOException {
        createRealFile();

        final FileContent content = readWriteFile.getContent();
        try {
            content.getOutputStream();
        } finally {
            content.close();
        }
    }

    @Test
    public void testReadWriteGetContentRandomInputStream() throws IOException {
        createRealFile();

        final FileContent content = readWriteFile.getContent();
        try {
            content.getRandomAccessContent(RandomAccessMode.READ).close();
        } finally {
            content.close();
        }
    }

    @Test
    public void testReadWriteGetContentRandomOutputStream() throws IOException {
        createRealFile();

        final FileContent content = readWriteFile.getContent();
        try {
            content.getRandomAccessContent(RandomAccessMode.READWRITE).close();
        } finally {
            content.close();
        }
    }

    @Test
    public void testReadWriteGetContentGetFile() throws FileSystemException {
        final FileObject sameFile = readWriteFile.getContent().getFile();
        assertTrue(sameFile.isWriteable());
    }

    @Test
    public void testAncestorLimitedGetParent() throws FileSystemException {
        assertNull(ancestorLimitedFile.getParent());
    }

    @Test(expected = FileSystemException.class)
    public void testAncestorLimitedResolveFileParent() throws FileSystemException {
        ancestorLimitedFile.resolveFile("../");
    }

    @Test
    public void testAncestorLimitedResolveFileChild() throws FileSystemException {
        final FileObject child = ancestorLimitedFile.resolveFile("unexisting_file");
        assertNotNull(child);
    }

    private static class ConstantlyLimitingFileObject extends
            AbstractLimitingFileObject<ConstantlyLimitingFileObject> {
        private final boolean readOnly;
        private boolean allowReturnAncestor;

        public ConstantlyLimitingFileObject(final FileObject fileObject, final boolean readOnly,
                final boolean allowReturnAncestor) {
            super(fileObject);
            this.readOnly = readOnly;
            this.allowReturnAncestor = allowReturnAncestor;
        }

        @Override
        protected boolean isReadOnly() {
            return readOnly;
        }

        @Override
        protected ConstantlyLimitingFileObject doDecorateFile(FileObject file) {
            return new ConstantlyLimitingFileObject(file, readOnly, allowReturnAncestor);
        }

        @Override
        protected boolean canReturnAncestor(ConstantlyLimitingFileObject decoratedAncestor) {
            return allowReturnAncestor;
        }
    }
}
