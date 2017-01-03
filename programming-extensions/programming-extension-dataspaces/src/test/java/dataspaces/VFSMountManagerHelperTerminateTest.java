/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package dataspaces;

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

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;


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
