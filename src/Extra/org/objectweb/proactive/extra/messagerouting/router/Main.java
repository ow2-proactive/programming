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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extra.messagerouting.router;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extra.messagerouting.PAMRConfig;


/** Start a router.
 * 
 * @since ProActive 4.1.0
 */
class Main {
    static final private Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.FORWARDING_ROUTER);

    private RouterConfig config;
    private Options options;

    public static void main(String[] args) throws IOException {
        new Main(args);
    }

    private Main(String[] args) throws IOException {
        this.config = parseOptions(args);

        try {
            Router.createAndStart(this.config);
        } catch (IOException e) {
            logger.fatal("Failed to start the router:", e);
            System.exit(1);
        }
    }

    private RouterConfig parseOptions(String[] args) {
        RouterConfig config = new RouterConfig();

        CommandLineParser parser = new PosixParser();

        this.options = new Options();
        this.options.addOption("p", "port", true,
                "Specifies the port on which the server listens for connections (default 33647).");
        this.options.addOption("i", "ip", true, "Bind to a given IP address or hostname");
        this.options.addOption("4", "ipv4", false, "Force the router to use IPv4 addresses only");
        this.options.addOption("6", "ipv6", false, "Force the router to use IPv6 addresses only");
        this.options.addOption("w", "nbWorkers", true, "Size of the worker thread pool");
        this.options.addOption("h", "help", false, "Print help message");
        this.options.addOption("v", "verbose", false, "Verbose mode. Print clients (dis)connections");

        CommandLine line = null;

        try {
            String arg;

            line = parser.parse(options, args);

            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("router", this.options);

                System.exit(0);
            }

            arg = line.getOptionValue("p");
            if (arg == null) {
                config.setPort(RouterImpl.DEFAULT_PORT);
            } else {
                try {
                    short i = new Short(arg);
                    config.setPort(i);
                } catch (NumberFormatException e) {
                    printHelpAndExit("Invalid port number: " + arg);
                } catch (IllegalArgumentException e) {
                    printHelpAndExit("Invalid port number: " + arg);
                }
            }

            if (line.hasOption("4")) {
                System.setProperty("java.net.preferIPv4Stack", "true");
            }

            if (line.hasOption("6")) {
                System.setProperty("java.net.preferIPv6Stack", "true");
            }

            arg = line.getOptionValue("i");
            if (arg == null) {
                try {
                    InetAddress addr = InetAddress.getLocalHost();
                    config.setInetAddress(addr);
                } catch (UnknownHostException e) {
                    printHelpAndExit(e);
                }
            } else {
                try {
                    InetAddress addr = InetAddress.getByName(arg);
                    config.setInetAddress(addr);
                } catch (UnknownHostException e) {
                    printHelpAndExit("Unknown hostname or IP address: " + arg);
                }
            }

            arg = line.getOptionValue("w");
            if (arg == null) {
                int n = Runtime.getRuntime().availableProcessors();
                config.setNbWorkerThreads(n);
            } else {
                try {
                    int i = new Integer(arg);
                    config.setNbWorkerThreads(i);
                } catch (NumberFormatException e) {
                    printHelpAndExit("Invalid worker number: " + arg);
                }
            }

            if (line.hasOption("v")) {
                Logger l = Logger.getLogger(PAMRConfig.Loggers.FORWARDING_ROUTER_ADMIN);
                l.setLevel(Level.DEBUG);
            }
        } catch (UnrecognizedOptionException e) {
            printHelpAndExit(e.getMessage());
        } catch (MissingArgumentException e) {
            printHelpAndExit(e.getMessage());
        } catch (ParseException e) {
            printHelpAndExit(e);
        }

        return config;
    }

    private void printHelpAndExit(String error) {
        System.err.println(error);
        System.err.println();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("router", options);

        System.exit(1);

    }

    private void printHelpAndExit(Exception e) {
        e.printStackTrace();
        System.err.println();

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("router", options);

        System.exit(1);
    }
}
