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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.vfs2.FileObject;
import org.apache.log4j.Level;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.vfs.VFSMountManagerHelper;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;


public class VFSMountManagerHelperTerminateTest {

    private static File spacesDir;

    private static FileSystemServerDeployer server;

    static {
        CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP.setValue(false);
        ProActiveLogger.getLogger(Loggers.DATASPACES).setLevel(Level.DEBUG);
    }

    @Test
    public void testTerminate() throws Exception {

        spacesDir = new File(System.getProperty("java.io.tmpdir"), "ProActive SpaceMountManagerTest");

        if (server == null) {
            server = new FileSystemServerDeployer("inputserver", spacesDir.toString(), true, true);
            System.out.println("Started File Server at " + Arrays.toString(server.getVFSRootURLs()));
        }

        String[] validUrls = server.getVFSRootURLs();
        for (String validUrl : validUrls) {
            FileObject mounted = VFSMountManagerHelper.mount(validUrl);
            Assert.assertTrue(mounted.exists());
        }

        VFSMountManagerHelper.terminate();

        VFSMountManagerHelper.closeFileSystems(Arrays.asList(validUrls));

        // Ensure that we can regenerate the mount helper
        for (String validUrl : validUrls) {
            FileObject mounted = VFSMountManagerHelper.mount(validUrl);
            Assert.assertTrue(mounted.exists());
        }

    }

    @AfterClass
    public static void tearDown() throws ProActiveException {
        server.terminate();

        VFSMountManagerHelper.terminate();

        if (spacesDir != null && spacesDir.exists()) {
            assertTrue(vfsprovider.AbstractIOOperationsBase.deleteRecursively(spacesDir));
            spacesDir = null;
        }
    }
}
