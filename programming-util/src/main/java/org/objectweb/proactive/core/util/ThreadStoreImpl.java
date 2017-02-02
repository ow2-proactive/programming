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
 * A straightford implementation of the threadstore interface.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/05
 * @since   ProActive 0.9.2
 */

public class ThreadStoreImpl implements ThreadStore, java.io.Serializable {
    private int counter;

    private boolean defaultOpenState;

    private transient boolean open;

    /**
     * Creates a new ThreadStore that is opened after creation.
     */
    public ThreadStoreImpl() {
        this(true);
    }

    /**
     * Constructor for ThreadStoreImpl.
     * @param isOpened true is the store is opened after creation
     */
    public ThreadStoreImpl(boolean isOpened) {
        defaultOpenState = isOpened;
        open = defaultOpenState;
    }

    /**
     * @see ThreadStore#threadCount()
     */
    public int threadCount() {
        return counter;
    }

    /**
     * @see ThreadStore#enter()
     */
    public synchronized void enter() {
        while (!open) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
        counter++;
    }

    /**
     * @see ThreadStore#exit()
     */
    public synchronized void exit() {
        counter--;
        notifyAll();
    }

    /**
     * @see ThreadStore#close()
     */
    public synchronized void close() {
        open = false;
        while ((counter != 0) && !open) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * @see ThreadStore#open()
     */
    public synchronized void open() {
        open = true;
        notifyAll();
    }

    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // set open to the default value
        open = defaultOpenState;
    }
}
