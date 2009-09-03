package org.objectweb.proactive.extensions.vfsprovider.server;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileInfo;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileType;


public class FileInfoImpl implements FileInfo {

    private static final long serialVersionUID = -1670153419776942194L;

    private final long lastModifiedTime;

    private final long size;

    private final FileType fileType;

    private final boolean hidden;

    private final boolean readable;

    private final boolean writable;

    public FileInfoImpl(File file) throws IOException {
        try {
            lastModifiedTime = file.lastModified();
            size = file.length();
            fileType = file.isDirectory() ? FileType.DIRECTORY : FileType.FILE;
            hidden = file.isHidden();
            readable = file.canRead();
            writable = file.canWrite();
        } catch (SecurityException sec) {
            throw new IOException(ProActiveLogger.getStackTraceAsString(sec));
        }
        if (lastModifiedTime == 0)
            throw new IOException("Unable to read \"last modified time\" attribute");
    }

    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    public long getSize() {
        return size;
    }

    public FileType getType() {
        return fileType;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }
}
