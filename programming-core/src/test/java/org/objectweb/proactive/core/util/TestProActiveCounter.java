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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;


public class TestProActiveCounter {
    static final long MAX = 1000000;

    static final int NB_THREAD = 10;

    @Test
    public void testProActivecounter() throws InterruptedException {
        Vector<Long> results = new Vector<Long>();
        Thread[] threads = new Thread[NB_THREAD];

        for (int i = 0; i < NB_THREAD; i++) {
            threads[i] = new Consumer(results);
            threads[i].start();
        }

        for (int i = 0; i < NB_THREAD; i++) {
            threads[i].join();
        }

        Collections.sort(results);
        for (int i = 0; i < results.size(); i++) {
            Assert.assertEquals(results.get(i).longValue(), i);
        }
    }

    class Consumer extends Thread {
        List<Long> l = new ArrayList<Long>();

        List<Long> results;

        public Consumer(List<Long> results) {
            this.results = results;
        }

        @Override
        public void run() {
            for (int i = 0; i < (MAX / NB_THREAD); i++) {
                l.add(ProActiveCounter.getUniqID());
                Thread.yield();
            }
            results.addAll(l);
        }
    }
}
