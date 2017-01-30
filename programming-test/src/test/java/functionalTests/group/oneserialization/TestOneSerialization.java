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
package functionalTests.group.oneserialization;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTest;
import functionalTests.group.A;


/**
 * do only serialization of the MethodCall object (in broadcast call only)
 *
 * @author The ProActive Team
 */

public class TestOneSerialization extends GCMFunctionalTest {
    private A typedGroup = null;

    public TestOneSerialization() throws ProActiveException {
        super(2, 1);
        super.startDeployment();
    }

    @org.junit.Test
    public void action() throws Exception {
        //### Keep the surrounding comments when  ###
        //### changing the code (used by the doc) ###
        //@snippet-start group_unique_serialization
        PAGroup.setUniqueSerialization(this.typedGroup);
        this.typedGroup.onewayCall();
        PAGroup.unsetUniqueSerialization(this.typedGroup);
        //@snippet-end group_unique_serialization

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
