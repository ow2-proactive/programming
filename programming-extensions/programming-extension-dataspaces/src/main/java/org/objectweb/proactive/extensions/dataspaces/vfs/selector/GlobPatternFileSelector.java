package org.objectweb.proactive.extensions.dataspaces.vfs.selector;

import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.log4j.Logger;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.FastFileSelector;
import org.objectweb.proactive.extensions.dataspaces.vfs.selector.fast.SelectorUtils;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * A VFS file selector meant to behave like {@link FastFileSelector}: given a
 * list of include patterns and a list of excludes patterns, this selector will
 * respond true if the file matches one of the includes patterns and does not
 * match any of the excludes patterns. In order to perform the match operation,
 * the base folder uri is extracted from the file uri and the remaining is
 * matched against the patterns. The matching operation is performed by
 * {@link SelectorUtils#matchPath(String, String)}
 *
 * @author The ProActive Team
 */
public final class GlobPatternFileSelector implements FileSelector {

    public static final Logger log = Logger.getLogger(GlobPatternFileSelector.class);

    private final Set<String> includes = new HashSet<String>();
    private final Set<String> excludes = new HashSet<String>();

    public GlobPatternFileSelector() {

    }

    public GlobPatternFileSelector(List<String> includes, List<String> excludes) {
        this.includes.addAll(includes);
        this.excludes.addAll(excludes);
    }

    @Override
    public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
        String buri = fileInfo.getBaseFolder().getURL().toString();
        String furi = fileInfo.getFile().getURL().toString();

        // we replace in a raw way the base uri (this replacement is not interpreted as a regex)
        String name = furi.replace(buri, "");
        // we remove any prepending slashes to the path remaining
        name = name.replaceFirst("/*", "");

        log.debug("Checking file " + name + "(" + furi + ")");

        if (matches(name, includes)) {
            log.debug("File " + name + " matches an include pattern");

            if (!matches(name, excludes)) {
                log.debug("File " + furi + " selected for copy.");
                return true;
            }
        }

        return false;
    }

    private boolean matches(String name, Set<String> exprs) {
        if (exprs == null || exprs.isEmpty()) {
            return false;
        } else if (exprs.contains(name)) {
            return true;
        } else {
            for (String expr : exprs) {
                if (SelectorUtils.matchPath(expr, name)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
        return true;
    }

    public void addIncludes(Collection<String> files) {
        if (files != null) {
            includes.addAll(files);
        }
    }

    public void addExcludes(Collection<String> files) {
        if (files != null) {
            excludes.addAll(files);
        }
    }

    public Set<String> getIncludes() {
        return new HashSet<String>(includes);
    }

    public Set<String> getExcludes() {
        return new HashSet<String>(excludes);
    }

}
