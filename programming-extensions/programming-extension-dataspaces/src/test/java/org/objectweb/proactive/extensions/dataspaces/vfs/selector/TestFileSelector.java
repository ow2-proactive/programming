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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.vfs2.FileSystemException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.objectweb.proactive.utils.OperatingSystem;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;


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
    public void testGetFilePathRelativeToBaseURIWithSpaces() throws FileSystemException {
        testGetFilePathRelativeToBaseURI("/home/bobot/",
                                         "/home/bobot/documents and more/test",
                                         "documents and more/test");
    }

    @Test
    public void testGetFilePathRelativeToBaseURIEscaped() throws FileSystemException {
        testGetFilePathRelativeToBaseURI("/home/bobot/",
                                         "/home/bobot/documents%20and%20more/test",
                                         "documents and more/test");
    }

    @Test
    public void testGetFilePathRelativeToBaseURIWithScheme() throws FileSystemException {
        String baseURI = "file:///home/bobot/Projects/scheduling/data/";
        baseURI += "defaultuser/admin/bobot_2015-05-10_17.40.43.585/input";

        testGetFilePathRelativeToBaseURI(baseURI, baseURI + "/input_3.txt", "input_3.txt");
    }

    private void testGetFilePathRelativeToBaseURI(String baseURI, String fileURI, String expectedResult)
            throws FileSystemException {
        assertThat(fileSelector.getFilePathRelativeToBaseURI(baseURI, fileURI), equalTo(expectedResult));
    }

    @Test
    public void testFileSelectionWithImplicitGlobPattern() throws IOException {
        testFileSelectionWithPattern("**/[o]?/*", true, -1);
    }

    @Test
    public void testFileSelectionWithExplicitGlobPattern() throws IOException {
        testFileSelectionWithPattern("glob:**/o?/*", true, -1);
        testFileSelectionWithPattern("glob:oh/o?/*", true, -1);
    }

    @Test
    public void testFileSelectionWithRegex() throws IOException {
        testFileSelectionWithPattern("regex:^.*[_]chocolate$", true, -1);
    }

    private void testFileSelectionWithPattern(String pattern, boolean expectedResult, int nonTraversableDepth)
            throws FileSystemException {
        Path root = folder.getRoot().toPath();

        Path dir = root.resolve("oh/oh");
        Path file = dir.resolve("i_love_chocolate");

        testWithIncludePattern(root.relativize(file).toString(), pattern, expectedResult, nonTraversableDepth);
    }

    @Test
    public void testIncludePatternUnixPath() throws IOException {
        assumeFalse(isRunningOnWindows());
        testWithIncludePattern("/home/bobot/documents/test", "**/test", true, -1);
    }

    @Test
    public void testIncludePatternWindowsPath() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\Bobot\\Documents\\test", "**\\test", true, -1);
    }

    @Test
    public void testIncludePatternUnixPathWithSpaces() throws IOException {
        assumeFalse(isRunningOnWindows());
        testWithIncludePattern("/home/bobot/documents and more/test", "**/test", true, -1);
        testWithIncludePattern("/home/bobot/documents and more/test", "**/documents?and?more/test", true, -1);
        testWithIncludePattern("/home/bobot/documents and more/test", "home/bobot/documents/**", false, 2);
    }

    @Test
    public void testIncludePatternWindowsPathWithSpaces() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\Bobot\\Documents And More\\test", "**\\test", true, -1);
        // for a strange reason the following pattern only works with forward slashes
        testWithIncludePattern("C:\\Users\\Bobot\\Documents And More\\test", "**/Documents?And?More/test", true, -1);
        testWithIncludePattern("C:\\Users\\Bobot\\Documents And More\\test", "Users\\Bobot\\Documents\\**", false, 2);
    }

    @Test
    public void testIncludePatternUnixPathEscaped() throws IOException {
        assumeFalse(isRunningOnWindows());
        testWithIncludePattern("/home/bobot/documents%20and%20more/test", "**/test", true, -1);
        testWithIncludePattern("/home/bobot/documents%20and%20more/test", "**/documents?and?more/test", true, -1);
        testWithIncludePattern("/home/bobot/documents%20and%20more/test", "home/bobot/documents/**", false, 2);
    }

    @Test
    public void testIncludePatternWindowsPathEscaped() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\Bobot\\Documents%20And%20More\\test", "**\\test", true, -1);
        // for a strange reason the following pattern only works with forward slashes
        testWithIncludePattern("C:\\Users\\Bobot\\Documents%20And%20More\\test",
                               "**/Documents?And?More/test",
                               true,
                               -1);
        testWithIncludePattern("C:\\Users\\Bobot\\Documents%20And%20More\\test",
                               "Users\\Bobot\\Documents\\**",
                               false,
                               2);
    }

    @Test
    public void testIncludePatternWindowsPathUppercasePath() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\BOBOT\\Documents\\TEST", "**\\test", true, -1);
    }

    @Test
    public void testIncludePatternWindowsPathUppercasePattern() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\Bobot\\Documents\\test", "**\\TEST", true, -1);
    }

    @Test
    public void testIncludePatternUnixPathUppercasePathAndPattern() throws IOException {
        assumeFalse(isRunningOnWindows());
        testWithIncludePattern("/home/bobot/documents/test", "**/test", true, -1);
        testWithIncludePattern("/home/bobot/documents/test", "**/myfolder/test", false, -1);
        testWithIncludePattern("/home/bobot/documents/test", "home/bobot/myfolder/**", false, 2);
    }

    @Test
    public void testIncludePatternWindowsPathUppercasePathAndPattern() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\BOBOT\\Documents\\TEST", "**\\TEST", true, -1);
        testWithIncludePattern("C:\\Users\\BOBOT\\Documents\\TEST", "**\\MyFolder\\TEST", false, -1);
        testWithIncludePattern("C:\\Users\\BOBOT\\Documents\\TEST", "Users\\BOBOT\\MyFolder\\**", false, 2);
    }

    @Test
    public void testIncludeExcludePatternUnixPath() throws IOException {
        assumeFalse(isRunningOnWindows());
        testWithIncludeAndExcludePattern("/home/bobot/documents/test", "**/test", "**/test", false, -1);
    }

    @Test
    public void testIncludeExcludePatternWindowsPath() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludeAndExcludePattern("C:\\Users\\Bobot\\Documents\\test", "**\\test", "**\\test", false, -1);
    }

    @Test
    public void testIncludeExcludePatternEmptyUnixPath() throws FileSystemException {
        assumeFalse(isRunningOnWindows());
        testWithIncludeAndExcludePattern("/home/bobot/documents/test", null, null, false, 0);
    }

    @Test
    public void testIncludeExcludePatternEmptyWindowsPath() throws FileSystemException {
        assumeTrue(isRunningOnWindows());
        testWithIncludeAndExcludePattern("C:\\Users\\Bobot\\Documents\\test", null, null, false, 0);
    }

    @Test
    public void testExcludePatternUnixPath() throws FileSystemException {
        assumeFalse(isRunningOnWindows());
        testWithExcludePattern("/home/bobot/documents/test", "**", false, -1);
    }

    @Test
    public void testExcludePatternWindowsPath() throws FileSystemException {
        assumeTrue(isRunningOnWindows());
        testWithExcludePattern("C:\\Users\\Bobot\\Documents\\test", "**", false, -1);
    }

    @Test
    public void testIncludePatternUnixPathWindowsPathPattern() throws IOException {
        assumeFalse(isRunningOnWindows());
        testWithIncludePattern("/home/bobot/documents/test", "**\\test", true, -1);
    }

    @Test
    public void testIncludePatternWindowsPathUnixPathPattern() throws IOException {
        assumeTrue(isRunningOnWindows());
        testWithIncludePattern("C:\\Users\\Bobot\\Documents\\test", "**/test", true, -1);
    }

    private void testWithIncludePattern(String path, String includePattern, boolean expectedResult,
            int nonTraversableDepth) throws FileSystemException {
        testWithIncludeAndExcludePattern(path, includePattern, null, expectedResult, nonTraversableDepth);
    }

    private void testWithExcludePattern(String path, String excludePattern, boolean expectedResult,
            int nonTraversableDepth) throws FileSystemException {
        testWithIncludeAndExcludePattern(path, null, excludePattern, expectedResult, nonTraversableDepth);
    }

    private void testWithIncludeAndExcludePattern(String path, String includePattern, String excludePattern,
            boolean expectedResult, int nonTraversableDepth) throws FileSystemException {
        Path p = Paths.get(path);

        if (includePattern != null) {
            fileSelector.setIncludes(includePattern);
        }

        if (excludePattern != null) {
            fileSelector.setExcludes(excludePattern);
        }

        boolean result = fileSelector.matches(p);

        assertEquals(expectedResult, result);

        if (expectedResult) {
            Path appendedPath = p.getRoot();
            int depth = 0;
            for (Iterator<Path> it = p.iterator(); it.hasNext();) {
                Path name = it.next();
                if (appendedPath == null) {
                    appendedPath = name;
                } else {
                    appendedPath = appendedPath.resolve(name);
                }

                if (depth == nonTraversableDepth) {
                    assertFalse("Path " + appendedPath + " should not be traversable",
                                fileSelector.traverseDescendents(appendedPath));
                } else {
                    assertTrue("Path " + appendedPath + " should be traversable",
                               fileSelector.traverseDescendents(appendedPath));
                }

            }
        }

    }

    private boolean isRunningOnWindows() {
        return OperatingSystem.getOperatingSystem() == OperatingSystem.windows;
    }

    @Test
    public void testConstructorWithStringArraysAsInput() {
        String[] includes = new String[] { "a" };
        String[] excludes = new String[] { "b", "c" };

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
        String[] includes = new String[] { "a" };

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
        String[] includes = new String[] { "a" };
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
        String[] excludes = new String[] { "a" };

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

        String[] includes = new String[] { "b", "c" };
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

        String[] excludes = new String[] { "b", "c" };
        fileSelector.setExcludes(excludes);

        assertThat(fileSelector.getExcludes(), not(contains("a")));
    }

    @Test
    public void testTransformIncludes() {
        fileSelector.addIncludes("a", "b", "c", "d");
        fileSelector.transformIncludes(new Function<String, String>() {
            @Override
            public String apply(String input) {
                return "prefix" + input;
            }
        });

        assertThat(fileSelector.getIncludes(), not(empty()));
        for (String pattern : fileSelector.getIncludes()) {
            assertThat(pattern, startsWith("prefix"));
        }
    }

    @Test
    public void testTransformExcludes() {
        fileSelector.addExcludes("a", "b", "c", "d");
        fileSelector.transformExcludes(new Function<String, String>() {
            @Override
            public String apply(String input) {
                return "prefix" + input;
            }
        });

        assertThat(fileSelector.getExcludes(), not(empty()));
        for (String pattern : fileSelector.getExcludes()) {
            assertThat(pattern, startsWith("prefix"));
        }
    }

}
