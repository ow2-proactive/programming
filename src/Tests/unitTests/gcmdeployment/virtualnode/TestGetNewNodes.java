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
package unitTests.gcmdeployment.virtualnode;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;

import functionalTests.FunctionalTest;


public class TestGetNewNodes extends FunctionalTest {
    final int COUNT_1 = 50;
    final int COUNT_2 = 100;

    GCMVirtualNodeImpl vn;
    GCMApplicationDescriptorMockup gcma;
    ProActiveRuntimeImpl part;

    @Before
    public void before() {
        vn = new GCMVirtualNodeImpl();
        gcma = new GCMApplicationDescriptorMockup();
        part = ProActiveRuntimeImpl.getProActiveRuntime();
        part.setCapacity(COUNT_1 + COUNT_2);
    }

    @Test
    public void test() {
        for (int i = 0; i < COUNT_1; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }

        Assert.assertEquals(COUNT_1, vn.getNewNodes().size());
        Assert.assertEquals(0, vn.getNewNodes().size());

        for (int i = 0; i < COUNT_2; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }

        Assert.assertEquals(COUNT_2, vn.getNewNodes().size());
        Assert.assertEquals(0, vn.getNewNodes().size());
    }
}
