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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.descriptor.lookupregister;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.extensions.pamr.remoteobject.PAMRRemoteObjectFactory;

import functionalTests.FunctionalTest;
import functionalTests.TestDisabler;


/**
 * Test lookup and register in deployment descriptors
 */
public class Test extends FunctionalTest {
    // private static String FS = File.separator;
    private static String AGENT_XML_LOCATION_UNIX = Test.class.getResource(
            "/functionalTests/descriptor/lookupregister/Agent.xml").getPath();

    ProActiveDescriptor proActiveDescriptorAgent;
    A a;

    @Before
    final public void disable() {
        TestDisabler.unsupportedProtocols(PAMRRemoteObjectFactory.PROTOCOL_ID);
    }

    @org.junit.Test
    public void action() throws Exception {
        proActiveDescriptorAgent = PADeployment.getProactiveDescriptor("file:" + AGENT_XML_LOCATION_UNIX,
                super.getVariableContract());
        proActiveDescriptorAgent.activateMappings();
        VirtualNode vnAgent = proActiveDescriptorAgent.getVirtualNode("Agent");
        PAActiveObject.newActive(A.class, new Object[] { "local" }, vnAgent.getNode());

        String url = URIBuilder.buildURI(ProActiveInet.getInstance().getHostname(), "Agent").toString();

        System.out.println(url);
        VirtualNode vnLookup = PADeployment.lookupVirtualNode(url);
        a = (A) vnLookup.getUniqueAO();

        assertTrue((a.getName().equals("local")));
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
