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
package functionalTests.remoteobject.turnremote;

import java.io.Serializable;
import java.rmi.dgc.VMID;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import functionalTests.GCMFunctionalTest;


public class TestTurnRemote extends GCMFunctionalTest {

    public TestTurnRemote() throws ProActiveException {
        super(1, 1);
        super.startDeployment();
    }

    @Test
    public void test() throws ProActiveException {
        Node node = super.getANode();
        AO ao = PAActiveObject.newActive(AO.class, new Object[] {}, node);
        RObject ro = ao.deploy();

        VMID localVMID = ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getVMID();
        VMID remoteVMID = ro.callMe();
        Assert.assertFalse(localVMID.equals(remoteVMID));

    }

    public static class AO implements Serializable {
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
