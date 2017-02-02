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
package org.objectweb.proactive.core.runtime;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.jmx.notification.GCMRuntimeRegistrationNotificationData;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.log.remote.ProActiveAppender;


/**
 * This class is a utility class allowing to start a ProActiveRuntime with a JVM.
 *
 */
public class StartPARuntime {
    Logger logger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    /** The URL of the parent ProActive Runtime */
    private String parentURL;

    /** identify the topology node which created this runtime */
    private long topologyId;

    /** identify the GCM Application */
    private long deploymendId;

    /** Capacity of this runtime */
    private long capacity;

    /** Command Line arguments */
    private String[] args;

    public static void main(String[] args) {
        try {
            ProActiveConfiguration.load();
            StartPARuntime startRuntime = new StartPARuntime(args);
            startRuntime.start();

            if (CentralPAPropertyRepository.PA_RUNTIME_STAYALIVE.isTrue()) {
                startRuntime.waitUntilInterupted();
            }

        } catch (Exception e) {
            e.printStackTrace();
            abort();
        }
    }

    static protected void abort(String str) {
        if ((str != null) && (str.length() > 0)) {
            System.err.println(str);
        }
        System.out.flush();
        System.err.flush();
        System.exit(1);
    }

    static protected void abort() {
        abort("");
    }

    public StartPARuntime(String[] args) {
        this.args = args;
        parseOptions();
    }

    protected void parseOptions() {
        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption(Params.parent.sOpt, Params.parent.toString(), true, Params.parent.desc);
        options.addOption(Params.capacity.sOpt, Params.capacity.toString(), true, Params.capacity.desc);
        options.addOption(Params.deploymentId.sOpt, Params.deploymentId.toString(), true, Params.deploymentId.desc);
        options.addOption(Params.topologyId.sOpt, Params.topologyId.toString(), true, Params.topologyId.desc);

        CommandLine line = null;

        try {
            String arg;

            line = parser.parse(options, args);

            parentURL = line.getOptionValue(Params.parent.sOpt);

            arg = line.getOptionValue(Params.capacity.sOpt);
            if (arg == null) {
                capacity = Runtime.getRuntime().availableProcessors();
                logger.info(capacity + " cores found. Capacity set to " + capacity);
            } else {
                capacity = new Long(arg);
            }

            arg = line.getOptionValue(Params.topologyId.sOpt);
            if (arg != null) {
                topologyId = new Long(arg);
            } else {
                topologyId = -1;
            }

            arg = line.getOptionValue(Params.deploymentId.sOpt);
            if (arg != null) {
                deploymendId = new Long(arg);
            } else {
                deploymendId = -1;
            }

        } catch (ParseException e) {
            logger.warn("Cannot parse command line arguments", e);
            abort();
        } catch (NumberFormatException e) {
            // TODO cmathieu gracefully handle errors
            logger.error("Capacity must be a number: " + line.getOptionValue(Params.capacity.toString()), e);
            abort();
        }
    }

    protected void start() {
        // Print some information message
        logger.info("Starting a ProActiveRuntime on " +
                    URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress()));

        if (logger.isDebugEnabled()) {
            logger.debug("**** Starting jvm with classpath " + System.getProperty("java.class.path"));
            logger.debug("****              with bootclasspath " + System.getProperty("sun.boot.class.path"));
        }

        // Creation & Setup of the local ProActive Runtime
        ProActiveRuntimeImpl localRuntimeImpl = ProActiveRuntimeImpl.getProActiveRuntime();
        localRuntimeImpl.setDeploymentId(deploymendId);
        localRuntimeImpl.setTopologyId(topologyId);
        localRuntimeImpl.setCapacity(capacity);

        ProActiveRuntime localRuntime = null;
        try {
            localRuntime = RuntimeFactory.getProtocolSpecificRuntime(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
        } catch (ProActiveException e1) {
            logger.warn("Cannot get the local ProActive Runtime", e1);
            abort();
        }

        ProActiveAppender.notifyIsReady();

        // Say hello to our parent if needed
        if (parentURL != null) {
            ProActiveRuntime parentRuntime;
            try {
                parentRuntime = RuntimeFactory.getRuntime(parentURL);

                localRuntimeImpl.setParent(parentRuntime);

                // Register
                GCMRuntimeRegistrationNotificationData notification = new GCMRuntimeRegistrationNotificationData(localRuntime,
                                                                                                                 deploymendId,
                                                                                                                 topologyId);
                parentRuntime.register(notification);

                waitUntilInterupted();
            } catch (ProActiveException e) {
                logger.warn("Cannot register to my parent", e);
                abort();
            }
        }

        if (CentralPAPropertyRepository.PA_RUNTIME_STAYALIVE.isTrue()) {
            waitUntilInterupted();
        }
    }

    public void waitUntilInterupted() {
        Object o = new Object();
        synchronized (o) {
            try {
                o.wait();
            } catch (InterruptedException e) {
                logger.info(e);
            }
        }
    }

    public enum Params {
        parent("p", "URL of the parent ProActive Runtime"),
        topologyId("i", "Topology Node"),
        deploymentId("d", "GCM Application identifier"),
        capacity("c", "Number of Node to be created");
        protected String sOpt;

        protected String desc;

        private Params(String sOpt, String desc) {
            this.sOpt = sOpt;
            this.desc = desc;
        }

        public String shortOpt() {
            return sOpt;
        }

        public String longOpt() {
            return toString();
        }
    }
}
