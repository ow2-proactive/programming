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
package org.objectweb.proactive.core.group;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * This class represents an throwable occured in a group communication.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class ExceptionInGroup extends RuntimeException {

    /** The Object who throwns the Throwable */
    private Object object;

    /** The index of the object who throwns the Throwable in the caller group */
    private int index;

    /** The Throwable thrown */
    private Throwable throwable;

    /**
     * Built an ThrowableInGroup
     * @param object - the object who throwns the throwable (use null if the object is unknown)
     * @param index - the index of the object who throwns the throwable in the caller group
     * @param throwable - the throwable thrown
     */
    public ExceptionInGroup(Object object, int index, Throwable throwable) {
        super(throwable);
        this.object = object;
        this.index = index;
        this.throwable = throwable;
    }

    /**
     * Returns the object who throwns the Throwable
     * @return the object who throwns the Throwable
     */
    public Object getObject() {
        return this.object;
    }

    /**
     * Returns the index of the object who throwns the Throwable
     * @return the index of the object who throwns the Throwable
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Returns the Throwable thrown
     * @return the Throwable thrown
     */
    public Throwable getThrowable() {
        return this.throwable;
    }
}
