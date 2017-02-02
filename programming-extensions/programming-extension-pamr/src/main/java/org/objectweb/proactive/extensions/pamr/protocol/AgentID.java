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
package org.objectweb.proactive.extensions.pamr.protocol;

import java.io.Serializable;


/** An unique identifier for Agents 
 *
 * Each client is identified by an unique {@link AgentID}. A client receive its
 * ID when it connects to the router for the first time. 
 * 
 * @since ProActive 4.1.0
 */

public class AgentID implements Serializable {
    static final public long MIN_DYNAMIC_AGENT_ID = 4096;

    final private long id;

    public AgentID(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public boolean isReserved() {
        return this.id < MIN_DYNAMIC_AGENT_ID && this.id >= 0;
    }

    @Override
    public String toString() {
        return Long.toString(id);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AgentID other = (AgentID) obj;
        if (id != other.id)
            return false;
        return true;
    }

}
