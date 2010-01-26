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
package org.objectweb.proactive.extensions.dataspaces.console;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;


/**
 * Class for starting the naming service manually.
 */
public class NamingServiceStarter {

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES_NAMING_SERVICE);

    private static NamingServiceDeployer namingServiceDeployer;

    /**
     * @param args
     */
    public static void main(String[] args) throws ProActiveException {
        final String name = NamingServiceStarter.class.getName();

        try {
            parseArgs(args);
        } catch (IllegalArgumentException e) {
            System.out.println("Usage: java " + name + " [naming service name]");
            System.out.println("       java " + name + " --help");
            System.out.println("Starts the naming service with default or specified name.");
            System.out.println("\t--help\tprints this screen");
            return;
        }

        setupHook();
        final String serviceName;
        if (args.length >= 1)
            serviceName = args[0];
        else
            serviceName = null;
        startNamingService(serviceName);
    }

    private static void parseArgs(String[] args) {
        if (args.length > 1 || (args.length == 1 && "--help".equals(args[0])))
            throw new IllegalArgumentException();
    }

    private static void setupHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                stopNamingService();
            }
        });
    }

    private static void startNamingService(String name) throws ProActiveException {
        if (name != null)
            namingServiceDeployer = new NamingServiceDeployer(name);
        else
            namingServiceDeployer = new NamingServiceDeployer();
        System.out.println("Naming Service successfully started on: " +
            namingServiceDeployer.getNamingServiceURL());
    }

    private static void stopNamingService() {
        if (namingServiceDeployer != null) {
            try {
                namingServiceDeployer.terminate();
            } catch (ProActiveException x) {
                ProActiveLogger.logEatedException(logger, x);
            }
            namingServiceDeployer = null;
        }
    }
}
