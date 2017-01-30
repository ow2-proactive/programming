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
package org.objectweb.proactive.core.util;

/**
 * <p>
 * A class implementing this interface provides a sort of store where threads like
 * customers can enter and exit. The store can open and close.
 * The rule are that no thread can enter when the store is closed and that the
 * store cannot close until all threads already inside exit.
 * Like in any other stores, once the store is closing, no more Thread can enter.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */
public interface ThreadStore {

    /**
     * Returns how many threads are in the store. This method is non blocking.
     * @return how many threads are in the store.
     */
    public int threadCount();

    /**
     * Signals that a thread wants to enter the store. If the store is opened
     * the call is non blocking.
     * If the store is closed or closing the call is blocking until the store
     * opens.
     */
    public void enter();

    /**
     * Signals that a thread exited the store. The call is non blocking.
     */
    public void exit();

    /**
     * Closes the store. The call is blocking until all threads
     * currently in the store exit. No other thread can enter the store
     * after this call.
     * Therefore the store can be closing or closed.
     */
    public void close();

    /**
     * Opens the store. The call is non blocking. It allows the thread waiting to
     * enter the store to proceed.
     */
    public void open();
}
