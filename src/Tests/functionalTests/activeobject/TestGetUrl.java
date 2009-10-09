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
package functionalTests.activeobject;

import java.io.IOException;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.GCMFunctionalTestDefaultNodes;


public class TestGetUrl extends GCMFunctionalTestDefaultNodes {

    public TestGetUrl() {
        super(1, 1);
    }

    @Test(expected = ProActiveRuntimeException.class)
    public void testNonAO() {
        Object o = new Object();
        String url = PAActiveObject.getUrl(o);
    }

    @Test
    public void testPAAactiveObject() throws ActiveObjectCreationException, NodeException, IOException {
        Node node = super.getANode();
        AO ao = (AO) PAActiveObject.newActive(AO.class.getName(), new Object[] {}, node);
        String url = PAActiveObject.getUrl(ao);
        ao = (AO) PAActiveObject.lookupActive(AO.class.getName(), url);
        ao.v();
    }

    public static class AO {
        public AO() {

        }

        public boolean v() {
            return true;
        }

    }

}
