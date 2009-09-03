package org.objectweb.proactive.extensions.dataspaces.exceptions;

import java.io.IOException;

import org.objectweb.proactive.core.util.log.ProActiveLogger;


public class FileSystemException extends IOException {

    private static final long serialVersionUID = -3555529502633312529L;

    public FileSystemException(Throwable e) {
        super(ProActiveLogger.getStackTraceAsString(e));
    }

    public FileSystemException(String msg) {
        super(msg);
    }
}
