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
package functionalTests.vfsprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;

import unitTests.vfsprovider.AbstractIOOperationsBase;


/**
 * ProActiveProvider and FileSystemServerImpl tests for autoclosing feature.
 */
public class TestProActiveProviderAutoclosing extends AbstractIOOperationsBase {
    private static final int AUTOCLOSE_TIME = 100;
    private static final int CHECKING_TIME = 10;
    private static final int SLEEP_TIME = AUTOCLOSE_TIME * 3;
    // big enough to ignore buffering behavior
    private static final int TEST_FILE_A_CHARS_NUMBER = 1000000;
    private static final int TEST_FILE_B_CHARS_NUMBER = 1000000;

    private static BufferedReader openBufferedReader(final FileObject fo) throws FileSystemException {
        return getBufferedReader(openInputStream(fo));
    }

    private static InputStream openInputStream(final FileObject fo) throws FileSystemException {
        return fo.getContent().getInputStream();
    }

    private static OutputStreamWriter openWriter(final FileObject fo) throws FileSystemException {
        return new OutputStreamWriter(fo.getContent().getOutputStream());
    }

    private static RandomAccessContent openRandomAccessContent(final FileObject fo,
            final RandomAccessMode mode) throws FileSystemException {
        return fo.getContent().getRandomAccessContent(mode);
    }

    private static BufferedReader getBufferedReader(final InputStream is) throws FileSystemException {
        return new BufferedReader(new InputStreamReader(is));
    }

    private static void assertContentEquals(final FileObject fo, final String expectedContent)
            throws FileSystemException, IOException {
        final BufferedReader reader = openBufferedReader(fo);
        try {
            assertEquals(expectedContent, reader.readLine());
        } finally {
            reader.close();
        }
    }

    private FileSystemServerDeployer serverDeployer;
    private DefaultFileSystemManager vfsManager;

    @Before
    public void setUp() throws Exception {
        // overwrite TEST_FILENAME with content for our needs
        final Writer writer = new BufferedWriter(new FileWriter(new File(testDir, TEST_FILENAME)));
        for (int i = 0; i < TEST_FILE_A_CHARS_NUMBER; i++) {
            writer.write("a");
        }
        for (int i = 0; i < TEST_FILE_B_CHARS_NUMBER; i++) {
            writer.write("b");
        }
        writer.close();

        // start FileSystemServer with proper settings
        CentralPAPropertyRepository.PA_VFSPROVIDER_SERVER_STREAM_AUTOCLOSE_CHECKING_INTERVAL_MILLIS
                .setValue(CHECKING_TIME);
        CentralPAPropertyRepository.PA_VFSPROVIDER_SERVER_STREAM_OPEN_MAXIMUM_PERIOD_MILLIS
                .setValue(AUTOCLOSE_TIME);
        serverDeployer = new FileSystemServerDeployer(testDir.getAbsolutePath(), true);

        // set up VFS manager with ProActiveProvider
        vfsManager = VFSFactory.createDefaultFileSystemManager();
    }

    @After
    public void tearDown() throws Exception {
        if (vfsManager != null) {
            vfsManager.close();
            vfsManager = null;
        }

        if (serverDeployer != null) {
            serverDeployer.terminate();
            serverDeployer = null;
        }
    }

    @Test
    public void testOutputStreamOpenAutocloseWrite() throws Exception {
        final FileObject fo = openFileObject("out.txt");
        final Writer writer = openWriter(fo);
        try {
            Thread.sleep(SLEEP_TIME);
            writer.write("test");
        } finally {
            writer.close();
        }
        assertContentEquals(fo, "test");
        fo.close();
    }

    @Test
    public void testOutputStreamOpenWriteAutocloseWrite() throws Exception {
        final FileObject fo = openFileObject("out.txt");
        final Writer writer = openWriter(fo);
        try {
            writer.write("abc");
            Thread.sleep(SLEEP_TIME);
            writer.write("def");
        } finally {
            writer.close();
        }
        assertContentEquals(fo, "abcdef");
        fo.close();
    }

    @Test
    public void testOutputStreamOpenWriteAutocloseFlush() throws Exception {
        final FileObject fo = openFileObject("out.txt");
        final Writer writer = openWriter(fo);
        try {
            writer.write("ghi");
            Thread.sleep(SLEEP_TIME);
            writer.flush();
            assertContentEquals(fo, "ghi");
        } finally {
            writer.close();
        }
        fo.close();
    }

    @Test
    public void testInputStreamOpenAutocloseRead() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        final BufferedReader reader = openBufferedReader(fo);
        try {
            Thread.sleep(SLEEP_TIME);
            for (int i = 0; i < TEST_FILE_A_CHARS_NUMBER; i++) {
                assertTrue('a' == reader.read());
            }
        } finally {
            reader.close();
        }
        fo.close();
    }

    @Test
    public void testInputStreamOpenReadAutocloseRead() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        final BufferedReader reader = openBufferedReader(fo);
        try {
            for (int i = 0; i < TEST_FILE_A_CHARS_NUMBER; i++) {
                assertTrue('a' == reader.read());
            }
            Thread.sleep(SLEEP_TIME);
            for (int i = 0; i < TEST_FILE_B_CHARS_NUMBER; i++) {
                assertTrue('b' == reader.read());
            }
        } finally {
            reader.close();
        }
        fo.close();
    }

    @Test
    public void testInputStreamOpenSkipAutocloseRead() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        // we have to use input stream to avoid Readers buffering (input stream buffering is ok) 
        final InputStream is = openInputStream(fo);
        try {
            is.skip(TEST_FILE_A_CHARS_NUMBER);
            Thread.sleep(SLEEP_TIME);
            assertTrue('b' == is.read());
        } finally {
            is.close();
        }
        fo.close();
    }

    @Test
    public void testRandomReadWriteAccessOpenAutocloseWrite() throws Exception {
        final FileObject fo = openFileObject("out.txt");
        fo.createFile();
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READWRITE);
        try {
            Thread.sleep(SLEEP_TIME);
            rac.write("test".getBytes());
        } finally {
            rac.close();
        }
        assertContentEquals(fo, "test");
        fo.close();
    }

    @Test
    public void testRandomReadWriteAccessOpenWriteAutocloseGetPosWrite() throws Exception {
        final FileObject fo = openFileObject("out.txt");
        fo.createFile();
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READWRITE);
        try {
            rac.write("abc".getBytes());
            Thread.sleep(SLEEP_TIME);
            assertEquals("abc".getBytes().length, rac.getFilePointer());
            rac.write("def".getBytes());
        } finally {
            rac.close();
        }
        assertContentEquals(fo, "abcdef");
        fo.close();
    }

    @Test
    public void testRandomReadWriteAccessOpenWriteSeekAutocloseGetPosWrite() throws Exception {
        final FileObject fo = openFileObject("out.txt");
        fo.createFile();
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READWRITE);
        try {
            rac.write("abcdef".getBytes());
            rac.seek("abc".getBytes().length);
            Thread.sleep(SLEEP_TIME);
            assertEquals("abc".getBytes().length, rac.getFilePointer());
            rac.write("ghi".getBytes());
        } finally {
            rac.close();
        }
        assertContentEquals(fo, "abcghi");
        fo.close();
    }

    @Test
    public void testRandomReadOnlyAccessOpenAutocloseRead() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READ);
        try {
            Thread.sleep(SLEEP_TIME);
            for (int i = 0; i < TEST_FILE_A_CHARS_NUMBER; i++) {
                assertTrue('a' == rac.readByte());
            }
        } finally {
            rac.close();
        }
        fo.close();
    }

    @Test
    public void testRandomReadOnlyAccessOpenReadAutocloseRead() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READ);
        try {
            for (int i = 0; i < TEST_FILE_A_CHARS_NUMBER; i++) {
                assertTrue('a' == rac.readByte());
            }
            Thread.sleep(SLEEP_TIME);
            for (int i = 0; i < TEST_FILE_B_CHARS_NUMBER; i++) {
                assertTrue('b' == rac.readByte());
            }
        } finally {
            rac.close();
        }
        fo.close();
    }

    @Test
    public void testRandomReadOnlyAccessOpenReadSeekAutocloseGetPosRead() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READ);
        try {
            rac.readByte();
            rac.seek(TEST_FILE_A_CHARS_NUMBER);
            Thread.sleep(SLEEP_TIME);
            assertEquals(TEST_FILE_A_CHARS_NUMBER, rac.getFilePointer());
            assertTrue('b' == rac.readByte());
        } finally {
            rac.close();
        }
        fo.close();
    }

    @Test
    public void testRandomReadOnlyAccessInputStreamOpenAutocloseRead() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READ);
        final BufferedReader reader = getBufferedReader(rac.getInputStream());
        try {
            Thread.sleep(SLEEP_TIME);
            for (int i = 0; i < TEST_FILE_A_CHARS_NUMBER; i++) {
                assertTrue('a' == reader.read());
            }
        } finally {
            rac.close();
        }
        fo.close();
    }

    @Test
    public void testRandomReadOnlyAccessInputStreamOpenReadAutocloseRead() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READ);
        final BufferedReader reader = getBufferedReader(rac.getInputStream());
        try {
            for (int i = 0; i < TEST_FILE_A_CHARS_NUMBER; i++) {
                assertTrue('a' == reader.read());
            }
            Thread.sleep(SLEEP_TIME);
            for (int i = 0; i < TEST_FILE_B_CHARS_NUMBER; i++) {
                assertTrue('b' == reader.read());
            }
        } finally {
            rac.close();
        }
        fo.close();
    }

    @Test
    public void testRandomReadOnlyAccessInputStreamOpenSeekAutocloseReadGetPos() throws Exception {
        final FileObject fo = openFileObject(TEST_FILENAME);
        final RandomAccessContent rac = openRandomAccessContent(fo, RandomAccessMode.READ);
        InputStream is = rac.getInputStream();
        try {
            rac.seek(TEST_FILE_A_CHARS_NUMBER);
            // reget input stream
            is = rac.getInputStream();
            Thread.sleep(SLEEP_TIME);
            for (int i = 0; i < TEST_FILE_B_CHARS_NUMBER; i++) {
                assertTrue('b' == is.read());
            }
            assertEquals(TEST_FILE_A_CHARS_NUMBER + TEST_FILE_B_CHARS_NUMBER, rac.getFilePointer());
        } finally {
            rac.close();
        }
        fo.close();
    }

    private FileObject openFileObject(final String fileName) throws FileSystemException {
        return vfsManager.resolveFile(serverDeployer.getVFSRootURL()).resolveFile(fileName);
    }
}
