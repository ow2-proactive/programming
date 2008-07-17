/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package functionalTests.gcmdeployment.remoteobject;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGCMRemoteObjectsSubscribeFromAO extends GCMFunctionalTestDefaultNodes {
    public TestGCMRemoteObjectsSubscribeFromAO() {
        super(1, 1);
    }

    @Test
    public void testRemote() throws ActiveObjectCreationException, NodeException, InterruptedException {

        Node node = super.getANode();
        RemoteAO rao = (RemoteAO) PAActiveObject.newActive(RemoteAO.class.getName(),
                new Object[] { super.gcmad }, node);

        Assert.assertTrue(rao.isSuccess().booleanValue());
    }

    static public class RemoteAO implements Serializable, RunActive {
        GCMApplication gcma;
        boolean success = false;

        public RemoteAO() {

        }

        public RemoteAO(GCMApplication gcma) {
            this.gcma = gcma;
        }

        public void callback(Node node, String vnName) {
            System.out.println("Callback occured !");
            success = true;
        }

        public void runActivity(Body body) {
            Service service = new Service(body);

            try {
                GCMVirtualNode vn1 = gcma.getVirtualNode(GCMFunctionalTestDefaultNodes.VN_NAME);
                vn1.subscribeNodeAttachment(PAActiveObject.getStubOnThis(), "callback", true);

                service.blockingServeOldest("callback");
            } catch (Exception e) {
                e.printStackTrace();
            }

            service.blockingServeOldest("isSuccess");
        }

        public BooleanWrapper isSuccess() {
            return new BooleanWrapper(success);
        }
    }
}
