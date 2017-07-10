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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.apache.commons.vfs2.provider.AbstractRandomAccessStreamContent;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.util.MonitorInputStream;
import org.apache.commons.vfs2.util.MonitorOutputStream;
import org.apache.commons.vfs2.util.RandomAccessMode;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.StreamNotFoundException;
import org.objectweb.proactive.extensions.vfsprovider.exceptions.WrongStreamTypeException;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileInfo;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;
import org.objectweb.proactive.extensions.vfsprovider.protocol.StreamMode;


/**
 * VFS FileObject implementation for ProActive protocol.
 *
 * @see ProActiveFileProvider
 */
public class ProActiveFileObject extends AbstractFileObject {
    // logging in VFS (not ProActive) way
    private static Log log = LogFactory.getLog(ProActiveFileObject.class);

    private static final FileInfo IMAGINARY_FILE_INFO = new FileInfo() {

        public long getLastModifiedTime() {
            return 0;
        }

        public long getSize() {
            return 0;
        }

        public org.objectweb.proactive.extensions.vfsprovider.protocol.FileType getType() {
            return null;
        }

        public boolean isHidden() {
            return false;
        }

        public boolean isReadable() {
            return false;
        }

        public boolean isWritable() {
            return false;
        }
    };

    private FileInfo fileInfo;

    private ProActiveFileSystem proactiveFS;

    protected ProActiveFileObject(AbstractFileName name, ProActiveFileSystem fs) {
        super(name, fs);
        this.proactiveFS = fs;
    }

    private ProActiveFileObject(AbstractFileName name, FileInfo info, ProActiveFileSystem fs) {
        super(name, fs);
        this.proactiveFS = fs;
        this.fileInfo = info;
    }

    // let's access server this way, as ProActiveFileSystem is responsible
    // for managing its instance(s)
    private FileSystemServer getServer() throws FileSystemException {
        return proactiveFS.getServer();
    }

    private String getPath() throws FileSystemException {
        return ((ProActiveFileName) getName()).getPathDecoded();
    }

    @Override
    protected void doAttach() throws Exception {
        synchronized (proactiveFS) {
            if (fileInfo == null) {
                fileInfo = getServer().fileGetInfo(getPath());
                if (fileInfo == null) {
                    fileInfo = IMAGINARY_FILE_INFO;
                }
            }
        }
    }

    @Override
    protected void doDetach() throws Exception {
        synchronized (proactiveFS) {
            fileInfo = null;
        }
    }

    @Override
    protected void onChange() throws Exception {
        synchronized (proactiveFS) {
            if (isAttached()) {
                doDetach();
                doAttach();
            }
        }
    }

    @Override
    protected long doGetContentSize() throws Exception {
        synchronized (proactiveFS) {
            return fileInfo.getSize();
        }
    }

    @Override
    protected FileType doGetType() throws Exception {
        synchronized (proactiveFS) {
            if (fileInfo.getType() == null) {
                return FileType.IMAGINARY;
            }

            switch (fileInfo.getType()) {
                case FILE:
                    return FileType.FILE;
                case DIRECTORY:
                    return FileType.FOLDER;
                default:
                    throw new RuntimeException("Unexpected file type");
            }
        }
    }

    @Override
    protected boolean doIsHidden() throws Exception {
        synchronized (proactiveFS) {
            return fileInfo.isHidden();
        }
    }

    @Override
    protected boolean doIsReadable() throws Exception {
        synchronized (proactiveFS) {
            return fileInfo.isReadable();
        }
    }

    @Override
    protected boolean doIsWriteable() throws Exception {
        synchronized (proactiveFS) {
            return fileInfo.isWritable();
        }
    }

    @Override
    protected long doGetLastModifiedTime() throws Exception {
        synchronized (proactiveFS) {
            return fileInfo.getLastModifiedTime();
        }
    }

    @Override
    protected String[] doListChildren() throws Exception {
        synchronized (proactiveFS) {
            final Set<String> files = getServer().fileListChildren(getPath());
            if (files == null) {
                return null;
            }

            final String result[] = new String[files.size()];
            int i = 0;
            for (final String f : files) {
                result[i++] = UriParser.encode(f);
            }
            return result;
        }
    }

    @Override
    protected FileObject[] doListChildrenResolved() throws Exception {
        synchronized (proactiveFS) {

            String[] childrenNames = doListChildren();
            Map<String, FileInfo> fileInfoMap = getServer().fileListChildrenInfo(getPath());

            ProActiveFileNameParser parser = ProActiveFileNameParser.getInstance();

            FileObject[] fileObjects = new FileObject[childrenNames.length];
            for (int i = 0; i < childrenNames.length; i++) {
                String currenURI = getName().getURI();
                if (!currenURI.endsWith("/")) {
                    currenURI = currenURI + "/";
                }
                ProActiveFileName name = (ProActiveFileName) parser.parseUri(null, null, currenURI + childrenNames[i]);
                fileObjects[i] = new ProActiveFileObject(name, fileInfoMap.get(childrenNames[i]), proactiveFS);
            }

            return fileObjects;
        }
    }

    @Override
    protected void doCreateFolder() throws Exception {
        synchronized (proactiveFS) {
            getServer().fileCreate(getPath(),
                                   org.objectweb.proactive.extensions.vfsprovider.protocol.FileType.DIRECTORY);
        }
    }

    @Override
    protected void doDelete() throws Exception {
        synchronized (proactiveFS) {
            getServer().fileDelete(getPath(), false);
        }
    }

    @Override
    protected InputStream doGetInputStream() throws Exception {
        synchronized (proactiveFS) {
            return new MonitorInputStream(new ProActiveInputStream());
        }
    }

    @Override
    protected OutputStream doGetOutputStream(boolean append) throws Exception {
        synchronized (proactiveFS) {
            return new MonitorOutputStream(new ProActiveOutputStream(append));
        }
    }

    @Override
    protected RandomAccessContent doGetRandomAccessContent(RandomAccessMode mode) throws Exception {
        synchronized (proactiveFS) {
            return new ProActiveRandomAccessContent(mode);
        }
    }

    private class ProActiveInputStream extends AbstractProActiveInputStreamAdapter {
        private long position;

        private long streamId;

        public ProActiveInputStream() throws IOException {
            streamId = getServer().streamOpen(getPath(), StreamMode.SEQUENTIAL_READ);
        }

        @Override
        protected long getStreamId() {
            return streamId;
        }

        @Override
        protected FileSystemServer getServer() throws FileSystemException {
            return ProActiveFileObject.this.getServer();
        }

        @Override
        public synchronized void close() throws IOException {
            try {
                getServer().streamClose(streamId);
            } catch (StreamNotFoundException e) {
                //ignore
            }
        }

        @Override
        protected void notifyBytesRead(long bytesNumber) {
            position += bytesNumber;
        }

        @Override
        protected void reopenStream() throws IOException {
            ProActiveFileObject.log.debug("Reopening input stream: " + streamId);
            try {
                streamId = getServer().streamOpen(getPath(), StreamMode.SEQUENTIAL_READ);
                if (position > 0) {
                    final long skipped = getServer().streamSkip(streamId, position);
                    if (skipped != position) {
                        close();
                        throw new IOException("Could not skip proper number of bytes");
                    }
                }
            } catch (Exception x) {
                throw Utils.generateAndLogIOExceptionCouldNotReopen(log, x);
            }
        }
    }

    private class ProActiveOutputStream extends AbstractProActiveOutputStreamAdapter {
        private long streamId;

        private ProActiveOutputStream(final boolean append) throws IOException {
            final StreamMode mode = append ? StreamMode.SEQUENTIAL_APPEND : StreamMode.SEQUENTIAL_WRITE;
            streamId = getServer().streamOpen(getPath(), mode);
        }

        @Override
        protected long getStreamId() {
            return streamId;
        }

        @Override
        protected FileSystemServer getServer() throws FileSystemException {
            return ProActiveFileObject.this.getServer();
        }

        @Override
        public synchronized void close() throws IOException {
            try {
                getServer().streamClose(streamId);
            } catch (StreamNotFoundException e) {
                // ignore
            }
        }

        @Override
        protected void notifyBytesWritten(long bytesNumber) {
            // ignore
        }

        @Override
        protected void reopenStream() throws IOException {
            ProActiveFileObject.log.debug("Reopening output stream: " + streamId);
            try {
                streamId = getServer().streamOpen(getPath(), StreamMode.SEQUENTIAL_APPEND);
            } catch (Exception x) {
                throw Utils.generateAndLogIOExceptionCouldNotReopen(log, x);
            }
        }
    }

    private class ProActiveRandomAccessContent extends AbstractRandomAccessStreamContent {
        private long streamId;

        private final StreamMode streamMode;

        private long bufInputStreamPosition;

        private long position;

        private DataInputStream dis;

        private DataOutputStream dos;

        private ProActiveRandomAccessContent(RandomAccessMode mode) throws IOException {
            super(mode);
            if (mode == RandomAccessMode.READ) {
                streamMode = StreamMode.RANDOM_ACCESS_READ;
            } else if (mode == RandomAccessMode.READWRITE) {
                streamMode = StreamMode.RANDOM_ACCESS_READ_WRITE;
            } else {
                throw new IllegalArgumentException("Unexpected random access mode");
            }
            streamId = getServer().streamOpen(getPath(), streamMode);
        }

        @Override
        protected DataInputStream getDataInputStream() throws IOException {
            if (dis == null) {
                dis = createDataInputStream();
            }
            return dis;
        }

        private DataInputStream createDataInputStream() throws IOException {
            final InputStream is = new AbstractProActiveInputStreamAdapter() {
                @Override
                protected long getStreamId() {
                    return streamId;
                }

                @Override
                protected FileSystemServer getServer() throws FileSystemException {
                    return ProActiveFileObject.this.getServer();
                }

                @Override
                public void close() throws IOException {
                    dis = null;
                }

                @Override
                protected void notifyBytesRead(long bytes) {
                    position += bytes;
                }

                @Override
                protected void reopenStream() throws IOException {
                    ProActiveRandomAccessContent.this.reopenStream();
                }

                @Override
                public int read() throws IOException {
                    checkNotClosed();
                    return super.read();
                }

                @Override
                public int read(byte[] b) throws IOException {
                    checkNotClosed();
                    return super.read(b);
                }

                @Override
                public synchronized int read(byte[] b, int off, int len) throws IOException {
                    checkNotClosed();
                    return super.read(b, off, len);
                }

                @Override
                public synchronized long skip(long n) throws IOException {
                    checkNotClosed();
                    return super.skip(n);
                }

                @Override
                public int available() throws IOException {
                    checkNotClosed();
                    return super.available();
                }

                private void checkNotClosed() throws IOException {
                    if (dis == null) {
                        throw new IOException("Stream closed");
                    }
                }
            };

            if (streamMode == StreamMode.RANDOM_ACCESS_READ) {
                // user gets buffered MonitorInputStream, so we have to count read bytes
                // for getFilePointer() in that buffered stream, not on raw stream
                return new DataInputStream(new FilterInputStream(new MonitorInputStream(is)) {
                    @Override
                    public int read() throws IOException {
                        final int result = super.read();
                        if (result > 0) {
                            bufInputStreamPosition++;
                        }
                        return result;
                    }

                    @Override
                    public int read(byte[] b) throws IOException {
                        final int result = super.read(b);
                        if (result > 0) {
                            bufInputStreamPosition += result;
                        }
                        return result;
                    }

                    @Override
                    public int read(byte[] b, int off, int len) throws IOException {
                        final int result = super.read(b, off, len);
                        if (result > 0) {
                            bufInputStreamPosition += result;
                        }
                        return result;
                    }

                    @Override
                    public long skip(long n) throws IOException {
                        final long result = super.skip(n);
                        bufInputStreamPosition += result;
                        return result;
                    }
                });
            } else {
                return new DataInputStream(is);
            }
        }

        private DataOutputStream getDataOutputStream() throws IOException {
            if (dos == null) {
                dos = createDataOutputStream();
            }
            return dos;
        }

        private DataOutputStream createDataOutputStream() throws IOException {
            final OutputStream is = new AbstractProActiveOutputStreamAdapter() {
                @Override
                protected long getStreamId() {
                    return streamId;
                }

                @Override
                protected FileSystemServer getServer() throws FileSystemException {
                    return ProActiveFileObject.this.getServer();
                }

                @Override
                public void close() throws IOException {
                    dos = null;
                }

                @Override
                protected void notifyBytesWritten(long bytes) {
                    position += bytes;
                }

                @Override
                protected void reopenStream() throws IOException {
                    ProActiveRandomAccessContent.this.reopenStream();
                }

                @Override
                public void write(int b) throws IOException {
                    checkNotClosed();
                    super.write(b);
                }

                @Override
                public void write(byte[] b) throws IOException {
                    checkNotClosed();
                    super.write(b);
                }

                @Override
                public synchronized void write(byte[] b, int off, int len) throws IOException {
                    checkNotClosed();
                    super.write(b, off, len);
                }

                private void checkNotClosed() throws IOException {
                    if (dos == null) {
                        throw new IOException("Stream closed");
                    }
                }
            };
            return new DataOutputStream(is);
        }

        private void reopenStream() throws IOException {
            ProActiveFileObject.log.debug("Reopening random access stream: " + streamId);
            try {
                streamId = getServer().streamOpen(getPath(), streamMode);
            } catch (Exception x) {
                throw Utils.generateAndLogIOExceptionCouldNotReopen(log, x);
            }

            if (position > 0) {
                try {
                    getServer().streamSeek(streamId, position);
                } catch (Exception x) {
                    close();
                    throw Utils.generateAndLogIOExceptionCouldNotReopen(log, x);
                }
            }
        }

        private void checkStreamModeReadWrite() throws IOException {
            if (streamMode != StreamMode.RANDOM_ACCESS_READ_WRITE) {
                throw new IOException("Incorrect stream mode");
            }
        }

        public void close() throws IOException {
            try {
                // these 2 closes should never fail
                if (dis != null) {
                    dis.close();
                }
                if (dos != null) {
                    dos.close();
                }
            } finally {
                try {
                    getServer().streamClose(streamId);
                } catch (StreamNotFoundException e) {
                    // ignore
                }
            }
        }

        public long getFilePointer() {
            if (streamMode == StreamMode.RANDOM_ACCESS_READ) {
                return bufInputStreamPosition;
            }
            return position;
        }

        public long length() throws IOException {
            try {
                try {
                    return getServer().streamGetLength(streamId);
                } catch (StreamNotFoundException e) {
                    reopenStream();
                    return getServer().streamGetLength(streamId);
                }
            } catch (WrongStreamTypeException e) {
                throw Utils.generateAndLogIOExceptionWrongStreamType(log, e);
            } catch (StreamNotFoundException e) {
                throw Utils.generateAndLogIOExceptionStreamNotFound(log, e);
            }
        }

        public void seek(long pos) throws IOException {
            try {
                try {
                    getServer().streamSeek(streamId, pos);
                } catch (StreamNotFoundException e) {
                    reopenStream();
                    getServer().streamSeek(streamId, pos);
                }
                this.position = pos;
                if (streamMode == StreamMode.RANDOM_ACCESS_READ) {
                    this.bufInputStreamPosition = pos;
                    getDataInputStream().close();
                }
            } catch (WrongStreamTypeException e) {
                throw Utils.generateAndLogIOExceptionWrongStreamType(log, e);
            } catch (StreamNotFoundException e) {
                throw Utils.generateAndLogIOExceptionStreamNotFound(log, e);
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().write(b);
        }

        @Override
        public void writeBoolean(boolean v) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeBoolean(v);
        }

        @Override
        public void writeByte(int v) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeByte(v);
        }

        @Override
        public void writeBytes(String s) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeBytes(s);
        }

        @Override
        public void writeChar(int v) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeChar(v);
        }

        @Override
        public void writeChars(String s) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeChars(s);
        }

        @Override
        public void writeDouble(double v) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeDouble(v);
        }

        @Override
        public void writeFloat(float v) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeFloat(v);
        }

        @Override
        public void writeInt(int v) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeInt(v);
        }

        @Override
        public void writeLong(long v) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeLong(v);
        }

        @Override
        public void writeShort(int v) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeShort(v);
        }

        @Override
        public void writeUTF(String str) throws IOException {
            checkStreamModeReadWrite();
            getDataOutputStream().writeUTF(str);
        }
    }
}
