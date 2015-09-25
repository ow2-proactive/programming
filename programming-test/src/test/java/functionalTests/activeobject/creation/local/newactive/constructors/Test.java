/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.activeobject.creation.local.newactive.constructors;

import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import functionalTests.FunctionalTest;

import static org.junit.Assert.assertTrue;


/**
 * Test newActive method on the local default node
 */
public class Test extends FunctionalTest {
    B b1;
    B b2;
    B b3;
    B b4;
    B b5;
    B b6;
    B b7;
    String name;
    String nodeUrl;

    @org.junit.Test
    public void action() throws Exception {
        b1 = PAActiveObject.newActive(B.class, new Object[] { "toto" });

        // We want B(String) to be taken rather than B(Object)
        assertTrue(b1.getChoosed().equals("C2"));

        b2 = PAActiveObject.newActive(B.class, new Object[] { 1 });

        // We want B(int) to be taken (autoboxing)
        assertTrue(b2.getChoosed().equals("C3"));

        b3 = PAActiveObject.newActive(B.class, new Object[] { 1L });

        // We want B(Long) to be taken rather than B(long) : remember that we pass an array of Object so we actually pass a Long !
        assertTrue(b3.getChoosed().equals("C5"));

        boolean exception_thrown = false;
        try {
            b4 = PAActiveObject.newActive(B.class, new Object[] { "s1", "s2" });
        } catch (ActiveObjectCreationException ex) {
            exception_thrown = true;
        }

        // We want that an exception is thrown because the choice (C6 or C7) is ambiguous
        assertTrue(exception_thrown);

        b5 = PAActiveObject.newActive(B.class, new Object[] { "s1", null });

        // Here C6 is a non-ambiguous choice
        assertTrue(b5.getChoosed().equals("C6"));

        b6 = PAActiveObject.newActive(B.class, new Object[] { null, "s2" });

        // Here C7 is a not ambiguous choice
        assertTrue(b6.getChoosed().equals("C7"));

        exception_thrown = false;
        try {
            b7 = PAActiveObject.newActive(B.class, new Object[] { null, null });
        } catch (ActiveObjectCreationException ex) {
            exception_thrown = true;
        }
        // again we expect an exception (can't choose between C6 and C7)
        assertTrue(exception_thrown);

        // Here C9 should be taken rather than C8 and C10
        b7 = PAActiveObject.newActive(B.class, new Object[] { new Vector() });
        assertTrue(b7.getChoosed().equals("C9"));
    }
}
