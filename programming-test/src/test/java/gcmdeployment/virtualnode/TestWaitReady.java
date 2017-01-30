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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;

import functionalTests.FunctionalTest;


public class TestWaitReady extends FunctionalTest {
    static final int TIMEOUT = 1000;

    GCMVirtualNodeImpl vn;

    GCMApplicationDescriptorMockup gcma;

    ProActiveRuntimeImpl part;

    @BeforeClass
    static public void setCapacity() {
        ProActiveRuntimeImpl.getProActiveRuntime().setCapacity(5);
    }

    @Before
    public void before() {
        vn = new GCMVirtualNodeImpl();
        gcma = new GCMApplicationDescriptorMockup();
        part = ProActiveRuntimeImpl.getProActiveRuntime();
    }

    @Test(expected = ProActiveTimeoutException.class)
    public void timeoutReached() throws ProActiveTimeoutException {
        vn.setCapacity(5);
        vn.waitReady(TIMEOUT);
    }

    @Test
    public void everythingOK() throws ProActiveTimeoutException {
        vn.setCapacity(5);
        for (int i = 0; i < 5; i++) {
            vn.addNode(new FakeNode(gcma, part));
        }
        vn.waitReady(TIMEOUT);
    }
}
