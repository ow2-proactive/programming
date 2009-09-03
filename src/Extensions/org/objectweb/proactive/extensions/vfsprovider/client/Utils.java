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
