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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.activeobject.miscellaneous.fifocrash;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.core.xml.VariableContractType;

import functionalTests.GCMFunctionalTestDefaultNodes;


/**
 * Tests that a crash in the receive reply from an ActiveObject doesn't crash the sender's Active
 * Object. See JIRA: PROACTIVE-234
 */
public class Test extends GCMFunctionalTestDefaultNodes {

    boolean success = false;

    public Test() {
        super(1, 1);
        super.vContract.setVariableFromProgram("jvmargDefinedByTest", "-Xmx512M",
                VariableContractType.DescriptorDefaultVariable);
        PAProperties.PA_FUTUREMONITORING_TTM.setValue(0);

    }

    @org.junit.Test
    public void action() throws Exception {
        Node node = super.getANode();
        AOCrash2 ao2 = PAActiveObject.newActive(AOCrash2.class, new Object[] {}, node);
        AOCrash1 ao1 = (AOCrash1) PAActiveObject.newActive(AOCrash1.class.getName(), new Object[] { ao2 },
                node);
        // The call to foo will trigger a receiveReply on object ao1 from object ao2
        ao1.foo();
        // We terminate ao1 like a warrior
        ao1.terminate();

        // We test the life expectancy of ao2
        for (int i = 0; i < 20; i++) {
            Thread.sleep(100);
            // If the timeout expires, then ao2 is really dead
            BooleanWrapper bw = ao2.alive();
            PAFuture.waitFor(bw, 10000);

        }

    }

}
