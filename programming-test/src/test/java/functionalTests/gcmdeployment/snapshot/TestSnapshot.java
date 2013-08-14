/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.gcmdeployment.snapshot;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationSnapshot;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeSnapshot;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.utils.Sleeper;

import functionalTests.GCMFunctionalTest;
import functionalTests.ProActiveSetup;


public class TestSnapshot extends GCMFunctionalTest {
    static final String VN_NAME = "nodes";
    static final int NB_VMS = 2;

    public TestSnapshot() throws ProActiveException {
        super(1, 1);
        super.startDeployment();
    }

    @Test(timeout = 20000)
    public void test() throws ProActiveException, InterruptedException {
        Root r = PAActiveObject.newActive(Root.class, new Object[] {}, super.getANode());

        GCMApplicationSnapshot app = r.deploy();
        PAFuture.waitFor(app);
        r.killVm();

        try {
            app.getAllNodes();
            Assert.fail("An ISE should be thrown since a virtual is defined");
        } catch (IllegalStateException e) {

        }

        Assert.assertNotNull(app.getDescriptorURL());
        Assert.assertEquals(1, app.getVirtualNodeNames().size());
        Assert.assertEquals(1, app.getVirtualNodes().size());

        logger.info("Deployement finished");
        GCMVirtualNodeSnapshot vn = app.getVirtualNode(VN_NAME);

        List<Node> nodes = vn.getCurrentNodes();
        Assert.assertEquals(NB_VMS, nodes.size());

        for (Node node : nodes) {
            RuntimeFactory.getRuntime(node.getProActiveRuntime().getURL());
        }

        app.kill();

        for (Node node : nodes) {
            int retry = 3;
            while (retry-- > 0) {
                try {
                    RuntimeFactory.getRuntime(node.getProActiveRuntime().getURL());
                    new Sleeper(500).sleep();
                } catch (ProActiveException e) {
                    break;
                }
            }

            if (retry == 0) {
                Assert.fail("This call must fail since the runtime has been killed");
            }
        }
    }

    public static class Root {
        public Root() {

        }

        public GCMApplicationSnapshot deploy() throws ProActiveException {

            ProActiveSetup paSetup = new ProActiveSetup();
            VariableContractImpl vc = paSetup.getVariableContract();

            vc.setVariableFromProgram(VC_HOSTCAPACITY, Integer.toString(NB_VMS),
                    VariableContractType.DescriptorDefaultVariable);
            File f = null;
            try {
                f = new File(this.getClass().getResource("/functionalTests/_CONFIG/JunitApp.xml").toURI());
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            GCMApplication app = PAGCMDeployment.loadApplicationDescriptor(f, vc);
            app.startDeployment();
            GCMVirtualNode vn = app.getVirtualNode(VN_NAME);

            while (vn.getNbCurrentNodes() != NB_VMS) {
                new Sleeper(500).sleep();
            }

            System.out.println("I'm ready");
            return new GCMApplicationSnapshot(app);
        }

        public void killVm() {
            PALifeCycle.exitSuccess();
        }
    }

    public static class AO {
        public AO() {
        }

        public void sayHello() {
            System.out.println("Hello");
        }
    }
}
