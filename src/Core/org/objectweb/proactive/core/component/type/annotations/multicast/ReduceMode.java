/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.type.annotations.multicast;

import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.exceptions.ReductionException;


/**
 * <p>This enumeration defines the various reduction modes available for
 * methods. 
 * </p>
 * <p>It implements the method of the <code>ReduceBehavior</code> interface
 * depending on the selected mode.
 *
 * @author The ProActive Team
 * 
 */
@PublicAPI
public enum ReduceMode implements ReduceBehavior, Serializable {
    /**
     * This reduction mode allows to extract of the list of results the only one result that the list contains.
     */
    SELECT_UNIQUE_VALUE,

    /**
     * The reduction mode is given as a
     * parameter, as a class signature.
     */
    CUSTOM;

    /*
     * @see org.objectweb.proactive.core.component.type.annotations.ReduceBehavior#reduce(java.util.List<?>)
     */
    public Object reduce(List<?> values) throws ReductionException {
        switch (this) {
            case SELECT_UNIQUE_VALUE:
                if (!(values.size() == 1)) {
                    throw new ReductionException(
                        "invalid number of values to reduce: expected [1] but received [" + values.size() +
                            "]");
                }
                return values.iterator().next();

            default:
                return SELECT_UNIQUE_VALUE.reduce(values);
        }
    }

}
