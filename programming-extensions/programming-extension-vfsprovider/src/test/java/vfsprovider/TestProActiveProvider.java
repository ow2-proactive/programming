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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.AbstractVfsTestCase;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.test.ProviderTestConfig;
import org.apache.commons.vfs2.test.ProviderTestSuite;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileName;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileProvider;

import junit.framework.Test;


/**
 * Test suite for VFS ProActiveProvider basing on VFS generic provider tests (junit3).
 */
public class TestProActiveProvider implements ProviderTestConfig {
    private final static URL TEST_DATA_SRC_ZIP_URL = TestProActiveProvider.class.getResource("/vfsprovider/_DATA/test-data.zip");

    private final static File testDir = new File(System.getProperty("java.io.tmpdir"),
                                                 "ProActive-TestProActiveProvider");

    public static Test suite() throws Exception {
        final TestProActiveProvider providerTest = new TestProActiveProvider();
        return new ProviderTestSuite(providerTest) {
            @Override
            protected void setUp() throws Exception {
                providerTest.setUp();
                super.setUp();
            }

            @Override
            protected void tearDown() throws Exception {
                super.tearDown();
                providerTest.tearDown();
            }
        };
    }

    public static void extractZip(final ZipInputStream zipStream, final File dstFile) throws IOException {
        ZipEntry zipEntry;
        while ((zipEntry = zipStream.getNextEntry()) != null) {
            final File dstSubFile = new File(dstFile, zipEntry.getName());

            if (zipEntry.isDirectory()) {
                dstSubFile.mkdirs();
                if (!dstSubFile.exists() || !dstSubFile.isDirectory())
                    throw new IOException("Could not create directory: " + dstSubFile);
            } else {
                final OutputStream os = new BufferedOutputStream(new FileOutputStream(dstSubFile));
                try {
                    int data;
                    while ((data = zipStream.read()) != -1)
                        os.write(data);
                } finally {
                    os.close();
                }
            }
        }
    }

    private FileSystemServerDeployer deployer;

    private FilesCache cache;

    @Override
    public DefaultFileSystemManager getDefaultFileSystemManager() {
        return new DefaultFileSystemManager();
    }

    public void prepare(final DefaultFileSystemManager manager) throws Exception {
        final ProActiveFileProvider provider = new ProActiveFileProvider();
        for (final String scheme : ProActiveFileName.getAllVFSSchemes()) {
            manager.addProvider(scheme, provider);
        }
    }

    public FileObject getBaseTestFolder(FileSystemManager fs) throws Exception {
        final String vfsRootURL = deployer.getVFSRootURL();
        return fs.resolveFile(vfsRootURL);
    }

    public FilesCache getFilesCache() {
        return cache;
    }

    @Override
    public boolean isFileSystemRootAccessible() {
        return true;
    }

    public void setUp() throws IOException, URISyntaxException {
        cache = new SoftRefFilesCache();
        setUpTestDir();
        startDeployer();
    }

    public void tearDown() throws ProActiveException {
        cache = null;
        removeTestDir();
        stopDeployer();
    }

    private void startDeployer() throws IOException {
        deployer = new FileSystemServerDeployer(AbstractVfsTestCase.getTestDirectory(), false);
    }

    private void stopDeployer() throws ProActiveException {
        if (deployer != null) {
            deployer.terminate();
            deployer = null;
        }
    }

    private void setUpTestDir() throws URISyntaxException, IOException {
        // create dir 
        if (testDir.exists()) {
            removeTestDir();
        }
        assertFalse(testDir.exists());
        assertTrue(testDir.mkdirs());

        // extract files from archive with VFS provider test data
        final ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(TEST_DATA_SRC_ZIP_URL.openStream()));
        try {
            extractZip(zipInputStream, testDir);
        } finally {
            zipInputStream.close();
        }

        // set VFS tests property
        System.setProperty("test.basedir", testDir.getAbsolutePath());
    }

    private void removeTestDir() {
        AbstractIOOperationsBase.deleteRecursively(testDir);
        assertFalse(testDir.exists());
    }
}
