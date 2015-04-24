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

    private static final long serialVersionUID = 62L;

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
