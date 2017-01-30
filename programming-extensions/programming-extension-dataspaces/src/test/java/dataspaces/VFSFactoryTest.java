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
package dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSFactory;


public class VFSFactoryTest {

    private DefaultFileSystemManager manager;

    private File testFile;

    @Before
    public void setUp() throws Exception {
        manager = VFSFactory.createDefaultFileSystemManager();
        testFile = new File(System.getProperty("java.io.tmpdir"), "ProActive-VFSFactoryTest");
        final OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(testFile));
        osw.write("test");
        osw.close();
    }

    @After
    public void tearDown() {
        if (manager != null) {
            manager.close();
            manager = null;
        }
        if (testFile != null) {
            testFile.delete();
            testFile = null;
        }
    }

    @Test
    public void testCreateVirtualFileSystem() throws FileSystemException {
        manager.createVirtualFileSystem(DataSpacesURI.SCHEME);
    }

    @Test
    public void testReplicator() throws Exception {
        assertNotNull(manager.getReplicator());
    }

    @Test
    public void testTempStorage() throws Exception {
        assertNotNull(manager.getTemporaryFileStore());
    }

    @Test
    public void testLocalFileProvider() throws Exception {
        FileObject fo = null;
        try {
            fo = manager.resolveFile(testFile.getCanonicalPath());
            assertTrue(fo.exists());

            final InputStream ios = fo.getContent().getInputStream();
            final BufferedReader reader = new BufferedReader(new InputStreamReader(ios));
            assertEquals("test", reader.readLine());
        } finally {
            if (fo != null)
                fo.close();
        }
    }
}
