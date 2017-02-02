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
package functionalTests.activeobject.creation.local.newactive.constructors;

import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;

import functionalTests.FunctionalTest;


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
