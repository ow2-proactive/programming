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
