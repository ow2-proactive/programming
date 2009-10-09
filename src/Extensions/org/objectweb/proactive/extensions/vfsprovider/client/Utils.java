/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
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
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;


/**
 * Error-handling utils class.
 */
class Utils {
    public static IOException generateAndLogIOExceptionWrongStreamType(Log log, WrongStreamTypeException e) {
        log
                .error("File server unexpectedly does not allow to perform some type of operation on an opened stream");
        return new IOException(
            "File server unexpectedly does not allow to perform some type of operation on an opened stream\n" +
                ProActiveLogger.getStackTraceAsString(e)); // FIXME: Java6 IOException
    }

    public static IOException generateAndLogIOExceptionStreamNotFound(Log log, StreamNotFoundException e) {
        log.error("File server unexpectedly closed (possibly reopened) file stream");
        return new IOException("File server unexpectedly closed (possibly reopened) file stream\n" +
            ProActiveLogger.getStackTraceAsString(e)); // FIXME: Java6 IOException
    }

    public static IOException generateAndLogIOExceptionCouldNotReopen(Log log, Exception x) {
        log.error("Could not reopen stream correctly");
        return new IOException("Could not reopen stream correctly\n" +
            ProActiveLogger.getStackTraceAsString(x)); // FIXME: Java6 IOException
    }
}
