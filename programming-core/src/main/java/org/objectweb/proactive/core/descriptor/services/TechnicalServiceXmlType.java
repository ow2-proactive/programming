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
package org.objectweb.proactive.core.descriptor.services;

import java.util.Map;


public class TechnicalServiceXmlType {
    private String id;

    private Class<?> type;

    private Map<String, String> args;

    public TechnicalServiceXmlType() {
    }

    public TechnicalServiceXmlType(String id, Class<?> type, Map<String, String> args) {
        this.id = id;
        this.type = type;
        this.args = args;
    }

    /**
     * @return Returns the args.
     */
    public Map<String, String> getArgs() {
        return args;
    }

    /**
     * @param args The args to set.
     */
    public void setArgs(Map<String, String> args) {
        this.args = args;
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Returns the type.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(Class<?> type) {
        this.type = type;
    }
}
