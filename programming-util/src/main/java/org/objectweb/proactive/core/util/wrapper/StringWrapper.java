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
 * <p>An reifiable object for wrapping the Java type <code>String</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author The ProActive Team
 *
 * Created on Jul 28, 2005
 */
@PublicAPI
@XmlRootElement
public class StringWrapper implements Serializable {

    /**
     * The not reifiable value.
     */
    protected String stringValue;

    /**
     * The no arguments constructor for ProActive.
     */
    public StringWrapper() {
        // nothing to do
    }

    /**
     * Construct an reifiable object for a <code>String</code>.
     * @param value the class <code>String</code> value.
     */
    public StringWrapper(String value) {
        this.stringValue = value;
    }

    /**
     * @deprecated use {@link StringWrapper#getStringValue()}
     * Return the value of the <code>String</code>.
     * @return the none reifiable value.
     */
    @Deprecated
    public String stringValue() {
        return this.stringValue;
    }

    /**
     * Return the value of the <code>String</code>.
     * @return the none reifiable value.
     */
    public String getStringValue() {
        return this.stringValue;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.stringValue;
    }

    @Override
    public boolean equals(Object obj) {
        return ((obj instanceof StringWrapper) && ((StringWrapper) obj).getStringValue().equals(stringValue));
    }

    @Override
    public int hashCode() {
        return stringValue.hashCode();
    }
}
