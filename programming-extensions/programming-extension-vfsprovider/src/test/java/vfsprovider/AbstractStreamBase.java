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
package vfsprovider;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.server.Stream;


/**
 * Any failure may be caused by temporal native file system state. Stream capabilities mostly are
 * not independent (and hence the test cases aren't..), although these tests try to avoid this
 * assumption, wherever it is straightforward. Only read capability is often used to assert the
 * stream behavior.
 * <p>
 * Often a {@link Integer#MAX_VALUE} is used for a long argument, as few native methods may not
 * accept {@link Long#MAX_VALUE} and throw an IOException.
 */
public abstract class AbstractStreamBase extends AbstractIOOperationsBase {

    protected static final String TEST_FILE_NEW_CONTENT = "fouxdufafafauxdufafafafa";

    protected static final int TEST_FILE_NEW_CONTENT_LEN = TEST_FILE_NEW_CONTENT.getBytes().length;

    protected Stream stream;

    protected abstract Stream getInstance(File f) throws Exception;

    @Override
    public String getTestDirFilename() {
        return "ProActive-StreamOperationsTest";
    }

    /**
     * Subclasses may override this method to provide more sophisticated {@link #getPositionTest()}
     * test if are capable to change the streams pointer position through
     * writing/reading/skipping/seeking.
     * 
     * @param s
     *            stream of whose pointer position change
     * @return position change offset
     * @throws Exception
     *             in case of any exception
     */
    protected long changePosition(Stream s) throws Exception {
        return 0;
    }

    /**
     * Subclasses may override this method to provide more sophisticated
     * {@link #getLengthAfterChange()} test if are capable to change the files length through
     * writing or seeking.
     * 
     * @param s
     *            stream of whose length change
     * @return new length
     * @throws Exception
     *             in case of any exception
     */
    protected long changeFileLength(Stream s) throws Exception {
        return TEST_FILE_CONTENT_LEN;
    }

    @Before
    public void setUp() throws Exception {
        stream = getInstance(testFile);
        assertNotNull(stream);
    }

    @After
    public void tearDown() throws IOException {
        if (stream != null)
            stream.close();
    }

    @Test
    public void createFromNotExistingFileTest() throws Exception {
        final File notExisting = new File(testDir, "not-existing-file");
        assertFalse(notExisting.exists());
        getInstance(notExisting).close();
    }

    @Test(expected = FileNotFoundException.class)
    public void createFromDirectoryTest() throws Exception {
        assertTrue(testDir.isDirectory());
        getInstance(testDir);
    }

    @Test
    public void readTest() throws IOException, WrongStreamTypeException {
        final int len = TEST_FILE_CONTENT_LEN;
        final byte[] content = stream.read(len);
        assertArrayEquals(TEST_FILE_CONTENT.getBytes(), content);
    }

    @Test
    public void readZeroTest() throws IOException, WrongStreamTypeException {
        final byte[] content = stream.read(0);
        Assert.assertEquals(0, content.length);
    }

    @Test
    public void readMoreTest() throws IOException, WrongStreamTypeException {
        final int len = TEST_FILE_CONTENT_LEN + 10;
        final byte[] content = stream.read(len);
        assertArrayEquals(TEST_FILE_CONTENT.getBytes(), content);
    }

    /**
     * Override it if reading is not supported.
     */
    @Test
    public void skipTest() throws IOException, WrongStreamTypeException {
        final long skipped = stream.skip(1);
        final int len = TEST_FILE_CONTENT_LEN - 1;
        final byte[] content = stream.read(len);

        assertEquals(1, skipped);
        assertArrayEquals(TEST_FILE_CONTENT.substring(1).getBytes(), content);
    }

    /**
     * Override it if reading is not supported.
     */
    @Test
    public void skipTestZero() throws IOException, WrongStreamTypeException {
        final long skipped = stream.skip(0);
        final int len = TEST_FILE_CONTENT_LEN;
        final byte[] content = stream.read(len);

        assertEquals(0, skipped);
        assertArrayEquals(TEST_FILE_CONTENT.getBytes(), content);
    }

    /**
     * Override it if reading is not supported.
     */
    @Test
    public void skipTestMore() throws IOException, WrongStreamTypeException {
        final long skipped = stream.skip(Integer.MAX_VALUE);
        final int len = TEST_FILE_CONTENT_LEN;
        final byte[] content = stream.read(len);

        assertEquals(len, skipped);
        assertNull(content);
    }

    @Test
    public void getLengthTest() throws IOException, WrongStreamTypeException {
        assertEquals(TEST_FILE_CONTENT_LEN, stream.getLength());
    }

    @Test
    public void getLengthAfterChange() throws Exception {
        final long len = changeFileLength(stream);
        assertEquals(len, stream.getLength());
    }

    @Test
    public void getPositionTest() throws Exception {
        final long pos = changePosition(stream);
        assertEquals(pos, stream.getPosition());
    }

    @Test
    public void writeTest() throws IOException, WrongStreamTypeException {
        final byte[] content = new byte[TEST_FILE_NEW_CONTENT_LEN * 2];
        final InputStream is;

        stream.write(TEST_FILE_NEW_CONTENT.getBytes());
        stream.close();
        is = new FileInputStream(testFile);
        assertEquals(TEST_FILE_NEW_CONTENT_LEN, is.read(content));

        byte actual[] = new byte[TEST_FILE_NEW_CONTENT_LEN];
        System.arraycopy(content, 0, actual, 0, actual.length);
        assertArrayEquals(TEST_FILE_NEW_CONTENT.getBytes(), actual);
        is.close();
    }

    @Test
    public void flushTest() throws IOException, WrongStreamTypeException {
        final byte[] content = new byte[TEST_FILE_NEW_CONTENT_LEN * 2];
        final InputStream is;

        stream.write(TEST_FILE_NEW_CONTENT.getBytes());
        is = new FileInputStream(testFile);
        assertEquals(TEST_FILE_NEW_CONTENT_LEN, is.read(content));

        byte actual[] = new byte[TEST_FILE_NEW_CONTENT_LEN];
        System.arraycopy(content, 0, actual, 0, actual.length);
        assertArrayEquals(TEST_FILE_NEW_CONTENT.getBytes(), actual);
        is.close();
    }

    /**
     * Override it if reading is not supported (e.g. use getPosition instead).
     */
    @Test
    public void seekTest() throws IOException, WrongStreamTypeException {
        stream.seek(1);
        final int len = TEST_FILE_CONTENT_LEN - 1;
        final byte[] content = stream.read(len);
        assertArrayEquals(TEST_FILE_CONTENT.substring(1).getBytes(), content);
    }

    /**
     * Override it when no seek or getLength operation is supported. Test: getLength should not be
     * affected after seek operation
     */
    @Test
    public void seekAndGetLengthTest() throws IOException, WrongStreamTypeException {
        final long len = stream.getLength();
        stream.seek(Integer.MAX_VALUE);
        assertEquals(len, stream.getLength());
    }
}
