/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests.activeobject.garbagecollection;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;

import functionalTests.FunctionalTest;

import static junit.framework.Assert.assertTrue;


/**
 * Test DGC
 */

public class Test extends FunctionalTest {

    /*
     * \ v \ 1--->---2 \ /| ^ v | \ / | 3 v / \ | v ^ | / \| 4 5
     */
    private static void buildGraph() throws Exception {
        A a1 = PAActiveObject.newActive(A.class, null);
        A a2 = PAActiveObject.newActive(A.class, null);
        A a3 = PAActiveObject.newActive(A.class, null);
        A a4 = PAActiveObject.newActive(A.class, null);
        A a5 = PAActiveObject.newActive(A.class, null);

        a1.addRef(a2);
        a2.addRef(a3);
        a2.addRef(a5);
        a3.addRef(a1);
        a3.addRef(a4);
        a5.addRef(a3);
    }

    @org.junit.Test
    public void action() throws Exception {
        /*
         * if ("BEA Systems, Inc.".equals(System.getProperty("java.vendor")) &&
         * "1.6.0_01".equals(System.getProperty("java.version"))) { // With the local GC in this
         * JVM, weak references // to stubs are seemingly never cleared. return; }
         */
        assertTrue(A.countCollected() == 0);
        buildGraph();
        while (A.countCollected() != 5) {
            System.gc();
            Thread.sleep(500);
        }
    }

    @Before
    public void initTest() throws Exception {
        /* This must be done before initializing ProActive, and the DGC */
        PAProperties.PA_DGC.setValue(true);
        PAProperties.PA_DGC_TTB.setValue(500);
    }
}
