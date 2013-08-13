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
 * A ProActive property.
 * 
 * A property is a way for the user to configure the behavior of ProActive. Properties
 * can be initialized from the command line (Java property), from configurations files 
 * or programmatically. Once loaded a property this API <b>must</b> be used. Any change 
 * to a Java property will not be reflected. 
 *  
 * @author ProActive team
 * @since  ProActive 5.2.0
 */
public interface PAProperty {
    static public enum PropertyType {
        STRING, INTEGER, BOOLEAN, LONG, LIST
    }

    /**
     * Indicate if this property is an alias or not.
     * 
     * @return
     *    true if an alias, false otherwise 
     */
    public boolean isAlias();

    /**
     * The name of this property.
     * 
     * @return
     *    the real name of this property
     */
    public String getName();

    /**
     * The name of the aliased property.
     * 
     * A property can be an alias for another. In this case the alias
     * name is the name of the targeted property. Otherwise it returns the 
     * same value than {@link #getName()}
     * 
     * @return
     *   the name of the aliased property
     */
    public String getAliasedName();

    /**
     * Return the type of this property.
     * 
     * @return 
     *    the type of this property
     */
    public PropertyType getType();

    /**
     * Return the value of this property.
     * 
     * @return 
     *    A string representation of the value,
     */
    public String getValueAsString();

    /**
     * Return the default value of this property.
     * 
     * If no default value has been specified then null is returned.
     * 
     * @return
     *    the default value or null.
     */
    public String getDefaultValue();

    /**
     * Indicates if the property is set.
     * 
     * @return 
     *    true if and only if the property has been set.
     */
    public boolean isSet();

    /**
     * Unset the property.
     * 
     * Subsequent calls to getValueAsString will return null.
     */
    public void unset();

    /**
     * Returns the string to be passed on the command line
     *
     * The property surrounded by '-D' and '='
     *
     * @return the string to be passed on the command line
     */
    public String getCmdLine();

    /**
     * Check if the value is valid for this property
     * 
     * @param value 
     *    the value to be testes
     * @return 
     *    true if and only if the value is valid for this property type
     */
    boolean isValid(String value);

    /**
     * Indicates if this property must be exported in the VM
     * 
     * @return 
     *    true it is a system property, false otherwise
     */
    public boolean isSystemProperty();

}
