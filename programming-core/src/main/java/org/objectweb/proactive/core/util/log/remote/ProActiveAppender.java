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

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.spi.ServiceRegistry;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.utils.SafeTimerTask;


/**
 * A log4j appender which send the logging events to a remote log collector
 * through ProActive
 * 
 * This class is a wrapper around the {@link LoggingEventSenderSPI}. It is in
 * charge of starting the flushing thread and to register a shutdown hook to
 * send all the log messages before the JVM exits.
 * 
 * Users can provide a custom implementation by registering a new service
 * provider for the server {@link LoggingEventSenderSPI}. The service provider
 * to use is defined by the ProActive property
 * {@link PAProperties#PA_LOG4J_APPENDER_PROVIDER}
 * 
 */
public final class ProActiveAppender extends AppenderSkeleton {

    final static List<ProActiveAppender> appenders = new LinkedList<ProActiveAppender>();

    final private LoggingEventSenderSPI spi;

    final private AtomicBoolean collectorKnow;

    final private ConcurrentLinkedQueue<LoggingEvent> bufferedEvents;

    final private Timer timer;

    final private int GRACE_TIME = 30000; // ms

    public ProActiveAppender() {
        super();

        appenders.add(this);

        this.collectorKnow = new AtomicBoolean(false);
        this.bufferedEvents = new ConcurrentLinkedQueue<LoggingEvent>();
        this.spi = findSPI();

        startFlushingThread();

        this.timer = new Timer();
        this.timer.schedule(new ConsolePrinter(), GRACE_TIME);
    }

    public ProActiveAppender(LoggingEventSenderSPI spi, ProActiveLogCollector collector) {
        super();

        this.collectorKnow = new AtomicBoolean(true);
        // these fields are never used since the collector is already known
        this.bufferedEvents = null;
        this.timer = null;

        this.spi = spi;
        this.spi.setCollector(collector);

        startFlushingThread();
    }

    @Override
    protected void append(LoggingEvent event) {
        /*
         * Since provider can be multithreaded, thread local informations must be
         * saved retrieved before giving the logging event to the provider.
         */
        event.getThreadName();
        event.getRenderedMessage();
        event.getNDC();
        event.getMDCCopy();
        event.getThrowableStrRep();

        spi.append(event);

        if (!this.collectorKnow.get()) {
            bufferedEvents.add(event);
        }
    }

    @Override
    public void close() {
        this.spi.terminate();
        super.closed = true;
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    private void startFlushingThread() {
        // Start the flushing thread
        Thread t;
        t = new Thread(new TerminateLogFlusher(this.spi));
        Runtime.getRuntime().addShutdownHook(t);

        t = new Thread(new RunLogFlusher(this.spi));
        t.setDaemon(false);
        t.setName("ProActive log4j flusher");
        t.start();
    }

    private LoggingEventSenderSPI findSPI() {
        LoggingEventSenderSPI spi = null;

        // Do not use a PAProperties here, or an infinite recursion will kill
        // your mom
        String provider = System.getProperty("proactive.log4j.appender.provider");
        if (provider != null) {
            // User asked for a specific provider
            Iterator<LoggingEventSenderSPI> iter;
            iter = ServiceRegistry.lookupProviders(LoggingEventSenderSPI.class);
            while (iter.hasNext()) {
                LoggingEventSenderSPI next = iter.next();
                if (next.getClass().getName().equals(provider)) {
                    spi = next;
                    break;
                }
            }

            if (spi == null) {
                System.out.println("Cannot find service provider: " + provider + " for the service " +
                                   LoggingEventSenderSPI.class.getName() +
                                   ". Any service provider will be used as fallback");
            }
        }

        // Use the first provider as fallback
        if (spi == null) {
            Iterator<LoggingEventSenderSPI> iter;
            iter = ServiceRegistry.lookupProviders(LoggingEventSenderSPI.class);
            spi = iter.next();
        }

        // No service provider found
        if (spi == null) {
            throw new ProActiveRuntimeException("No provider availble for the service " +
                                                LoggingEventSenderSPI.class.getName());
        }

        return spi;
    }

    private class RunLogFlusher implements Runnable {
        final private LoggingEventSenderSPI remoteAppender;

        public RunLogFlusher(LoggingEventSenderSPI remoteAppender) {
            this.remoteAppender = remoteAppender;
        }

        public void run() {
            remoteAppender.run();
        }

    }

    private class TerminateLogFlusher implements Runnable {
        final private LoggingEventSenderSPI remoteAppender;

        public TerminateLogFlusher(LoggingEventSenderSPI remoteAppender) {
            this.remoteAppender = remoteAppender;
        }

        public void run() {
            remoteAppender.terminate();
        }
    }

    class ConsolePrinter extends SafeTimerTask {
        @Override
        public void safeRun() {
            if (collectorKnow.get()) {
                // The collector has been retrieved
                // The buffered log event can safely be discarded
                bufferedEvents.clear();
                return;
            }

            synchronized (bufferedEvents) {
                System.out.println("The ProActive log4j collector is still not availabled, printing logging events on the console to avoid log loss");
                Iterator<LoggingEvent> it = bufferedEvents.iterator();
                while (it.hasNext()) {
                    LoggingEvent event = it.next();
                    it.remove();
                    System.out.println(getLayout().format(event));
                }
            }

            timer.schedule(new ConsolePrinter(), GRACE_TIME);
        }
    }

    static public void notifyIsReady() {
        for (ProActiveAppender appender : appenders) {
            appender.doLookup();
        }
    }

    private void doLookup() {
        try {
            String url = CentralPAPropertyRepository.PA_LOG4J_COLLECTOR.getValue();
            if (url != null) {
                ProActiveLogCollector collector;
                collector = (ProActiveLogCollector) PARemoteObject.lookup(URI.create(url));
                spi.setCollector(collector);
                this.collectorKnow.set(true);
                this.bufferedEvents.clear();
            } else {
                System.err.println("ProActiveAppender loaed but" +
                                   CentralPAPropertyRepository.PA_LOG4J_COLLECTOR.getName() + " is null");
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }
}
