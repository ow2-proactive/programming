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
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.dataspaces.vfs.adapter;

import static java.util.Arrays.asList;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.List;

import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.FileContent;
import org.objectweb.proactive.extensions.dataspaces.api.RandomAccessContent;
import org.objectweb.proactive.extensions.dataspaces.api.RandomAccessMode;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;


public class VFSContentAdapter implements FileContent {

    final private org.apache.commons.vfs.FileContent adaptee;
    final private DataSpacesFileObject owningFile;

    public VFSContentAdapter(org.apache.commons.vfs.FileContent content, DataSpacesFileObject dsFileObject) {
        adaptee = content;
        owningFile = dsFileObject;
    }

    public void close() throws FileSystemException {
        try {
            adaptee.close();
        } catch (org.apache.commons.vfs.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public List<Certificate> getCertificates() throws FileSystemException {
        try {
            final Certificate[] vfsCerts = adaptee.getCertificates();
            return asList(vfsCerts);
        } catch (org.apache.commons.vfs.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public String getContentEncoding() {
        try {
            return adaptee.getContentInfo().getContentEncoding();
        } catch (org.apache.commons.vfs.FileSystemException e) {
            // parsing errors, a file server fault or not supported meta information
            return null;
        }
    }

    public String getContentMIMEType() {
        try {
            return adaptee.getContentInfo().getContentType();
        } catch (org.apache.commons.vfs.FileSystemException e) {
            // parsing errors, a file server fault or not supported meta information
            return null;
        }
    }

    public DataSpacesFileObject getFile() {
        return owningFile;
    }

    public InputStream getInputStream() throws FileSystemException {
        try {
            return adaptee.getInputStream();
        } catch (org.apache.commons.vfs.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public long getLastModifiedTime() throws FileSystemException {
        try {
            return adaptee.getLastModifiedTime();
        } catch (org.apache.commons.vfs.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public OutputStream getOutputStream() throws FileSystemException {
        try {
            return adaptee.getOutputStream();
        } catch (org.apache.commons.vfs.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public OutputStream getOutputStream(boolean append) throws FileSystemException {
        try {
            return adaptee.getOutputStream(append);
        } catch (org.apache.commons.vfs.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
        final org.apache.commons.vfs.util.RandomAccessMode vfsMode = buildVFSRandomAccessMode(mode);
        try {
            // according to this VFS build it cannot be null but check it in adaptVFSResult method
            return adaptVFSResult(adaptee.getRandomAccessContent(vfsMode));
        } catch (org.apache.commons.vfs.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public long getSize() throws FileSystemException {
        try {
            return adaptee.getSize();
        } catch (org.apache.commons.vfs.FileSystemException e) {
            throw new FileSystemException(e);
        }
    }

    public boolean isOpen() {
        return adaptee.isOpen();
    }

    private static RandomAccessContent adaptVFSResult(
            org.apache.commons.vfs.RandomAccessContent randomAccessContent) {
        return randomAccessContent == null ? null : new VFSRandomAccessContentAdapter(randomAccessContent);
    }

    private static org.apache.commons.vfs.util.RandomAccessMode buildVFSRandomAccessMode(RandomAccessMode mode) {
        switch (mode) {
            case READ_ONLY:
                return org.apache.commons.vfs.util.RandomAccessMode.READ;
            case READ_WRITE:
                return org.apache.commons.vfs.util.RandomAccessMode.READWRITE;
            default:
                return null;
        }
    }
}
