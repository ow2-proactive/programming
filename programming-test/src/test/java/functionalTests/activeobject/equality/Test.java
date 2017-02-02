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
package functionalTests.activeobject.equality;

import org.junit.Ignore;

import functionalTests.FunctionalTest;


/**
 * This test has to be run AFTER the tests of :
 * - active object creation
 * - lookupactive
 * - groups
 *
 * @author The ProActive Team
 */
public class Test extends FunctionalTest {
    @Ignore
    @org.junit.Test
    public void test() throws Exception {
        throw new Exception("FIXME: test dependency");
    }

    /*
     * XXX: FIXME Disabled
     * 
     * private A group1 = null; private A group2 = null; private A group3 = null; private A a1 =
     * null; private A a2 = null; private functionalTests.activeobject.equality.RegisteredObject
     * registeredA1 = null; private functionalTests.activeobject.equality.RegisteredObject
     * registeredA2 = null;
     * 
     * public Test() { super("comparisons with active objects using equals", "compares active
     * objects, groups and standard objects using with the equals method"); }
     * 
     * @Override public void action() throws Exception { a1 = (A)
     * ProActive.newActive(A.class.getName(), new Object[] { "a1" }, TestNodes.getSameVMNode()); a2
     * =
     * (A) ProActive.newActive(A.class.getName(), new Object[] { "a2" },
     * TestNodes.getLocalVMNode());
     * 
     * group1 = (A) ProActiveGroup.newGroup(A.class.getName()); group2 = (A)
     * ProActiveGroup.newGroup(A.class.getName()); group3 = (A)
     * ProActiveGroup.newGroup(A.class.getName());
     * 
     * registeredA1 = (functionalTests.activeobject.equality.RegisteredObject)
     * ProActive.newActive(functionalTests.activeobject.equality.RegisteredObject.class.getName(),
     * new Object[] { "toto" }); registeredA1.register(); registeredA2 =
     * (functionalTests.activeobject.equality.RegisteredObject)
     * ProActive.lookupActive(functionalTests.activeobject.equality.RegisteredObject.class.getName()
     * ,
     * UrlBuilder.buildUrlFromProperties("localhost", "A"));
     * 
     * ProActiveGroup.getGroup(group1).add(a1); ProActiveGroup.getGroup(group1).add(a2);
     * 
     * ProActiveGroup.getGroup(group2).add(a1); ProActiveGroup.getGroup(group2).add(a2);
     * 
     * ProActiveGroup.getGroup(group3).add(a1); }
     * 
     * @Override public void initTest() throws Exception { // nothing to do }
     * 
     * @Override public void endTest() throws Exception { // nothing to do }
     * 
     * @Override public boolean postConditions() throws Exception { Assertions.assertEquals(group1,
     * group2); Assertions.assertEquals(new Integer(group1.hashCode()), new
     * Integer(group2.hashCode())); Assertions.assertNonEquals(group1, group3);
     * Assertions.assertNonEquals(a1, "x"); Assertions.assertNonEquals(a1, null);
     * Assertions.assertNonEquals(group1, "x"); Assertions.assertNonEquals(group1, null);
     * Assertions.assertNonEquals(a1, group3); Assertions.assertNonEquals(group3, a1);
     * Assertions.assertEquals(registeredA1, registeredA2); Assertions.assertEquals(new
     * Integer(registeredA1.hashCode()), new Integer(registeredA2.hashCode())); return true; }
     */
}
