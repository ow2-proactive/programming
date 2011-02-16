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
 * $ACTIVEEON_INITIAL_DEV$
 */
package cnxchecker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Connection checker server
 * 
 * The server binds to a given TCP port and waits for client. All the bytes received are
 * sent back to the client (echo protocol). If something goes wrong an error message is 
 * printed on the standard output.
 * 
 * @author ProActive team
 * @since  ProActive 5.1.0
 */
public class Server {

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("p", "port", true, "TCP port to bind to (random by default)");
        options.addOption("h", "help", false, "Print help");

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("server", options);
                System.exit(0);
            }

            int port = 0;
            if (cmd.hasOption("p")) {
                try {
                    port = Integer.parseInt(cmd.getOptionValue("p"));
                } catch (NumberFormatException e) {
                    printAndExit("Invalid port number " + cmd.getOptionValue("p"));
                }

                if (port < 0 || port > 65535) {
                    printAndExit("Invalid port number " + port);
                }
            }

            Server server = new Server(port);
            server.doit();
        } catch (ParseException e) {
            printAndExit("Failed to parse options: " + e.getMessage());
        }
    }

    private static void printAndExit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    final int port;

    public Server(int port) {
        this.port = port;
    }

    public void doit() {
        try {
            ServerSocket ss = new ServerSocket(port);
            System.out.println("[INFO] Listenning on " + ss);
            while (true) {
                try {
                    Socket s = ss.accept();
                    Thread handler = new ClientHandler(s);
                    handler.start();
                } catch (IOException e) {
                    printAndExit("Failed to accept a new client: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            printAndExit("Failed to bind to " + port + ": " + e.getMessage());
        }
    }

    static class ClientHandler extends Thread {
        final Socket s;
        final InputStream in;
        final OutputStream out;
        final String remote;

        public ClientHandler(final Socket s) throws IOException {
            this.s = s;
            this.setName("Handler for " + s);
            this.in = this.s.getInputStream();
            this.out = this.s.getOutputStream();
            this.remote = this.s.getRemoteSocketAddress().toString();
        }

        @Override
        public void run() {
            int totalBytes = 0;
            long start = System.currentTimeMillis();

            while (true) {
                byte[] buf = new byte[4096];
                int ret;
                try {
                    ret = this.in.read(buf);
                    if (ret == -1) {
                        System.out.println("[INFO] Remote client " + this.remote + " disconnected");
                        break;
                    } else {
                        try {
                            this.out.write(buf, 0, ret);
                            totalBytes += ret;
                        } catch (IOException e) {
                            System.out.println("[ERROR] Failed to write to " + this.remote + " -> " +
                                e.getMessage());
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.out
                            .println("[ERROR] Failed to read from " + this.remote + " -> " + e.getMessage());
                    break;
                }
            }

            double throughput = ((1.0 * totalBytes) / (System.currentTimeMillis() - start));
            System.out.printf("[INFO]: Thread for %s exiting. Throughput: %.2f KiB/s\n", this.remote,
                    throughput);

        }
    }
}
