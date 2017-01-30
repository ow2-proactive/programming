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
package functionalTests.group.exception;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.ExceptionListException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeFactory;

import functionalTests.GCMFunctionalTest;
import functionalTests.group.A;


/**
 * do an (a)synchronous call that rise exception
 *
 * @author The ProActive Team
 */

public class TestException extends GCMFunctionalTest {
    private A typedGroup = null;

    private A resultTypedGroup = null;

    public TestException() throws ProActiveException {
        super(2, 1);
        super.startDeployment();
    }

    @org.junit.Test
    public void action() throws Exception {
        Object[][] params = { { "Agent0" }, { "Agent1" }, { "Agent2" } };
        Node[] nodes = { NodeFactory.getDefaultNode(), super.getANode(), super.getANode() };
        this.typedGroup = (A) PAGroup.newGroup(A.class.getName(), params, nodes);

        this.resultTypedGroup = this.typedGroup.asynchronousCallException();

        // was the result group created ?
        assertTrue(resultTypedGroup != null);
        // System.err.println(
        //        "the result group containing exception is not build");
        Group<A> group = PAGroup.getGroup(this.typedGroup);
        Group<?> groupOfResult = PAGroup.getGroup(this.resultTypedGroup);

        // has the result group the same size as the caller group ?
        assertTrue(groupOfResult.size() == group.size());
        //    System.err.println(
        //        "the result group containing exception has the correct size");
        boolean exceptionInResultGroup = true;
        for (int i = 0; i < groupOfResult.size(); i++) {
            exceptionInResultGroup &= (groupOfResult.get(i) instanceof Throwable);
        }

        // is the result group containing exceptions ?
        assertTrue(exceptionInResultGroup);
        //        System.err.println(
        //                "the result group doesn't contain (exclusively) exception");

        // has the ExceptionListException the correct size ?
        ExceptionListException el = groupOfResult.getExceptionList();
        assertTrue((el.size() == groupOfResult.size()));
        //        System.err.println(
        //                "the ExceptionListException hasn't the right size");
        A resultOfResultGroup = this.resultTypedGroup.asynchronousCall();
        Group<A> groupOfResultResult = PAGroup.getGroup(resultOfResultGroup);

        // has the result-result group the correct size ?
        assertTrue(groupOfResultResult.size() == groupOfResult.size());
        //            System.err.println(
        //                "the result of a call on a group containing exception hasn't the correct size");
        boolean nullInResultResultGroup = true;
        for (int i = 0; i < groupOfResultResult.size(); i++) {
            nullInResultResultGroup &= (groupOfResultResult.get(i) == null);
        }

        // is the result group containing null ?
        assertTrue(nullInResultResultGroup);
        //            System.err.println(
        //                "the result group of a group containing exception doesn't contain null");

        // are the exceptions deleted ?
        groupOfResult.purgeExceptionAndNull();
        assertTrue(groupOfResult.size() == 0);
        //            System.err.println(
        //                "the exceptions in a group are not correctly (totaly) purged");

        // are the null deleted ?
        groupOfResultResult.purgeExceptionAndNull();
        assertTrue(groupOfResultResult.size() == 0);
        //            System.err.println(
        //                "the null in a group are not correctly (totaly) purged");
    }
}
