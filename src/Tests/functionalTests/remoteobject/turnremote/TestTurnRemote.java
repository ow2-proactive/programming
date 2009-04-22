/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.remoteobject.turnremote;

import java.io.Serializable;
import java.rmi.dgc.VMID;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestTurnRemote extends GCMFunctionalTestDefaultNodes {

    public TestTurnRemote() {
        super(1, 1);
    }

    @Test
    public void test() throws ProActiveException {
        Node node = super.getANode();
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {}, node);
        RObject ro = ao.deploy();

        VMID localVMID = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        VMID remoteVMID = ro.callMe();
        Assert.assertFalse(localVMID.equals(remoteVMID));

    }

    public static class AO implements Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 41L;

        public AO() {
        }

        public RObject deploy() throws ProActiveException {
            return PARemoteObject.turnRemote(new RObject());
        }
    }

    public static class RObject {
        public RObject() {
        }

        public VMID callMe() {
            return ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        }
    }

}
