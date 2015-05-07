/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.dataspaces.vfs.selector;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * The purpose of this class is to select files according to a list of include
 * patterns and a list of excludes patterns. This selector will respond {@code
 * true} if the file that is analyzed matches one of the includes patterns and
 * does not match any of the excludes patterns. In order to perform the match
 * operation, the base folder URI is extracted from the file URI and the
 * remaining is matched against the patterns.
 * <p/>
 * The matching operation is delegated to the JDK implementation. By default, if
 * no syntax is specified with a pattern, then glob patterns are used. Other
 * syntaxes are supported, by prefixing patterns with a prefix such as {@code
 * regex:} for REGEX patterns.
 * 
 * @author The ProActive Team
 * @see FileSystem#getPathMatcher(String)
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
public class FileSelector implements org.apache.commons.vfs2.FileSelector, Serializable {

    private static final Logger log = Logger.getLogger(FileSelector.class);

    private static final String PREFIX_GLOB_PATTERN = "glob:";

    private static final String PREFIX_REGEX_PATTERN = "regex:";

    private Set<String> includes;

    private Set<String> excludes;

    private final transient FileSystem fileSystem;

    public FileSelector() {
        fileSystem = FileSystems.getDefault();

        includes = new HashSet<>();
        excludes = new HashSet<>();
    }

    public FileSelector(Collection<String> includes, Collection<String> excludes) {
        this();
        addAll(this.includes, includes);
        addAll(this.excludes, excludes);
    }

    public FileSelector(String[] includes, String[] excludes) {
        this();
        addAll(this.includes, includes);
        addAll(this.excludes, excludes);
    }

    public FileSelector(String... includes) {
        this();
        addAll(this.includes, includes);
    }

    public void clear() {
        includes.clear();
        excludes.clear();
    }

    @Override
    public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        return matches(getFilePathRelativeToBaseURI(fileInfo));
    }

    public boolean matches(Path path) {

        boolean isDebugEnabled = log.isDebugEnabled();

        if (isDebugEnabled) {
            log.debug("Checking file '" + path + "'");
        }

        if (matches(path, includes)) {
            if (isDebugEnabled) {
                log.debug("Path '" + path + "' matches an include pattern " + includes);
            }

            if (!matches(path, excludes)) {
                if (isDebugEnabled) {
                    log.debug("Path '" + path + "' matches no exclude pattern");
                    log.debug("Path '" + path + "' selected for copy.");
                }

                return true;
            }
        }

        return false;
    }

    private Path getFilePathRelativeToBaseURI(FileSelectInfo fileSelectInfo) throws FileSystemException {
        String baseURI = fileSelectInfo.getBaseFolder().getURL().toString();
        String fileURI = fileSelectInfo.getFile().getURL().toString();

        return fileSystem.getPath(getFilePathRelativeToBaseURI(baseURI, fileURI));
    }

    /*
     * Returns a file path relative to {@code baseURI}. In other words, it
     * removes {@code baseURI} from the specified {@code fileURI} along with the
     * leading slashes.
     * <p>
     * It is assumed that the {@code baseURI} is always present in {@code fileURI}.
     */
    protected String getFilePathRelativeToBaseURI(String baseURI, String fileURI) {
        int nbSlashToIgnore = 0;

        // compute begin indexing for substring
        for (int i = baseURI.length(); i < fileURI.length(); i++) {
            if (fileURI.charAt(i) == '/') {
                nbSlashToIgnore++;
            } else {
                break;
            }
        }

        return fileURI.substring(baseURI.length() + nbSlashToIgnore);
    }

    private boolean matches(Path path, Set<String> patterns) {
        if (patterns.isEmpty()) {
            return false;
        }

        if (patterns.contains(path.toString())) {
            return true;
        } else {
            for (PathMatcher pattern : asPathMatchers(patterns)) {
                if (pattern.matches(path)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<PathMatcher> asPathMatchers(Collection<String> patterns) {
        Set<PathMatcher> result = new HashSet<>(patterns.size());

        for (String pattern : patterns) {
            result.add(asPathMatcher(pattern));
        }

        return result;
    }

    private PathMatcher asPathMatcher(String pattern) {
        // if the syntax is not specified with the pattern
        // then it is assumed that the pattern is a glob pattern
        if (!pattern.startsWith(PREFIX_GLOB_PATTERN) &&
                !pattern.startsWith(PREFIX_REGEX_PATTERN)) {
            pattern = PREFIX_GLOB_PATTERN + pattern;
        }

        return fileSystem.getPathMatcher(pattern);
    }

    @Override
    public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return true;
    }

    public void addIncludes(Collection<String> patterns) {
        addAll(includes, patterns);
    }

    public void addIncludes(String... patterns) {
        addAll(includes, patterns);
    }

    public void addExcludes(Collection<String> patterns) {
        addAll(excludes, patterns);
    }

    public void addExcludes(String... patterns) {
        addAll(excludes, patterns);
    }

    public Set<String> getIncludes() {
        return ImmutableSet.copyOf(includes);
    }

    public Set<String> getExcludes() {
        return ImmutableSet.copyOf(excludes);
    }

    public void setIncludes(Collection<String> patterns) {
        includes.clear();
        addAll(includes, patterns);
    }

    public void setIncludes(String... patterns) {
        includes.clear();
        addAll(includes, patterns);
    }

    public void setExcludes(Collection<String> patterns) {
        excludes.clear();
        addAll(excludes, patterns);
    }

    public void setExcludes(String... patterns) {
        excludes.clear();
        addAll(excludes, patterns);
    }

    public void transformIncludes(Function<String, String> function) {
        includes = Sets.newHashSet(Collections2.transform(includes, function));
    }

    public void transformExcludes(Function<String, String> function) {
        excludes = Sets.newHashSet(Collections2.transform(excludes, function));
    }

    private <T> void addAll(Set<T> receivingSet, Collection<T> elementsToAdd) {
        if (elementsToAdd != null && elementsToAdd.size() > 0) {
            receivingSet.addAll(elementsToAdd);
        }
    }

    @SafeVarargs
    private final <T> void addAll(Set<T> receivingSet, T... elementsToAdd) {
        if (elementsToAdd != null && elementsToAdd.length > 0) {
            Collections.addAll(receivingSet, elementsToAdd);
        }
    }

    /**
     * Return a string representation of this selector. All selection patterns
     * are displayed.
     */
    @Override
    public String toString() {
        return "FileSelector{" +
                "includes=" + includes +
                ", excludes=" + excludes +
                '}';
    }

}
