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
package org.objectweb.proactive.extensions.masterworker.util;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Queue;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * This collection provides both Set and Queue functionnalities <br/>
 * @author The ProActive Team
 *
 * @param <T> type of the elements contained in the Queue
 */
public class HashSetQueue<T> extends LinkedHashSet<T> implements Queue<T> {

    /**
     *
     */

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Queue#element()
     */
    public T element() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Iterator<T> it = iterator();
        return it.next();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Queue#offer(java.lang.Object)
     */
    public boolean offer(T o) {
        return add(o);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Queue#peek()
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        Iterator<T> it = iterator();
        return it.next();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Queue#poll()
     */
    public T poll() {
        if (isEmpty()) {
            return null;
        }
        Iterator<T> it = iterator();
        T t = it.next();
        it.remove();
        return t;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Queue#remove()
     */
    public T remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        Iterator<T> it = iterator();
        T t = it.next();
        it.remove();
        return t;
    }
}
