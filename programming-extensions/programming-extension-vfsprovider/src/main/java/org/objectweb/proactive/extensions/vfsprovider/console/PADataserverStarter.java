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
package org.objectweb.proactive.extensions.vfsprovider.console;

import java.io.IOException;
import java.util.Arrays;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;
import org.objectweb.proactive.utils.JVMPropertiesPreloader;
import org.objectweb.proactive.utils.SecurityManagerConfigurator;


/**
 * Class for starting PAdataserver manually.
 */
public class PADataserverStarter {

    private static String rootDirectory;

    private static String providerName;

    private static FileSystemServerDeployer deployer;

    public static void main(String[] args) throws IOException {
        final String name = PADataserverStarter.class.getName();

        try {
            parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.out.println("Usage: java " + name + " <root directory> [ProActive dataserver name]");
            System.out.println(" -h,--help\tprints the help screen\n");
            System.out.println("Starts the ProActive dataserver for <root directory> with default or specified name.");
            System.out.println("ProActive system properties can be set using command line too.");
            System.out.println("Syntax is: -Dproperty=value");
            return;
        }

        SecurityManagerConfigurator.configureSecurityManager(PADataserverStarter.class.getResource("/all-permissions.security.policy")
                                                                                      .toString());
        setupHook();
        startServer();
    }

    private static void parseArgs(String[] args) {
        args = JVMPropertiesPreloader.overrideJVMProperties(args);
        final int len = args.length;

        if (len == 0 || len > 2 || (len == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))))
            throw new IllegalArgumentException();

        rootDirectory = args[0];
        if (len == 2)
            providerName = args[1];
    }

    private static void setupHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    stopServer();
                } catch (ProActiveException e) {
                    throw new ProActiveRuntimeException(e);
                }
            }
        });
    }

    private static void startServer() throws IOException {
        if (providerName == null)
            deployer = new FileSystemServerDeployer(rootDirectory, true);
        else
            deployer = new FileSystemServerDeployer(providerName, rootDirectory, true);

        final String[] urls = deployer.getVFSRootURLs();
        System.out.println("ProActive dataserver successfully started.\nVFS URLs of this provider: " +
                           Arrays.asList(urls));
    }

    private static void stopServer() throws ProActiveException {
        deployer.terminate();
    }
}
