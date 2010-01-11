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
package functionalTests.gcmdeployment.technicalservice;

import java.net.URL;

import org.junit.Assert;
import org.junit.Before;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

import functionalTests.FunctionalTest;


/**
 * Deployment descriptor technical services.
 */
public class TestApplicationLevel extends FunctionalTest {
    private Node node;

    @Before
    public void before() throws ProActiveException {
        URL desc = this.getClass().getResource("TestApplicationLevelApplication.xml");

        VariableContractImpl vc = new VariableContractImpl();
        vc.setVariableFromProgram(FunctionalTest.VAR_JVM_PARAMETERS, FunctionalTest.getJvmParameters()
                .toString(), VariableContractType.ProgramVariable);
        GCMApplication app = PAGCMDeployment.loadApplicationDescriptor(desc, vc);
        app.startDeployment();
        GCMVirtualNode vn = app.getVirtualNode("nodes");
        node = vn.getANode();
    }

    @org.junit.Test
    public void action() throws Exception {
        Assert.assertEquals("aaa", node.getProperty("arg1"));
        Assert.assertNull(node.getProperty("arg2"));
    }
}
