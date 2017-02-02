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
package functionalTests.group.creation;

import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTest;
import functionalTests.group.A;


/**
 * create a group with 3 active objects
 *
 * @author The ProActive Team
 */

public class TestCreation extends GCMFunctionalTest {
    private A typedGroup = null;

    Node node0;

    Node node1;

    Node node2;

    public TestCreation() throws ProActiveException {
        super(2, 1);
        super.startDeployment();
    }

    private A createGroup() throws Exception {
        //### Keep the surrounding comments when  ###
        //### changing the code (used by the doc) ###		  
        //@snippet-start group_creation_example
        node0 = NodeFactory.getDefaultNode();
        node1 = super.getANode();
        node2 = super.getANode();

        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { node0, node1, node2 };

        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);
        //@snippet-end group_creation_example

        return this.typedGroup;
    }

    @org.junit.Test
    public void action() throws Exception {
        this.createGroup();

        // was the group created ?
        assertTrue(typedGroup != null);
        Group<A> agentGroup = PAGroup.getGroup(this.typedGroup);

        // has the group the right size ?
        assertTrue(agentGroup.size() == 3);

        //### Keep the surrounding comments when  ###
        //### changing the code (used by the doc) ###
        //@snippet-start group_get_members
        A agent0 = (A) agentGroup.get(0);
        A agent1 = (A) agentGroup.get(1);
        A agent2 = (A) agentGroup.get(2);
        //@snippet-end group_get_members

        Assert.assertEquals(node0.getNodeInformation().getURL().toLowerCase(), agent0.getNodeName().toLowerCase());
        Assert.assertEquals(node1.getNodeInformation().getURL().toLowerCase(), agent1.getNodeName().toLowerCase());
        Assert.assertEquals(node2.getNodeInformation().getURL().toLowerCase(), agent2.getNodeName().toLowerCase());

        Assert.assertEquals("Agent0", agent0.getName());
        Assert.assertEquals("Agent1", agent1.getName());
        Assert.assertEquals("Agent2", agent2.getName());
    }
}
