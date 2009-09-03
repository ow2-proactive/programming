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
