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
package gcmdeployment.virtualnode;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;

import functionalTests.FunctionalTest;


public class TestGetCurrentNodes extends FunctionalTest {
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

        Assert.assertEquals(COUNT_1, vn.getCurrentNodes().size());
        Assert.assertEquals(COUNT_1, vn.getNbCurrentNodes());

        for (int i = 0; i < COUNT_2; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }

        Assert.assertEquals(COUNT_1 + COUNT_2, vn.getCurrentNodes().size());
        Assert.assertEquals(COUNT_1 + COUNT_2, vn.getNbCurrentNodes());
    }
}
