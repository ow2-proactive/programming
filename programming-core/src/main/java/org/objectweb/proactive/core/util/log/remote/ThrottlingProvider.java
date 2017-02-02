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
package org.objectweb.proactive.core.util.log.remote;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Category;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;


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
 * <li>org.objectweb.proactive.core.util.log.remote.throttlingprovider.period</li>
 * </ul>
 *
 * When the log collector is not reachable, logging events are put into a local logfile
 * by default. They can be dropped by setting the
 * <code>org.objectweb.core.util.log.remote.throttlingprovider.nologfile</code> property to true.
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
    private int period;

    final String periodProperty = "org.objectweb.proactive.core.util.log.remote.ThrottlingProvider.period";

    /**
     * Send the events to the collector as soon as THRESHOLD logging events are
     * available.
     */
    private int threshold;

    final String thresholdProperty = "org.objectweb.proactive.core.util.log.remote.ThrottlingProvider.threshold";

    /**
     * The buffer size. If the buffer is full, clients are blocked until the
     * flushing thread is able to recover a steady state.
     */
    private int qsize;

    final String qsizeProperty = "org.objectweb.proactive.core.util.log.remote.ThrottlingProvider.qsize";

    /**
     * If the logging events must be dropped or saved into a log file when the log collector is not available
     */
    private boolean noLogFile;

    final String noLogFileProperty = "org.objectweb.proactive.core.util.log.remote.ThrottlingProvider.nologfile";

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

    /** The log collector is unavailable since this date
     * 
     * Reset to null each time the collector becomes available again
     */
    private Date failureDate;

    /** The first cause of log collector unavailability */
    private Throwable failureCause;

    /** Number of dropped messages since the collector is unavailable */
    private long nbDroppedMsg;

    private FileAppender errorAppender;

    public ThrottlingProvider(int period, int threshold, int qsize, boolean noLogFile) {
        this.period = period;
        this.threshold = threshold;
        this.qsize = qsize;
        this.noLogFile = noLogFile;

        this.buffer = new ArrayBlockingQueue<LoggingEvent>(this.qsize);
        this.terminate = new AtomicBoolean(false);
        this.pendingEvents = new AtomicInteger();
        this.mustNotify = new AtomicBoolean(false);
        this.failureDate = null;
        this.failureCause = null;
        this.nbDroppedMsg = 0;

    }

    public ThrottlingProvider() {
        // Default value
        this(DEFAULT_PERIOD, DEFAULT_THRESHOLD, DEFAULT_QSIZE, false);

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
                System.out.println("Invalid parameter for property " + periodProperty + ". Must be a positive integer");
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
                System.out.println("Invalid parameter for property " + qsizeProperty + ". Must be a positive integer");
            }
        }
        this.qsize = value;

        boolean b = false;
        prop = System.getProperty(noLogFileProperty);
        if (prop != null) {
            b = Boolean.parseBoolean(prop);
        }
        this.noLogFile = b;
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
                LinkedList<LoggingEvent> events = new LinkedList<LoggingEvent>();

                if (this.failureDate != null) {
                    String msg;
                    if (this.noLogFile) {
                        msg = this.nbDroppedMsg + " logging events dropped by " + this.getClass().getName() + "since " +
                              DateFormat.getDateInstance().format(this.failureDate);
                    } else {
                        msg = this.nbDroppedMsg + " logging events dropped by " + this.getClass().getName() + "since " +
                              DateFormat.getDateInstance().format(this.failureDate) +
                              ". A copy of theses logging events is available in " + this.getErrorAppender().getFile() +
                              " on " + ProActiveRuntimeImpl.getProActiveRuntime().getVMInformation().getHostName();
                    }
                    Logger l = Logger.getLogger(this.getClass());
                    LoggingEvent le = new LoggingEvent(Category.class.getName(), l, Level.INFO, msg, this.failureCause);
                    events.add(le);
                }

                for (int i = 0; i < nbEvent; i++) {
                    events.add(buffer.poll());
                }

                try {
                    if (events.size() != 0) {
                        collector.sendEvent(events);
                        this.failureDate = null;
                        this.failureCause = null;
                        this.nbDroppedMsg = 0;
                    }
                } catch (Throwable t) {
                    if (this.failureDate == null) {
                        this.failureDate = new Date();
                        this.failureCause = t;
                    } else {
                        // Remove the inserted logging event
                        events.remove(0);
                    }

                    this.nbDroppedMsg += nbEvent;

                    for (LoggingEvent le : events) {
                        this.logError(le);
                    }
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
            this.logError(event);
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
    synchronized private FileAppender getErrorAppender() {
        if (this.errorAppender == null && !this.noLogFile) {
            try {
                Layout layout = new PatternLayout("%X{shortid@hostname} - [%p %20.20c{2}] %m%n");
                ProActiveRuntimeImpl part = ProActiveRuntimeImpl.getProActiveRuntime();
                String runtimeName = part.getVMInformation().getName();
                this.errorAppender = new FileAppender(layout, "throttlingProvider-" + runtimeName + ".log");

                // Add an explanatory message at the begining of the file
                String msg = "This file has been created to prevent logging event loss when the distributed log4j framework is enabled. If a logging event is created while the provider is sending the buffered logging events to the log collector, it is dumped into this file. It prevents logging events loss and infinite loops. If you see this file, most likely, the log collector became unreachable or some low level ProActive loggers have been activated.";
                Logger l = Logger.getLogger(this.getClass());
                LoggingEvent le = new LoggingEvent(Category.class.getName(), l, Level.INFO, msg, null);
                this.errorAppender.append(le);
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

    private void logError(LoggingEvent le) {
        if (!this.noLogFile) {
            FileAppender appender = getErrorAppender();
            if (appender != null) {
                appender.doAppend(le);
            } else {
                System.err.println(le.getLoggerName() + " " + le.getLevel() + " " + le.getMessage());
            }
        }
    }
}
