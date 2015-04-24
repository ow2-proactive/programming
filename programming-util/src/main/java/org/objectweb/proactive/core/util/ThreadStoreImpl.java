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

    private static final long serialVersionUID = 62L;
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
