/*
 * ################################################################
 *
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
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject.benchmark;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Assert;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet;


/**
 * BenchmarkOrderTest
 *
 * This test ensures that the protocols are correctly sorted in RemoteObjectSet according to :
 * - the natural order (order received inside the stub)
 * - the benchmark result
 * - the property proactive.communication.protocols.order
 * - the fact that a protocol is reachable or not
 * - the default protocol in use
 *
 * @author The ProActive Team
 */
public class BenchmarkOrderTest {

    @org.junit.Test
    public void checkSort() throws Exception {

        // a:a b:b c:c ... syntax represent uris, in that case the protocols would be a, b and c

        // natural order
        Assert.assertArrayEquals(new String[] { "a:a", "b:b", "c:c" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { 3, 2, 1 }, new String[0], "a:a"));

        // natural order + one protocol unreachable + unreachable protocol is different from default protocol
        // => unreachable default protocol is removed from the list
        Assert.assertArrayEquals(
                new String[] { "b:b", "c:c" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { Integer.MIN_VALUE, 2, 1 },
                        new String[0], "b:b"));

        // natural order + one protocol unreachable + unreachable protocol equals default protocol
        // => unreachable default protocol is put at the end of the list but not removed
        Assert.assertArrayEquals(
                new String[] { "b:b", "c:c", "a:a" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { Integer.MIN_VALUE, 2, 1 },
                        new String[0], "a:a"));

        // natural order + two protocols unreachable (different than default)
        Assert.assertArrayEquals(
                new String[] { "c:c" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { Integer.MIN_VALUE,
                        Integer.MIN_VALUE, 1 }, new String[0], "c:c"));

        // natural order + two protocols unreachable (one is the default)
        Assert.assertArrayEquals(
                new String[] { "c:c", "a:a" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { Integer.MIN_VALUE,
                        Integer.MIN_VALUE, 1 }, new String[0], "a:a"));

        // natural order + fixed order property (same order)
        Assert.assertArrayEquals(
                new String[] { "a:a", "b:b", "c:c" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { 3, 2, 1 }, new String[] { "a", "b",
                        "c" }, "a:a"));

        // natural order + fixed order property (reverse order)
        Assert.assertArrayEquals(
                new String[] { "c:c", "b:b", "a:a" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { 3, 2, 1 }, new String[] { "c", "b",
                        "a" }, "a:a"));

        // inverse natural order + fixed order property
        Assert.assertArrayEquals(
                new String[] { "c:c", "b:b", "a:a" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { 1, 2, 3 }, new String[] { "c", "b",
                        "a" }, "a:a"));

        // natural order + fixed order property in only one protocol
        Assert.assertArrayEquals(
                new String[] { "c:c", "a:a", "b:b" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { 3, 2, 1 }, new String[] { "c" },
                        "a:a"));

        // natural order + fixed order property + 1 unreachable protocol
        Assert.assertArrayEquals(
                new String[] { "b:b", "a:a" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { 3, 2, Integer.MIN_VALUE },
                        new String[] { "c", "b", "a" }, "a:a"));

        // natural order + fixed order property + 2 unreachable protocols + 1 is the default
        Assert.assertArrayEquals(
                new String[] { "a:a" },
                doSort(new String[] { "a:a", "b:b", "c:c" }, new int[] { 3, Integer.MIN_VALUE,
                        Integer.MIN_VALUE }, new String[] { "c", "b", "a" }, "a:a"));
    }

    /**
     * Sort the uri list using parameters
     * @param inputuris an array of uris which needs to be sorted
     * @param benchValues the benchmark results values
     * @param fixedOrder defines the fixed order of protocols
     * @param defaultUri the default uri of the remote object set
     * @return a sorted array
     * @throws Exception
     */
    private String[] doSort(String[] inputuris, int[] benchValues, String[] fixedOrder, String defaultUri)
            throws Exception {
        Assert.assertTrue(inputuris.length == benchValues.length);
        ConcurrentHashMap<URI, Integer> benchmarkres = new ConcurrentHashMap<URI, Integer>();
        ArrayList<URI> input = new ArrayList<URI>();

        for (int i = 0; i < inputuris.length; i++) {
            benchmarkres.put(new URI(inputuris[i]), benchValues[i]);
            input.add(new URI(inputuris[i]));
        }

        ArrayList<URI> outputList = RemoteObjectSet.sortProtocols(input, Arrays.asList(fixedOrder),
                benchmarkres, new URI(defaultUri));
        String[] output = new String[outputList.size()];
        for (int i = 0; i < outputList.size(); i++) {
            output[i] = outputList.get(i).toString();
        }
        return output;
    }

}
