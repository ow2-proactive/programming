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
package functionalTests.mop.replaceobject;

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

        Assert.assertSame("case 1 - swapping an object by another one, object not correctly replaced", to, result);
        result = or.restoreObject();
        Assert.assertSame("case 1 - swapping an object by another one, restoring the replaced object failed",
                          from,
                          result);
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
        Assert.assertSame("case 2 - swapping an object by null, restoring the replaced object failed", from, result);
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
