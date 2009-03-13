/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
package functionalTests.activeobject.request.forgetonsend;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


/**
 * Test ForgetOnSend strategies on SPMD groups
 */

public class TestAnnotation extends FunctionalTest {

    private boolean result;
    private B b1, b2;
    private SlowlySerializableObject obj1, obj4;

    /**
     * Check if @Sterile annotation is taken into account
     */
    @Test
    public void ptpAnnotation() {
        try {
            b1 = (B) PAActiveObject.newActive(B.class.getName(), new Object[] { "B1" });
            b2 = (B) PAActiveObject.newActive(B.class.getName(), new Object[] { "B2" });
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        PAActiveObject.setForgetOnSend(b1, "b");

        result = true;

        b1.h(new SlowlySerializableObject("test", 1000)); // fos + future
        b1.g(); // standard, sterile annotation (=> only waits on b1)

        String res = b1.takeFast(); // standard
        if (!res.equals("hg")) {
            result = false;
        }
    }
}