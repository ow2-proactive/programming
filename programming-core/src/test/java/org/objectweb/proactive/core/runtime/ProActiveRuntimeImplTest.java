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
package org.objectweb.proactive.core.runtime;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;


public class ProActiveRuntimeImplTest {

    @Test
    public void testGetProActiveHome_Defined_PAHOME() throws Exception {
        CentralPAPropertyRepository.PA_HOME.setValue("aValue");

        String proActiveHome = new ProActiveRuntimeImpl().getProActiveHome();

        assertEquals("aValue", proActiveHome);
    }

    @Test
    public void testGetProActiveHome_Undefined_PAHOME_ProActiveJar() throws Exception {
        String proActiveHome = new ProActiveRuntimeImpl().guessProActiveHomeFromJarClassloader(new URL("jar:file:/tmp/dist/lib/ProActive.jar!/MyClass.class").getPath());

        assertEquals(new File("/tmp").getCanonicalPath(), proActiveHome);
    }

    @Test
    public void testGetProActiveHome_Undefined_PAHOME_GradleJars() throws Exception {
        String proActiveHome = new ProActiveRuntimeImpl().guessProActiveHomeFromJarClassloader(new URL("jar:file:/tmp/dist/lib/programming-core-2.4.2.jar!/MyClass.class").getPath());

        assertEquals(new File("/tmp").getCanonicalPath(), proActiveHome);
    }
}
