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
package functionalTests.group.onewaycall;

import static junit.framework.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTestDefaultNodes;

import functionalTests.group.A;


/**
 * do a oneway call on a previously created group
 *
 * @author The ProActive Team
 */

public class Test extends GCMFunctionalTestDefaultNodes {
    private A typedGroup = null;

    public Test() {
        super(2, 1);
    }

    @org.junit.Test
    public void action() throws Exception {
        this.typedGroup.onewayCall();

        boolean allOnewayCallDone = true;
        Group<A> group = PAGroup.getGroup(this.typedGroup);
        Iterator<A> it = group.iterator();
        while (it.hasNext()) {
            allOnewayCallDone &= ((A) it.next()).isOnewayCallReceived();
        }
        assertTrue(allOnewayCallDone);
    }

    @Before
    public void preConditions() throws Exception {

        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);
        PAGroup.getGroup(this.typedGroup).setRatioMemberToThread(1);

        boolean NoOnewayCallDone = true;
        Group<A> group = PAGroup.getGroup(this.typedGroup);
        Iterator<A> it = group.iterator();
        while (it.hasNext()) {
            NoOnewayCallDone &= !((A) it.next()).isOnewayCallReceived();
        }
        assertTrue(NoOnewayCallDone);
    }
}
