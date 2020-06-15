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
package org.objectweb.proactive.core.body.future;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.exceptions.FutureMonitoringPingFailureException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.ActiveObjectLocationInfo;
import org.objectweb.proactive.core.util.HeartbeatResponse;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class FutureMonitoring implements Runnable {

    /** Ping one body every 15s */
    private static int TTM = 15000;

    /**
     * For each body, the list of futures to monitor. We ping the updater body,
     * so we should detect a broken automatic continuations chain.
     */
    private static final Map<UniqueID, ConcurrentLinkedQueue<FutureProxy>> futuresToMonitor = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<UniqueID, String> nodeUrls = new ConcurrentHashMap<>();

    static final Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
    static {

        /* Dynamically configurable to make shorter tests */
        String ttm = CentralPAPropertyRepository.PA_FUTUREMONITORING_TTM.getValueAsString();
        if (ttm != null) {
            int tmp = Integer.parseInt(ttm);
            if (tmp >= 0) {
                TTM = tmp;
            } else {
                logger.error(CentralPAPropertyRepository.PA_FUTUREMONITORING_TTM.getName() +
                             " must be positive. This value is ignored");
            }
        }

        if (TTM > 0) {
            Thread t = new Thread(new FutureMonitoring(), "Monitoring the Futures");
            t.setDaemon(true);
            t.start();
        } else {
            logger.info("Future Monitoring is disabled");
        }
    }

    /** To avoid copy-pasting */
    private static void monitoringDelay() {
        try {
            Thread.sleep(TTM);
        } catch (InterruptedException e) {
            logger.warn("Monitoring interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * @return true iff a body has been pinged
     */
    private static boolean pingBody(UniqueID bodyId) {
        boolean pinged = false;
        String nodeUrl = nodeUrls.get(bodyId);

        Collection<FutureProxy> futureProxies = futuresToMonitor.get(bodyId);

        if (futureProxies != null) {
            pinged = pingBodyAndUpdateFutures(futureProxies, bodyId, nodeUrl);
        }

        return pinged;
    }

    private static boolean pingBodyAndUpdateFutures(Collection<FutureProxy> futures, UniqueID bodyId, String nodeUrl) {
        boolean pinged = false;
        FutureMonitoringPingFailureException bodyException = null;
        for (FutureProxy fp : futures) {
            if (!pinged) {

                /* Not yet pinged somebody */
                UniversalBody remoteBody = null;

                synchronized (fp) {
                    if (fp.isAwaited()) {
                        if (fp.getCreatorID().equals(bodyId)) {
                            remoteBody = fp.getCreator();
                        } else {
                            remoteBody = fp.getUpdater();
                        }
                    }
                }
                if (remoteBody != null) {
                    /* OK, Found somebody to ping */
                    pinged = true;
                    try {
                        Integer state = (Integer) remoteBody.receiveHeartbeat();
                        /* If the object is dead, ping failed ... */
                        if (state.equals(HeartbeatResponse.IS_DEAD)) {
                            throw new ProActiveRuntimeException("Awaited body " + bodyId + " on " + nodeUrl +
                                                                " has been terminated.");
                        }
                        /* Successful ping, nothing more to do */
                        return true;
                    } catch (Exception e) {
                        /*
                         * Ping failure, update all awaited futures on this node with the exception
                         */
                        bodyException = new FutureMonitoringPingFailureException(bodyId, nodeUrl, e);
                    }
                }
            }

            if (bodyException != null) {
                synchronized (fp) {
                    if (fp.isAwaited()) {
                        fp.receiveReply(new MethodCallResult(null, bodyException));
                    }
                }
            }
        }
        return pinged;
    }

    /**
     * Arrange to ping a single body every TTM
     * There is a single daemon thread running the monitoring, so
     * it will end with the JVM.
     */
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (UniqueID bodyId : futuresToMonitor.keySet()) {
                pingBody(bodyId);
            }
            monitoringDelay();
        }
    }

    private static UniqueID getUpdaterBodyId(FutureProxy fp) {
        UniversalBody body = fp.getUpdater();
        if (body == null) {
            logger.error("", new Exception("Cannot monitor this future, unknown updater body"));
            return null;
        }
        return body.getID();
    }

    private static UniqueID getCreatorBodyId(FutureProxy fp) {
        UniversalBody body = fp.getCreator();
        if (body == null) {
            logger.error("", new Exception("Cannot monitor this future, unknown updater body"));
            return null;
        }
        return body.getID();
    }

    private static ActiveObjectLocationInfo getLocationInfo(UniversalBody body) {
        if (body == null) {
            logger.error("", new Exception("Cannot monitor this future, unknown updater body"));
            return null;
        }
        UniqueID id = body.getID();
        String nodeUrl = "[unknown]";
        try {
            nodeUrl = body.getNodeURL();
        } catch (Throwable e) {

        }
        return new ActiveObjectLocationInfo(id, nodeUrl);
    }

    public static void removeFuture(FutureProxy fp) {
        UniqueID updaterId = getUpdaterBodyId(fp);
        if (updaterId == null) {
            return;
        }
        UniqueID creatorId = getCreatorBodyId(fp);
        if (creatorId == null) {
            return;
        }
        removeBodyId(fp, updaterId, futuresToMonitor);
        removeBodyId(fp, creatorId, futuresToMonitor);
    }

    private static void removeBodyId(FutureProxy fp, UniqueID bodyId,
            Map<UniqueID, ConcurrentLinkedQueue<FutureProxy>> monitors) {
        synchronized (monitors) {
            /*
             * Avoid a race with monitorFutureProxy(FutureProxy)
             */
            ConcurrentLinkedQueue<FutureProxy> futures = monitors.get(bodyId);
            if (futures != null) {
                futures.remove(fp);
                if (futures.isEmpty()) {
                    monitors.remove(bodyId);
                    nodeUrls.remove(bodyId);
                }
            }
        }
    }

    public static void monitorFutureProxy(FutureProxy fp) {
        if (fp.isAvailable()) {
            return;
        }
        ActiveObjectLocationInfo infoUpdater = getLocationInfo(fp.getUpdater());
        monitorActiveObject(fp, infoUpdater, futuresToMonitor);
        ActiveObjectLocationInfo infoCreator = getLocationInfo(fp.getCreator());
        monitorActiveObject(fp, infoCreator, futuresToMonitor);
    }

    private static void monitorActiveObject(FutureProxy fp, ActiveObjectLocationInfo locationInfo,
            Map<UniqueID, ConcurrentLinkedQueue<FutureProxy>> monitors) {
        UniqueID bodyId = locationInfo.getBodyId();
        if (bodyId == null) {
            return;
        }
        String nodeUrl = locationInfo.getNodeUrl();
        synchronized (monitors) {
            /*
             * Avoid a race with the suppression in the ConcurrentHashMap when the
             * ConcurrentLinkedQueue is empty.
             */
            ConcurrentLinkedQueue<FutureProxy> futures = monitors.get(bodyId);
            if (futures == null) {
                futures = new ConcurrentLinkedQueue<>();
                monitors.put(bodyId, futures);
                nodeUrls.put(bodyId, nodeUrl);
            }
            if (!futures.contains(fp)) {
                futures.add(fp);
            }
        }
    }
}
