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

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A boolean ProActive Property
 *
 * @since ProActive 4.3.0
 */
@PublicAPI
public class PAPropertyBoolean extends PAProperty {
    static final private Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION);

    public static final String TRUE = "true";
    public static final String FALSE = "false";

    public PAPropertyBoolean(String name, boolean isSystemProp) {
        super(name, PropertyType.BOOLEAN, isSystemProp);
    }

    public PAPropertyBoolean(String name, boolean isSystemProp, boolean defaultValue) {
        this(name, isSystemProp);
        this.setDefaultValue(new Boolean(defaultValue).toString());
    }

    /**
     *
     * @return the value of this property
     */
    public boolean getValue() {
        String str = super.getValueAsString();
        return Boolean.parseBoolean(str);
    }

    /**
     * Update the value of this property
     * @param value the new value
     */
    public void setValue(boolean value) {
        super.setValue(new Boolean(value).toString());
    }

    /**
     * Indicates if this property is true.
     *
     * This method can only be called with boolean property. Otherwise an {@link IllegalArgumentException}
     * is thrown.
     *
     * If the value is illegal for a boolean property, then false is returned and a warning is
     * printed.
     *
     * @return true if the property is set to true.
     */
    public boolean isTrue() {
        if (!isSet()) {
            return false;
        }

        String val = super.getValueAsString();
        if (TRUE.equals(val)) {
            return true;
        }
        if (FALSE.equals(val)) {
            return false;
        }

        logger.warn(this.name + " is a boolean property but its value is nor " + TRUE + " nor " + FALSE +
            " " + "(" + val + "). ");
        return false;
    }

    @Override
    public boolean isValid(String value) {
        if (TRUE.equals(value) || FALSE.equals(value))
            return true;

        return false;
    }

}
