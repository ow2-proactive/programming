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
package org.objectweb.proactive;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;


/**
 *
 * An exception thrown when a problem occurs during the creation of an ActiveObject
 * </p><p>
 * <b>see <a href="../../../../html/ActiveObjectCreation.html">active object creation documentation</a></b>
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 */
@PublicAPI
public class ActiveObjectCreationException extends ProActiveException {

    /**
     * Constructs a <code>ProActiveException</code> with no specified
     * detail message.
     */
    public ActiveObjectCreationException() {
    }

    /**
     * Constructs a <code>ActiveObjectCreationException</code> with the specified detail message.
     * @param s the detail message
     */
    public ActiveObjectCreationException(String s) {
        super(s);
    }

    /**
     * Constructs a <code>ActiveObjectCreationException</code> with the specified
     * detail message and nested exception.
     *
     * @param s the detail message
     * @param detail the nested exception
     */
    public ActiveObjectCreationException(String s, Throwable detail) {
        super(s, detail);
    }

    /**
     * Constructs a <code>ActiveObjectCreationException</code> with the specified
     * detail message and nested exception.
     * @param detail the nested exception
     */
    public ActiveObjectCreationException(Throwable detail) {
        super(detail);
    }
}
