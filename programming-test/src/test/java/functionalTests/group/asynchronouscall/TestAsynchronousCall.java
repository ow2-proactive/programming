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
package functionalTests.group.asynchronouscall;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTest;
import functionalTests.group.A;


/**
 * do an (a)synchronous call on a previously created group
 * @author The ProActive Team
 */

public class TestAsynchronousCall extends GCMFunctionalTest {
    private A typedGroup = null;

    private A resultTypedGroup = null;

    public TestAsynchronousCall() throws ProActiveException {
        super(2, 1);
        super.startDeployment();
    }

    @Before
    public void preConditions() throws Exception {

        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);

        PAGroup.getGroup(this.typedGroup).setRatioMemberToThread(1);

        assertTrue(this.typedGroup != null);
    }

    @org.junit.Test
    public void action() throws Exception {
        this.resultTypedGroup = this.typedGroup.asynchronousCall();

        // was the result group created ?
        assertTrue(this.resultTypedGroup != null);

        Group<A> group = PAGroup.getGroup(this.typedGroup);
        Group<A> groupOfResult = PAGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        assertTrue(groupOfResult.size() == group.size());

        boolean rightRankingOfResults = true;
        for (int i = 0; i < group.size(); i++) {
            rightRankingOfResults &= ((A) groupOfResult.get(i)).getName()
                                                               .equals((((A) group.get(i)).asynchronousCall()).getName());
        }

        // is the result of the n-th group member at the n-th position in the result group ?
        assertTrue(rightRankingOfResults);
    }

}
