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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.config;

import org.objectweb.proactive.utils.ArgCheck;


/**
 * An alias for a {@link PAProperty}.
 * 
 * Sometimes it is useful to define an alias. For example when a the name of a 
 * property must be changed. It allows backward compatibility, both the old and 
 * the new name can be used.
 * 
 * An alias forwards all the methods calls to the target property.
 * 
 * @author ProActive team
 * @since  ProActive 5.2.0
 */
public class PAPropertyAlias implements PAProperty {
    /** Name of the property (alias name )*/
    private final String name;
    /** The targeted property */
    private final PAPropertyImpl target;

    /**
     * Create an alias for a {@link PAProperty}.
     * 
     * @param target
     *    The targeted property
     * @param name
     *    The name of this alias
     * @throws NullPointerException
     *    If target or name is null
     */
    public PAPropertyAlias(PAPropertyImpl target, String name) throws NullPointerException {
        this.name = ArgCheck.requireNonNull(name);
        this.target = ArgCheck.requireNonNull(target);
    }

    @Override
    public String getAliasedName() {
        return this.target.getAliasedName();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isAlias() {
        return true;
    }

    @Override
    public PropertyType getType() {
        return this.target.getType();
    }

    @Override
    public String getValueAsString() {
        return this.target.getValueAsString();
    }

    @Override
    public String getDefaultValueAsString() {
        return this.target.getDefaultValueAsString();
    }

    @Override
    public boolean isSet() {
        return this.target.isSet();
    }

    @Override
    public void unset() {
        this.target.unset();
    }

    @Override
    public String getCmdLine() {
        return this.target.getCmdLine();
    }

    @Override
    public boolean isValid(String value) {
        return this.target.isValid(value);
    }

    @Override
    public boolean isSystemProperty() {
        return this.target.isSystemProperty();
    }

    @Override
    public String toString() {
        return this.name + "=" + this.getValueAsString() + "(alias " + this.target.getAliasedName() + ")";
    }
}
