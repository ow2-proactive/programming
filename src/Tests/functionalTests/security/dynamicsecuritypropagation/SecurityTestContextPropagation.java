/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
package functionalTests.security.dynamicsecuritypropagation;

import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityDescriptorHandler;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;

import functionalTests.FunctionalTest;
import functionalTests.security.A;


/**
 * Test the dynamic propagation of an application context
 *
 * @author The ProActive Team
 *
 */
public class SecurityTestContextPropagation extends FunctionalTest {

    /**
     *
     */
    private ProActiveSecurityManager psm = null;

    @Test
    public void action() throws Exception {
        A a = PAActiveObject.newActive(functionalTests.security.A.class, new Object[] {});

        assertTrue("hello".equals(a.hello("hello")));
    }

    @Before
    public void initTest() throws Exception {
        PolicyServer ps = ProActiveSecurityDescriptorHandler
                .createPolicyServer(SecurityTestContextPropagation.class.getResource(
                        "/functionalTests/security/applicationPolicy.xml").getPath());
        psm = new ProActiveSecurityManager(EntityType.OBJECT, ps);

        // set the default security manager
        ProActiveMetaObjectFactory.newInstance().setProActiveSecurityManager(psm);
    }
}
