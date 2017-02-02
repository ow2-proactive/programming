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
package gcmdeployment.variablecontract;

import java.io.File;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationImpl;
import org.objectweb.proactive.extensions.gcmdeployment.Helpers;

import functionalTests.FunctionalTest;


public class TestVariableContract extends FunctionalTest {
    static final String VAR_NAME = "VARIABLE";

    static final String VAR_VALUE = "value";

    static final String VAR_DEFAULTVALUE = "plop";

    @Test
    public void test() throws ProActiveException, URISyntaxException {
        File desc = null;
        desc = new File(this.getClass().getResource("TestVariableContractApplication.xml").toURI());

        VariableContractImpl vContractRes;
        GCMApplicationImpl gcmad;

        gcmad = new GCMApplicationImpl(Helpers.fileToURL(desc));
        vContractRes = gcmad.getVariableContract();
        Assert.assertEquals(VAR_DEFAULTVALUE, vContractRes.getValue(VAR_NAME));

        VariableContractImpl vContract = new VariableContractImpl();
        vContract.setVariableFromProgram(VAR_NAME, VAR_VALUE, VariableContractType.DescriptorDefaultVariable);
        gcmad = new GCMApplicationImpl(Helpers.fileToURL(desc), vContract);
        vContractRes = gcmad.getVariableContract();
        Assert.assertEquals(VAR_VALUE, vContractRes.getValue(VAR_NAME));

    }
}
