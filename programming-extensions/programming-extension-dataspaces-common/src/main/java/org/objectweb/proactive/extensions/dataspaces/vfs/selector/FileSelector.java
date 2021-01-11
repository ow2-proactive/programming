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
package org.objectweb.proactive.extensions.dataspaces.vfs.selector;

import java.io.Serializable;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.springframework.util.AntPathMatcher;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;


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

    private final transient Map<String, PathMatcher> pathMatcherCache;

    public FileSelector() {
        fileSystem = FileSystems.getDefault();

        includes = new HashSet<>();
        excludes = new HashSet<>();
        pathMatcherCache = new HashMap<>();
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

    public boolean matches(Path path) throws FileSystemException {

        boolean isDebugEnabled = log.isDebugEnabled();

        Path unescapedPath = Paths.get(UriParser.decode(path.toString()));

        if (isDebugEnabled) {
            log.debug("Checking file '" + unescapedPath + "'");
        }

        if (matches(unescapedPath, includes)) {
            if (isDebugEnabled) {
                log.debug("Path '" + unescapedPath + "' matches an include pattern " + includes);
            }

            if (!matches(unescapedPath, excludes)) {
                if (isDebugEnabled) {
                    log.debug("Path '" + unescapedPath + "' matches no exclude pattern");
                    log.debug("Path '" + unescapedPath + "' selected for copy.");
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
    protected String getFilePathRelativeToBaseURI(String baseURI, String fileURI) throws FileSystemException {
        int nbSlashToIgnore = 0;

        // compute begin indexing for substring
        for (int i = baseURI.length(); i < fileURI.length(); i++) {
            if (fileURI.charAt(i) == '/') {
                nbSlashToIgnore++;
            } else {
                break;
            }
        }

        return UriParser.decode(fileURI.substring(baseURI.length() + nbSlashToIgnore));
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
        if (!pattern.startsWith(PREFIX_GLOB_PATTERN) && !pattern.startsWith(PREFIX_REGEX_PATTERN)) {
            pattern = PREFIX_GLOB_PATTERN + pattern;
        }
        if (pathMatcherCache.containsKey(pattern)) {
            return pathMatcherCache.get(pattern);
        }

        PathMatcher matcher = fileSystem.getPathMatcher(pattern);
        pathMatcherCache.put(pattern, matcher);
        return matcher;
    }

    private String convertPathForAntPathMatcher(Path path, String targetSeparator) {
        String convertedPath = path.toString().replace("\\", targetSeparator).replace("/", targetSeparator);
        if (convertedPath.startsWith(targetSeparator)) {
            convertedPath = convertedPath.substring(targetSeparator.length());
        }
        return convertedPath;
    }

    @Override
    public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        Path path = getFilePathRelativeToBaseURI(fileInfo);
        return traverseDescendents(path);
    }

    public boolean traverseDescendents(Path path) {
        boolean atLeastOnePatternMatchSoFar = false;

        // Only include patterns can be handled, as if an exclude pattern is for example **/dir, it is not possible to decide on a parent folder if the folder should be skipped

        for (String include : includes) {
            if (include.startsWith(PREFIX_GLOB_PATTERN)) {
                include = include.replace(PREFIX_GLOB_PATTERN, "");
            } else if (include.startsWith(PREFIX_REGEX_PATTERN)) {
                return true;
            }
            if (include.contains("[") || include.contains("{")) {
                // AntPathMatcher does not support square or curly brackets expression, simply return true
                return true;
            }
            String targetSeparator = include.contains("\\") ? "\\" : "/";

            String convertedPath = convertPathForAntPathMatcher(path, targetSeparator);

            AntPathMatcher pathMatcher = new AntPathMatcher();
            pathMatcher.setPathSeparator(targetSeparator);
            // set case sensitive false by default, as it's better to traverse descendants as to exclude wrong files
            pathMatcher.setCaseSensitive(false);
            try {
                atLeastOnePatternMatchSoFar = atLeastOnePatternMatchSoFar ||
                                              pathMatcher.matchStart(include, convertedPath.toString());
            } catch (Exception e) {
                // maybe the pattern uses a syntax not supported by AntPathMatcher.
                log.debug("Exception occurred while using AntPathMatcher with the pattern " + include, e);
                // in that case we cannot take a decision and must traverse the folder.
                atLeastOnePatternMatchSoFar = true;
            }
        }
        return atLeastOnePatternMatchSoFar;
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
        return "FileSelector{" + "includes=" + includes + ", excludes=" + excludes + '}';
    }

}
