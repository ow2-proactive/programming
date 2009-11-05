/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.vfsprovider.console;

import java.io.IOException;
import java.net.URISyntaxException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;


/**
 * Class for starting PA Provider server manually.
 */
public class PAProviderServerStarter {

    private static String rootDirectory;

    private static String providerName;

    private static FileSystemServerDeployer deployer;

    /**
     * @param args
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws IOException {
        final String name = PAProviderServerStarter.class.getName();

        try {
            parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.out.println("Usage: java " + name + " <root directory> [PAProvider name]");
            System.out.println("       java " + name + " --help");
            System.out
                    .println("Starts the PA Provider server for <root directory> with default or specified name.");
            System.out.println("\t--help\tprints this screen");
            return;
        }

        setupHook();
        startServer();
    }

    private static void parseArgs(String[] args) {
        final int len = args.length;

        if (len == 0 || len > 2 || (len == 1 && "--help".equals(args[0])))
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

        final String url = deployer.getVFSRootURL();
        System.out.println("PAProvider successfully started.\nVFS URL of this provider: " + url);
    }

    private static void stopServer() throws ProActiveException {
        deployer.terminate();
    }
}
