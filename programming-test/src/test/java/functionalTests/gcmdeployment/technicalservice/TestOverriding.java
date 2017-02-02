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
package functionalTests.gcmdeployment.technicalservice;

import org.junit.Assert;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.GCMFunctionalTest;


/**
 * Deployment descriptor technical services.
 */
public class TestOverriding extends GCMFunctionalTest {
    static public final String VAR_OS = "os";

    public TestOverriding() throws ProActiveException {
        super(TestOverriding.class.getResource("TestOverridingApplication.xml"));
        super.setHostCapacity(4);
        super.setVmCapacity(1);
        super.startDeployment();
    }

    @org.junit.Test
    public void action() throws Exception {
        GCMVirtualNode vn1 = super.gcmad.getVirtualNode("VN1");
        GCMVirtualNode vn2 = super.gcmad.getVirtualNode("VN2");
        GCMVirtualNode vn3 = super.gcmad.getVirtualNode("VN3");
        GCMVirtualNode vn4 = super.gcmad.getVirtualNode("VN4");

        Node node;

        node = vn1.getANode();
        Assert.assertEquals("application", node.getProperty("arg1"));

        node = vn2.getANode();
        Assert.assertEquals("VN2", node.getProperty("arg1"));

        node = vn3.getANode();
        Assert.assertEquals("NP1", node.getProperty("arg1"));

        node = vn4.getANode();
        Assert.assertEquals("NP1", node.getProperty("arg1"));
    }
}
