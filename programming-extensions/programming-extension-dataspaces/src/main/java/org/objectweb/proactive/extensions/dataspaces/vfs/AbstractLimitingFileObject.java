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
package org.objectweb.proactive.extensions.dataspaces.vfs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileContentInfo;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.NameScope;
import org.apache.commons.vfs2.RandomAccessContent;
import org.apache.commons.vfs2.impl.DecoratedFileObject;
import org.apache.commons.vfs2.util.RandomAccessMode;


/**
 * Abstract FileObject decorator, checking if to limit write or ancestor access to the file basing
 * on pluggable rules.
 * <p>
 * Decorator may limit write access basing on {@link #isReadOnly()} method: direct write access
 * (like deleting file, opening output stream from getContent()), write checks (like isWriteable()).
 * <p>
 * Decorator may limit ancestor access (for resolve and getParent queries) starting from some level
 * basing on pluggable rule {@link #canReturnAncestor(FileObject)}.
 * <p>
 * It also decorates every returned FileObject. Way of decorating returned files is also pluggable
 * through {@link #doDecorateFile(FileObject)}.
 * <p>
 * Limits for write actions and resolve queries are enforced by throwing {@link FileSystemException}
 * , query methods like {@link #isWriteable()} changes behavior, while {@link #getParent()} return
 * null for accessing disallowed ancestor.
 * <p>
 * <strong>Known limitations of decorator:</strong>
 * <ul>
 * <li>canRenameTo() invoked on non-decorated object with decorated object as target may return
 * false information</li>
 * <li>returned FileContent returns undecorated file for getFile(); depends on VFS bug: VFS-259
 * (fixed in VFS fork)</li>
 * <li>moveTo() invoked on non-decorated object with decorated object as target may not work for
 * some buggy providers; depends on VFS bug: VFS-258 (fixed in VFS fork)</li>
 * </ul>
 * <p>
 * Generic type should be an implementor type - class returned by
 * {@link #doDecorateFile(FileObject)}.
 */
// I do not know if it is technically possible to express generic type as an implementor class type.
// So, implementors have to set it to their own type.
public abstract class AbstractLimitingFileObject<T extends FileObject> extends DecoratedFileObject {

    public AbstractLimitingFileObject(final FileObject fileObject) {
        super(fileObject);
    }

    /**
     * @param file
     *            file to decorate for calls returning FileObjects; never <code>null</code>
     * @return decorated FileObject
     */
    protected abstract T doDecorateFile(FileObject file);

    /**
     * @return <code>true</code> if file is read-only, <code>false</code> otherwise
     */
    protected abstract boolean isReadOnly();

    /**
     * Checks whether provided ancestor can be returned. The rule should be consistent among
     * siblings and ancestors of given ancestor. I.e. if one ancestor is denied, then limitation
     * should concern also its siblings and ancestors in file system tree.
     *
     * @param decoratedAncestor
     *            ancestor to return, with decorator already applied by
     *            {@link #doDecorateFile(FileObject)}.
     * @return <code>true</code> if this decorated ancestor can be returned in any call,
     *         <code>false</code> otherwise
     */
    protected abstract boolean canReturnAncestor(T decoratedAncestor);

    @Override
    public boolean canRenameTo(FileObject newfile) {
        return !isReadOnly();
    }

    @Override
    public boolean isWriteable() throws FileSystemException {
        return !isReadOnly();
    }

    @Override
    public void copyFrom(FileObject srcFile, FileSelector selector) throws FileSystemException {
        checkIsNotReadOnly();
        super.copyFrom(srcFile, selector);
    }

    @Override
    public void createFile() throws FileSystemException {
        checkIsNotReadOnly();
        super.createFile();
    }

    @Override
    public void createFolder() throws FileSystemException {
        checkIsNotReadOnly();
        super.createFolder();
    }

    @Override
    public boolean delete() throws FileSystemException {
        checkIsNotReadOnly();
        return super.delete();
    }

    @Override
    public int delete(FileSelector selector) throws FileSystemException {
        checkIsNotReadOnly();
        return super.delete(selector);
    }

    @Override
    public void moveTo(FileObject destFile) throws FileSystemException {
        checkIsNotReadOnly();
        super.moveTo(destFile);
    }

    @Override
    public FileObject resolveFile(String name, NameScope scope) throws FileSystemException {
        final T resolvedDecorated = decorateFile(super.resolveFile(name, scope));
        checkIsNotDisallowedAncestor(resolvedDecorated);
        return resolvedDecorated;
    }

    @Override
    public FileObject resolveFile(String path) throws FileSystemException {
        final T resolvedDecorated = decorateFile(super.resolveFile(path));
        checkIsNotDisallowedAncestor(resolvedDecorated);
        return resolvedDecorated;
    }

    @Override
    public FileObject[] findFiles(FileSelector selector) throws FileSystemException {
        return decorateFiles(super.findFiles(selector));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void findFiles(FileSelector selector, boolean depthwise, List selected) throws FileSystemException {
        final List<FileObject> selectedList = new ArrayList<FileObject>();
        super.findFiles(selector, depthwise, selectedList);
        selected.addAll(decorateFiles(selectedList));
    }

    @Override
    public FileObject getChild(String name) throws FileSystemException {
        final FileObject child = super.getChild(name);
        if (child == null)
            return null;
        return decorateFile(child);
    }

    @Override
    public FileObject[] getChildren() throws FileSystemException {
        return decorateFiles(super.getChildren());
    }

    @Override
    public FileObject getParent() throws FileSystemException {
        final T parentDecorated = decorateFile(super.getParent());
        if (isDisallowedAncestor(parentDecorated)) {
            return null;
        }
        return parentDecorated;
    }

    @Override
    public FileContent getContent() throws FileSystemException {
        return new LimitingFileContent(super.getContent());
    }

    private void checkIsNotReadOnly() throws FileSystemException {
        if (isReadOnly()) {
            throw new FileSystemException("File is read-only: " + this.toString());
        }
    }

    private T decorateFile(final FileObject file) {
        if (file == null)
            return null;
        return doDecorateFile(file);
    }

    private FileObject[] decorateFiles(final FileObject files[]) throws FileSystemException {
        if (files == null)
            return null;

        final FileObject result[] = new FileObject[files.length];
        for (int i = 0; i < files.length; i++)
            result[i] = decorateFile(files[i]);
        return result;
    }

    private List<FileObject> decorateFiles(final List<FileObject> files) throws FileSystemException {
        final List<FileObject> result = new ArrayList<FileObject>(files.size());
        for (final FileObject fo : files)
            result.add(decorateFile(fo));
        return result;
    }

    private boolean isDisallowedAncestor(final T decoratedFile) {
        if (decoratedFile == null)
            return false;
        if (getName().isDescendent(decoratedFile.getName(), NameScope.DESCENDENT_OR_SELF)) {
            return false;
        }
        return !canReturnAncestor(decoratedFile);
    }

    private void checkIsNotDisallowedAncestor(final T decoratedFile) throws FileSystemException {
        if (isDisallowedAncestor(decoratedFile)) {
            throw new FileSystemException("Access denied to one of resolved file ancestor(s)");
        }
    }

    private class LimitingFileContent implements FileContent {
        private FileContent content;

        public LimitingFileContent(FileContent content) {
            this.content = content;
        }

        public void close() throws FileSystemException {
            content.close();
        }

        public Object getAttribute(String attrName) throws FileSystemException {
            return content.getAttribute(attrName);
        }

        public String[] getAttributeNames() throws FileSystemException {
            return content.getAttributeNames();
        }

        public Map<String, Object> getAttributes() throws FileSystemException {
            return content.getAttributes();
        }

        public Certificate[] getCertificates() throws FileSystemException {
            return content.getCertificates();
        }

        public FileContentInfo getContentInfo() throws FileSystemException {
            return content.getContentInfo();
        }

        public FileObject getFile() {
            return decorateFile(content.getFile());
        }

        public InputStream getInputStream() throws FileSystemException {
            return content.getInputStream();
        }

        public long getLastModifiedTime() throws FileSystemException {
            return content.getLastModifiedTime();
        }

        public OutputStream getOutputStream() throws FileSystemException {
            checkIsNotReadOnly();
            return content.getOutputStream();
        }

        public OutputStream getOutputStream(boolean append) throws FileSystemException {
            checkIsNotReadOnly();
            return content.getOutputStream(append);
        }

        public RandomAccessContent getRandomAccessContent(RandomAccessMode mode) throws FileSystemException {
            if (mode.requestWrite())
                checkIsNotReadOnly();
            return content.getRandomAccessContent(mode);
        }

        public long getSize() throws FileSystemException {
            return content.getSize();
        }

        public boolean hasAttribute(String attrName) throws FileSystemException {
            return content.hasAttribute(attrName);
        }

        public boolean isOpen() {
            return content.isOpen();
        }

        /**
         * No check for read-only state is performed with the following write
         * operations since the current file object is the provider not the recipient.
         */
        @Override
        public long write(FileContent output) throws IOException {
            return content.write(output);
        }

        @Override
        public long write(FileObject file) throws IOException {
            return content.write(file);
        }

        @Override
        public long write(OutputStream output) throws IOException {
            return content.write(output);
        }

        @Override
        public long write(OutputStream output, int bufferSize) throws IOException {
            return content.write(output, bufferSize);
        }

        public void removeAttribute(String attrName) throws FileSystemException {
            checkIsNotReadOnly();
            content.removeAttribute(attrName);
        }

        public void setAttribute(String attrName, Object value) throws FileSystemException {
            checkIsNotReadOnly();
            content.setAttribute(attrName, value);
        }

        public void setLastModifiedTime(long modTime) throws FileSystemException {
            checkIsNotReadOnly();
            content.setLastModifiedTime(modTime);
        }

    }

}
