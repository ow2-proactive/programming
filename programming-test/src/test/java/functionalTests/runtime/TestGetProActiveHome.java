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
package functionalTests.runtime;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import functionalTests.FunctionalTest;


/**
 * Test default runtime creation
 */
@Ignore
// coupled to project structure
public class TestGetProActiveHome extends FunctionalTest {

    @Test
    public void action() throws Exception {
        File paHome = new File(CentralPAPropertyRepository.PA_HOME.getValue());

        // Hack to unset PA_HOME
        CentralPAPropertyRepository.PA_HOME.unset();
        System.out.println(ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome());

        File computedPaHome = new File(ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome());
        Assert.assertEquals(paHome.getCanonicalPath(), computedPaHome.getCanonicalPath());
    }
}
