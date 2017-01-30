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
package org.objectweb.proactive.extensions.pnp;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections4.queue.CircularFifoQueue;


/**
 * Dynamic heartbeat timeout mechanism
 */
public class PNPTimeoutHandler {

    private final long defaultHearbeatPeriod;

    private final int heartbeatFactor;

    private final CircularFifoQueue<Long> lastRecordedHeartBeatIntervals;

    private long lastHeartBeat = 0;

    private long lastInterval = 0;

    private boolean heartbeatReceived = false;

    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final ReentrantReadWriteLock.ReadLock readLock = rwLock.readLock();

    private final ReentrantReadWriteLock.WriteLock writeLock = rwLock.writeLock();

    public PNPTimeoutHandler(long defaultHearbeatPeriod, int heartbeatFactor, int heartbeatWindow) {
        this.defaultHearbeatPeriod = defaultHearbeatPeriod;
        this.heartbeatFactor = heartbeatFactor;
        this.lastRecordedHeartBeatIntervals = new CircularFifoQueue<>(heartbeatWindow);
    }

    /**
     * Returns the computed timeout based on last received heartbeats intervals
     *
     * @return
     */
    public long getTimeout() {
        readLock.lock();
        try {
            if (defaultHearbeatPeriod == 0 || lastInterval == 0)
                return heartbeatFactor * defaultHearbeatPeriod;
            else {
                long averageInterval = computeAverageInterval();
                if (lastInterval > averageInterval)
                    return heartbeatFactor * Math.max(defaultHearbeatPeriod, lastInterval);
                else
                    return heartbeatFactor * Math.max(defaultHearbeatPeriod, averageInterval);
            }
        } finally {
            readLock.unlock();
        }
    }

    private long computeAverageInterval() {
        long sum = 0;
        for (long interval : lastRecordedHeartBeatIntervals) {
            sum += interval;
        }
        return Math.round(sum / lastRecordedHeartBeatIntervals.size());
    }

    public void resetNotification() {
        writeLock.lock();
        try {
            heartbeatReceived = false;
        } finally {
            writeLock.unlock();
        }
    }

    public HeartBeatNotificationData getNotificationData() {
        readLock.lock();
        try {
            return new HeartBeatNotificationData(heartbeatReceived, lastInterval, getTimeout());
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Registers a heartbeat arrival
     *
     * @param time system time when the heartbeat was received
     */
    public void recordHeartBeat(long time) {
        writeLock.lock();
        try {
            heartbeatReceived = true;
            if (lastHeartBeat == 0) {
                lastHeartBeat = time;
                return;
            }
            lastInterval = time - lastHeartBeat;
            lastRecordedHeartBeatIntervals.add(lastInterval);
            lastHeartBeat = time;

        } finally {
            writeLock.unlock();
        }
    }

    public static class HeartBeatNotificationData {

        private boolean heartBeatReceived;

        private long lastHeartbeatInterval;

        private long newTimeout;

        public boolean heartBeatReceived() {
            return heartBeatReceived;
        }

        public long getLastHeartBeatInterval() {
            return lastHeartbeatInterval;
        }

        public long getTimeout() {
            return newTimeout;
        }

        public HeartBeatNotificationData(boolean heartBeatReceived, long lastHeartbeatInterval, long newTimeout) {
            this.heartBeatReceived = heartBeatReceived;
            this.lastHeartbeatInterval = lastHeartbeatInterval;
            this.newTimeout = newTimeout;
        }
    }
}
