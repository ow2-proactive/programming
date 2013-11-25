/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.runtime;

import java.io.File;
import java.net.URL;

import org.junit.Test;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import static org.junit.Assert.assertEquals;


public class ProActiveRuntimeImplTest {

    @Test
    public void testGetProActiveHome_Defined_PAHOME() throws Exception {
        CentralPAPropertyRepository.PA_HOME.setValue("aValue");

        String proActiveHome = new ProActiveRuntimeImpl().getProActiveHome();

        assertEquals("aValue", proActiveHome);
    }

    @Test
    public void testGetProActiveHome_Undefined_PAHOME_ProActiveJar() throws Exception {
        String proActiveHome = new ProActiveRuntimeImpl().guessProActiveHomeFromJarClassloader(
          new URL("jar:file:/tmp/dist/lib/ProActive.jar!/MyClass.class").getPath());

        assertEquals(new File("/tmp").getAbsolutePath(), proActiveHome);
    }

    @Test
    public void testGetProActiveHome_Undefined_PAHOME_GradleJars() throws Exception {
        String proActiveHome = new ProActiveRuntimeImpl().guessProActiveHomeFromJarClassloader(
          new URL("jar:file:/tmp/dist/lib/programming-core-2.4.2.jar!/MyClass.class").getPath());

        assertEquals(new File("/tmp").getAbsolutePath(), proActiveHome);
    }
}
