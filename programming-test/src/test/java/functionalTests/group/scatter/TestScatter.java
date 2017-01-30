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
package functionalTests.group.scatter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTest;
import functionalTests.group.A;


/**
 * distributes the parameters of a method call to member
 *
 * @author The ProActive Team
 */

public class TestScatter extends GCMFunctionalTest {
    private A typedGroup = null;

    private A parameterGroup = null;

    private A resultTypedGroup = null;

    public TestScatter() throws ProActiveException {
        super(4, 1);
        super.startDeployment();
    }

    @org.junit.Test
    public void action() throws Exception {
        //### Keep the surrounding comments when  ###
        //### changing the code (used by the doc) ###
        //@snippet-start group_scatter_creation
        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);
        Object[][] paramsParameter = { { "AgentA" }, { "AgentB" }, { "AgentC" } };
        Node[] nodesParameter = { super.getANode(), NodeFactory.getDefaultNode(), super.getANode() };
        this.parameterGroup = (A) PAGroup.newGroup(A.class.getName(), paramsParameter, nodesParameter);
        //@snippet-end group_scatter_creation

        //### Keep the surrounding comments when  ###
        //### changing the code (used by the doc) ###
        //@snippet-start group_scatter_example
        PAGroup.setScatterGroup(this.parameterGroup);
        this.resultTypedGroup = this.typedGroup.asynchronousCall(this.parameterGroup);
        PAGroup.unsetScatterGroup(this.parameterGroup);
        //@snippet-end group_scatter_example

        // was the result group created ?
        assertTrue(this.resultTypedGroup != null);

        Group<A> group = PAGroup.getGroup(this.typedGroup);
        Group<A> groupResult = PAGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        assertTrue(groupResult.size() == group.size());

        Group<A> groupParameter = PAGroup.getGroup(this.parameterGroup);

        for (int i = 0; i < group.size(); i++) {
            // is the result of the n-th group member called with the n-th parameter at the n-th position in the result group ?
            assertEquals(((A) groupResult.get(i)).getName(),
                         (((A) group.get(i)).asynchronousCall((A) groupParameter.get(i))).getName());
        }

        // is the result of the n-th group member called with the n-th parameter at the n-th position in the result group ?
    }
}
