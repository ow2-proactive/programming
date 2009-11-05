/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package functionalTests;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


@Ignore
public class GCMFunctionalTest extends FunctionalTest {

    static public final String VAR_OS = "os";

    public URL applicationDescriptor;
    public GCMApplication gcmad;

    public GCMFunctionalTest() {
        super.vContract.setVariableFromProgram(VAR_OS, OperatingSystem.getOperatingSystem().name(),
                VariableContractType.DescriptorDefaultVariable);

    }

    public GCMFunctionalTest(URL applicationDescriptor) {
        this();
        this.applicationDescriptor = applicationDescriptor;
    }

    @Before
    public void startDeployment() throws ProActiveException {
        logger.info(GCMFunctionalTest.class.getName() + " @Before: startDeployment");
        if (gcmad != null) {
            throw new IllegalStateException("deployment already started");
        }

        gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor, super.vContract);
        gcmad.startDeployment();
    }

    @After
    public void killDeployment() throws Throwable {
        logger.info(GCMFunctionalTest.class.getName() + " @After: killDeployment");
        if (gcmad != null) {
            gcmad.kill();
        }
        logger.info(GCMFunctionalTest.class.getName() + " @After: killDeployment");
    }
}
