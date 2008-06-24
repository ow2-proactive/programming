/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package unitTests.gcmdeployment.virtualnode;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.FakeNode;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeImpl;


public class TestWaitReady {
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
