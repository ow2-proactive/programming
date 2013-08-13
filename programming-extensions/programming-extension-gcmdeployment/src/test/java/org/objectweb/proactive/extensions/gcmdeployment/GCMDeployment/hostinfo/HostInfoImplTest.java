/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
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
 *  * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo;

import org.objectweb.proactive.utils.OperatingSystem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HostInfoImplTest {
    HostInfoImpl notInitialized;
    HostInfoImpl halfInitialized;
    HostInfoImpl fullyInitialized;

    @Before
    public void before() {
        notInitialized = new HostInfoImpl();

        halfInitialized = new HostInfoImpl();
        halfInitialized.setId("toto");
        halfInitialized.addTool(new Tool("tool", "//path"));

        fullyInitialized = new HostInfoImpl();
        fullyInitialized.setId("id");
        fullyInitialized.setOs(OperatingSystem.unix);
        fullyInitialized.setHomeDirectory("//homeidr");
        fullyInitialized.setUsername("usermane");
        fullyInitialized.addTool(new Tool("tool", "//path"));
    }

    @Test
    public void getTool1() {
        Assert.assertNotNull(fullyInitialized.getTool("tool"));
        Assert.assertNull(fullyInitialized.getTool("tool2"));
    }

    @Test
    public void equality() {
        HostInfoImpl tmp = new HostInfoImpl();
        tmp.setId("id");
        Assert.assertTrue(tmp.equals(fullyInitialized));

        tmp = new HostInfoImpl();
        tmp.setId("xxxxxxx");
        Assert.assertFalse(tmp.equals(fullyInitialized));
    }

    @Test(expected = IllegalStateException.class)
    public void checkReadygetHalfInitialized() {
        halfInitialized.check();
    }

    @Test(expected = IllegalStateException.class)
    public void checkReadygetHomeDirectory() {
        notInitialized.check();
    }
}
