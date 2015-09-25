/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.log.throttlingprovider;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Assert;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.log.remote.ProActiveAppender;
import org.objectweb.proactive.core.util.log.remote.ProActiveLogCollector;
import org.objectweb.proactive.core.util.log.remote.ThrottlingProvider;


public class TestDeconnection {
    int counter = 0;

    @Test(timeout = 20000)
    public void test() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException, InterruptedException {
        int period = 500;
        int threshold = 10;
        int qsize = 100;

        ThrottlingProvider tp = new ThrottlingProvider(period, threshold, qsize, true);
        FailingCollector collector = new FailingCollector();
        // Start the flushing thread
        new ProActiveAppender(tp, collector);

        // append 1000 logging events
        sendXEvents(1000, tp);

        int nbEvents;
        do {
            Thread.sleep(period);
            Queue<LoggingEvent> les = collector.getReceivedLoggingEvents();
            nbEvents = les.size();
        } while (nbEvents != 1000);

        collector.clear();
        collector.fail();
        sendXEvents(1000, tp);
        Thread.sleep(period * 2);
        collector.resume();

        // Events have been dropped by the throttling provider
        // A logging event is sent to notify the collector
        nbEvents = 0;
        do {
            Thread.sleep(period);
            Queue<LoggingEvent> les = collector.getReceivedLoggingEvents();
            nbEvents = les.size();
        } while (nbEvents < 1);

        System.out.println(nbEvents);
        Assert.assertTrue(nbEvents == 1);
    }

    public void sendXEvents(int x, ThrottlingProvider tp) {
        for (int i = 0; i < x; i++) {
            String msg = "Event " + counter++;
            Logger l = Logger.getLogger(this.getClass());
            LoggingEvent le = new LoggingEvent(Category.class.getName(), l, Level.WARN, msg, null);

            tp.append(le);
        }
    }

    private class FailingCollector extends ProActiveLogCollector {
        private boolean fail = false;
        final private ConcurrentLinkedQueue<LoggingEvent> receivedLoggingEvents;

        public FailingCollector() {
            this.fail = false;
            this.receivedLoggingEvents = new ConcurrentLinkedQueue<LoggingEvent>();
        }

        @Override
        public void sendEvent(List<LoggingEvent> events) {
            if (fail)
                throw new ProActiveRuntimeException("you fail");

            this.receivedLoggingEvents.addAll(events);
        }

        public void fail() {
            fail = true;
        }

        public void resume() {
            fail = false;
        }

        public Queue<LoggingEvent> getReceivedLoggingEvents() {
            return this.receivedLoggingEvents;
        }

        public void clear() {
            this.receivedLoggingEvents.clear();
        }
    }
}
