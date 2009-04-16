package functionalTests.log.throttlingprovider;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import junit.framework.Assert;

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

        ThrottlingProvider tp = new ThrottlingProvider(period, threshold, qsize);
        FailingCollector collector = new FailingCollector();
        ProActiveAppender appender = new ProActiveAppender(tp, collector);

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
            LoggingEvent le = les.poll();
        } while (nbEvents < 1);

        System.out.println(nbEvents);
        Assert.assertTrue(nbEvents == 1);
        System.out.println("OK");
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
