/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.config;

import org.objectweb.proactive.core.ProActiveRuntimeException;


/**
 * An integer ProActive property
 *
 * @since ProActive 4.3.0
 */
public class PAPropertyInteger extends PAProperty {
    public PAPropertyInteger(String name, boolean isSystemProp) {
        super(name, PropertyType.INTEGER, isSystemProp);
    }

    /**
     *
     * @return the value of this proactive property
     */
    public int getValue() {
        String str = super.getValueAsString();
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new ProActiveRuntimeException("Invalid value for ProActive property " + this.name +
                " must be an integer", e);
        }
    }

    /**
     * Update the value of this proactive property
     * @param value the new value
     */
    public void setValue(int value) {
        super.setValue(new Integer(value).toString());
    }

    @Override
    public boolean isValid(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
