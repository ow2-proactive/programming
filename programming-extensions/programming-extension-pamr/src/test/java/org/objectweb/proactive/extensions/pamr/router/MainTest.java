package org.objectweb.proactive.extensions.pamr.router;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;


public class MainTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    @Test
    public void config_file_is_defined_as_parameter() throws Exception {
        File configFileAsArg = tmpFolder.newFile();
        File defaultConfigFile = tmpFolder.newFile();

        final RouterConfig testedConfig = createRouterConfiguration(
                new String[] { "-f", configFileAsArg.getPath() }, defaultConfigFile);

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