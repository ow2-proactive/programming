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
package org.objectweb.proactive.extensions.calcium.futures;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.calcium.exceptions.MuscleException;
import org.objectweb.proactive.extensions.calcium.exceptions.TaskException;
import org.objectweb.proactive.extensions.calcium.statistics.Stats;


/**
 * This class represents a future result on a parameter submitted to the Calcium framework via a {@link org.objectweb.proactive.extensions.calcium.Stream Stream}.
 *
 * The ProActive Team
 */
@PublicAPI
public interface CalFuture<R> {

    /**
     * @return true if the result is available
     */
    public boolean isDone();

    /**
     * Waits if necessary for the computation to complete, and then retrieves its result.
     *
     * @return the computed result
     *
     * @throws InterruptedException
     * @throws MuscleException If an exception was thrown by a muscle code
     * @throws TaskException If an internal exception was thrown by Calcium.
     */
    public R get() throws InterruptedException, MuscleException, TaskException;

    /**
     * Retrieves the computation statistics.
     *
     * @return <code>null</code> if the result is not available. The stats otherwise.
     * @throws InterruptedException
     */
    public Stats getStats();
}
