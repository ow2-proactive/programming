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
package org.objectweb.proactive.core;

import org.objectweb.proactive.annotation.PublicAPI;


@PublicAPI
/**
 * Exception thrown when a blocking operation times out. Blocking operations for which a timeout is
 * specified need a means to indicate that the timeout has occurred. For many such operations it is
 * possible to return a value that indicates timeout; when that is not possible or desirable then
 * <tt>TimeoutException</tt> should be declared and thrown.
 * 
 * @since 4.0
 */
public class ProActiveTimeoutException extends ProActiveRuntimeException {

    public ProActiveTimeoutException() {
        super();
    }

    public ProActiveTimeoutException(String message) {
        super(message);
    }

    public ProActiveTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProActiveTimeoutException(Throwable cause) {
        super(cause);
    }

}