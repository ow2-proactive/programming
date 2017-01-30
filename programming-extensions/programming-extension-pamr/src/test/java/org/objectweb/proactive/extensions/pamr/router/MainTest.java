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
package org.objectweb.proactive.extensions.pamr.router;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class MainTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void config_file_is_defined_as_parameter() throws Exception {
        File configFileAsArg = tmpFolder.newFile();
        File defaultConfigFile = tmpFolder.newFile();

        final RouterConfig testedConfig = createRouterConfiguration(new String[] { "-f", configFileAsArg.getPath() },
                                                                    defaultConfigFile);

        assertEquals(configFileAsArg, testedConfig.getReservedAgentConfigFile());
    }

    @Test
    public void config_file_is_not_defined_as_parameter() throws Exception {
        File defaultConfigFile = tmpFolder.newFile();

        final RouterConfig testedConfig = createRouterConfiguration(new String[] {}, defaultConfigFile);

        assertEquals(defaultConfigFile, testedConfig.getReservedAgentConfigFile());
    }

    @Test
    public void no_default_config_file() throws Exception {
        final RouterConfig testedConfig = createRouterConfiguration(new String[] {}, null);

        assertNull(testedConfig.getReservedAgentConfigFile());
    }

    private RouterConfig createRouterConfiguration(final String[] args, final File defaultConfigFile)
            throws IOException {
        final RouterConfig[] testedConfig = new RouterConfig[1];

        String path = defaultConfigFile == null ? null : defaultConfigFile.getPath();
        new Main(args, path) {
            @Override
            void startRouter(RouterConfig routerConfig) {
                testedConfig[0] = routerConfig;
            }
        };
        return testedConfig[0];
    }

}
