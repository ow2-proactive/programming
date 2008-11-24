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
package functionalTests.activeobject.internalclass;

import java.io.Serializable;

import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;

import functionalTests.FunctionalTest;


/*
 * See PROACTIVE-277
 * Notes:
 * 
 * Member or nested top-level classes must be public to be activated.
 * As local classes (i.e. member classes defined in a code block) cannot be public, they cannot be activated.
 * Enclosing classes for both member or nested top-level classes must also have an empty constructor as for activated classes.
 * 
 */
public class TestInternalClassAO extends FunctionalTest {

    @Test
    public void test() throws ActiveObjectCreationException, NodeException {

        // new active
        MemberClass ao = (MemberClass) PAActiveObject.newActive(MemberClass.class.getName(), new Object[] {});
        NestedTopLevelClass sao = (NestedTopLevelClass) PAActiveObject.newActive(NestedTopLevelClass.class
                .getName(), new Object[] {});

        // turn active
        MemberClass ao2 = new MemberClass();
        NestedTopLevelClass sao2 = new NestedTopLevelClass();
        ao2 = (MemberClass) PAActiveObject.turnActive(ao2);
        sao2 = (NestedTopLevelClass) PAActiveObject.turnActive(sao2);
    }

    public class MemberClass implements Serializable {
        public MemberClass() {
            // Empty
        }
    }

    public static class NestedTopLevelClass implements Serializable {
        public NestedTopLevelClass() {
            // Empty
        }
    }

}
