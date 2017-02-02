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
package functionalTests.group.barrier;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Before;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.api.PASPMD;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTest;


/**
 * perform a barrier call on an SPMD group
 *
 * @author The ProActive Team
 */

public class TestBarrier extends GCMFunctionalTest {
    private A spmdgroup = null;

    public TestBarrier() throws ProActiveException {
        super(2, 1);
        super.startDeployment();
    }

    @Before
    public void preConditions() throws Exception {
        //@snippet-start spmd_creation
        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        this.spmdgroup = (A) PASPMD.newSPMDGroup(A.class.getName(), params, nodes);
        //@snippet-end spmd_creation
        assertTrue(spmdgroup != null);
        assertTrue(PAGroup.size(spmdgroup) == 3);
    }

    @org.junit.Test
    public void action() throws Exception {
        this.spmdgroup.start();

        String errors = "";
        Iterator<A> it = PAGroup.getGroup(this.spmdgroup).iterator();
        while (it.hasNext()) {
            errors += ((A) it.next()).getErrors();
        }
        System.err.print(errors);
        assertTrue("".equals(errors));
    }
}
