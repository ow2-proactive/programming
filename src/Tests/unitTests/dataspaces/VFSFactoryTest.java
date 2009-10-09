/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
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
