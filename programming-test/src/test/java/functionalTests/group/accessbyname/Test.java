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
package functionalTests.group.accessbyname;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.group.Group;

import functionalTests.FunctionalTest;
import functionalTests.group.A;


/**
 * This class tests the access to named elements of a group.
 *
 * @author The ProActive Team
 */

public class Test extends FunctionalTest {
    A typedGroup;

    private A createGroup() throws Exception {
        //### Keep the surrounding comments when  ###
        //### changing the code (used by the doc) ###
        //@snippet-start typed_group_name_access_creation
        typedGroup = (A) PAGroup.newGroup(A.class.getName());

        Group<A> group = PAGroup.getGroup(typedGroup);
        group.addNamedElement("number0", new A("Agent0"));
        group.add(new A("Agent1"));
        group.addNamedElement("number2", new A("Agent2"));
        //@snippet-end typed_group_name_access_creation

        return this.typedGroup;
    }

    @org.junit.Test
    public void action() throws Exception {
        this.createGroup();

        // was the group created ?
        assertTrue(typedGroup != null);
        Group<A> group = PAGroup.getGroup(this.typedGroup);

        // has the group the right size ?
        assertTrue(group.size() == 3);

        //		tests for named elements
        //### Keep the surrounding comments when  ###
        //### changing the code (used by the doc) ###
        //@snippet-start typed_group_name_access_example
        A agent0_indexed = (A) group.get(0);
        A agent0_named = (A) group.getNamedElement("number0");
        A agent1_indexed = (A) group.get(1);
        A agent2_named = (A) group.getNamedElement("number2");
        A agent2_indexed = (A) group.get(2);
        //@snippet-end typed_group_name_access_example

        // tests correct ordering and access to named elements
        assertTrue(((agent0_indexed == agent0_named)));
        assertTrue(agent2_indexed == agent2_named);
        assertTrue(agent1_indexed.getName().equals("Agent1"));

        group.removeNamedElement("number0");
        // tests removal and re-ordering
        assertTrue(group.size() == 2);
        assertTrue(group.get(0) == agent1_indexed);
        assertTrue(!group.containsKey("number0"));
    }
}
