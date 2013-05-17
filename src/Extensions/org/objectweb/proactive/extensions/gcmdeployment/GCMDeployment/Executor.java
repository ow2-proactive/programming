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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.OperatingSystem;

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;


public class Executor {
    final static private Executor singleton = new Executor();
    private List<Thread> threads;
    private long jobId;

    private Executor() {
        GCMD_LOGGER.trace("Executor started");
        threads = new ArrayList<Thread>();
        jobId = 0;
    }

    static public synchronized Executor getExecutor() {
        return singleton;
    }

    public void submit(String command) {
        Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT + ".job." + jobId);
        jobId++;

        logger.debug("Command submited: " + command);
        try {
            logger.info("executing command=" + command);

            ProcessBuilder pb = null;
            switch (OperatingSystem.getOperatingSystem()) {
                case unix:
                    pb = new ProcessBuilder(CentralPAPropertyRepository.PA_GCMD_UNIX_SHELL.getValue(), "-c",
                        command);
                    break;
                case windows:
                    // if command is passed to the ProcessBuilder as single string it can be corrupted on windows
                    // (see PROACTIVE-1176)
                    String[] tokenizedCommand = tokenizeCommand(command);
                    pb = new ProcessBuilder(tokenizedCommand);
                    break;
            }

            pb.redirectErrorStream(true);
            Process p = pb.start();
            InputStreamMonitor<String> streamMonitor = new InputStreamMonitor<String>(p.getInputStream(),
                command, "" + command.subSequence(0, 100), logger);
            streamMonitor.start();
            threads.add(streamMonitor);
        } catch (IOException e) {
            logger.warn("Cannot execute: " + command, e);
        }
    }

    private static String[] tokenizeCommand(String command) {
        StringTokenizer st = new StringTokenizer(command);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            cmdarray[i] = st.nextToken();
        }
        return cmdarray;
    }

    public void submit(List<List<String>> commandList) {
        Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT + ".job." + jobId);
        jobId++;
        for (int i = 0; i < commandList.size(); i++) {
            List<String> command = commandList.get(i);
            logger.debug("Command submitted: " + command);
            try {
                logger.info("executing command=" + command);

                ProcessBuilder pb = null;
                switch (OperatingSystem.getOperatingSystem()) {
                    case unix:
                        pb = new ProcessBuilder(command);
                        break;
                    case windows:
                        // if command is passed to the ProcessBuilder as single string it can be corrupted on windows 
                        // (see PROACTIVE-1176)
                        // String[] tokenizedCommand = tokenizeCommand(command);
                        pb = new ProcessBuilder(command);
                        break;
                }

                pb.redirectErrorStream(true);
                Process p = pb.start();
                InputStreamMonitor<List<String>> streamMonitor = new InputStreamMonitor<List<String>>(p
                        .getInputStream(), command, "Process" + i, logger);
                streamMonitor.start();
                threads.add(streamMonitor);
            } catch (IOException e) {
                logger.warn("Cannot execute: " + command, e);
            }
        }
    }

    static private class InputStreamMonitor<T> extends Thread {
        InputStream stream;
        T cmd;
        Logger logger;

        public InputStreamMonitor(InputStream stream, T cmd, String name, Logger logger) {
            this.logger = logger;
            logger.trace("Monitor started: " + cmd);
            this.stream = stream;
            this.cmd = cmd;
            setDaemon(true);
            setName("GCM Deployment Monitor for " + name);
        }

        @Override
        public void run() {
            try {
                BufferedReader br;
                String line;

                br = new BufferedReader(new InputStreamReader(stream));
                while ((line = br.readLine()) != null) {
                    logger.info(line);
                }
                logger.trace("Monitor exited: " + cmd);
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }
}
