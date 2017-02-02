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
package functionalTests.gcmdeployment.executable;

import java.io.File;

import org.junit.After;
import org.junit.Assert;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderExecutable.Instances;

import functionalTests.GCMFunctionalTest;


public class AbstractTExecutable extends GCMFunctionalTest {

    final String cookie;

    final File tmpDir;

    public AbstractTExecutable(Instances instances) throws ProActiveException {
        super(AbstractTExecutable.class.getResource("TestExecutable.xml"));
        super.setHostCapacity(2);
        super.setVmCapacity(2);

        cookie = Long.valueOf(ProActiveRandom.nextLong()).toString();
        tmpDir = new File(ProActiveConfiguration.getInstance().getProperty("java.io.tmpdir") + File.separator +
                          this.getClass().getName() + cookie + File.separator);

        super.setVariable("tmpDir", tmpDir.toString(), VariableContractType.DescriptorDefaultVariable);
        super.setVariable("instances", instances.toString(), VariableContractType.DescriptorDefaultVariable);

        System.out.println("Temporary directory is: " + tmpDir.toString());
        Assert.assertTrue(tmpDir.mkdir());

        super.startDeployment();
    }

    @After
    public void after() {
        for (File file : tmpDir.listFiles()) {
            file.delete();
        }
        tmpDir.delete();
    }

}
