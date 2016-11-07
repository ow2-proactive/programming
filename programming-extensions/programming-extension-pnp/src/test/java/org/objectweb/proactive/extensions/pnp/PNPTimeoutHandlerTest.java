/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2016 INRIA/University of
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
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pnp;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PNPTimeoutHandlerTest {

    private void recordHeartBeats(PNPTimeoutHandler handler, List<Integer> beats) {
        for (long beat : beats) {
            handler.recordHeartBeat(beat);
        }
    }

    @Test
    public void testHeartBeatReceived() {
        PNPTimeoutHandler handler = new PNPTimeoutHandler(10, 2, 2);

        recordHeartBeats(handler, Lists.newArrayList(1));
        Assert.assertTrue("heartbeat was received", handler.getNotificationData().heartBeatReceived());

        handler.resetNotification();
        Assert.assertFalse("handler was reset and heartbeat was not received", handler.getNotificationData().heartBeatReceived());

        Assert.assertEquals("last interval should not be set", 0, handler.getNotificationData().getLastHeartBeatInterval());
        Assert.assertEquals("Timeout should be the default", 10 * 2, handler.getNotificationData().getTimeout());

        recordHeartBeats(handler, Lists.newArrayList(1 + 12));
        Assert.assertTrue("heartbeat was received", handler.getNotificationData().heartBeatReceived());
        Assert.assertEquals("last interval should be set", 12, handler.getNotificationData().getLastHeartBeatInterval());
        Assert.assertEquals("Timeout should be set accordingly", 12 * 2, handler.getNotificationData().getTimeout());
    }


    @Test
    public void testDefaultZero() {
        PNPTimeoutHandler handler = new PNPTimeoutHandler(0, 2, 2);

        recordHeartBeats(handler, Lists.newArrayList(
                1,
                1 + 10,
                1 + 10 + 12,
                1 + 10 + 12 + 14,
                1 + 10 + 12 + 14 + 16));
        Assert.assertEquals("If default heartbeat period is 0, the timeout should be zero", 0, handler.getTimeout());

    }


    @Test
    public void testNoData() {
        PNPTimeoutHandler handler = new PNPTimeoutHandler(10, 2, 2);

        recordHeartBeats(handler, Lists.newArrayList(1));
        Assert.assertEquals("If not enough data to compute an interval, default should be used", 10 * 2, handler.getTimeout());

    }


    @Test
    public void testGrowing() {
        PNPTimeoutHandler handler = new PNPTimeoutHandler(10, 2, 2);

        recordHeartBeats(handler, Lists.newArrayList(
                1,
                1 + 10,
                1 + 10 + 12,
                1 + 10 + 12 + 14,
                1 + 10 + 12 + 14 + 16));
        Assert.assertEquals("After growing beat delay, timeout should grow to the last beat interval", 16 * 2, handler.getTimeout());

    }

    @Test
    public void testGrowingDecreasing() {
        PNPTimeoutHandler handler = new PNPTimeoutHandler(10, 2, 2);

        recordHeartBeats(handler, Lists.newArrayList(
                1,
                1 + 10,
                1 + 10 + 12,
                1 + 10 + 12 + 14,
                1 + 10 + 12 + 14 + 16,
                1 + 10 + 12 + 14 + 16 + 10,
                1 + 10 + 12 + 14 + 16 + 10 + 10));
        Assert.assertEquals("After growing beat delay and then normal period, timeout should grow then shrink to the average", 10 * 2, handler.getTimeout());

    }


    @Test
    public void testUpsAndDowns1() {
        PNPTimeoutHandler handler = new PNPTimeoutHandler(10, 2, 2);

        recordHeartBeats(handler, Lists.newArrayList(
                1,
                1 + 10,
                1 + 10 + 20,
                1 + 10 + 20 + 10,
                1 + 10 + 20 + 10 + 20));
        Assert.assertEquals("When beat interval is oscillating between two values, if last value is bigger than average, then it should be used", 20 * 2, handler.getTimeout());

    }

    @Test
    public void testUpsAndDowns2() {
        PNPTimeoutHandler handler = new PNPTimeoutHandler(10, 2, 2);

        recordHeartBeats(handler, Lists.newArrayList(
                1,
                1 + 10,
                1 + 10 + 20,
                1 + 10 + 20 + 10,
                1 + 10 + 20 + 10 + 20,
                1 + 10 + 20 + 10 + 20 + 10));
        Assert.assertEquals("When beat interval is oscillating between two values, if last value is lower than average, then the average should be used", 15 * 2, handler.getTimeout());

    }
}
