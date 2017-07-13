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
package org.objectweb.proactive.extensions.vfsprovider.client;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;


/**
 * Error-handling utils class.
 */
class Utils {
    public static IOException generateAndLogIOExceptionWrongStreamType(Log log, WrongStreamTypeException e) {
        log.error("File server unexpectedly does not allow to perform some type of operation on an opened stream");
        return new IOException6("File server unexpectedly does not allow to perform some type of operation on an opened stream",
                                e);
    }

    public static IOException generateAndLogIOExceptionWrongStreamType(Logger log, WrongStreamTypeException e) {
        log.error("File server unexpectedly does not allow to perform some type of operation on an opened stream");
        return new IOException6("File server unexpectedly does not allow to perform some type of operation on an opened stream",
                                e);
    }

    public static IOException generateAndLogIOExceptionStreamNotFound(Log log, StreamNotFoundException e) {
        log.error("File server unexpectedly closed (possibly reopened) file stream");
        return new IOException6("File server unexpectedly closed (possibly reopened) file stream", e);
    }

    public static IOException generateAndLogIOExceptionStreamNotFound(Logger log, StreamNotFoundException e) {
        log.error("File server unexpectedly closed (possibly reopened) file stream");
        return new IOException6("File server unexpectedly closed (possibly reopened) file stream", e);
    }

    public static IOException generateAndLogIOExceptionCouldNotReopen(Log log, Exception x) {
        log.error("Could not reopen stream correctly");
        return new IOException6("Could not reopen stream correctly", x);
    }

    public static IOException generateAndLogIOExceptionCouldNotReopen(Logger log, Exception x) {
        log.error("Could not reopen stream correctly");
        return new IOException6("Could not reopen stream correctly", x);
    }
}
