/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.masterworker.interfaces.internal;

import org.objectweb.proactive.core.util.wrapper.BooleanWrapper;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * A WorkerWatcher is responsible of watching workers'activity<br/>
 * @author The ProActive Team
 */
public interface WorkerWatcher {

    /**
     * adds a worker to be watched
     * @param worker worker which must be watched
     */
    void addWorkerToWatch(Worker worker);

    /**
     * stops watching a worker
     * @param worker workers which needn't be watched anymore
     */
    void removeWorkerToWatch(Worker worker);

    /**
     * Sets the period at which ping messages are sent to the workers <br/>
     * @param periodMillis the new ping period
     */
    void setPingPeriod(long periodMillis);

    /**
     * terminates the watcher's activity
     * @return true if the object terminated successfully
     */
    BooleanWrapper terminate();
}
