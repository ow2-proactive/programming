/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.messagerouting;

import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.extra.messagerouting.protocol.TypeHelper;


public class TestTypeHelper {

    @Test
    public void testIntBound() {
        Random rand = new Random();
        int[] data = new int[] { Integer.MIN_VALUE, -2, -1, 0, 1, 2, Integer.MAX_VALUE };
        byte[] buf = new byte[32];

        for (int i = 0; i < data.length; i++) {
            int retval;
            int val = data[i];
            int offset = rand.nextInt(24);

            TypeHelper.intToByteArray(val, buf, offset);
            retval = TypeHelper.byteArrayToInt(buf, offset);

            Assert.assertEquals(val, retval);
        }
    }

    @Test
    public void testRandomInt() {
        Random rand = new Random();
        byte[] buf = new byte[32];

        for (int i = 0; i < 10000000; i++) {
            int retval;
            int val = rand.nextInt();
            int offset = rand.nextInt(24);

            TypeHelper.intToByteArray(val, buf, offset);
            retval = TypeHelper.byteArrayToInt(buf, offset);

            Assert.assertEquals(val, retval);
        }
    }

    @Test
    public void testLongBound() {
        Random rand = new Random();
        long[] data = new long[] { Long.MIN_VALUE, Integer.MIN_VALUE, -2, -1, 0, 1, 2, Integer.MAX_VALUE,
                Long.MAX_VALUE };
        byte[] buf = new byte[32];

        for (int i = 0; i < data.length; i++) {
            long retval;
            long val = data[i];
            int offset = rand.nextInt(24);

            TypeHelper.longToByteArray(val, buf, offset);
            retval = TypeHelper.byteArrayToLong(buf, offset);
            System.out.println(val);
            System.out.println(retval);
            if (val == retval) {
                System.out.println("ok");
            }
            System.out.println(val == retval);
            Assert.assertTrue((0L + val) == (0L + retval));
        }
    }

    @Test
    public void testRandomLong() {
        Random rand = new Random();
        byte[] buf = new byte[32];

        for (int i = 0; i < 10000000; i++) {
            long retval;
            long val = rand.nextLong();
            int offset = rand.nextInt(24);

            TypeHelper.longToByteArray(val, buf, offset);
            retval = TypeHelper.byteArrayToLong(buf, offset);

            Assert.assertEquals(val, retval);
        }
    }

}
