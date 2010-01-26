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
package org.objectweb.proactive.extensions.masterworker.core;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.Worker;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerDeadListener;
import org.objectweb.proactive.extensions.masterworker.interfaces.internal.WorkerWatcher;
import org.objectweb.proactive.extensions.annotation.ActiveObject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * The Pinger Active Object is responsible for watching workers'activity. <br>
 * It reports workers failure to the Master<br>
 *
 * @author The ProActive Team
 */
@ActiveObject
public class AOPinger implements WorkerWatcher, RunActive, InitActive, Serializable {

    /**
     *
     */

    /** pinger log4j logger */
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.MASTERWORKER_WORKERS);
    private static final boolean debug = logger.isDebugEnabled();

    /** Stub on the active object */
    private AOPinger stubOnThis;

    /** is this active object terminated */
    private boolean terminated;

    /** interval when workers are sent a ping message */
    private long pingPeriod;

    /** Who will be notified when workers are dead (in general : the master) */
    private WorkerDeadListener listener;

    /** Worker group */
    private Set<Worker> workerGroup = null;

    /** for internal use */
    private transient Thread localThread;

    /** ProActive empty constructor */
    @Deprecated
    public AOPinger() {
    }

    /**
     * Creates a pinger with the given listener
     *
     * @param listener object which will be notified when a worker is dead
     */
    public AOPinger(final WorkerDeadListener listener) {
        this.listener = listener;
        terminated = false;
        pingPeriod = Long.parseLong(PAProperties.PA_MASTERWORKER_PINGPERIOD.getValue());

        workerGroup = new HashSet<Worker>();
    }

    /** {@inheritDoc} */
    public void addWorkerToWatch(final Worker worker) {
        workerGroup.add(worker);
    }

    /** {@inheritDoc} */
    public void initActivity(final Body body) {

        stubOnThis = (AOPinger) PAActiveObject.getStubOnThis();
        body.setImmediateService("terminate", false);

    }

    /** {@inheritDoc} */
    public void removeWorkerToWatch(final Worker worker) {
        workerGroup.remove(worker);
    }

    /** {@inheritDoc} */
    public void runActivity(final Body body) {
        localThread = Thread.currentThread();
        Service service = new Service(body);
        while (!terminated) {
            try {

                long checkpoint1 = System.currentTimeMillis();
                ArrayList<Worker> workers = new ArrayList<Worker>(workerGroup);
                for (Worker worker : workers) {
                    try {
                        if (debug) {
                            logger.debug("Pinging " + worker.getName());
                        }
                        worker.heartBeat();
                    } catch (Exception e) {
                        if (debug) {
                            logger.debug("Misfunctioning worker, investigating...");
                        }
                        stubOnThis.workerMissing(worker);
                    }
                }

                long checkpoint2 = System.currentTimeMillis();
                if (pingPeriod > (checkpoint2 - checkpoint1)) {
                    Thread.sleep(pingPeriod - (checkpoint2 - checkpoint1));
                }

                // we serve everything
                while (service.hasRequestToServe()) {
                    service.serveOldest();
                }
            } catch (InterruptedException ex) {
                // do not print message, pinger is terminating

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (debug) {
            logger.debug("Pinger Terminated...");
        }

        // we clear the service to avoid dirty pending requests
        service.flushAll();
        // we block the communications because a getTask request might still be coming from a worker created just before the master termination
        body.blockCommunication();
        // we finally terminate the pinger
        body.terminate();
    }

    /** {@inheritDoc} */
    public void setPingPeriod(long periodMillis) {
        this.pingPeriod = periodMillis;
    }

    /**
     * Reports that a worker is missing
     *
     * @param worker the missing worker
     */
    public void workerMissing(final Worker worker) {
        synchronized (workerGroup) {
            if (debug) {
                logger.debug("A worker is missing...reporting back to the Master");
            }

            if (workerGroup.contains(worker)) {
                listener.isDead(worker);
                workerGroup.remove(worker);
            }
        }
    }

    /** {@inheritDoc} */
    public BooleanWrapper terminate() {
        if (debug) {
            logger.debug("Terminating Pinger...");
        }
        workerGroup.clear();
        workerGroup = null;
        this.terminated = true;
        localThread.interrupt();
        localThread = null;

        stubOnThis = null;

        return new BooleanWrapper(true);
    }

}
