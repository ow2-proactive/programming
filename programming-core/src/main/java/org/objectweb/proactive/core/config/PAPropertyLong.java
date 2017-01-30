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
