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
package functionalTests.gcmdeployment.virtualnode;

import java.io.FileNotFoundException;

import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;


public class TestVirtualNode3 extends GCMFunctionalTest {
    public TestVirtualNode3() throws FileNotFoundException, ProActiveException {
        super(LocalHelpers.getDescriptor(TestVirtualNode3.class));
        super.startDeployment();
    }

    @Test
    public void test() throws FileNotFoundException, ProActiveException, InterruptedException {
        // failure = timeout reached

        GCMVirtualNode vn = super.gcmad.getVirtualNode("vn");
        while (true) {
            if (1 == vn.getCurrentNodes().size())
                return; // test passed

            Thread.sleep(1000);
        }

    }
}
