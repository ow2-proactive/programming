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
package org.objectweb.proactive.core.util;

import java.io.Serializable;


/**
 * Defines a mutable, serializable and not final Long.
 * @author The ProActive Team
 */
public class MutableLong implements Serializable {
    private long value;

    public MutableLong() {
    }

    public MutableLong(long value) {
        this.value = value;
    }

    public long getValue() {
        return this.value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public long add(long toAdd) {
        this.value += toAdd;
        return this.value;
    }

    public boolean isLessThan(MutableLong l) {
        return this.value < l.getValue();
    }

    @Override
    public int hashCode() {
        return (int) this.value;
    }

    @Override
    public boolean equals(Object mi) {
        if (mi instanceof MutableLong) {
            return this.value == ((MutableLong) mi).getValue();
        } else {
            return false;
        }
    }
}
