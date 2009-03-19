package org.objectweb.proactive.core.util.log.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.MDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;


/**
 * The default logging event sender service provider
 * 
 * This provider use a simple but flexible flushing policy. Logging events are
 * send to the collector as soon as one of this condition is met:
 * <ul>
 * <li>the number of buffered message reached a threshold</li>
 * <li>logging events have not been flushed since a given period (in
 * milliseconds)</li>
 * </ul>
 * 
 * If the sender provider is overloaded, clients are blocked until the provider
 * is able to recover a steady state.
 * 
 * The queue size, the threshold and the period can be configured by setting the
 * following java properties:
 * <ul>
 * <li>org.objectweb.proactive.core.util.log.remote.throttlingprovider.qsize
 * <li>org.objectweb.proactive.core.util.log.remote.throttlingprovider.threshold
 * </li>
 * <li>org.objectweb.proactive.core.util.log.remote.throttlingprovider. period</li>
 * </ul>
 * 
 */
public final class ThrottlingProvider extends LoggingEventSenderSPI {
    final static int DEFAULT_QSIZE = 10000;
    final static int DEFAULT_PERIOD = 10000; // ms
    final static int DEFAULT_THRESHOLD = 50;

    final static private String MDC_FLUSHING_TAG = "ThrottlingProvider";

    /**
     * A logging event cannot be buffered more than this amount of time (in
     * milliseconds).
     */
    int period;
    final String periodProperty = "org.objectweb.proactive.core.util.log.remote.ThrottlingProvider.period";

    /**
     * Send the events to the collector as soon as THRESHOLD logging events are
     * available.
     */
    int threshold;
    final String thresholdProperty = "org.objectweb.proactive.core.util.log.remote.ThrottlingProvider.threshold";

    /**
     * The buffer size. If the buffer is full, clients are blocked until the
     * flushing thread is able to recover a steady state.
     */
    int qsize;
    final String qsizeProperty = "org.objectweb.proactive.core.util.log.remote.ThrottlingProvider.qsize";

    /** Logging Events to be send to the collector */
    final private ArrayBlockingQueue<LoggingEvent> buffer;

    /** True if the flushing thread should terminate */
    final private AtomicBoolean terminate;

    /** Number of event to be send by the next call to gatherAndSend */
    final private AtomicInteger pendingEvents;

    /** Indicates if the flushing thread must be notified by the clients */
    final private AtomicBoolean mustNotify;

    /**
     * Avoid race condition between the flushing thread and the shutdown hook
     */
    final private Object gatherAndSendMutex = new Object();

    private FileAppender errorAppender;

    public ThrottlingProvider(int period, int threshold, int qsize) {
        this.period = period;
        this.threshold = threshold;
        this.qsize = qsize;

        this.buffer = new ArrayBlockingQueue<LoggingEvent>(this.qsize);
        this.terminate = new AtomicBoolean(false);
        this.pendingEvents = new AtomicInteger();
        this.mustNotify = new AtomicBoolean(false);
    }

    public ThrottlingProvider() {
        // Default value
        this(DEFAULT_PERIOD, DEFAULT_THRESHOLD, DEFAULT_QSIZE);

        // Use the values defined by properties if set 

        String prop;
        int value;

        value = DEFAULT_PERIOD;
        prop = System.getProperty(periodProperty);
        if (prop != null) {
            try {
                int i = Integer.parseInt(prop);
                if (i > 0) {
                    value = i;
                } else {
                    throw new NumberFormatException("Must be positive");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid parameter for property " + periodProperty +
                    ". Must be a positive integer");
            }
        }
        this.period = value;

        value = DEFAULT_THRESHOLD;
        prop = System.getProperty(thresholdProperty);
        if (prop != null) {
            try {
                int i = Integer.parseInt(prop);
                if (i > 0) {
                    value = i;
                } else {
                    throw new NumberFormatException("Must be positive");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid parameter for property " + thresholdProperty +
                    ". Must be a positive integer");
            }
        }
        this.threshold = value;

        value = DEFAULT_QSIZE;
        prop = System.getProperty(qsizeProperty);
        if (prop != null) {
            try {
                int i = Integer.parseInt(prop);
                if (i > 0) {
                    value = i;
                } else {
                    throw new NumberFormatException("Must be positive");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid parameter for property " + qsizeProperty +
                    ". Must be a positive integer");
            }
        }
        this.qsize = value;
    }

    @Override
    public void run() {
        do {
            MDC.put(MDC_FLUSHING_TAG, "");
            gatherAndSend();
            MDC.remove(MDC_FLUSHING_TAG);

            /* Avoid the cost of wait/notify if the threshold is already reached */
            if (this.pendingEvents.get() < threshold) {
                synchronized (this) {
                    try {
                        /*
                         * Advertise the writer threads that the flushing thread
                         * must be notified.
                         */
                        this.mustNotify.set(true);
                        this.wait(period);
                        this.mustNotify.set(false);
                    } catch (InterruptedException e) {
                        // Miam miam miam
                    }
                }
            }
        } while (!this.terminate.get());
    }

    private void gatherAndSend() {
        synchronized (this.gatherAndSendMutex) {
            ProActiveLogCollector collector = getCollector();
            if (collector != null) {
                /*
                 * Since there is at most one reader at the same time,
                 * pendingEvents can safely be used to know the number of
                 * available events.
                 * 
                 * get and set _MUST_ be atomic
                 */
                final int nbEvent = this.pendingEvents.getAndSet(0);

                /* Remove events from the buffer and send them */
                ArrayList<LoggingEvent> events = new ArrayList<LoggingEvent>(nbEvent);
                for (int i = 0; i < nbEvent; i++) {
                    events.add(buffer.poll());
                }

                if (events.size() != 0) {
                    collector.sendEvent(events);
                }
            }
        }
    }

    @Override
    public void append(LoggingEvent event) {
        if (MDC.get(MDC_FLUSHING_TAG) != null) {
            /*
             * This event has been created while sending a message to the
             * collector To avoid infinite loop, we use a dedicated file
             * appender
             */
            Appender errorAppender = this.getErrorAppender();
            if (errorAppender != null) {
                this.errorAppender.append(event);
            } else {
                System.err.println(event.getLoggerName() + " " + event.getLevel() + " " + event.getMessage());
            }
        } else {
            /*
             * Add the event into the buffer. If the buffer is full, the calling
             * thread will block until we are able to recover a steady state.
             */
            if (!buffer.offer(event)) {
                // THROTTLING ENABLED

                boolean added = false;
                do {
                    try {
                        buffer.put(event);
                        added = true;
                    } catch (InterruptedException e) {
                        // Miam Miam Miam
                    }
                } while (!added);
            }

            /*
             * The flushing thread must be notified if the threshold is reached
             * and the flag is set.
             */
            int counter = pendingEvents.incrementAndGet();
            if (counter > this.threshold) {
                if (mustNotify.get()) {
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
            }
        }
    }

    /** Lazily load the error appender */
    synchronized private Appender getErrorAppender() {
        if (this.errorAppender == null) {
            try {
                Layout layout = new PatternLayout("%X{shortid@hostname} - [%p %20.20c{2}] %m%n");
                this.errorAppender = new FileAppender(layout, "throttlingProvider.log");
            } catch (IOException e) {
                System.err.println("Failed to create FileAppender");
                e.printStackTrace();
            }
        }

        return this.errorAppender;
    }

    @Override
    public void terminate() {
        this.terminate.set(true);
        this.gatherAndSend();
    }
}
