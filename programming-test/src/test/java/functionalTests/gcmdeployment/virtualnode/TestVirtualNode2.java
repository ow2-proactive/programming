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

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTest;
import functionalTests.gcmdeployment.LocalHelpers;


public class TestVirtualNode2 extends GCMFunctionalTest {
    public TestVirtualNode2() throws FileNotFoundException, ProActiveException {
        super(LocalHelpers.getDescriptor(TestVirtualNode2.class));
        super.startDeployment();
    }

    @Test
    public void test() throws FileNotFoundException, ProActiveException, InterruptedException {
        Thread.sleep(8000);

        GCMVirtualNode vn1 = gcmad.getVirtualNode("vn1");
        GCMVirtualNode vn2 = gcmad.getVirtualNode("vn2");

        boolean fairness = true;
        int diff = vn1.getCurrentNodes().size() - vn2.getCurrentNodes().size();
        if ((diff < -1) || (diff > 1)) {
            fairness = false;
        }

        Assert.assertTrue("Allocation is not fair between greedy VNs", fairness);
    }
}
