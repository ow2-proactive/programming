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
 * <p>An reifiable object for wrapping the primitive Java type <code>float</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author The ProActive Team
 *
 * Created on Jul 28, 2005
 */
@PublicAPI
@XmlRootElement
public class FloatWrapper implements Serializable {

    /**
     * The primitive value.
     */
    protected Float floatValue;

    /**
     * The no arguments constructor for ProActive.
     */
    public FloatWrapper() {
        // nothing to do
    }

    /**
     * Construct an reifiable object for a <code>float</code>.
     * @param value the primitive <code>float</code> value.
     */
    public FloatWrapper(float value) {
        this.floatValue = value;
    }

    /**
     * Return the value of the <code>float</code>.
     * @return the primitive value.
     */
    public float getFloatValue() {
        return this.floatValue;
    }

    /**
     * @deprecated use {@link FloatWrapper#getFloatValue()}
     * Return the value of the <code>float</code>.
     * @return the primitive value.
     */
    @Deprecated
    public float floatValue() {
        return this.floatValue;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.floatValue + "";
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof FloatWrapper) {
            return ((FloatWrapper) arg0).getFloatValue() == this.floatValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.floatValue.hashCode();
    }
}
