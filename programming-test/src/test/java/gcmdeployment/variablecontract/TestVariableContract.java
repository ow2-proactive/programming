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
package gcmdeployment.variablecontract;

import java.io.File;
import java.net.URISyntaxException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationImpl;
import org.objectweb.proactive.extensions.gcmdeployment.Helpers;
import functionalTests.FunctionalTest;
import org.junit.Assert;
import org.junit.Test;


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
