/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
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
package functionalTests.mop;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.mop.ObjectReferenceReplacer;
import org.objectweb.proactive.core.mop.ObjectReplacer;

import functionalTests.FunctionalTest;
import functionalTests.activeobject.implicitgetstubonthis.A;
import functionalTests.activeobject.implicitgetstubonthis.B;


public class TestMopReplaceObject extends FunctionalTest {

    /**
     * @param args
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public void simpleObjectReplace() throws IllegalArgumentException, IllegalAccessException {

        // case 1 - swapping an object by another one.
        Object from = new Object();
        Object to = new Object();

        ObjectReplacer or = new ObjectReferenceReplacer(from, to);

        Object result = null;

        result = or.replaceObject(from);

        Assert.assertSame("case 1 - swapping an object by another one, object not correctly replaced", to,
                result);
        result = or.restoreObject();
        Assert.assertSame("case 1 - swapping an object by another one, restoring the replaced object failed",
                from, result);
    }

    @Test
    public void replaceObjectBynull() throws IllegalArgumentException, IllegalAccessException {

        // case 1 - swapping an object by another one.
        Object from = new Object();
        Object to = null;

        ObjectReplacer or = new ObjectReferenceReplacer(from, to);

        Object result = null;

        result = or.replaceObject(from);

        Assert.assertSame("case 2 - replacing an object by null, object not correctly replaced", to, result);
        result = or.restoreObject();
        Assert.assertSame("case 2 - swapping an object by null, restoring the replaced object failed", from,
                result);
    }

    @Test
    public void replaceObjectInArray() throws IllegalArgumentException, IllegalAccessException {

        // case 3 - swapping an object in an array by another one.
        int arraySize = 10;
        Object[] array = new Object[arraySize];

        Object from = null;

        // select an arbitrary element of the array to be replaced
        int pickObjectInArray = new Random().nextInt(arraySize);

        // fill the array
        Object tmp = null;
        for (int i = 0; i < array.length; i++) {
            tmp = new Object();
            array[i] = tmp;
            if (i == pickObjectInArray) {
                from = tmp;
            }
        }

        Object to = new Object();
        // perform the replacement
        ObjectReplacer or = new ObjectReferenceReplacer(from, to);

        Object[] resultArray = null;

        // restore initial state
        resultArray = (Object[]) or.replaceObject(array);

        Assert.assertSame(resultArray[pickObjectInArray], to);
        Assert.assertSame(array, resultArray);
        resultArray = (Object[]) or.restoreObject();
        Assert.assertSame(from, array[pickObjectInArray]);
        Assert.assertSame(from, resultArray[pickObjectInArray]);
    }

    @Test
    public void replaceObjectInMultiDimArray() throws IllegalArgumentException, IllegalAccessException {

        // case 3 - swapping an object in an multi-dimensional array by another one.
        int arraySize = 10;
        Object[][] array = new Object[arraySize][arraySize];

        Object from = null;

        // select an arbitrary element of the array to be replaced
        int pickObjectInArrayX = new Random().nextInt(arraySize);
        int pickObjectInArrayY = new Random().nextInt(arraySize);

        // fill the array
        Object tmp = null;
        for (int i = 0; i < arraySize; i++) {
            for (int j = 0; j < arraySize; j++) {
                tmp = new Object();
                array[i][j] = tmp;
                if ((i == pickObjectInArrayX) && (j == pickObjectInArrayY)) {
                    from = tmp;
                }
            }
        }

        Object to = new Object();
        // perform the replacement
        ObjectReplacer or = new ObjectReferenceReplacer(from, to);

        Object[][] resultArray = null;

        // restore initial state
        resultArray = (Object[][]) or.replaceObject(array);

        Assert.assertSame(resultArray[pickObjectInArrayX][pickObjectInArrayY], to);
        Assert.assertSame(array, resultArray);
        resultArray = (Object[][]) or.restoreObject();
        Assert.assertSame(from, array[pickObjectInArrayX][pickObjectInArrayY]);
        Assert.assertSame(from, resultArray[pickObjectInArrayX][pickObjectInArrayY]);
    }

    @Test
    public void replaceFieldInObject() throws IllegalArgumentException, IllegalAccessException {
        A aFrom = new A();
        A aTo = new A();
        B b = new B(aFrom);

        ObjectReplacer or = new ObjectReferenceReplacer(aFrom, aTo);

        B bNew = (B) or.replaceObject(b);

        Assert.assertSame(aTo, bNew.getA());
        Assert.assertSame(b.getA(), bNew.getA());
        bNew = (B) or.restoreObject();

        Assert.assertSame(aFrom, bNew.getA());
    }

}
