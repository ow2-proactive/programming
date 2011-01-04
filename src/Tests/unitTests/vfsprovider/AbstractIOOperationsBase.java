/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package unitTests.vfsprovider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;

import functionalTests.FunctionalTest;


@Ignore
public abstract class AbstractIOOperationsBase extends FunctionalTest {

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
