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
package org.objectweb.proactive.core.group.spmd;

import java.io.Serializable;


/**
 * This class describes the state of a barrier.
 *
 * @author The ProActive Team
 */
public class BarrierState implements Serializable {

    private static final long serialVersionUID = 61L;

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
