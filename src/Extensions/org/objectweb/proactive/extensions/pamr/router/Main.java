/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pamr.router;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
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
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.pamr.PAMRLog4jCompat;
import org.objectweb.proactive.extensions.pamr.protocol.MagicCookie;
import org.objectweb.proactive.extensions.pamr.protocol.message.ReloadConfigurationMessage;


/** Start a router.
 * 
 * @since ProActive 4.1.0
 */
class Main {
    static final private Logger logger = ProActiveLogger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER);

    private Options options;

    public static void main(String[] args) throws IOException {
        new PAMRLog4jCompat().ensureCompat();
        new Main(args);
    }

    private Main(String[] args) throws IOException {
        CommandLineParser parser = new PosixParser();

        this.options = new Options();
        this.options.addOption("p", "port", true,
                "Specifies the port on which the server listens for connections (default 33647).");
        this.options.addOption("i", "ip", true, "Bind to a given IP address or hostname");
        this.options.addOption("4", "ipv4", false, "Force the router to use IPv4 addresses only");
        this.options.addOption("6", "ipv6", false, "Force the router to use IPv6 addresses only");
        this.options.addOption("t", "timeout", true, "The heartbeat timeout value");
        this.options.addOption("e", "evictTimeout", true,
                "Timeout for the eviction of disconnected clients (default: -1, means no eviction)");
        this.options.addOption("w", "nbWorkers", true, "Size of the worker thread pool");
        this.options.addOption("f", "configFile", true, "configuration file");
        this.options.addOption("h", "help", false, "Print help message");
        this.options.addOption("v", "verbose", false, "Verbose mode. Print clients (dis)connections");
        this.options.addOption("r", "reload", false, "Reload configuration file");
        this.options.addOption("c", "cookie", true,
                "This admin cookie to provide to reload the configuration");

        CommandLine line = null;

        try {
            String arg;

            line = parser.parse(options, args);

            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("router", this.options);
                System.exit(0);
            }

            boolean reload = line.hasOption("r");
            if (reload) {
                /*
                 * RELOAD CONFIGURATION
                 */

                // Check incompatible options
                boolean error = false;
                error |= line.hasOption("4");
                error |= line.hasOption("6");
                error |= line.hasOption("w");
                error |= line.hasOption("f");
                error |= line.hasOption("t");
                error |= line.hasOption("e");
                if (error) {
                    printHelpAndExit("Options -4 -6 -w -f -t -e are not compatible with -r");
                }

                int port = -1;
                InetAddress ia = null;
                MagicCookie magicCookie = null;

                // Parse options
                arg = line.getOptionValue("p");
                if (arg == null) {
                    port = RouterImpl.DEFAULT_PORT;
                } else {
                    try {
                        port = Integer.parseInt(arg);
                    } catch (NumberFormatException e) {
                        printHelpAndExit("Invalid port number: " + arg);
                    } catch (IllegalArgumentException e) {
                        printHelpAndExit("Invalid port number: " + arg);
                    }
                }

                arg = line.getOptionValue("i");
                if (arg == null) {
                    try {
                        ia = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        printHelpAndExit("Failed to resolve localhost: " + e.getMessage());
                    }
                } else {
                    try {
                        ia = InetAddress.getByName(arg);
                    } catch (UnknownHostException e) {
                        printHelpAndExit("Unknown hostname or IP address: " + arg);
                    }
                }

                arg = line.getOptionValue("c");
                if (arg == null) {
                    printHelpAndExit("The --cookie option is mandatory when reloading the configuration");
                } else {
                    try {
                        magicCookie = new MagicCookie(arg);
                    } catch (IllegalArgumentException e) {
                        printHelpAndExit(e);
                    }
                }

                if (line.hasOption("v")) {
                    Logger l = Logger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER_ADMIN);
                    l.setLevel(Level.DEBUG);
                }

                // Send a message to the router to reload the configuration
                try {
                    Socket socket = new Socket(ia, port);
                    System.out.println("Asking " + socket + " to reload it's configuration");
                    ReloadConfigurationMessage rlm = new ReloadConfigurationMessage(magicCookie);
                    socket.getOutputStream().write(rlm.toByteArray());
                    socket.close();
                    System.out.println("Done. Check the router logs");
                } catch (Throwable e) {
                    logger.fatal("Failed to reload the conifiguration router:", e);
                    System.exit(1);
                }
            } else {
                /*
                 * START ROUTER
                 */

                RouterConfig config = new RouterConfig();

                // Check incompatible options
                boolean error = false;
                error |= line.hasOption("c");
                if (error) {
                    printHelpAndExit("");
                    printHelpAndExit("Option -c is only compatible with -r");

                }

                // Parses option to fill config
                arg = line.getOptionValue("p");
                if (arg == null) {
                    config.setPort(RouterImpl.DEFAULT_PORT);
                } else {
                    try {
                        int i = Integer.parseInt(arg);
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
                if (arg != null) { // null by defaul is ok -> wildcard !
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

                arg = line.getOptionValue("f");
                if (arg != null) {
                    File f = new File(arg);
                    if (!f.exists()) {
                        System.err.println("Invalid configuration file: " + arg + " does not exist");
                        System.exit(1);
                    }
                    if (!f.isFile()) {
                        System.err.println("Invalid configuration file: " + arg + " is not a file");
                        System.exit(1);
                    }
                    if (!f.canRead()) {
                        System.err.println("Invalid configuration file: " + arg + " is not readable");
                        System.exit(1);
                    }

                    config.setReservedAgentConfigFile(f);
                }

                if (line.hasOption("v")) {
                    Logger l = Logger.getLogger(PAMRConfig.Loggers.PAMR_ROUTER_ADMIN);
                    l.setLevel(Level.DEBUG);
                }

                arg = line.getOptionValue("t");
                if (arg != null) {
                    try {
                        int i = Integer.parseInt(arg);
                        if (i < 0) {
                            printHelpAndExit("Invalid timeout value. Must be a positive long");
                        }
                        config.setHeartbeatTimeout(i);
                    } catch (NumberFormatException e) {
                        printHelpAndExit("Invalid timeout value");
                    }
                }

                arg = line.getOptionValue("e");
                if (arg != null) {
                    try {
                        long i = Long.parseLong(arg);
                        if (i == 0 || i < -1) {
                            printHelpAndExit("Invalid client eviction timeout value. Must be either -1 or positive");
                        }
                        config.setClientEvictionTimeout(i);
                    } catch (NumberFormatException e) {
                        printHelpAndExit("Invalid client eviction timeout value");
                    }
                }

                // Start the router 
                try {
                    Router.createAndStart(config);
                } catch (Exception e) {
                    logger.fatal("Failed to start the router:", e);
                    System.exit(1);
                }
            }
        } catch (UnrecognizedOptionException e) {
            printHelpAndExit(e.getMessage());
        } catch (MissingArgumentException e) {
            printHelpAndExit(e.getMessage());
        } catch (ParseException e) {
            printHelpAndExit(e);
        }
    }

    private void printHelpAndExit(String error) {
        if (error != null && error.length() != 0) {
            System.err.println(error);
            System.err.println();
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("router", options);
        System.err.println();

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
