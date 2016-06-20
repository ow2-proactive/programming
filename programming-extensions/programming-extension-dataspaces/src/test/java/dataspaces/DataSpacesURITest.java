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
package dataspaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceType;
import org.objectweb.proactive.extensions.dataspaces.exceptions.MalformedURIException;


public class DataSpacesURITest {

    private DataSpacesURI uri;
    private DataSpacesURI uri2;

    @Test
    public void testCreateURIApp() {
        uri = DataSpacesURI.createURI("123");

        assertEquals("123", uri.getAppId());
        assertNull(uri.getSpaceType());
        assertNull(uri.getName());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
        assertNull(uri.getRelativeToSpace());
        assertTrue(uri.isSpacePartOnly());
        assertFalse(uri.isSpacePartFullyDefined());
        assertFalse(uri.isSuitableForUserPath());
    }

    @Test
    public void testCreateURIAppType() {
        uri = DataSpacesURI.createURI("123", SpaceType.SCRATCH);

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertNull(uri.getName());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
        assertNull(uri.getRelativeToSpace());
        assertTrue(uri.isSpacePartOnly());
        assertFalse(uri.isSpacePartFullyDefined());
        assertFalse(uri.isSuitableForUserPath());
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntime() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertEquals("runtimeA", uri.getRuntimeId());
        assertNull(uri.getName());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
        assertNull(uri.getRelativeToSpace());
        assertTrue(uri.isSpacePartOnly());
        assertFalse(uri.isSpacePartFullyDefined());
        assertFalse(uri.isSuitableForUserPath());
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeNode() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertEquals("runtimeA", uri.getRuntimeId());
        assertEquals("nodeB", uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getName());
        assertNull(uri.getUserPath());
        assertNull(uri.getRelativeToSpace());
        assertTrue(uri.isSpacePartFullyDefined());
        assertTrue(uri.isSpacePartOnly());
        assertFalse(uri.isSuitableForUserPath());
    }

    @Test
    public void testCreateScratchSpaceURIAppWrongRuntimeNode1() {
        try {
            DataSpacesURI.createScratchSpaceURI("123", "", "nodeB");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppWrongRuntimeNode2() {
        try {
            DataSpacesURI.createScratchSpaceURI("123", "ooops/ooops", "nodeB");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeWrongNode1() {
        try {
            DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeWrongNode2() {
        try {
            DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "ooops/ooops");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppNoRuntimeNodeNoActiveObject() {
        try {
            uri = DataSpacesURI.createScratchSpaceURI("123", null, "nodeB");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeNodeActiveObject() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertEquals("runtimeA", uri.getRuntimeId());
        assertEquals("nodeB", uri.getNodeId());
        assertEquals("aoC", uri.getActiveObjectId());
        assertEquals("aoC", uri.getRelativeToSpace());
        assertNull(uri.getUserPath());
        assertNull(uri.getName());
        assertTrue(uri.isSpacePartFullyDefined());
        assertTrue(uri.isSuitableForUserPath());
        assertFalse(uri.isSpacePartOnly());
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeNodeWrongActiveObject1() {
        try {
            DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeNodeWrongActiveObject2() {
        try {
            DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "ooops/ooops");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppNoRuntimeNodeActiveObject() {
        try {
            uri = DataSpacesURI.createScratchSpaceURI("123", null, "nodeB", "aoC");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeNoNodeActiveObject() {
        try {
            uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", null, "aoC");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppNoRuntimeNoNodeActiveObject() {
        try {
            uri = DataSpacesURI.createScratchSpaceURI("123", null, null, "aoC");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeNodeActiveObjectPath() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "dir/file.txt");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertEquals("runtimeA", uri.getRuntimeId());
        assertEquals("nodeB", uri.getNodeId());
        assertEquals("aoC", uri.getActiveObjectId());
        assertEquals("dir/file.txt", uri.getUserPath());
        assertEquals("aoC/dir/file.txt", uri.getRelativeToSpace());
        assertNull(uri.getName());
        assertTrue(uri.isSpacePartFullyDefined());
        assertTrue(uri.isSuitableForUserPath());
        assertFalse(uri.isSpacePartOnly());
    }

    @Test
    public void testCreateScratchSpaceURIAppRuntimeNodeNoActiveObjectPath1() {
        try {
            DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "", "dir/file.txt");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateScratchSpaceURIAppNoRuntimeNoNodeActiveObjectPath1() {
        try {
            DataSpacesURI.createScratchSpaceURI("123", null, null, "aoC", "dir/file.txt");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateInOutSpaceURIAppTypeName1() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "stats");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.INPUT, uri.getSpaceType());
        assertEquals("stats", uri.getName());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
        assertNull(uri.getRelativeToSpace());
        assertTrue(uri.isSpacePartFullyDefined());
        assertTrue(uri.isSpacePartOnly());
        assertTrue(uri.isSuitableForUserPath());
    }

    @Test
    public void testCreateInOutSpaceURIAppTypeName2() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, "stats");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.OUTPUT, uri.getSpaceType());
        assertEquals("stats", uri.getName());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
        assertNull(uri.getRelativeToSpace());
        assertTrue(uri.isSpacePartFullyDefined());
        assertTrue(uri.isSpacePartOnly());
        assertTrue(uri.isSuitableForUserPath());
    }

    @Test
    public void testCreateInOutSpaceURIAppTypeWrongName1() {
        try {
            DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateInOutSpaceURIAppTypeWrongName2() {
        try {
            DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "ooops/ooops");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateInOutSpaceURIAppWrongTypeName() {
        try {
            uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.SCRATCH, "stats");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateInOutSpaceURIAppNoTypeName() {
        try {
            uri = DataSpacesURI.createInOutSpaceURI("123", null, "stats");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateInOutSpaceURIAppTypeNamePath() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, "stats", "dir/abc.txt");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.OUTPUT, uri.getSpaceType());
        assertEquals("stats", uri.getName());
        assertEquals("dir/abc.txt", uri.getUserPath());
        assertEquals("dir/abc.txt", uri.getRelativeToSpace());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertTrue(uri.isSpacePartFullyDefined());
        assertTrue(uri.isSuitableForUserPath());
        assertFalse(uri.isSpacePartOnly());
    }

    @Test
    public void testCreateInOutSpaceURIAppTypeNoNamePath() {
        try {
            uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, null, "dir/abc.txt");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testCreateInOutSpaceURIAppNoTypeNoNamePath() {
        try {
            uri = DataSpacesURI.createInOutSpaceURI("123", null, null, "dir/abc.txt");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    private String slash(boolean slashBoolean) {
        if (slashBoolean)
            return "/";
        return "";
    }

    private void testParseURIApp(boolean slash) throws MalformedURIException {
        uri = DataSpacesURI.parseURI("vfs:///123" + slash(slash));

        assertEquals("123", uri.getAppId());
        assertNull(uri.getSpaceType());
        assertNull(uri.getName());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
    }

    @Test
    public void testParseURIAppSlash() throws MalformedURIException {
        testParseURIApp(true);
    }

    @Test
    public void testParseURIAppNoSlash() throws MalformedURIException {
        testParseURIApp(false);
    }

    private void testParseURIAppType(boolean slash) throws MalformedURIException {
        uri = DataSpacesURI.parseURI("vfs:///123/input" + slash(slash));

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.INPUT, uri.getSpaceType());
        assertNull(uri.getName());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
    }

    @Test
    public void testParseURIAppTypeSlash() throws MalformedURIException {
        testParseURIAppType(true);
    }

    @Test
    public void testParseURIAppTypeNoSlash() throws MalformedURIException {
        testParseURIAppType(false);
    }

    private void testParseURIAppTypeName(boolean slash) throws MalformedURIException {
        uri = DataSpacesURI.parseURI("vfs:///123/input/abc" + slash(slash));

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.INPUT, uri.getSpaceType());
        assertEquals("abc", uri.getName());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
    }

    @Test
    public void testParseURIAppTypeNameSlash() throws MalformedURIException {
        testParseURIAppTypeName(true);
    }

    @Test
    public void testParseURIAppTypeNameNoSlash() throws MalformedURIException {
        testParseURIAppTypeName(false);
    }

    @Test
    public void testParseURIAppTypeNamePath() throws MalformedURIException {
        uri = DataSpacesURI.parseURI("vfs:///123/input/abc/file.txt");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.INPUT, uri.getSpaceType());
        assertEquals("abc", uri.getName());
        assertEquals("file.txt", uri.getUserPath());
        assertNull(uri.getRuntimeId());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
    }

    @Test
    public void testParseURIAppTypeWrongNamePath() {
        try {
            DataSpacesURI.parseURI("vfs:///123/input//file.txt");
            fail("expected exception");
        } catch (MalformedURIException x) {
        }
    }

    private void testParseURIAppTypeRuntime(boolean slash) throws MalformedURIException {
        uri = DataSpacesURI.parseURI("vfs:///123/scratch/runtimeA" + slash(slash));

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertEquals("runtimeA", uri.getRuntimeId());
        assertNull(uri.getName());
        assertNull(uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getUserPath());
    }

    @Test
    public void testParseURIAppTypeRuntimeSlash() throws MalformedURIException {
        testParseURIAppTypeRuntime(true);
    }

    @Test
    public void testParseURIAppTypeRuntimeNoSlash() throws MalformedURIException {
        testParseURIAppTypeRuntime(false);
    }

    private void testParseURIAppTypeRuntimeNode(boolean slash) throws MalformedURIException {
        uri = DataSpacesURI.parseURI("vfs:///123/scratch/runtimeA/nodeB" + slash(slash));

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertEquals("runtimeA", uri.getRuntimeId());
        assertEquals("nodeB", uri.getNodeId());
        assertNull(uri.getActiveObjectId());
        assertNull(uri.getName());
        assertNull(uri.getUserPath());
    }

    @Test
    public void testParseURIAppTypeRuntimeNodeSlash() throws MalformedURIException {
        testParseURIAppTypeRuntimeNode(true);
    }

    @Test
    public void testParseURIAppTypeRuntimeNodeNoSlash() throws MalformedURIException {
        testParseURIAppTypeRuntimeNode(false);
    }

    private void testParseURIAppTypeRuntimeNodeActiveObject(boolean slash) throws MalformedURIException {
        uri = DataSpacesURI.parseURI("vfs:///123/scratch/runtimeA/nodeB/aoC" + slash(slash));

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertEquals("runtimeA", uri.getRuntimeId());
        assertEquals("nodeB", uri.getNodeId());
        assertEquals("aoC", uri.getActiveObjectId());
        assertNull(uri.getName());
        assertNull(uri.getUserPath());
    }

    @Test
    public void testParseURIAppTypeRuntimeNodeActiveObjectSlash() throws MalformedURIException {
        testParseURIAppTypeRuntimeNodeActiveObject(true);
    }

    @Test
    public void testParseURIAppTypeRuntimeNodeActiveObjectNoSlash() throws MalformedURIException {
        testParseURIAppTypeRuntimeNodeActiveObject(false);
    }

    @Test
    public void testParseURIAppTypeRuntimeNodePath() throws MalformedURIException {
        uri = DataSpacesURI.parseURI("vfs:///123/scratch/runtimeA/nodeB/aoC/file.txt");

        assertEquals("123", uri.getAppId());
        assertEquals(SpaceType.SCRATCH, uri.getSpaceType());
        assertEquals("runtimeA", uri.getRuntimeId());
        assertEquals("nodeB", uri.getNodeId());
        assertEquals("aoC", uri.getActiveObjectId());
        assertEquals("file.txt", uri.getUserPath());
        assertNull(uri.getName());
    }

    @Test
    public void testParseURIAppTypeWrongRuntimeNodeActiveObjectPath() {
        try {
            uri = DataSpacesURI.parseURI("vfs:///123/scratch//nodeB/file.txt");
            fail("expected exception");
        } catch (MalformedURIException x) {
        }
    }

    @Test
    public void testParseURIAppTypeRuntimeWrongNodeActiveObjectPath() {
        try {
            uri = DataSpacesURI.parseURI("vfs:///123/scratch/runtimeA//aoC/file.txt");
            fail("expected exception");
        } catch (MalformedURIException x) {
        }
    }

    @Test
    public void testParseURIAppTypeRuntimeNodeWrongActiveObjectPath() {
        try {
            uri = DataSpacesURI.parseURI("vfs:///123/scratch/runtimeA/nodeB//file.txt");
            fail("expected exception");
        } catch (MalformedURIException x) {
        }
    }

    @Test
    public void testParseURIAppBadType() {
        try {
            uri = DataSpacesURI.parseURI("vfs:///123/abc/");
            fail("expected exception");
        } catch (MalformedURIException x) {
        }
    }

    @Test
    public void testWithPath() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, "123", "dir/abc.txt");
        uri2 = uri.withUserPath("xyz");

        assertEquals("xyz", uri2.getUserPath());
        assertEquals("dir/abc.txt", uri.getUserPath());
    }

    @Test
    public void testWithPathEmpty() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, "123", "dir/abc.txt");
        uri2 = uri.withUserPath(null);

        assertNull(uri2.getUserPath());
        assertEquals("dir/abc.txt", uri.getUserPath());
    }

    @Test
    public void testWithPathIllegal1() {
        uri = DataSpacesURI.createURI("123", SpaceType.OUTPUT);
        try {
            uri.withUserPath("xyz");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testWithPathIllegal2() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        try {
            uri.withUserPath("xyz");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testWithActiveObjectId() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        uri2 = uri.withActiveObjectId("aoD");

        assertEquals("aoD", uri2.getActiveObjectId());
        assertEquals("aoC", uri.getActiveObjectId());
    }

    @Test
    public void testWithActiveObjectIdEmpty1() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        uri2 = uri.withActiveObjectId(null);

        assertNull(uri2.getActiveObjectId());
        assertEquals("aoC", uri.getActiveObjectId());
    }

    @Test
    public void testWithActiveObjectIdEmpty2() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        uri2 = uri.withActiveObjectId(null);

        assertNull(uri2.getActiveObjectId());
        assertNull(uri.getActiveObjectId());
    }

    @Test
    public void testWithActiveObjectIdIllegal1() {
        uri = DataSpacesURI.createURI("123", SpaceType.OUTPUT);
        try {
            uri.withActiveObjectId("aoId");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testWithActiveObjectIdIllegal2() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        try {
            uri.withActiveObjectId("aoId");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testWithRelativeToSpace() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "file.txt");
        uri2 = uri.withRelativeToSpace("aoD/dir1");

        assertEquals("aoD", uri2.getActiveObjectId());
        assertEquals("dir1", uri2.getUserPath());
        assertEquals("aoC", uri.getActiveObjectId());
        assertEquals("file.txt", uri.getUserPath());
    }

    @Test
    public void testWithRelativeToSpaceSubdirs() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "file.txt");
        uri2 = uri.withRelativeToSpace("aoD/dir1/dir2/file.txt");

        assertEquals("aoD", uri2.getActiveObjectId());
        assertEquals("dir1/dir2/file.txt", uri2.getUserPath());
        assertEquals("aoC", uri.getActiveObjectId());
        assertEquals("file.txt", uri.getUserPath());
    }

    @Test
    public void testWithRelativeToSpaceActiveObjectIdOnly() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "file.txt");
        uri2 = uri.withRelativeToSpace("aoD");

        assertEquals("aoD", uri2.getActiveObjectId());
        assertNull(uri2.getUserPath());
        assertEquals("aoC", uri.getActiveObjectId());
        assertEquals("file.txt", uri.getUserPath());
    }

    @Test
    public void testWithRelativeToSpaceUserPathOnly() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "nameA", "file.txt");
        uri2 = uri.withRelativeToSpace("dir1/file.txt");

        assertNull(uri2.getActiveObjectId());
        assertEquals("dir1/file.txt", uri2.getUserPath());
        assertNull(uri.getActiveObjectId());
        assertEquals("file.txt", uri.getUserPath());
    }

    @Test
    public void testWithRelativeToSpaceEmpty1() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "file.txt");
        uri2 = uri.withRelativeToSpace(null);

        assertNull(uri2.getActiveObjectId());
        assertNull(uri2.getUserPath());
        assertEquals("aoC", uri.getActiveObjectId());
        assertEquals("file.txt", uri.getUserPath());
    }

    @Test
    public void testWithRelativeToSpaceEmpty2() {
        uri = DataSpacesURI.createURI("123");
        uri2 = uri.withRelativeToSpace(null);

        assertNull(uri2.getActiveObjectId());
        assertNull(uri2.getUserPath());
    }

    @Test
    public void testWithRelativeToSpaceIllegal1() {
        uri = DataSpacesURI.createURI("123", SpaceType.OUTPUT);
        try {
            uri.withRelativeToSpace("file.txt");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testWithRelativeToSpaceIllegal2() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        try {
            uri.withRelativeToSpace("aoId");
            fail("expected exception");
        } catch (IllegalArgumentException x) {
        }
    }

    @Test
    public void testGetSpacePartOnly1() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "dir/file.txt");
        uri2 = uri.getSpacePartOnly();

        assertTrue(uri2.isSpacePartOnly());
        assertEquals("aoC", uri.getActiveObjectId());
        assertEquals("dir/file.txt", uri.getUserPath());
    }

    @Test
    public void testGetSpacePartOnly2() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, "stats", "dir/file.txt");
        uri2 = uri.getSpacePartOnly();

        assertTrue(uri2.isSpacePartOnly());
        assertEquals("dir/file.txt", uri.getUserPath());
    }

    @Test
    public void testToStringFullyDefinedNoPath1() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, "abc");
        assertEquals("vfs:///123/output/abc", uri.toString());
    }

    @Test
    public void testToStringFullyDefinedNoPath2() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        assertEquals("vfs:///123/scratch/runtimeA/nodeB/aoC", uri.toString());
    }

    @Test
    public void testToStringFullyDefinedPath1() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, "abc", "dir/abc.txt");
        assertEquals("vfs:///123/output/abc/dir/abc.txt", uri.toString());
    }

    @Test
    public void testToStringFullyDefinedPath2() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "dir/abc.txt");
        assertEquals("vfs:///123/scratch/runtimeA/nodeB/aoC/dir/abc.txt", uri.toString());
    }

    @Test
    public void testToStringNotFullyDefinedApp() {
        uri = DataSpacesURI.createURI("123");
        assertEquals("vfs:///123", uri.toString());
    }

    @Test
    public void testToStringNotFullyDefinedAppType1() {
        uri = DataSpacesURI.createURI("123", SpaceType.INPUT);
        assertEquals("vfs:///123/input", uri.toString());
    }

    @Test
    public void testToStringNotFullyDefinedAppType2() {
        uri = DataSpacesURI.createURI("123", SpaceType.SCRATCH);
        assertEquals("vfs:///123/scratch", uri.toString());
    }

    @Test
    public void testToStringNotFullyDefinedAppTypeRuntime() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        assertEquals("vfs:///123/scratch/runtimeA", uri.toString());
    }

    @Test
    public void testToStringNotFullyDefinedAppTypeRuntimeNode() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        assertEquals("vfs:///123/scratch/runtimeA/nodeB", uri.toString());
    }

    private void assertURIGreaterThanURI2() {
        assertTrue(uri.compareTo(uri2) > 0);
        assertTrue(uri2.compareTo(uri) < 0);
    }

    private void assertURIEqualURI2() {
        assertTrue(uri.compareTo(uri2) == 0);
        assertTrue(uri2.compareTo(uri) == 0);
    }

    @Test
    public void testCompareToDifferentLevelsPathVsActiveObject() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "abc.txt");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToDifferentLevelsActiveObjectVsNode() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToDifferentLevelsNodeVsRuntime() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToDifferentLevelsRuntimeVsType() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        uri2 = DataSpacesURI.createURI("123", SpaceType.SCRATCH);
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToDifferentLevelsNameVsType() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name");
        uri2 = DataSpacesURI.createURI("123", SpaceType.INPUT);
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToDifferentLevelsTypeVsApp() {
        uri = DataSpacesURI.createURI("123", SpaceType.SCRATCH);
        uri2 = DataSpacesURI.createURI("123");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToDifferentLevelsPathVsApp() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "abc.txt");
        uri2 = DataSpacesURI.createURI("123");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameLevelsDiffPath1() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name", "abc.txt2");
        uri2 = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name", "abc.txt");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameLevelsDiffPath2() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "abc.txt2");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "abc.txt");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameLevelsDiffActiveObject() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC2", "abc.txt");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "abc.txt");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameLevelsDiffNode() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB2", "aoC", "abc.txt");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "abc.txt");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameLevelsDiffRuntime() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA2", "nodeB", "aoC", "abc.txt");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "abc.txt");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameLevelsDiffName() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name2");
        uri2 = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameLevelsDiffType() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.OUTPUT, "name", "abc.txt");
        uri2 = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name", "abc.txt");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameLevelsDiffApp() {
        uri = DataSpacesURI.createInOutSpaceURI("124", SpaceType.INPUT, "name", "abc.txt");
        uri2 = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name", "abc.txt");
        assertURIGreaterThanURI2();
    }

    @Test
    public void testCompareToSameApp() {
        uri = DataSpacesURI.createURI("123");
        uri2 = DataSpacesURI.createURI("123");
        assertURIEqualURI2();
    }

    @Test
    public void testCompareToSameAppType1() {
        uri = DataSpacesURI.createURI("123", SpaceType.INPUT);
        uri2 = DataSpacesURI.createURI("123", SpaceType.INPUT);
        assertURIEqualURI2();
    }

    @Test
    public void testCompareToSameAppType2() {
        uri = DataSpacesURI.createURI("123", SpaceType.INPUT);
        uri2 = DataSpacesURI.createURI("123", SpaceType.INPUT);
        assertURIEqualURI2();
    }

    @Test
    public void testCompareToSameAppTypeName() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name");
        uri2 = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name");
        assertURIEqualURI2();
    }

    @Test
    public void testCompareToSameAppTypeNamePath() {
        uri = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name", "file.txt");
        uri2 = DataSpacesURI.createInOutSpaceURI("123", SpaceType.INPUT, "name", "file.txt");
        assertURIEqualURI2();
    }

    @Test
    public void testCompareToSameAppTypeRuntime() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        assertURIEqualURI2();
    }

    @Test
    public void testCompareToSameAppTypeRuntimeNode() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        assertURIEqualURI2();
    }

    @Test
    public void testCompareToSameAppTypeRuntimeNodeActiveObject() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        assertURIEqualURI2();
    }

    @Test
    public void testCompareToSameAppTypeRuntimeNodeActiveObjectPath() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "file.txt");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC", "file.txt");
        assertURIEqualURI2();
    }

    @Test
    public void testEqualsPositive() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        assertEquals(uri, uri2);
    }

    @Test
    public void testEqualsNegative() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        assertFalse(uri.equals(uri2));
    }

    @Test
    public void testHashCode() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        uri2 = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB", "aoC");
        assertEquals(uri.hashCode(), uri2.hashCode());
    }

    @Test
    public void testNextURIApp() {
        uri = DataSpacesURI.createURI("123");
        uri2 = uri.nextURI();

        assertEquals("123\0", uri2.getAppId());
    }

    @Test
    public void testNextURIAppType() {
        uri = DataSpacesURI.createURI("123", SpaceType.INPUT);
        uri2 = uri.nextURI();

        assertEquals("123", uri2.getAppId());
        assertEquals(SpaceType.OUTPUT, uri2.getSpaceType());
    }

    @Test
    public void testNextURIAppTypeLast() {
        uri = DataSpacesURI.createURI("123", SpaceType.SCRATCH);
        uri2 = uri.nextURI();

        assertEquals("123\0", uri2.getAppId());
        assertNull(uri2.getSpaceType());
    }

    @Test
    public void testNextURIAppTypeRuntime() {
        uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA");
        uri2 = uri.nextURI();

        assertEquals("123", uri2.getAppId());
        assertEquals(SpaceType.SCRATCH, uri2.getSpaceType());
        assertEquals("runtimeA\0", uri2.getRuntimeId());
    }

    @Test
    public void testNextURISpaceFullyDefined() {
        try {
            uri = DataSpacesURI.createScratchSpaceURI("123", "runtimeA", "nodeB");
            uri.nextURI();
            fail("expected exception");
        } catch (IllegalStateException x) {
        }
    }
}
