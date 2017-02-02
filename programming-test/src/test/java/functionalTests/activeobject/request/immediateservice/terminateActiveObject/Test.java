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
package functionalTests.activeobject.request.immediateservice.terminateActiveObject;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;

import functionalTests.FunctionalTest;


/**
 * Test immediate termination of an active object
 */
public class Test extends FunctionalTest {
    B b;

    @Before
    public void before() throws Exception {
        b = PAActiveObject.newActive(B.class, new Object[] { "blue" });
        b.changeColor("red");
        PAActiveObject.terminateActiveObject(b, true);
    }

    @org.junit.Test(expected = Exception.class)
    public void action() throws Exception {
        b.getColor();
    }

    public static void main(String[] args) {
        Test test = new Test();
        try {
            test.action();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
