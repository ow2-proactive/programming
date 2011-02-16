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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


/**
 * Connection checker client
 * 
 * Clients connect to the server, then periodically send a packet 
 * 
 * @author ProActive team
 * @since  ProActive 5.1.0
 */
public class Client {
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("H", "host", true, "Remote IP address");
        options.addOption("p", "port", true, "Remote TCP port");
        options.addOption("s", "size", true, "Packet size in bytes (1 KiB)");
        options.addOption("f", "freq", true, "Packets per seconds  (1 Hz)");
        options.addOption("w", "workers", true, "Number of Workers (1)");
        options.addOption("d", "duration", true, "Duration of the test in seconds (60 s)");
        options.addOption("h", "help", false, "Print help");
        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption("h")) {
                HelpFormatter hf = new HelpFormatter();
                hf.printHelp("client", options);
                System.exit(0);
            }

            // host
            InetAddress ia = null;
            if (cmd.hasOption("H")) {
                String host = cmd.getOptionValue("H");

                try {
                    ia = InetAddress.getByName(host);
                } catch (UnknownHostException e) {
                    printAndExit("Unknown host: " + host);
                }
            } else {
                printAndExit("Host option is mandatory");
            }

            // port
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

            // size
            int size = 1024;
            if (cmd.hasOption("s")) {
                try {
                    size = Integer.parseInt(cmd.getOptionValue("s"));
                } catch (NumberFormatException e) {
                    printAndExit("Invalid packet size " + cmd.getOptionValue("s"));
                }

                if (size < 0) {
                    printAndExit("Invalid packet size: " + port);
                }
            }

            // freq
            double freq = 1;
            if (cmd.hasOption("f")) {
                try {
                    freq = Double.parseDouble(cmd.getOptionValue("f"));
                } catch (NumberFormatException e) {
                    printAndExit("Invalid frequency: " + cmd.getOptionValue("f"));
                }

                if (freq <= 0) {
                    printAndExit("Invalid frequency: " + freq);
                }
            }

            // workers
            int workers = 1;
            if (cmd.hasOption("w")) {
                try {
                    workers = Integer.parseInt(cmd.getOptionValue("w"));
                } catch (NumberFormatException e) {
                    printAndExit("Invalid number of workers: " + cmd.getOptionValue("w"));
                }

                if (workers < 0) {
                    printAndExit("Invalid number of workers: " + workers);
                }
            }

            // duration
            int duration = 60000;
            if (cmd.hasOption("d")) {
                try {
                    duration = Integer.parseInt(cmd.getOptionValue("d")) * 1000;
                } catch (NumberFormatException e) {
                    printAndExit("Invalid duration: " + cmd.getOptionValue("d"));
                }

                if (duration < 0) {
                    printAndExit("Invalid duration: " + duration);
                }
            }

            Client client = new Client(ia, port, size, freq, workers, duration);
            client.doit();
        } catch (ParseException e) {
            printAndExit("Failed to parse options: " + e.getMessage());
        }

        System.exit(0);
    }

    private static void printAndExit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }

    final InetAddress ia;
    final int port;
    final Worker[] workers;
    final byte[] packet;
    final long sleepTime;
    final long duration;

    public Client(InetAddress ia, int port, int psize, double freq, int workers, long duration) {
        this.ia = ia;
        this.port = port;
        this.workers = new Worker[workers];
        this.packet = new byte[psize];
        for (int i = 0; i < psize; i++) {
            this.packet[i] = (byte) i;
        }
        this.sleepTime = (long) (1000.0 / freq);
        this.duration = duration;
    }

    private void doit() {
        // Check we can connect to the server
        try {
            Socket s = new Socket(this.ia, this.port);
            s.close();
        } catch (IOException e) {
            printAndExit("Failed to contact " + this.ia + " " + this.port);
        }

        // Start the workers
        for (int i = 0; i < this.workers.length; i++) {
            try {
                workers[i] = new Worker(i);
                workers[i].start();
            } catch (Exception e) {
                System.out.println("[ERROR] worker#" + i + ": failed to start thread -> " + e.getMessage());
                System.exit(1);
            }
        }

        try {
            Thread.sleep(this.duration);
        } catch (InterruptedException e) {
            // Ok
        }

        for (int i = 0; i < this.workers.length; i++) {
            workers[i].terminate();
        }

        System.out.println("Exiting");
    }

    private class Worker extends Thread {
        volatile boolean terminate;

        final int index;
        final Socket s;
        final InputStream in;
        final OutputStream out;

        public Worker(int index) throws IOException {
            this.index = index;
            this.setName("Worker " + index);
            this.s = new Socket(ia, port);
            this.in = this.s.getInputStream();
            this.out = this.s.getOutputStream();

            this.terminate = false;
        }

        @Override
        public void run() {
            while (!terminate) {
                try {
                    this.out.write(packet);
                } catch (IOException e) {
                    reportError("Failed to send packet to the server");
                    break;
                }

                try {
                    byte[] buf = new byte[packet.length];
                    int read = 0;
                    while (read < buf.length) {
                        int retVal = in.read(buf, read, buf.length - read);
                        if (retVal == -1) {
                            reportError("Failed to read full response from the server " +
                                (buf.length - read) + " bytes miss");
                            break;
                        }
                        read += retVal;
                    }

                    if (!Arrays.equals(packet, buf)) {
                        reportError("Received corrupted packet");
                        break;
                    }
                } catch (IOException e) {
                    reportError("Read failed", e);
                    break;
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // Ok
                }
            }
        }

        private void reportError(String msg) {
            this.reportError(msg, null);
        }

        private void reportError(String msg, Exception e) {
            StringBuilder sb = new StringBuilder();
            sb.append("[ERROR] worker# " + this.index + ": ");
            sb.append(msg);
            if (e != null) {
                sb.append(" -> " + e.getMessage());
            }
            System.out.println(sb.toString());

            this.terminate();
        }

        private void terminate() {
            this.terminate = true;
        }
    }
}
