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
package org.objectweb.proactive.extensions.vfsprovider.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;


/**
 * Error-handling utils class.
 */
class Utils {
    public static IOException generateAndLogIOExceptionWrongStreamType(Log log, WrongStreamTypeException e) {
        log.error("File server unexpectedly does not allow to perform some type of operation on an opened stream");
        return new IOException6(
            "File server unexpectedly does not allow to perform some type of operation on an opened stream",
            e);
    }

    public static IOException generateAndLogIOExceptionStreamNotFound(Log log, StreamNotFoundException e) {
        log.error("File server unexpectedly closed (possibly reopened) file stream");
        return new IOException6("File server unexpectedly closed (possibly reopened) file stream", e);
    }

    public static IOException generateAndLogIOExceptionCouldNotReopen(Log log, Exception x) {
        log.error("Could not reopen stream correctly");
        return new IOException6("Could not reopen stream correctly", x);
    }
}
