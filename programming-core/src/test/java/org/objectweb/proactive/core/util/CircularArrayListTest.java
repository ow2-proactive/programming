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
package org.objectweb.proactive.core.util;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.core.mop.Utils;


public class CircularArrayListTest {
    private Random rand = new Random();

    private CircularArrayList<Integer> cal;

    @Before
    public void setUp() {
        cal = new CircularArrayList<Integer>();
    }

    /**
     * Add and remove 50 elements and check that size() is ok
     */
    @Test
    public void addAndRemove() {
        int nbElem = 50;

        for (int i = 0; i < nbElem; i++)
            cal.add(i);
        assertTrue(cal.size() == nbElem);

        for (int i = 0; i < nbElem; i++)
            cal.remove(0);
        assertTrue(cal.size() == 0);
    }

    /**
     * Remove() on an empty list must thrown an {@link IndexOutOfBoundsException} exception
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void removeTooManyElems() {
        cal.remove(0);
    }

    /**
     * Serialization
     * @throws java.io.IOException
     */
    @Test
    @SuppressWarnings("unchecked")
    public void serialization() throws IOException {
        int nbElem = 50;

        for (int i = 0; i < nbElem; i++)
            cal.add(i);

        CircularArrayList<Integer> r = (CircularArrayList<Integer>) Utils.makeDeepCopy(cal);
        assertTrue(r.equals(cal));
    }

    @Test
    public void collectionAsParameter() {
        Collection<Integer> col = new ArrayList<Integer>();
        for (int i = 0; i < 50; i++)
            col.add(i);

        CircularArrayList<Integer> o = new CircularArrayList<Integer>(col);

        assertTrue(col.equals(o));

        assertTrue(o.size() == col.size());
    }

    @Test
    public void testAddAll() {
        for (int test = 0; test < 1000; test++) {
            CircularArrayList<Integer> cal = getRandomList();
            Integer[] orig = (Integer[]) cal.toArray(new Integer[0]);

            ArrayList<Integer> l = new ArrayList<Integer>();
            for (int i = -10; i < 0; i++)
                l.add(i);

            int size = cal.size();
            cal.addAll(l);

            // Unchanged values
            for (int i = 0; i < size; i++) {
                int expected = (int) orig[i];
                int actual = (int) cal.get(i);
                Assert.assertEquals("Bad unchanged value", expected, actual);
            }

            // Inserted values
            for (int i = 0; i < l.size(); i++) {
                int expected = (int) l.get(i);
                int actual = (int) cal.get(size + i);
                Assert.assertEquals("Bad inserted value", expected, actual);
            }

            Assert.assertEquals("Bad size value", (size + l.size()), cal.size());
        }

    }

    @Test
    public void testAddAllWithIndex() {
        for (int test = 0; test < 5000; test++) {
            CircularArrayList<Integer> cal = getRandomList();
            Integer[] orig = (Integer[]) cal.toArray(new Integer[0]);

            ArrayList<Integer> l = new ArrayList<Integer>();
            for (int i = -10; i < 0; i++)
                l.add(i);

            int size = cal.size();
            int index = cal.isEmpty() ? 0 : rand.nextInt(cal.size);
            cal.addAll(index, l);

            // Unchanged values
            for (int i = 0; i < index; i++) {
                int expected = (int) orig[i];
                int actual = (int) cal.get(i);
                Assert.assertEquals("Bad unchanged value", expected, actual);
            }

            // Inserted values
            for (int i = 0; i < l.size(); i++) {
                int expected = (int) l.get(i);
                int actual = (int) cal.get(index + i);
                Assert.assertEquals("Bad inserted value", expected, actual);
            }

            // Shifter values
            for (int i = 0; i < cal.size() - index - l.size(); i++) {
                int expected = (int) orig[index + i];
                int actual = (int) cal.get(index + l.size() + i);
                Assert.assertEquals("Bad shifted value", expected, actual);

            }

            Assert.assertEquals("Bad size value", (size + l.size()), cal.size());

        }
    }

    private CircularArrayList<Integer> getRandomList() {
        CircularArrayList<Integer> cal = new CircularArrayList<Integer>(rand.nextInt(10));
        for (int i = 0; i < rand.nextInt(15); i++) {
            for (int j = 0; j < rand.nextInt(100); j++) {
                cal.add(rand.nextInt(1000));
            }

            for (int j = 0; j < rand.nextInt(cal.size() + 1); j++) {
                cal.remove(rand.nextInt(cal.size()));
            }
        }

        return cal;
    }

    @Test
    public void testAddWithGap() {
        final List<Character> list = new CircularArrayList<Character>(6); // [------]
        list.add('a'); // [a-----]
        list.add('b'); // [ab----]
        list.add('c'); // [abc---]
        list.add('d'); // [abcd--]
        list.add('e'); // [abcde-]
        list.subList(0, 4).clear(); // [----e-]
        list.add('f'); // [----ef]
        list.add('g'); // [g---ef]
        list.add('h'); // [gh--ef]
        list.add(1, 'x'); // ArrayIndexOutOfBoundsException
    }
}
