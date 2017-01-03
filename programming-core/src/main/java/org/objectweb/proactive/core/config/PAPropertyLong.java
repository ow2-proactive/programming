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
package org.objectweb.proactive.core.config;

/**
 * An long ProActive property
 *
 * @since ProActive 5.2.0
 */
public class PAPropertyLong extends PAPropertyImpl {

    public PAPropertyLong(String name, boolean isSystemProp) {
        super(name, PropertyType.LONG, isSystemProp, null);
    }

    public PAPropertyLong(String name, boolean isSystemProp, long defaultValue) {
        super(name, PropertyType.LONG, isSystemProp, Long.toString(defaultValue));
    }

    final public long getValue() {
        String str = super.getValueAsString();
        return parseValue(str);
    }

    final public long getDefaultValue() {
        String str = super.getDefaultValueAsString();
        return parseValue(str);
    }

    private long parseValue(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid value for ProActive property " + super.getAliasedName() +
                " must be a long", e);
        }
    }

    final public void setValue(long value) {
        super.internalSetValue(new Long(value).toString());
    }

    @Override
    final public boolean isValid(String value) {
        try {
            Long.parseLong(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
