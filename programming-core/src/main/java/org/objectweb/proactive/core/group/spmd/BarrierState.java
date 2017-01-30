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
package org.objectweb.proactive.core.group.spmd;

import java.io.Serializable;


/**
 * This class describes the state of a barrier.
 *
 * @author The ProActive Team
 */
public class BarrierState implements Serializable {

    /** The number of calls awaited to finish the barrier */
    private int awaitedCalls = 0;

    /** The number of calls already received */
    private int receivedCalls = 0;

    /**
     * Returns the number of awaited calls to finish the barrier
     * @return the number of awaited calls to finish the barrier
     */
    public int getAwaitedCalls() {
        return this.awaitedCalls;
    }

    /**
     * Returns the number of received calls to finish the barrier
     * @return the number of received calls to finish the barrier
     */
    public int getReceivedCalls() {
        return this.receivedCalls;
    }

    /**
     * Sets the number of calls need to finish the barrier
     * @param nbCalls the number of calls need to finish the barrier
     */
    public void setAwaitedCalls(int nbCalls) {
        this.awaitedCalls = nbCalls;
    }

    /**
     * Increments the number of received calls to finish the barrier
     */
    public void incrementReceivedCalls() {
        this.receivedCalls++;
    }
}
