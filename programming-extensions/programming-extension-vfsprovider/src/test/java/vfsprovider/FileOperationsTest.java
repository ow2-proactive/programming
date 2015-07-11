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
package vfsprovider;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.proactive.extensions.vfsprovider.protocol.FileInfo;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileOperations;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileType;
import org.objectweb.proactive.extensions.vfsprovider.server.FileSystemServerImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;


/**
 * Tests for file management related operations.
 */
public class FileOperationsTest extends AbstractIOOperationsBase {

    private static final String TEST_SEPARATOR = "\\"; // mix a little
    private static final String READONLY_FILENAME = "another.txt";
    private static final String DIR_FILENAME = "dir";

    private FileOperations server;
    private File readonlyFile;
    private File anotherDir;
    private static final Set<String> ROOT_FILENAMES_EXPECTED = new HashSet<String>();

    static {
        ROOT_FILENAMES_EXPECTED.add(READONLY_FILENAME);
        ROOT_FILENAMES_EXPECTED.add(DIR_FILENAME);
        ROOT_FILENAMES_EXPECTED.add(TEST_FILENAME);
    }

    @Override
    public String getTestDirFilename() {
        return "PROACTIVE-FileOperationsTest";
    }

    @Before
    public void init() throws IOException {
        server = new FileSystemServerImpl(testDir.getAbsolutePath());
        readonlyFile = new File(testDir, READONLY_FILENAME);
        anotherDir = new File(testDir, DIR_FILENAME);
        assertTrue(anotherDir.mkdir());
        assertTrue(readonlyFile.createNewFile());
        assertTrue(readonlyFile.setReadOnly());
    }

    @Test(expected = IOException.class)
    public void listChildrenNotAbsolute() throws IOException {
        server.fileListChildren(DIR_FILENAME);
    }

    @Test(expected = IOException.class)
    public void getInfoNotAbsolute() throws IOException {
        server.fileGetInfo(DIR_FILENAME);
    }

    @Test(expected = IOException.class)
    public void listChildrenInfoNotAbsolute() throws IOException {
        server.fileListChildrenInfo(DIR_FILENAME);
    }

    @Test(expected = IOException.class)
    public void createFileNotAbsolute() throws IOException {
        server.fileCreate("new_name_without_prefix", FileType.FILE);
    }

    @Test(expected = IOException.class)
    public void deleteFileNotAbsolute() throws IOException {
        server.fileDelete(DIR_FILENAME, true);
    }

    @Test(expected = IOException.class)
    public void renameFileNotAbsolute() throws IOException {
        server.fileRename(TEST_FILENAME, "new_filename");
    }

    @Test(expected = IOException.class)
    public void setLastModifiedTimeNotAbsolute() throws IOException {
        server.fileSetLastModifiedTime(TEST_FILENAME, 12345667);
    }

    public void listChildrenNotExisting() throws IOException {
        assertNull(server.fileListChildren(TEST_SEPARATOR + "not_existing"));
    }

    @Test
    public void getInfoNotExisting() throws IOException {
        assertNull(server.fileGetInfo(TEST_SEPARATOR + "not_existing"));
    }

    public void listChildrenInfoNotExisting() throws IOException {
        assertNull(server.fileListChildrenInfo(TEST_SEPARATOR + "not_existing"));
    }

    @Test
    public void deleteFileNotExisting() throws IOException {
        server.fileDelete(TEST_SEPARATOR + "not_existing", true);
    }

    @Test(expected = IOException.class)
    public void renameFileNotExisting() throws IOException {
        server.fileRename(TEST_SEPARATOR + "not_existing", "new_filename");
    }

    @Test(expected = IOException.class)
    public void setLastModifiedTimeNotExisting() throws IOException {
        server.fileSetLastModifiedTime(TEST_SEPARATOR + "not_existing", 12345667);
    }

    @Test
    public void listChildren() throws IOException {
        Set<String> files = server.fileListChildren("\\");
        assertEquals(ROOT_FILENAMES_EXPECTED, files);
    }

    @Test
    public void listChildren2() throws IOException {
        Set<String> files = server.fileListChildren(TEST_SEPARATOR + DIR_FILENAME);
        assertEquals(0, files.size());
    }

    @Test
    public void listChildrenInfo() throws IOException {
        Map<String, FileInfo> files = server.fileListChildrenInfo(TEST_SEPARATOR);
        assertEquals(ROOT_FILENAMES_EXPECTED, files.keySet());
        assertFileInfoMatch(files.get(DIR_FILENAME), FileType.DIRECTORY, false, true, true);
        assertFileInfoMatch(files.get(TEST_FILENAME), FileType.FILE, false, true, true);
        assertFileInfoMatch(files.get(READONLY_FILENAME), FileType.FILE, false, true, false);
    }

    @Test
    public void listChildrenInfo2() throws IOException {
        Map<String, FileInfo> files = server.fileListChildrenInfo(TEST_SEPARATOR + DIR_FILENAME);
        assertEquals(0, files.size());
    }

    @Test
    public void getInfo() throws IOException {
        FileInfo fi;
        fi = server.fileGetInfo(TEST_SEPARATOR + DIR_FILENAME);
        assertFileInfoMatch(fi, FileType.DIRECTORY, false, true, true);

        fi = server.fileGetInfo(TEST_SEPARATOR + READONLY_FILENAME);
        assertFileInfoMatch(fi, FileType.FILE, false, true, false);

        fi = server.fileGetInfo(TEST_SEPARATOR + TEST_FILENAME);
        assertFileInfoMatch(fi, FileType.FILE, false, true, true);
    }

    @Test
    public void createFile() throws IOException {
        final String path = TEST_SEPARATOR + DIR_FILENAME + "/newfile";
        testCreateFile(path, FileType.FILE);
    }

    @Test
    public void createFileWithParent() throws IOException {
        final String path = TEST_SEPARATOR + DIR_FILENAME + "/newdir/newdir/newfile";
        testCreateFile(path, FileType.FILE);
    }

    @Test
    public void createFileInRoot() throws IOException {
        final String path = "/newfile";
        testCreateFile(path, FileType.FILE);
    }

    @Test
    public void createDir() throws IOException {
        final String path = TEST_SEPARATOR + DIR_FILENAME + "/newDir";
        testCreateFile(path, FileType.DIRECTORY);
    }

    @Test
    public void createDirWithParent() throws IOException {
        final String path = TEST_SEPARATOR + DIR_FILENAME + "/newdir/newdir/newDir";
        testCreateFile(path, FileType.DIRECTORY);
    }

    @Test
    public void createDirInRoot() throws IOException {
        final String path = "/newfile";
        testCreateFile(path, FileType.DIRECTORY);
    }

    @Test
    public void createFileExisting() throws IOException {
        final String path = TEST_SEPARATOR + TEST_FILENAME;
        testCreateFile(path, FileType.FILE);
    }

    @Test
    public void createDirExisting() throws IOException {
        final String path = TEST_SEPARATOR + DIR_FILENAME;
        testCreateFile(path, FileType.DIRECTORY);
    }

    @Test(expected = IOException.class)
    public void createFileAsExistingDir() throws IOException {
        final String path = TEST_SEPARATOR + DIR_FILENAME;
        server.fileCreate(path, FileType.FILE);
    }

    @Test(expected = IOException.class)
    public void createDirAsExistingFile() throws IOException {
        final String path = TEST_SEPARATOR + TEST_FILENAME;
        server.fileCreate(path, FileType.DIRECTORY);
    }

    @Test
    public void deleteFileInRoot() throws IOException {
        final String path = TEST_SEPARATOR + TEST_FILENAME;
        server.fileDelete(path, false);
        assertNull(server.fileGetInfo(path));
    }

    @Test
    public void deleteDirInRoot() throws IOException {
        final String path = TEST_SEPARATOR + DIR_FILENAME;
        server.fileDelete(path, false);
        assertNull(server.fileGetInfo(path));
    }

    @Test
    public void deleteDirRecursively() throws IOException {
        final String dirPath = TEST_SEPARATOR + DIR_FILENAME;
        assertTrue(new File(this.anotherDir, "newFile").createNewFile());
        server.fileDelete(dirPath, true);
        assertNull(server.fileGetInfo(dirPath));
    }

    @Test(expected = IOException.class)
    public void deleteDirNotEmpty() throws IOException {
        final String dirPath = TEST_SEPARATOR + DIR_FILENAME;
        assertTrue(new File(this.anotherDir, "newFile").createNewFile());
        server.fileDelete(dirPath, false);
    }

    @Test(expected = IOException.class)
    public void deleteRootRecursively() throws IOException {
        final String path = TEST_SEPARATOR;
        server.fileDelete(path, true);
    }

    @Test(expected = IOException.class)
    public void deleteRoot() throws IOException {
        final String path = TEST_SEPARATOR;
        server.fileDelete(path, false);
    }

    @Test(expected = IOException.class)
    public void renameRoot() throws IOException {
        server.fileRename(TEST_SEPARATOR, TEST_SEPARATOR + DIR_FILENAME);
    }

    @Test
    public void renameFile() throws IOException {
        final String newPath = "/newfile";
        server.fileRename(TEST_SEPARATOR + TEST_FILENAME, newPath);
        final FileInfo fi = server.fileGetInfo(newPath);
        assertFileInfoMatch(fi, FileType.FILE, false, true, true);
    }

    @Test
    public void renameFile2() throws IOException {
        final String newPath = TEST_SEPARATOR + DIR_FILENAME + "/newfile";
        server.fileRename(TEST_SEPARATOR + TEST_FILENAME, newPath);
        final FileInfo fi = server.fileGetInfo(newPath);
        assertFileInfoMatch(fi, FileType.FILE, false, true, true);
    }

    /**
     * This test may be platform dependent as rename operation MAY succeed when a destination file
     * already exists.
     */
    @Ignore
    @Test(expected = IOException.class)
    public void renameFileToExistingDir() throws IOException {
        final String newPath = TEST_SEPARATOR + DIR_FILENAME;
        server.fileRename(TEST_SEPARATOR + TEST_FILENAME, newPath);
    }

    @Test
    public void renameDir() throws IOException {
        final String newPath = "/newdir";
        server.fileRename(TEST_SEPARATOR + DIR_FILENAME, newPath);
        final FileInfo fi = server.fileGetInfo(newPath);
        assertFileInfoMatch(fi, FileType.DIRECTORY, false, true, true);
    }

    @Test
    public void setLastModifiedTimeOnDir() throws IOException {
        final String path = TEST_SEPARATOR + DIR_FILENAME;
        final long time = Calendar.getInstance().getTimeInMillis();
        server.fileSetLastModifiedTime(path, time);
        final long timeRead = server.fileGetInfo(path).getLastModifiedTime();
        assertEquals(time / 1000, timeRead / 1000);
    }

    @Test
    public void setLastModifiedTimeOnFile() throws IOException {
        final String path = TEST_SEPARATOR + TEST_FILENAME;
        final long time = Calendar.getInstance().getTimeInMillis();
        server.fileSetLastModifiedTime(path, time);
        final long timeRead = server.fileGetInfo(path).getLastModifiedTime();
        assertEquals(time / 1000, timeRead / 1000);
    }

    private void testCreateFile(final String path, final FileType fileType) throws IOException {
        final FileInfo fi;
        server.fileCreate(path, fileType);
        fi = server.fileGetInfo(path);
        assertFileInfoMatch(fi, fileType, false, true, true);
    }

    private void assertFileInfoMatch(FileInfo fileInfo, FileType fType, boolean hidden, boolean readable,
            boolean writable) {

        assertEquals(fileInfo.getType(), fType);
        assertEquals(fileInfo.isHidden(), hidden);
        assertEquals(fileInfo.isReadable(), readable);
        if (!writable) {
            assumeFalse(
                    "Probably running the test as root as we cannot make the file read only (JDK-6931128)",
                    fileInfo.isWritable());
        }
        assertEquals(fileInfo.isWritable(), writable);
    }
}
