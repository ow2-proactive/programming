package org.objectweb.proactive.core.util.log.remote;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.spi.ServiceRegistry;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.config.PAProperties;


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
        this.timer.schedule(new ConsolePinter(), GRACE_TIME);
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
        spi.append(event);

        if (!this.collectorKnow.get()) {
            bufferedEvents.add(event);
        }
    }

    @Override
    public void close() {
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
                System.out
                        .println("Cannot find service provider: " + provider + " for the service " +
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

    class ConsolePinter extends TimerTask {
        @Override
        public void run() {
            if (collectorKnow.get()) {
                // The collector has been retrieved
                // The buffered log event can safely be discarded
                bufferedEvents.clear();
                return;
            }

            synchronized (bufferedEvents) {
                System.out
                        .println("The ProActive log4j collector is still not availabled, printing logging events on the console to avoid log loss");
                Iterator<LoggingEvent> it = bufferedEvents.iterator();
                while (it.hasNext()) {
                    LoggingEvent event = it.next();
                    it.remove();
                    System.out.println(getLayout().format(event));
                }
            }

            timer.schedule(new ConsolePinter(), GRACE_TIME);
        }
    }

    static public void notifyIsReady() {
        for (ProActiveAppender appender : appenders) {
            appender.doLookup();
        }
    }

    private void doLookup() {
        try {
            String url = PAProperties.PA_LOG4J_COLLECTOR.getValue();
            if (url != null) {
                ProActiveLogCollector collector;
                collector = (ProActiveLogCollector) PARemoteObject.lookup(URI.create(url));
                spi.setCollector(collector);
                this.collectorKnow.set(true);
                this.bufferedEvents.clear();
            } else {
                System.err.println("ProActiveAppender loaed but" + PAProperties.PA_LOG4J_COLLECTOR.getKey() +
                    " is null");
            }
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }
}