/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.util.wrapper;

import java.io.Serializable;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <p>A reifiable object for wrapping the Java type <code>Object</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author The ProActive Team
 */
@PublicAPI
public class GenericTypeWrapper<T extends Object> implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    /**
     *
     */
    private T o;

    /**
     * Empty no args Constructor
     *
     */
    public GenericTypeWrapper() {
    }

    /**
     * Constructor for  the wrapper
     * @param o the object to wrap
     */
    public GenericTypeWrapper(T o) {
        this.o = o;
    }

    /**
     * Retrieves the wrapp object
     * @return the wrapped object
     */
    public T getObject() {
        return this.o;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg) {
        if (arg instanceof GenericTypeWrapper) {
            return ((GenericTypeWrapper) arg).getObject().equals(this.o);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.o.hashCode();
    }
}
