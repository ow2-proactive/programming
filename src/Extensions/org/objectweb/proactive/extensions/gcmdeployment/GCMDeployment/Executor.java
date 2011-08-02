/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import static org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers.GCMD_LOGGER;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.OperatingSystem;


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
                    pb = new ProcessBuilder(command);
                    break;
            }

            pb.redirectErrorStream(true);
            Process p = pb.start();
            InputStreamMonitor streamMonitor = new InputStreamMonitor(p.getInputStream(), command, logger);
            streamMonitor.start();
            threads.add(streamMonitor);
        } catch (IOException e) {
            logger.warn("Cannot execute: " + command, e);
        }
    }

    static private class InputStreamMonitor extends Thread {
        InputStream stream;
        String cmd;
        Logger logger;

        public InputStreamMonitor(InputStream stream, String cmd, Logger logger) {
            this.logger = logger;
            logger.trace("Monitor started: " + cmd);
            this.stream = stream;
            this.cmd = cmd;
            setDaemon(true);
            setName("GCM Deployment Monitor for " + cmd.subSequence(0, 100));
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
