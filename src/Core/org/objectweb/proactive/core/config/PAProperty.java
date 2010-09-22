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

/**
 * A ProActive property
 *
 * A ProActive property is a typed Java property. This abstraction must be used
 * instead of {@link System#getProperty(String)} and
 * {@link System#setProperty(String, String)} in ProActive
 *
 * @since ProActive 4.3.0
 */
abstract public class PAProperty {
    public enum PropertyType {
        STRING, INTEGER, BOOLEAN;
    }

    final String name;
    final PropertyType type;
    final boolean isSystemProperty;
    volatile String defaultValue;

    PAProperty(String name, PropertyType type, boolean isSystemProp) {
        this.name = name;
        this.type = type;
        this.isSystemProperty = isSystemProp;
    }

    /**
     * Returns the key associated to this property
     * @return the key associated to this property
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns this property type
     * @return The type of this property
     */
    public PropertyType getType() {
        return this.type;
    }

    /**
     * Returns the value of this property
     * @return the value of this property
     */
    public String getValueAsString() {
        return ProActiveConfiguration.getInstance().getProperty(this.name);
    }

    /**
     * Set the value of this property
     * @param value new value of the property
     */
    public void setValue(String value) {
        ProActiveConfiguration.getInstance().setProperty(this.name, value, this.isSystemProperty);
    }

    /**
     * Set the default value of this property
     * @param value new value of the property
     */
    protected void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    /**
     * Set the default value of this property
     * @param value new value of the property
     */
    protected String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Indicates if the property is set.
     * @return true if and only if the property has been set.
     */
    public boolean isSet() {
        return ProActiveConfiguration.getInstance().getProperty(this.name) != null;
    }

    /**
     * Unset the property 
     * 
     * @since ProActive 4.4.0
     */
    public void unset() {
        ProActiveConfiguration.getInstance().unsetProperty(this.name);
    }

    /**
     * Returns the string to be passed on the command line
     *
     * The property surrounded by '-D' and '='
     *
     * @return the string to be passed on the command line
     */
    public String getCmdLine() {
        return "-D" + this.name + '=';
    }

    /**
     * Check if the value is valid for this property
     * @param value a property value
     * @return true if and only if the value is valid for this property type
     */
    public abstract boolean isValid(String value);

    /**
     *
     * @return true is this property must be exported in the VM
     */
    public boolean isSystemProperty() {
        return this.isSystemProperty;
    }

    @Override
    public String toString() {
        return this.name + "=" + getValueAsString();
    }
}
