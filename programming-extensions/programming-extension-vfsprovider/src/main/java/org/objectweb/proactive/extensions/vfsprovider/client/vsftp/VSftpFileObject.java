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
package org.objectweb.proactive.extensions.vfsprovider.client.vsftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.vfs2.FileNotFoundException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.sftp.UserIsOwnerPosixPermissions;
import org.apache.commons.vfs2.util.FileObjectUtils;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.MonitorOutputStream;
import org.apache.commons.vfs2.util.PosixPermissions;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;


/**
 * @author ActiveEon Team
 * @since 07/01/2021
 */
public class VSftpFileObject extends AbstractFileObject<VSftpFileSystem> {

    // log4j logger
    private static Logger logger = ProActiveLogger.getLogger(Loggers.VFS_PROVIDER_SERVER);

    private VSftpFileSystem fileSystem;

    private static final long MOD_TIME_FACTOR = 1000L;

    private SftpATTRS attrs;

    private final String relPath;

    private boolean inRefresh;

    protected VSftpFileObject(AbstractFileName name, VSftpFileSystem fileSystem) throws FileSystemException {
        super(name, fileSystem);
        this.fileSystem = fileSystem;
        relPath = UriParser.decode(fileSystem.getRootName().getRelativeName(name));
        logger.info("relPath = " + relPath);
    }

    private String getResolvedPath() throws IOException {
        return resolvePath(relPath);
    }

    private String resolvePath(String path) throws IOException {
        String resolvedPath = path;
        for (Map.Entry<String, String> entry : fileSystem.getEnvironmentVariables().entrySet()) {
            resolvedPath = resolvedPath.replace(entry.getKey(), entry.getValue()).replace("//", "/");
        }
        return resolvedPath;
    }

    /** @since 2.0 */
    @Override
    protected void doDetach() throws Exception {
        attrs = null;
    }

    /**
     * Determines the type of this file, returns null if the file does not exist.
     */
    @Override
    protected FileType doGetType() throws Exception {
        if (attrs == null) {
            statSelf();
        }

        if (attrs == null) {
            return FileType.IMAGINARY;
        }

        if ((attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_PERMISSIONS) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-permissions.error");
        }
        if (attrs.isDir()) {
            return FileType.FOLDER;
        }
        return FileType.FILE;
    }

    /**
     * Called when the type or content of this file changes.
     */
    @Override
    protected void onChange() throws Exception {
        statSelf();
    }

    /**
     * Fetches file attributes from server.
     *
     * @throws IOException
     */
    private void statSelf() throws IOException {
        ChannelSftp channel = fileSystem.getChannel();
        try {
            setStat(channel.stat(getResolvedPath()));
        } catch (final SftpException e) {
            try {
                // maybe the channel has some problems, so recreate the channel and retry
                if (e.id != ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                    channel.disconnect();
                    channel = fileSystem.getChannel();
                    setStat(channel.stat(getResolvedPath()));
                } else {
                    // Really does not exist
                    attrs = null;
                }
            } catch (final SftpException innerEx) {
                // TODO - not strictly true, but jsch 0.1.2 does not give us
                // enough info in the exception. Should be using:
                // if ( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE )
                // However, sometimes the exception has the correct id, and
                // sometimes
                // it does not. Need to look into why.

                // Does not exist
                attrs = null;
            }
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Sets attrs from listChildrenResolved
     */
    private void setStat(final SftpATTRS attrs) {
        this.attrs = attrs;
    }

    private String sanitizePath(String path) {
        String sanitizedPath = path.replace("//", "/");
        if (sanitizedPath.equals(".")) {
            sanitizedPath = "";
        }
        if (sanitizedPath.startsWith("./")) {
            sanitizedPath = sanitizedPath.substring(2);
        }
        if (sanitizedPath.startsWith("/")) {
            sanitizedPath = sanitizedPath.substring(1);
        }
        if (sanitizedPath.endsWith("/")) {
            sanitizedPath.substring(0, sanitizedPath.length() - 1);
        }
        return sanitizedPath;
    }

    private void checkWriteOperation() throws IOException {
        Map<String, String> variables = fileSystem.getEnvironmentVariables();
        String sanitizedPath = sanitizePath(relPath);

        if (sanitizedPath.isEmpty() || sanitizedPath.equals(".") || variables.containsKey(sanitizedPath)) {
            throw new FileSystemException("This operation is not allowed on path " + relPath);
        }
    }

    /**
     * Creates this file as a folder.
     */
    @Override
    protected void doCreateFolder() throws Exception {
        checkWriteOperation();
        String resolvedPath = getResolvedPath();
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            channel.mkdir(resolvedPath);
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        if (attrs == null || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_ACMODTIME) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-modtime.error");
        }
        return attrs.getMTime() * MOD_TIME_FACTOR;
    }

    /**
     * Sets the last modified time of this file. Is only called if {@link #doGetType} does not return
     * {@link FileType#IMAGINARY}.
     *
     * @param modtime is modification time in milliseconds. SFTP protocol can send times with nanosecond precision but
     *            at the moment jsch send them with second precision.
     */
    @Override
    protected boolean doSetLastModifiedTime(final long modtime) throws Exception {
        checkWriteOperation();
        final int newMTime = (int) (modtime / MOD_TIME_FACTOR);
        attrs.setACMODTIME(attrs.getATime(), newMTime);
        flushStat();
        return true;
    }

    private void flushStat() throws IOException, SftpException {
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            channel.setStat(getResolvedPath(), attrs);
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Deletes the file.
     */
    @Override
    protected void doDelete() throws Exception {
        checkWriteOperation();
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            if (isFile()) {
                channel.rm(getResolvedPath());
            } else {
                channel.rmdir(getResolvedPath());
            }
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Renames the file.
     */
    @Override
    protected void doRename(final FileObject newFile) throws Exception {
        checkWriteOperation();
        final ChannelSftp channel = fileSystem.getChannel();
        try {
            final VSftpFileObject newSftpFileObject = (VSftpFileObject) FileObjectUtils.getAbstractFileObject(newFile);
            channel.rename(getResolvedPath(), newSftpFileObject.getResolvedPath());
        } finally {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * Returns the POSIX type permissions of the file.
     *
     * @param checkIds {@code true} if user and group ID should be checked (needed for some access rights checks)
     * @return A PosixPermission object
     * @throws Exception If an error occurs
     * @since 2.1
     */
    protected PosixPermissions getPermissions(final boolean checkIds) throws Exception {
        statSelf();
        boolean isInGroup = false;
        if (checkIds) {
            if (getAbstractFileSystem().isExecDisabled()) {
                // Exec is disabled, so we won't be able to ascertain the current user's UID and GID.
                // Return "always-true" permissions as a workaround, knowing that the SFTP server won't
                // let us perform unauthorized actions anyway.
                return new UserIsOwnerPosixPermissions(attrs.getPermissions());
            }

            for (final int groupId : fileSystem.getGroupsIds()) {
                if (groupId == attrs.getGId()) {
                    isInGroup = true;
                    break;
                }
            }
        }
        final boolean isOwner = checkIds ? attrs.getUId() == fileSystem.getUId() : false;
        return new PosixPermissions(attrs.getPermissions(), isOwner, isInGroup);
    }

    @Override
    protected boolean doIsReadable() throws Exception {
        return getPermissions(true).isReadable();
    }

    @Override
    protected boolean doSetReadable(final boolean readable, final boolean ownerOnly) throws Exception {
        final PosixPermissions permissions = getPermissions(false);
        final int newPermissions = permissions.makeReadable(readable, ownerOnly);
        if (newPermissions == permissions.getPermissions()) {
            return true;
        }

        attrs.setPERMISSIONS(newPermissions);
        flushStat();

        return true;
    }

    @Override
    protected boolean doIsWriteable() throws Exception {
        return getPermissions(true).isWritable();
    }

    @Override
    protected boolean doSetWritable(final boolean writable, final boolean ownerOnly) throws Exception {
        final PosixPermissions permissions = getPermissions(false);
        final int newPermissions = permissions.makeWritable(writable, ownerOnly);
        if (newPermissions == permissions.getPermissions()) {
            return true;
        }

        attrs.setPERMISSIONS(newPermissions);
        flushStat();

        return true;
    }

    @Override
    protected boolean doIsExecutable() throws Exception {
        return getPermissions(true).isExecutable();
    }

    @Override
    protected boolean doSetExecutable(final boolean executable, final boolean ownerOnly) throws Exception {
        final PosixPermissions permissions = getPermissions(false);
        final int newPermissions = permissions.makeExecutable(executable, ownerOnly);
        if (newPermissions == permissions.getPermissions()) {
            return true;
        }

        attrs.setPERMISSIONS(newPermissions);
        flushStat();

        return true;
    }

    /**
     * Lists the children of this file.
     */
    @Override
    protected String[] doListChildren() throws Exception {
        // use doListChildrenResolved for performance
        return null;
    }

    /**
     * Lists the children of this file.
     */
    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        // should not require a round-trip because type is already set.
        if (this.isFile()) {
            return null;
        }
        // List the contents of the folder
        Vector vector = null;
        final ChannelSftp channel = fileSystem.getChannel();

        logger.info("relPath = " + relPath);

        if (relPath.equals("/") || relPath.isEmpty() || relPath.equals(".") || relPath.equals("./")) {
            vector = new Vector();
            Map<String, String> envVariables = fileSystem.getEnvironmentVariables();
            logger.info("Env vars: " + envVariables);
            for (Map.Entry<String, String> variable : fileSystem.getEnvironmentVariables().entrySet()) {
                Vector<?> subVector = listFileOrDirectory(variable.getValue(), channel);
                for (@SuppressWarnings("unchecked") // OK because ChannelSftp.ls() is documented to return Vector<LsEntry>
                final Iterator<ChannelSftp.LsEntry> iterator = (Iterator<ChannelSftp.LsEntry>) subVector.iterator(); iterator.hasNext();) {
                    final ChannelSftp.LsEntry stat = iterator.next();
                    String name = stat.getFilename();
                    if (name.equals(".") || name.equals("./")) {
                        VLsEntry newEntry = new VLsEntry(variable.getKey(), variable.getKey(), stat.getAttrs());
                        vector.add(newEntry);
                    }
                }
            }

        } else {
            vector = listFileOrDirectory(getResolvedPath(), channel);
        }

        logger.info("vector = " + vector);

        FileSystemException.requireNonNull(vector, "vfs.provider.sftp/list-children.error");

        // Extract the child names
        final ArrayList<FileObject> children = new ArrayList<>();
        for (@SuppressWarnings("unchecked")
        final Iterator iterator = vector.iterator(); iterator.hasNext();) {
            Object statObject = iterator.next();
            final FileObject fo;
            if (statObject instanceof ChannelSftp.LsEntry) {
                final ChannelSftp.LsEntry stat = (ChannelSftp.LsEntry) statObject;

                String name = stat.getFilename();
                if (VFS.isUriStyle() && stat.getAttrs().isDir() && name.charAt(name.length() - 1) != '/') {
                    name = name + "/";
                }

                if (name.equals(".") || name.equals("..") || name.equals("./") || name.equals("../")) {
                    continue;
                }

                fo = fileSystem.resolveFile(fileSystem.getFileSystemManager()
                                                      .resolveName(getName(), UriParser.encode(name), NameScope.CHILD));
                ((VSftpFileObject) FileObjectUtils.getAbstractFileObject(fo)).setStat(stat.getAttrs());
            } else if (statObject instanceof VLsEntry) {
                VLsEntry stat = (VLsEntry) statObject;
                String name = stat.getFilename();
                if (VFS.isUriStyle() && stat.getAttrs().isDir() && name.charAt(name.length() - 1) != '/') {
                    name = name + "/";
                }

                if (name.equals(".") || name.equals("..") || name.equals("./") || name.equals("../")) {
                    continue;
                }

                fo = fileSystem.resolveFile(fileSystem.getFileSystemManager()
                                                      .resolveName(getName(), UriParser.encode(name), NameScope.CHILD));
                ((VSftpFileObject) FileObjectUtils.getAbstractFileObject(fo)).setStat(stat.getAttrs());
            } else {
                throw new FileSystemException("Unexpected entry class : " + statObject.getClass());
            }
            children.add(fo);
        }

        return children.toArray(new FileObject[children.size()]);
    }

    private Vector<?> listFileOrDirectory(final String path, final ChannelSftp channel)
            throws FileSystemException, SftpException {
        Vector<?> vector = null;
        try {
            // try the direct way to list the directory on the server to avoid too many roundtrips
            vector = channel.ls(path);
        } catch (final SftpException e) {
            String workingDirectory = null;
            try {
                if (relPath != null) {
                    workingDirectory = channel.pwd();
                    channel.cd(path);
                }
            } catch (final SftpException ex) {
                // VFS-210: seems not to be a directory
                return null;
            }

            SftpException lsEx = null;
            try {
                vector = channel.ls(".");
            } catch (final SftpException ex) {
                lsEx = ex;
            } finally {
                try {
                    if (relPath != null) {
                        channel.cd(workingDirectory);
                    }
                } catch (final SftpException xe) {
                    throw new FileSystemException("vfs.provider.sftp/change-work-directory-back.error",
                                                  workingDirectory,
                                                  lsEx);
                }
            }

            if (lsEx != null) {
                throw lsEx;
            }
        } finally {
            fileSystem.putChannel(channel);
        }
        return vector;
    }

    /**
     * Returns the size of the file content (in bytes).
     */
    @Override
    protected long doGetContentSize() throws Exception {
        if (attrs == null || (attrs.getFlags() & SftpATTRS.SSH_FILEXFER_ATTR_SIZE) == 0) {
            throw new FileSystemException("vfs.provider.sftp/unknown-size.error");
        }
        return attrs.getSize();
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(final RandomAccessMode mode) throws Exception {
        return new VSftpRandomAccessContent(this, mode);
    }

    /**
     * Creates an input stream to read the file content from. The input stream is starting at the given position in the
     * file.
     */
    InputStream getInputStream(final long filePointer) throws IOException {
        final ChannelSftp channel = fileSystem.getChannel();
        // Using InputStream directly from the channel
        // is much faster than the memory method.
        try {
            return new VSftpInputStream(channel, channel.get(getName().getPathDecoded(), null, filePointer));
        } catch (final SftpException e) {
            fileSystem.putChannel(channel);
            throw new FileSystemException(e);
        }
    }

    /**
     * Creates an input stream to read the file content from.
     */
    @SuppressWarnings("resource")
    @Override
    protected InputStream doGetInputStream(final int bufferSize) throws Exception {
        // VFS-113: avoid npe
        synchronized (getAbstractFileSystem()) {
            final ChannelSftp channel = fileSystem.getChannel();
            try {
                // return channel.get(getName().getPath());
                // hmmm - using the in memory method is soooo much faster ...

                // TODO - Don't read the entire file into memory. Use the
                // stream-based methods on ChannelSftp once they work properly

                /*
                 * final ByteArrayOutputStream outstr = new ByteArrayOutputStream();
                 * channel.get(relPath, outstr);
                 * outstr.close(); return new ByteArrayInputStream(outstr.toByteArray());
                 */

                InputStream inputStream;
                try {
                    // VFS-210: sftp allows to gather an input stream even from a directory and will
                    // fail on first read. So we need to check the type anyway
                    if (!getType().hasContent()) {
                        throw new FileSystemException("vfs.provider/read-not-file.error", getName());
                    }

                    inputStream = channel.get(getResolvedPath());
                } catch (final SftpException e) {
                    if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                        throw new FileNotFoundException(getName());
                    }

                    throw new FileSystemException(e);
                }

                return new VSftpInputStream(channel, inputStream, bufferSize);

            } finally {
                // getAbstractFileSystem().putChannel(channel);
            }
        }
    }

    /**
     * Creates an output stream to write the file content to.
     */
    @Override
    protected OutputStream doGetOutputStream(final boolean bAppend) throws Exception {
        // TODO - Don't write the entire file into memory. Use the stream-based
        // methods on ChannelSftp once the work properly
        /*
         * final ChannelSftp channel = getAbstractFileSystem().getChannel(); return new
         * SftpOutputStream(channel);
         */
        checkWriteOperation();

        final ChannelSftp channel = fileSystem.getChannel();
        return new VSftpOutputStream(channel,
                                     channel.put(getResolvedPath(),
                                                 bAppend ? ChannelSftp.APPEND : ChannelSftp.OVERWRITE));
    }

    /**
     * An InputStream that monitors for end-of-file.
     */
    private class VSftpInputStream extends MonitorInputStream {
        private final ChannelSftp channel;

        public VSftpInputStream(final ChannelSftp channel, final InputStream in) {
            super(in);
            this.channel = channel;
        }

        public VSftpInputStream(final ChannelSftp channel, final InputStream in, final int bufferSize) {
            super(in, bufferSize);
            this.channel = channel;
        }

        /**
         * Called after the stream has been closed.
         */
        @Override
        protected void onClose() throws IOException {
            fileSystem.putChannel(channel);
        }
    }

    /**
     * An OutputStream that wraps an sftp OutputStream, and closes the channel when the stream is closed.
     */
    private class VSftpOutputStream extends MonitorOutputStream {
        private final ChannelSftp channel;

        public VSftpOutputStream(final ChannelSftp channel, final OutputStream out) {
            super(out);
            this.channel = channel;
        }

        /**
         * Called after this stream is closed.
         */
        @Override
        protected void onClose() throws IOException {
            fileSystem.putChannel(channel);
        }
    }

    public class VLsEntry implements Comparable {
        private String filename;

        private String longname;

        private SftpATTRS attrs;

        public VLsEntry(String filename, String longname, SftpATTRS attrs) {
            setFilename(filename);
            setLongname(longname);
            setAttrs(attrs);
        }

        public String getFilename() {
            return filename;
        };

        void setFilename(String filename) {
            this.filename = filename;
        };

        public String getLongname() {
            return longname;
        };

        void setLongname(String longname) {
            this.longname = longname;
        };

        public SftpATTRS getAttrs() {
            return attrs;
        };

        void setAttrs(SftpATTRS attrs) {
            this.attrs = attrs;
        };

        public String toString() {
            return longname;
        }

        public int compareTo(Object o) throws ClassCastException {
            if (o instanceof ChannelSftp.LsEntry) {
                return filename.compareTo(((ChannelSftp.LsEntry) o).getFilename());
            }
            throw new ClassCastException("a decendent of LsEntry must be given.");
        }
    }
}
