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
