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

    private PropertyListValidator validator;

    public PAPropertyList(String name, String separator, boolean isSystemProp) {
        super(name, PropertyType.LIST, isSystemProp, null);
        this.separator = separator;
    }

    public PAPropertyList(String name, String separator, boolean isSystemProp, PropertyListValidator validator) {
        super(name, PropertyType.LIST, isSystemProp, null);
        this.separator = separator;
        this.validator = validator;
    }

    public PAPropertyList(String name, String separator, boolean isSystemProp, String defaultValue) {
        super(name, PropertyType.LIST, isSystemProp, defaultValue);
        this.separator = separator;
    }

    public PAPropertyList(String name, String separator, boolean isSystemProp, PropertyListValidator validator,
            String defaultValue) {
        super(name, PropertyType.LIST, isSystemProp, defaultValue);
        this.separator = separator;
        this.validator = validator;
    }

    final public List<String> getValue() {
        if (computedValue == null) {
            computedValue = computeStringToList(super.getValueAsString());
        }
        return computedValue;
    }

    final public List<String> getDefaultValue() {
        return computeStringToList(super.getDefaultValueAsString());
    }

    private List<String> computeStringToList(String value) {
        if (value != null) {
            ArrayList<String> tmplist = new ArrayList<>();
            for (String val : value.split(Pattern.quote(separator))) {
                val = val.trim();
                if (val.length() > 0) {
                    tmplist.add(val);
                }
            }
            if (validator != null) {
                validator.accept(tmplist);
            }
            return tmplist;
        } else {
            return null;
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
        computedValue = computeStringToList(value);
    }

    final public void setValue(List<String> value) {
        if (validator != null) {
            validator.accept(new ArrayList<>(value));
        }
        computedValue = value;

        computeListToString();
    }

    @Override
    final public boolean isValid(String value) {
        return true;
    }
}
