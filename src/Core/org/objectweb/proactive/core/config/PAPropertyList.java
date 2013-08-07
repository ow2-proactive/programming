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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * A String ProActive property
 *
 * @since ProActive 4.3.0
 */
public class PAPropertyList extends PAPropertyImpl {

    private String separator;

    private List<String> computedValue;

    public PAPropertyList(String name, String separator, boolean isSystemProp) {
        super(name, PropertyType.LIST, isSystemProp, null);
        this.separator = separator;
    }

    public PAPropertyList(String name, String separator, boolean isSystemProp, String defaultValue) {
        super(name, PropertyType.LIST, isSystemProp, defaultValue);
        this.separator = separator;
    }

    final public List<String> getValue() {
        if (computedValue == null) {
            computeStringToList();
        }
        return computedValue;
    }

    private void computeStringToList() {
        String value = super.getValueAsString();
        if (value != null) {
            computedValue = new ArrayList<String>();
            for (String val : value.split(Pattern.quote(separator))) {
                val = val.trim();
                if (val.length() > 0) {
                    computedValue.add(val);
                }
            }
        } else {
            computedValue = null;
        }
    }

    private void computeListToString() {
        StringBuilder sb = new StringBuilder();
        if (computedValue != null) {
            for (String val : computedValue) {
                sb.append(val);
                sb.append(separator);
            }
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            super.internalSetValue(sb.toString());
        } else {
            super.internalSetValue(null);
        }
    }

    final public void setValue(String value) {
        super.internalSetValue(value);
        computeStringToList();
    }

    final public void setValue(List<String> value) {
        computedValue = value;
        computeListToString();
    }

    @Override
    final public boolean isValid(String value) {
        return true;
    }
}
