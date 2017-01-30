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
package org.objectweb.proactive.core.util.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <p>A reifiable object for wrapping the Java type <code>Object</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author The ProActive Team
 */
@PublicAPI
@XmlRootElement
public class GenericTypeWrapper<T extends Object> implements Serializable {

    /**
     *
     */
    private T object;

    /**
     * Empty no args Constructor
     *
     */
    public GenericTypeWrapper() {
    }

    /**
     * Constructor for  the wrapper
     * @param object the object to wrap
     */
    public GenericTypeWrapper(T o) {
        this.object = o;
    }

    /**
     * Retrieves the wrapp object
     * @return the wrapped object
     */
    public T getObject() {
        return this.object;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg) {
        if (arg instanceof GenericTypeWrapper) {
            return ((GenericTypeWrapper) arg).getObject().equals(this.object);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.object.hashCode();
    }
}
