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
package functionalTests;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;


@Ignore
public abstract class ComponentTest extends FunctionalTest {

    /**
     * @param name
     */
    public ComponentTest() {
        //  super("[COMPONENTS] " + name);
    }

    /**
     * @param name
     * @param description
     */
    public ComponentTest(String name, String description) {
        //  super("Components : " + name, description);
    }

    @BeforeClass
    public static void componentPreConditions() throws Exception {
        if (!CentralPAPropertyRepository.PA_FUTURE_AC.isTrue()) {
            throw new Exception(
                "The components framework needs the automatic continuations (system property 'proactive.future.ac' set to 'true') to be operative");
        }

        //-Dfractal.provider=org.objectweb.proactive.core.component.Fractive
        CentralPAPropertyRepository.FRACTAL_PROVIDER
                .setValue("org.objectweb.proactive.core.component.Fractive");
    }
}
