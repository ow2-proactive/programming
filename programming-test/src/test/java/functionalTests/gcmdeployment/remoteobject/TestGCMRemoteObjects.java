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
package functionalTests.gcmdeployment.remoteobject;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTest;


public class TestGCMRemoteObjects extends GCMFunctionalTest {
    public TestGCMRemoteObjects() throws ProActiveException {
        super(1, 1);
        super.startDeployment();
    }

    @Test
    public void testRemote() throws ActiveObjectCreationException, NodeException, InterruptedException {
        Node node = super.getANode();
        AO ao = PAActiveObject.newActive(AO.class, new Object[] { super.gcmad }, node);
        Assert.assertTrue(ao.finished());
    }

    static public class AO implements Serializable, InitActive {
        GCMApplication gcma;

        boolean success = true;

        public AO() {

        }

        public AO(GCMApplication gcma) {
            this.gcma = gcma;
        }

        public void initActivity(Body body) {
            try {
                GCMVirtualNode vn1 = gcma.getVirtualNode(DEFAULT_VN_NAME);
                if (vn1 == null)
                    success = false;

                boolean atLeastOne = false;
                for (GCMVirtualNode vn : gcma.getVirtualNodes().values()) {
                    atLeastOne = true;
                    for (Node node : vn.getCurrentNodes()) {
                        System.out.println(node.getNodeInformation().getURL());
                    }
                }
                if (atLeastOne != true)
                    success = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean finished() {
            return success;
        }
    }
}
