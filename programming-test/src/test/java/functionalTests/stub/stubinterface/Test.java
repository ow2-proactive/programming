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
package functionalTests.stub.stubinterface;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.core.mop.MOP;

import functionalTests.FunctionalTest;


/**
 * Test stub generation for interface
 */
public class Test extends FunctionalTest {
    String result1;

    String result2;

    @org.junit.Test
    public void action() throws Exception {
        StringInterface i1 = (StringInterface) MOP.newInstance("functionalTests.stub.stubinterface.StringInterface",
                                                               "functionalTests.stub.stubinterface.StringInterfaceImpl",
                                                               null,
                                                               new Object[] { "toto" },
                                                               "functionalTests.stub.stubinterface.ProxyOne",
                                                               new Object[0]);
        result1 = i1.getMyString();

        StringInterfaceImpl i2 = (StringInterfaceImpl) MOP.newInstance("functionalTests.stub.stubinterface.StringInterfaceImpl",
                                                                       null,
                                                                       new Object[] { "titi" },
                                                                       "functionalTests.stub.stubinterface.ProxyOne",
                                                                       new Object[0]);
        result2 = i2.getMyString();

        assertTrue(result1.equals("toto"));
        assertTrue(result2.equals("titi"));
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
