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
package functionalTests.jmx.mbean;

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;

import functionalTests.GCMFunctionalTest;


/**
 * Test the creation of the JMX MBean. (i.e BodyWrapperMBean and ProActiveRuntimeWrapperMBean)
 * 
 * @author The ProActive Team
 */

public class Test extends GCMFunctionalTest {
    private A ao;

    public Test() throws ProActiveException {
        super(1, 1);
        super.setOptionalJvmParamters("-Dcom.sun.management.jmxremote -Dproactive.jmx.mbean=true -Dproactive.jmx.notification=true");
        super.startDeployment();
    }

    @Before
    public void initTest() throws Exception {
        Node node = super.getANode();
        ao = PAActiveObject.newActive(A.class, new Object[] {}, node);
    }

    @org.junit.Test
    public void action() throws Exception {
        assertTrue("The MBean associated to the active object doesn't exist!", ao.existBodyWrapperMBean());
        assertTrue("The MBean associated to the ProActive Runtime doesn't exist!",
                ao.existProActiveRuntimeWrapperMBean());
    }
}
