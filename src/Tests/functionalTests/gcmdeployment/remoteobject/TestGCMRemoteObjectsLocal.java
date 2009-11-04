/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.gcmdeployment.remoteobject;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGCMRemoteObjectsLocal extends GCMFunctionalTestDefaultNodes {
    public TestGCMRemoteObjectsLocal() {
        super(1, 1);
    }

    @Test
    public void testLocal() {
        GCMVirtualNode vn1 = super.gcmad.getVirtualNode(GCMFunctionalTestDefaultNodes.VN_NAME);
        Assert.assertNotNull(vn1);

        boolean atLeastOne = false;
        for (GCMVirtualNode vn : super.gcmad.getVirtualNodes().values()) {
            atLeastOne = true;
            for (Node node : vn.getCurrentNodes()) {
                System.out.println(node.getNodeInformation().getURL());
            }
            Assert.assertTrue(atLeastOne);
        }
    }
}
