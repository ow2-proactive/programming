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
 * A String ProActive property
 *
 * @since ProActive 4.3.0
 */
public class PAPropertyString extends PAPropertyImpl {
    public PAPropertyString(String name, boolean isSystemProp) {
        super(name, PropertyType.STRING, isSystemProp, null);
    }

    public PAPropertyString(String name, boolean isSystemProp, String defaultValue) {
        super(name, PropertyType.STRING, isSystemProp, defaultValue);
    }

    final public String getValue() {
        return super.getValueAsString();
    }

    final public String getDefaultValue() {
        return super.getDefaultValueAsString();
    }

    final public void setValue(String value) {
        super.internalSetValue(value);
    }

    @Override
    final public boolean isValid(String value) {
        return value != null;
    }
}
