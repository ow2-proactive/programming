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
package functionalTests.activeobject.implicitgetstubonthis;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


/**
 * test the implicit get stub on this feature
 */
public class TestImplicitGetStubOnThis extends FunctionalTest {

    @Test
    public void implicitGetStubOnThisNewActive()
            throws IllegalArgumentException, IllegalAccessException, ActiveObjectCreationException, NodeException {

        CentralPAPropertyRepository.PA_IMPLICITGETSTUBONTHIS.setValue(true);

        // first test -- one step replacement

        A a = PAActiveObject.newActive(A.class, new Object[] {});

        // aa is a future
        A aa = a.returnThis();

        Object implicitA = PAFuture.getFutureValue(aa);

        // let's see if implicitA is a stub/proxy to the remote object instead of a itself
        Assert.assertNotSame(implicitA, a);

        Assert.assertTrue(implicitA instanceof StubObject);
        // implicitA is at least a stubObject

        // stub equality on returned implicit get stub on this
        Assert.assertTrue(a.equals(implicitA));

        // second test -- embedded reference on this

        B b = a.aInB();
        A astub = b.getA();

        implicitA = PAFuture.getFutureValue(astub);

        // let's see if implicitA is a stub/proxy to the remote object instead of a itself
        Assert.assertNotSame(implicitA, a);

        Assert.assertTrue(implicitA instanceof StubObject);
        // implicitA is at least a stubObject

        // stub equality on returned implicit get stub on this
        Assert.assertTrue(astub.equals(implicitA));

        // now astub and a should be equals

        Assert.assertTrue(astub.equals(a));

        // third test -- AO a send this to AO B, B should receive a stub

        B bb = PAActiveObject.newActive(B.class, new Object[] {});

        Assert.assertTrue(a.callTakeAOnB(bb));

    }

}
