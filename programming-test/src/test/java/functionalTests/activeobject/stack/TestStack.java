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
package functionalTests.activeobject.stack;

import java.util.BitSet;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * TestStack this tests the proactive.stack_trace mechanism, @see org.objectweb.proactive.core.config.CentralPAPropertyRepository.PA_STACKTRACE
 *
 * It makes a sequence of recursive calls (each creating a new active object of the same class), both on asynchronous and synchronous methods
 *
 * It tests then that each of the calls are found in the received stack trace.
 *
 * @author The ProActive Team
 */
public class TestStack {

    @Test
    public void testStack() throws Exception {

        CentralPAPropertyRepository.PA_STACKTRACE.setValue(true);

        AOStack ao = PAActiveObject.newActive(AOStack.class, new Object[0]);

        System.out.println("**************** Testing with Asynchronous calls ****************");
        try {
            BooleanWrapper bv = ao.throwExceptionAsync4();
            bv.getBooleanValue();
        } catch (Exception e) {
            handleException(e, "throwExceptionAsync");
        }

        System.out.println("**************** Testing with Synchronous calls ****************");
        try {
            boolean bv = ao.throwExceptionSync4();
        } catch (Exception e) {
            handleException(e, "throwExceptionSync");
        }
    }

    private void handleException(Exception e, String mname) {
        System.out.println("Received Exception:");
        e.printStackTrace();
        BitSet bset = new BitSet(4);
        boolean testStackFound = false;
        // checks that every method throwExceptionX is found in the stack
        for (StackTraceElement elem : e.getStackTrace()) {
            if (elem.getMethodName().contains(mname) && !elem.getClassName().contains("_Stub")) {
                int length = elem.getMethodName().length();
                int found = Integer.parseInt(elem.getMethodName().substring(length - 1, length));
                bset.set(found, true);
            }
            // checks that the test method is found in the stack
            if (elem.getMethodName().contains("testStack")) {
                testStackFound = true;
            }
        }
        System.out.println("Found calls : " + bset);
        Assert.assertTrue("All " + mname + " methods have been found in the stack", bset.cardinality() == 4);
        Assert.assertTrue("Context testStack has been found", testStackFound);
    }
}
