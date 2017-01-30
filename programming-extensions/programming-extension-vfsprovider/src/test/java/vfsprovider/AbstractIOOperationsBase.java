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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;


@Ignore
public abstract class AbstractIOOperationsBase {

    protected static final String TEST_FILENAME = "test.txt";

    protected static final String TEST_FILE_CONTENT = "qwerty";

    protected static final int TEST_FILE_CONTENT_LEN = TEST_FILE_CONTENT.getBytes().length;

    protected File testFile;

    protected File testDir;

    public static boolean deleteRecursively(File file) {
        if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null)
                for (File ch : children)
                    deleteRecursively(ch);
        }
        return file.delete();
    }

    public String getTestDirFilename() {
        return "ProActive-AbstractIOOperationsBase";
    }

    @Before
    public void createFiles() throws Exception {
        testDir = new File(System.getProperty("java.io.tmpdir"), getTestDirFilename());
        testFile = new File(testDir, TEST_FILENAME);
        assertTrue(testDir.mkdirs());
        assertTrue(testFile.createNewFile());

        final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(testFile));
        osw.write(TEST_FILE_CONTENT);
        osw.close();
    }

    @After
    public void deleteFiles() throws IOException {
        if (testDir != null)
            deleteRecursively(testDir);
        assertFalse(testDir.exists());
        testDir = null;
    }
}
