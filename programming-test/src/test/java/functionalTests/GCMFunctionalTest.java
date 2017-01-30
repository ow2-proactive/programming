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
package functionalTests;

import java.net.URL;

import org.junit.Ignore;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.utils.ArgCheck;


@Ignore
public class GCMFunctionalTest extends FunctionalTest {
    static final private URL DEFAULT_APP_DESC = GCMFunctionalTest.class.getResource("/functionalTests/_CONFIG/JunitApp.xml");

    static public final String DEFAULT_VN_NAME = "nodes";

    static public final String VC_VMCAPACITY = "vmCapacity";

    static public final String VC_HOSTCAPACITY = "hostCapacity";

    static public final String VAR_GCMD = "deploymentDescriptor";

    static public final String VAR_JVMARG = "JVM_PARAMETERS";

    static public final String VAR_OPTJVMARG = "jvmargDefinedByTest";

    final private VariableContractImpl vc;

    final public URL applicationDescriptor;

    volatile public GCMApplication gcmad;

    public GCMFunctionalTest(int hostCapacity, int vmCapacity) {
        this.applicationDescriptor = DEFAULT_APP_DESC;
        this.vc = clone(super.getVariableContract());
        this.setHostCapacity(hostCapacity);
        this.setVmCapacity(vmCapacity);
    }

    public GCMFunctionalTest(URL applicationDescriptor) {
        ArgCheck.requireNonNull(applicationDescriptor);
        this.applicationDescriptor = applicationDescriptor;
        this.vc = clone(super.getVariableContract());
    }

    final public void setHostCapacity(int hostCapacity) {
        this.vc.setVariableFromProgram(VC_HOSTCAPACITY,
                                       Integer.toString(hostCapacity),
                                       VariableContractType.DescriptorDefaultVariable);
    }

    final public void setVmCapacity(int vmCapacity) {
        this.vc.setVariableFromProgram(VC_VMCAPACITY,
                                       Integer.toString(vmCapacity),
                                       VariableContractType.DescriptorDefaultVariable);
    }

    final public void setVariable(String name, String value, VariableContractType type) {
        this.vc.setVariableFromProgram(name, value, type);
    }

    final public void setOptionalJvmParamters(String value) {
        this.vc.setVariableFromProgram(VAR_OPTJVMARG, value, VariableContractType.DescriptorDefaultVariable);
    }

    final public VariableContractImpl getFinalVariableContract() {
        return this.clone(this.vc);
    }

    final public Node getANode() {
        return getANodeFrom(DEFAULT_VN_NAME);
    }

    final public Node getANodeFrom(String vnName) {
        if (gcmad == null || !gcmad.isStarted()) {
            throw new IllegalStateException("deployment is not started");
        }

        GCMVirtualNode vn = gcmad.getVirtualNode(vnName);
        return vn.getANode();
    }

    private VariableContractImpl clone(VariableContractImpl vc) {
        try {
            return (VariableContractImpl) vc.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    final public void startDeployment() throws ProActiveException {
        logger.info(GCMFunctionalTest.class.getName() + " @Before: startDeployment");
        if (gcmad != null) {
            throw new IllegalStateException("deployment already started");
        }

        gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor, this.clone(this.vc));
        gcmad.startDeployment();
    }

    final public void killDeployment() throws Throwable {
        logger.info(GCMFunctionalTest.class.getName() + " @After: killDeployment");
        if (gcmad != null) {
            gcmad.kill();
        }
        logger.info(GCMFunctionalTest.class.getName() + " @After: killDeployment");
    }
}
