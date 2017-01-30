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
package functionalTests.stub.proactive755;

import org.objectweb.proactive.api.PAActiveObject;

import functionalTests.FunctionalTest;


/**
 * Regression test for PROACTIVE-755
 * 
 *  invalid return type for generated stub when the 
 *  return type in an class is a subclass of 
 *  the one defined in the interface implemented by the class
 *  
 *  Interface A
 *     A getA()
 *
 * class AImpl impl A
   AIMpl getA()
 * 
 * the generated stub for AA returns A -- while AImpl should be expected
 *  
 */
public class Test extends FunctionalTest {

    @org.junit.Test
    public void proactive755() throws Exception {

        A aa = PAActiveObject.newActive(AImpl.class, new Object[] {});

        AImpl a = (AImpl) aa.getA();

    }

}
