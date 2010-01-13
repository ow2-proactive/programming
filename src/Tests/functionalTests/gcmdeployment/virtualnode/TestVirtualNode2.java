/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
    public TestVirtualNode2() throws FileNotFoundException {
        super(LocalHelpers.getDescriptor(TestVirtualNode2.class));
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
