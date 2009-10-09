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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.activeobject.request.tags;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;

import functionalTests.FunctionalTest;


/**
 * Tests for Message Tagging on the Request
 * 
 * 1/ Check the propagation of a Tag and its data
 * 2/ Check the non propagation of a Tag
 * 3/ Check the local Memory of a Tag
 *
 */
public class Test extends FunctionalTest {

    private A activeA;
    private int propagationResult;
    private int localmemoryValue1, localmemoryValue2;
    private boolean stopPropagationResult, noMemoryOnB;
    private boolean leaseExceededCleaningDone;
    private boolean leaseRenew;

    @Before
    public void action() throws Exception {
        PAProperties.PA_MEMORY_TAG_LEASE_PERIOD.setValue(5);
        PAProperties.PA_MAX_MEMORY_TAG_LEASE.setValue(10);
        activeA = (A) PAActiveObject.newActive(A.class.getName(), new Object[0]);
        activeA.initialize();
    }

    @org.junit.Test
    public void propagation() {
        propagationResult = activeA.propagateTag();
        stopPropagationResult = activeA.stopPropagateTag();

        assertTrue(propagationResult == 42);
        assertTrue(stopPropagationResult);

    }

    @org.junit.Test
    public void localMemory() {
        localmemoryValue1 = activeA.localMemory1();
        localmemoryValue2 = activeA.localMemory2();
        noMemoryOnB = activeA.checkNoLocalMemoryOnB();

        assertTrue(localmemoryValue1 == 0);
        assertTrue(localmemoryValue2 == 1);
        assertTrue(noMemoryOnB);
    }

    @org.junit.Test
    public void leaseCheck() {
        assertTrue(activeA.localMemoryLeaseExceeded());
        assertTrue(activeA.localMemoryLeaseClean2());
    }

}
