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
package org.objectweb.proactive.extensions.masterworker.interfaces;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.extensions.masterworker.TaskException;


/**
 * Interface describing the aspects of task submission and results collection only
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface SubMaster<T extends Task<R>, R extends Serializable> {

    /**
     * Reception order mode. Results can be received in Completion Order (the default) or Submission Order
     * @author The ProActive Team
     *
     */
    public enum OrderingMode {
        /**
         * Results of tasks are received in the same order as tasks were submitted
         */
        SubmitionOrder,
        /**
         * Results of tasks are received in the same order as tasks are completed (unspecified)
         */
        CompletionOrder
    }

    /**
     * Results of tasks are received in the same order as tasks were submitted
     */
    public OrderingMode SUBMISSION_ORDER = OrderingMode.SubmitionOrder;

    /**
     * Results of tasks are received in the same order as tasks are completed (unspecified)
     */
    public OrderingMode COMPLETION_ORDER = OrderingMode.CompletionOrder;

    //@snippet-start masterworker_order
    /**
     * Sets the current ordering mode <br/>
     * If reception mode is switched while computations are in progress,<br/>
     * then subsequent calls to waitResults methods will be done according to the new mode.<br/>
     * @param mode the new mode for result gathering
     */
    void setResultReceptionOrder(OrderingMode mode);

    //@snippet-end masterworker_order

    //@snippet-start masterworker_solve
    /**
     * Adds a list of tasks to be solved by the master <br/>
     * <b>Warning</b>: the master keeps a track of task objects that have been submitted to it and which are currently computing.<br>
     * Submitting two times the same task object without waiting for the result of the first computation is not allowed.
     * @param tasks list of tasks
     */
    void solve(List<T> tasks);

    //@snippet-end masterworker_solve
    //@snippet-start masterworker_collection
    /**
     * Wait for all results, will block until all results are computed <br>
     * The ordering of the results depends on the result reception mode in use <br>
     * @return a collection of objects containing the result
     * @throws org.objectweb.proactive.extensions.masterworker.TaskException if a task threw an Exception
     */
    List<R> waitAllResults() throws TaskException;

    /**
     * Wait for the first result available <br>
     * Will block until at least one Result is available. <br>
     * Note that in SubmittedOrder mode, the method will block until the next result in submission order is available<br>
     * @return an object containing the result
     * @throws TaskException if the task threw an Exception
     */
    R waitOneResult() throws TaskException;

    /**
     * Wait for at least one result is available <br>
     * If there are more results availables at the time the request is executed, then every currently available results are returned 
     * Note that in SubmittedOrder mode, the method will block until the next result in submission order is available and will return
     * as many successive results as possible<br>
     * @return a collection of objects containing the results
     * @throws TaskException if the task threw an Exception
     */
    List<R> waitSomeResults() throws TaskException;

    /**
     * Wait for a number of results<br>
     * Will block until at least k results are available. <br>
     * The ordering of the results depends on the result reception mode in use <br>
     * @param k the number of results to wait for
     * @return a collection of objects containing the results
     * @throws TaskException if the task threw an Exception
     */
    List<R> waitKResults(int k) throws TaskException;

    /**
     * Tells if the master is completely empty (i.e. has no result to provide and no tasks submitted)
     * @return the answer
     */
    boolean isEmpty();

    /**
     * Returns the number of available results <br/>
     * @return the answer
     */
    int countAvailableResults();

    //@snippet-end masterworker_collection
}
