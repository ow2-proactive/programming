/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
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
 */
package functionalTests;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;


public class GCMFunctionalTest extends FunctionalTest {

    static public final String VAR_OS = "os";

    public File applicationDescriptor;
    public VariableContractImpl vContract;
    public GCMApplication gcmad;

    public GCMFunctionalTest() {
        vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(VAR_OS, OperatingSystem.getOperatingSystem().name(),
                VariableContractType.DescriptorDefaultVariable);
    }

    public GCMFunctionalTest(File applicationDescriptor) {
        this();
        this.applicationDescriptor = applicationDescriptor;
    }

    @Before
    public void startDeployment() throws ProActiveException {
        if (gcmad != null) {
            throw new IllegalStateException("deployment already started");
        }

        gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor, vContract);
        gcmad.startDeployment();
    }

    @After
    public void killDeployment() {
        if (gcmad != null) {
            gcmad.kill();
        }
    }
}
