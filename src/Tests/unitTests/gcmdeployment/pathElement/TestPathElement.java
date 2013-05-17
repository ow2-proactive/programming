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
package unitTests.gcmdeployment.pathElement;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderProActive;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfoImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.Tool;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.Tools;
import org.objectweb.proactive.extensions.gcmdeployment.PathElement.PathBase;
import org.objectweb.proactive.utils.OperatingSystem;


public class TestPathElement {
    final String pathL = "/zzzz/plop";
    final String homeDirL = "/user/barbie";
    final String proactiveDirL = "/bin/proactive";
    final String toolDirL = "/tools/proactive";

    final String pathW = "Documents and Settings\\plop";
    final String homeDirW = "c:\\Documents and Settings\\Users\\plop";
    final String proactiveDirW = "c:\\Program Files (x86)\\proactive";
    final String toolDirW = "c:\\Program Files (x86)\\tools\\proactive";

    @Test
    public void testRoot() {
        PathElement pe;

        HostInfoImpl hostInfo1 = new HostInfoImpl();
        hostInfo1.setOs(OperatingSystem.unix);

        HostInfoImpl hostInfo2 = new HostInfoImpl();
        hostInfo2.setOs(OperatingSystem.windows);

        pe = new PathElement(pathL);
        Assert.assertEquals(pathL, pe.getRelPath());
        Assert.assertEquals(pathL, pe.getFullPath(hostInfo1, null));

        pe = new PathElement(pathW);
        Assert.assertEquals(pathW, pe.getRelPath());
        Assert.assertEquals(pathW, pe.getFullPath(hostInfo2, null));

        pe = new PathElement(pathL, PathBase.ROOT);
        Assert.assertEquals(pathL, pe.getRelPath());
        Assert.assertEquals(pathL, pe.getFullPath(hostInfo1, null));

        pe = new PathElement(pathW, PathBase.ROOT);
        Assert.assertEquals(pathW, pe.getRelPath());
        Assert.assertEquals(pathW, pe.getFullPath(hostInfo2, null));

        pe = new PathElement(pathL, "root");
        Assert.assertEquals(pathL, pe.getRelPath());
        Assert.assertEquals(pathL, pe.getFullPath(hostInfo1, null));

        pe = new PathElement(pathW, "root");
        Assert.assertEquals(pathW, pe.getRelPath());
        Assert.assertEquals(pathW, pe.getFullPath(hostInfo2, null));
    }

    @Test
    public void testHome() {
        HostInfoImpl hostInfo1 = new HostInfoImpl();
        hostInfo1.setHomeDirectory(homeDirL);
        hostInfo1.setOs(OperatingSystem.unix);

        HostInfoImpl hostInfo2 = new HostInfoImpl();
        hostInfo2.setHomeDirectory(homeDirW);
        hostInfo2.setOs(OperatingSystem.windows);

        PathElement pe = new PathElement(pathL, PathBase.HOME);
        String expected = PathElement.appendPath(homeDirL, pathL, hostInfo1);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo1, null));

        pe = new PathElement(pathW, PathBase.HOME);
        expected = PathElement.appendPath(homeDirW, pathW, hostInfo2);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo2, null));
    }

    @Test
    public void testProActive() {
        HostInfoImpl hostInfo1 = new HostInfoImpl();
        hostInfo1.setHomeDirectory(homeDirL);
        hostInfo1.setOs(OperatingSystem.unix);

        HostInfoImpl hostInfo2 = new HostInfoImpl();
        hostInfo2.setHomeDirectory(homeDirW);
        hostInfo2.setOs(OperatingSystem.windows);

        CommandBuilderProActive cb1 = new CommandBuilderProActive();
        cb1.setProActivePath(proactiveDirL);

        CommandBuilderProActive cb2 = new CommandBuilderProActive();
        cb2.setProActivePath(proactiveDirW);

        PathElement pe = new PathElement(pathL, PathBase.PROACTIVE);
        String expected = PathElement.appendPath(homeDirL, proactiveDirL, hostInfo1);
        expected = PathElement.appendPath(expected, pathL, hostInfo1);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo1, cb1));

        pe = new PathElement(pathW, PathBase.PROACTIVE);
        expected = PathElement.appendPath(homeDirW, proactiveDirW, hostInfo2);
        expected = PathElement.appendPath(expected, pathW, hostInfo2);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo2, cb2));
    }

    @Test
    public void testTool() {
        HostInfoImpl hostInfo1 = new HostInfoImpl();
        hostInfo1.setHomeDirectory(homeDirL);
        hostInfo1.setOs(OperatingSystem.unix);
        hostInfo1.addTool(new Tool(Tools.PROACTIVE.id, toolDirL));

        HostInfoImpl hostInfo2 = new HostInfoImpl();
        hostInfo2.setHomeDirectory(homeDirW);
        hostInfo2.setOs(OperatingSystem.windows);
        hostInfo2.addTool(new Tool(Tools.PROACTIVE.id, toolDirW));

        CommandBuilderProActive cb1 = new CommandBuilderProActive();
        cb1.setProActivePath(proactiveDirL);

        CommandBuilderProActive cb2 = new CommandBuilderProActive();
        cb1.setProActivePath(proactiveDirW);

        PathElement pe = new PathElement(pathL, PathBase.PROACTIVE);
        String expected = PathElement.appendPath(toolDirL, pathL, hostInfo1);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo1, cb1));

        pe = new PathElement(pathW, PathBase.PROACTIVE);
        expected = PathElement.appendPath(toolDirW, pathW, hostInfo2);
        Assert.assertEquals(expected, pe.getFullPath(hostInfo2, cb2));
    }

    @Test
    public void testToolException() {
        HostInfoImpl hostInfo1 = new HostInfoImpl();
        hostInfo1.setHomeDirectory(homeDirL);
        hostInfo1.setOs(OperatingSystem.unix);

        HostInfoImpl hostInfo2 = new HostInfoImpl();
        hostInfo2.setHomeDirectory(homeDirW);
        hostInfo2.setOs(OperatingSystem.windows);

        CommandBuilderProActive cb1 = new CommandBuilderProActive();
        PathElement pe = new PathElement(pathL, PathBase.PROACTIVE);
        String expected = PathElement.appendPath(homeDirL, toolDirL, hostInfo1);
        expected = PathElement.appendPath(expected, pathL, hostInfo1);
        Assert.assertEquals(null, pe.getFullPath(hostInfo1, cb1));

        CommandBuilderProActive cb2 = new CommandBuilderProActive();
        pe = new PathElement(pathW, PathBase.PROACTIVE);
        pe = new PathElement(pathW, PathBase.PROACTIVE);
        expected = PathElement.appendPath(homeDirW, toolDirW, hostInfo2);
        expected = PathElement.appendPath(expected, pathW, hostInfo2);
        Assert.assertEquals(null, pe.getFullPath(hostInfo2, cb2));

    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        PathElement pe = new PathElement(pathL, PathBase.PROACTIVE);
        Assert.assertEquals(pe, pe.clone());

        pe = new PathElement(pathW, PathBase.PROACTIVE);
        Assert.assertEquals(pe, pe.clone());
    }

    @Test
    public void testAppendPath() {
        String s1;
        String s2;
        String expected;

        HostInfoImpl hostInfo1 = new HostInfoImpl();
        hostInfo1.setOs(OperatingSystem.unix);

        expected = "/toto";
        s1 = "/";
        s2 = "toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo1));
        s1 = "/";
        s2 = "/toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo1));
        s1 = "";
        s2 = "toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo1));
        s1 = "";
        s2 = "toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo1));

        HostInfoImpl hostInfo2 = new HostInfoImpl();
        hostInfo2.setOs(OperatingSystem.windows);

        expected = "c:\\toto";
        s1 = "c:";
        s2 = "toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo2));
        s1 = "c:";
        s2 = "\\toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo2));
        s1 = "c:\\";
        s2 = "\\toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo2));

        expected = "\\toto";
        s1 = "";
        s2 = "toto";
        Assert.assertEquals(expected, PathElement.appendPath(s1, s2, hostInfo2));
    }
}
