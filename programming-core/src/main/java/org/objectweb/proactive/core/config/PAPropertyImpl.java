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

import org.objectweb.proactive.utils.ArgCheck;


/**
 * Abstract class to implement a typed {@link PAProperty}
 *
 * @since ProActive 4.3.0
 */
abstract class PAPropertyImpl implements PAProperty {
    /** Name of this property. */
    final String name;

    /** Type of this property. */
    final PropertyType type;

    /** Should this property be exported in the system env ? (Java property). */
    final boolean isSystemProperty;

    /** Default initialization value. */
    final String defaultValue;

    PAPropertyImpl(String name, PropertyType type, boolean isSystemProp, String defaultValue) {
        this.name = ArgCheck.requireNonNull(name);
        this.type = type;
        this.defaultValue = defaultValue;
        this.isSystemProperty = isSystemProp;
    }

    /**
     * Set the value of the Property.
     * 
     * @param value
     *    The new value of the property.
     */
    final void internalSetValue(String value) {
        ProActiveConfiguration.getInstance().setProperty(this.name, value, this.isSystemProperty);
    }

    @Override
    final public String getAliasedName() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isAlias() {
        return false;
    }

    @Override
    final public PropertyType getType() {
        return this.type;
    }

    @Override
    public String getDefaultValueAsString() {
        return this.defaultValue;
    }

    @Override
    final public boolean isSystemProperty() {
        return this.isSystemProperty;
    }

    @Override
    final public String getValueAsString() {
        return ProActiveConfiguration.getInstance().getProperty(this.name);
    }

    @Override
    final public boolean isSet() {
        return this.getValueAsString() != null;
    }

    @Override
    final public void unset() {
        ProActiveConfiguration.getInstance().unsetProperty(this.name);
    }

    @Override
    final public String getCmdLine() {
        return "-D" + this.name + '=';
    }

    @Override
    public String toString() {
        return this.name + "=" + this.getValueAsString();
    }
}
