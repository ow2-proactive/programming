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
package org.objectweb.proactive.core.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class ProActiveConfigurationTest {

    @Test
    public void configuration_default_values_and_override() throws Exception {
        System.setProperty(CentralPAPropertyRepository.PA_CONFIGURATION_FILE.getName(),
                           getClass().getResource("test.properties").toString());
        CentralPAPropertyRepository.PA_RUNTIME_STAYALIVE.setValue(false);

        ProActiveConfiguration configuration = ProActiveConfiguration.getInstance();

        assertEquals("rmi", configuration.getProperty(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getName()));
        assertEquals("false", configuration.getProperty(CentralPAPropertyRepository.PA_RUNTIME_STAYALIVE.getName()));
        assertEquals("true", configuration.getProperty(CentralPAPropertyRepository.PA_EXIT_ON_EMPTY.getName()));
    }
}
