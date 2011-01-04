/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package unitTests.ProActiveCounter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveCounter;


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
