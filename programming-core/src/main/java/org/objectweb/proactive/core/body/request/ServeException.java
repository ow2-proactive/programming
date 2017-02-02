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
package org.objectweb.proactive.core.body.request;

import org.objectweb.proactive.core.ProActiveException;


/**
 */
public class ServeException extends ProActiveException {

    /**
     * Constructs a <code>ServeException</code> with no specified
     * detail message.
     */
    public ServeException() {
    }

    /**
     * Constructs a <code>CommunicationException</code> with the specified detail message.
     * @param s the detail message
     */
    public ServeException(String s) {
        super(s);
    }

    /**
     * Constructs a <code>CommunicationException</code> with the specified
     * detail message and nested exception.
     *
     * @param s the detail message
     * @param ex the nested exception
     */
    public ServeException(String s, Throwable ex) {
        super(s, ex);
    }
}
