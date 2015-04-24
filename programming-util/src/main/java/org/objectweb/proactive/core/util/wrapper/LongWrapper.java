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
package org.objectweb.proactive.core.util.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <p>An reifiable object for wrapping the primitive Java type <code>long</code>.</p>
 * <p>Use this class as result for ProActive asynchronous method calls.</p>
 *
 * @author The ProActive Team
 *
 * Created on Jul 28, 2005
 */
@PublicAPI
@XmlRootElement
public class LongWrapper implements Serializable {

    private static final long serialVersionUID = 62L;

    /**
     * The primitive value.
     */
    protected Long longValue;

    /**
     * The no arguments constructor for ProActive.
     */
    public LongWrapper() {
        // nothing to do
    }

    /**
     * Construct an reifiable object for a <code>long</code>.
     * @param value the primitive <code>long</code> value.
     */
    public LongWrapper(long value) {
        this.longValue = value;
    }

    /**
     * @deprecated see {@link LongWrapper#getLongValue()}
     * Return the value of the <code>long</code>.
     * @return the primitive value.
     */
    @Deprecated
    public long longValue() {
        return this.longValue;
    }

    /**
     * Return the value of the <code>long</code>.
     * @return the primitive value.
     */
    public long getLongValue() {
        return this.longValue;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.longValue + "";
    }

    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof LongWrapper) {
            return ((LongWrapper) arg0).getLongValue() == this.longValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.longValue.hashCode();
    }
}
