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
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** Start a router.
 * 
 * @since ProActive 4.1.0
 */
class Main {
    static final private Logger logger = ProActiveLogger.getLogger(Loggers.FORWARDING_ROUTER);

    static final private int DEFAULT_PORT = 33647;

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
                config.setPort(DEFAULT_PORT);
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
