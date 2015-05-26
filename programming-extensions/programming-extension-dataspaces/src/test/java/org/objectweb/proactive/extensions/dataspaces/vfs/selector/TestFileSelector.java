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
import com.google.common.collect.ImmutableSet;
import org.apache.commons.vfs2.FileSystemException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.utils.OperatingSystem;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

public class TestFileSelector {

    private FileSelector fileSelector;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void tearDown() {
        fileSelector = new FileSelector();
    }

    @Test
    public void testGetFilePathRelativeToBaseURI() throws FileSystemException {
        testGetFilePathRelativeToBaseURI("/home/bobot/", "/home/bobot/folder/test", "folder/test");
    }

    @Test
    public void testGetFilePathRelativeToBaseURIMultipleSlashes() throws FileSystemException {
        testGetFilePathRelativeToBaseURI("/home/bobot/", "/home/bobot////folder/test", "folder/test");
    }

    @Test
    public void testGetFilePathRelativeToBaseURIWithScheme() throws FileSystemException {
        String baseURI = "file:///home/bobot/Projects/scheduling/data/";
        baseURI += "defaultuser/admin/bobot_2015-05-10_17.40.43.585/input";

        testGetFilePathRelativeToBaseURI(baseURI, baseURI + "/input_3.txt", "input_3.txt");
    }

    private void testGetFilePathRelativeToBaseURI(String baseURI, String fileURI, String expectedResult) {
        assertThat(fileSelector.getFilePathRelativeToBaseURI(
                baseURI, fileURI), equalTo(expectedResult));
    }

    @Test
    public void testFileSelectionWithImplicitGlobPattern() throws IOException {
        testFileSelectionWithPattern("**/[o]?/*");
    }

    @Test
    public void testFileSelectionWithExplicitGlobPattern() throws IOException {
        testFileSelectionWithPattern("glob:**/[o]?/*");
    }

    @Test
    public void testFileSelectionWithRegex() throws IOException {
        testFileSelectionWithPattern("regex:^.*[_]chocolate$");
    }

    private void testFileSelectionWithPattern(String pattern) {
        Path root = folder.getRoot().toPath();

        Path dir = root.resolve("oh/oh");
        Path file = dir.resolve("i_love_chocolate");

        testWithIncludePattern(root.relativize(file).toString(), pattern, true);
    }

    @Test
    public void testIncludePatternUnixPath() throws IOException {
        testWithIncludePattern("/home/bobot/documents/test", "**/test", true);
    }

    @Test
    public void testIncludePatternWindowsPath() throws IOException {
        testWithIncludePattern("C:\\Users\\Bobot\\Documents\\test", "**\\test", true);
    }

    @Test
    public void testIncludePatternUnixPathWithSpaces() throws IOException {
        testWithIncludePattern("/home/bobot/documents and more/test", "**/test", true);
    }

    @Test
    public void testIncludePatternWindowsPathWithSpaces() throws IOException {
        testWithIncludePattern("C:\\Users\\Bobot\\Documents And More\\test", "**\\test", true);
    }

    @Test
    public void testIncludePatternWindowsPathUppercasePath() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\BOBOT\\Documents\\TEST", "**\\test", true);
    }

    @Test
    public void testIncludePatternWindowsPathUppercasePattern() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\Bobot\\Documents\\test", "**\\TEST", true);
    }

    @Test
    public void testIncludePatternUnixPathUppercasePathAndPattern() throws IOException {
        testWithIncludePattern("/home/bobot/documents/test", "**/test", true);
    }

    @Test
    public void testIncludePatternWindowsPathUppercasePathAndPattern() throws IOException {
        testWithIncludePattern("C:\\Users\\BOBOT\\Documents\\TEST", "**\\TEST", true);
    }

    @Test
    public void testIncludeExcludePatternUnixPath() throws IOException {
        testWithIncludeAndExcludePattern("/home/bobot/documents/test", "**/test", "**/test", false);
    }

    @Test
    public void testIncludeExcludePatternWindowsPath() throws IOException {
        testWithIncludeAndExcludePattern("C:\\Users\\Bobot\\Documents\\test", "**\\test", "**\\test", false);
    }

    @Test
    public void testIncludeExcludePatternEmptyUnixPath() {
        testWithIncludeAndExcludePattern("/home/bobot/documents/test", null, null, false);
    }

    @Test
    public void testIncludeExcludePatternEmptyWindowsPath() {
        testWithIncludeAndExcludePattern("C:\\Users\\Bobot\\Documents\\test", null, null, false);
    }

    @Test
    public void testExcludePatternUnixPath() {
        testWithExcludePattern("/home/bobot/documents/test", "**", false);
    }

    @Test
    public void testExcludePatternWindowsPath() {
        testWithExcludePattern("C:\\Users\\Bobot\\Documents\\test", "**", false);
    }

    @Test
    public void testIncludePatternUnixPathWindowsPathPattern() throws IOException {
        testWithIncludePattern("/home/bobot/documents/test", "**\\test", true);
    }

    @Test
    public void testIncludePatternWindowsPathUnixPathPattern() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\Bobot\\Documents\\test", "**/test", true);
    }

    private void testWithIncludePattern(String path, String includePattern, boolean expectedResult) {
        testWithIncludeAndExcludePattern(path, includePattern, null, expectedResult);
    }

    private void testWithExcludePattern(String path, String excludePattern, boolean expectedResult) {
        testWithIncludeAndExcludePattern(path, null, excludePattern, expectedResult);
    }

    private void testWithIncludeAndExcludePattern(String path, String includePattern, String excludePattern, boolean expectedResult) {
        Path p = Paths.get(path);

        if (includePattern != null) {
            fileSelector.setIncludes(includePattern);
        }

        if (excludePattern != null) {
            fileSelector.setExcludes(excludePattern);
        }

        boolean result = fileSelector.matches(p);

        assertEquals(expectedResult, result);
    }

    private boolean isRunningOnWindows() {
        return OperatingSystem.getOperatingSystem() == OperatingSystem.windows;
    }

    @Test
    public void testConstructorWithStringArraysAsInput() {
        String[] includes = new String[]{"a"};
        String[] excludes = new String[]{"b", "c"};

        fileSelector = new FileSelector(includes, excludes);

        assertThat(fileSelector.getIncludes(), containsInAnyOrder(includes));
        assertThat(fileSelector.getExcludes(), containsInAnyOrder(excludes));

        fileSelector.clear();

        assertThat(fileSelector.getIncludes(), empty());
        assertThat(fileSelector.getExcludes(), empty());
    }

    @Test
    public void testConstructorWithCollectionsOfStringAsInput() {
        Set<String> includes = ImmutableSet.of("a");
        Set<String> excludes = ImmutableSet.of("b", "c");

        fileSelector = new FileSelector(includes, excludes);

        assertThat(fileSelector.getIncludes(), is(includes));
        assertThat(fileSelector.getExcludes(), is(excludes));
    }

    @Test
    public void testConstructorWithIncludePatternsOnlyAsStringArray() {
        String[] includes = new String[]{"a"};

        fileSelector = new FileSelector(includes);

        assertThat(fileSelector.getIncludes(), containsInAnyOrder(includes));
        assertThat(fileSelector.getExcludes(), empty());
    }

    @Test
    public void testEmptyConstructor() {
        assertThat(fileSelector.getIncludes(), empty());
        assertThat(fileSelector.getExcludes(), empty());
    }

    @Test
    public void testAddIncludesStringArray() {
        String[] includes = new String[]{"a"};
        fileSelector.addIncludes(includes);

        assertThat(fileSelector.getIncludes(), containsInAnyOrder(includes));
    }

    @Test
    public void testAddIncludesStringCollection() {
        Set<String> includes = ImmutableSet.of("a");

        assertThat(fileSelector.getIncludes(), empty());

        fileSelector.addIncludes(includes);

        assertThat(fileSelector.getIncludes(), is(includes));
    }

    @Test
    public void testAddExcludesStringArray() {
        String[] excludes = new String[]{"a"};

        assertThat(fileSelector.getIncludes(), empty());

        fileSelector.addExcludes(excludes);

        assertThat(fileSelector.getExcludes(), containsInAnyOrder(excludes));
    }

    @Test
    public void testAddExcludesStringCollection() {
        Set<String> excludes = ImmutableSet.of("a");

        assertThat(fileSelector.getIncludes(), empty());

        fileSelector.addExcludes(excludes);

        assertThat(fileSelector.getExcludes(), is(excludes));
    }

    @Test
    public void testClear() {
        fileSelector.addIncludes("a", "b");
        fileSelector.addExcludes("c", "d");
        fileSelector.clear();

        assertThat(fileSelector.getIncludes(), empty());
        assertThat(fileSelector.getExcludes(), empty());
    }

    @Test
    public void testSetIncludesStringCollection() {
        fileSelector.addIncludes("a");

        Set<String> includes = ImmutableSet.of("b", "c");
        fileSelector.setIncludes(includes);

        assertThat(fileSelector.getIncludes(), not(contains("a")));
    }

    @Test
    public void testSetIncludesStringArray() {
        fileSelector.addIncludes("a");

        String[] includes = new String[]{"b", "c"};
        fileSelector.setIncludes(includes);

        assertThat(fileSelector.getIncludes(), not(contains("a")));
    }

    @Test
    public void testSetExcludesStringCollection() {
        fileSelector.addExcludes("a");

        Set<String> excludes = ImmutableSet.of("b", "c");
        fileSelector.setExcludes(excludes);

        assertThat(fileSelector.getExcludes(), not(contains("a")));
    }

    @Test
    public void testSetExcludesStringArray() {
        fileSelector.addExcludes("a");

        String[] excludes = new String[]{"b", "c"};
        fileSelector.setExcludes(excludes);

        assertThat(fileSelector.getExcludes(), not(contains("a")));
    }

    @Test
    public void testTransformIncludes() {
        fileSelector.addIncludes("a", "b", "c", "d");
        fileSelector.transformIncludes(
                new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return "prefix" + input;
                    }
                }
        );

        assertThat(fileSelector.getIncludes(), not(empty()));
        for (String pattern : fileSelector.getIncludes()) {
            assertThat(pattern, startsWith("prefix"));
        }
    }

    @Test
    public void testTransformExcludes() {
        fileSelector.addExcludes("a", "b", "c", "d");
        fileSelector.transformExcludes(
                new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return "prefix" + input;
                    }
                }
        );

        assertThat(fileSelector.getExcludes(), not(empty()));
        for (String pattern : fileSelector.getExcludes()) {
            assertThat(pattern, startsWith("prefix"));
        }
    }

}
