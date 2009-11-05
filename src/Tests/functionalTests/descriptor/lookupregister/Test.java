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
package functionalTests.descriptor.lookupregister;

import static junit.framework.Assert.assertTrue;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;

import functionalTests.FunctionalTest;


/**
 * Test lookup and register in deployment descriptors
 */
public class Test extends FunctionalTest {
    // private static String FS = File.separator;
    private static String AGENT_XML_LOCATION_UNIX;

    static {
        if ("ibis".equals(PAProperties.PA_COMMUNICATION_PROTOCOL.getValue())) {
            AGENT_XML_LOCATION_UNIX = Test.class.getResource(
                    "/functionalTests/descriptor/lookupregister/AgentIbis.xml").getPath();
        } else {
            AGENT_XML_LOCATION_UNIX = Test.class.getResource(
                    "/functionalTests/descriptor/lookupregister/Agent.xml").getPath();
        }
    }

    ProActiveDescriptor proActiveDescriptorAgent;
    A a;

    @org.junit.Test
    public void action() throws Exception {
        proActiveDescriptorAgent = PADeployment.getProactiveDescriptor("file:" + AGENT_XML_LOCATION_UNIX,
                super.vContract);
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
