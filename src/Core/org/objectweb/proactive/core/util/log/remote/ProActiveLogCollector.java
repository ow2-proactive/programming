package org.objectweb.proactive.core.util.log.remote;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;


/**
 * A Remotely accessible log collector
 * 
 * Remote {@link ProActiveAppender}s send all the logging events to a log
 * collector which given them to the correct logger/appender. So, all the log4j
 * events of the application are handled by the same hierarchy on the same machine.
 */
public class ProActiveLogCollector {
    final private ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<String, Logger>();

    public void sendEvent(List<LoggingEvent> events) {
        for (LoggingEvent event : events) {
            Logger l;

            l = loggers.get(event.getLoggerName());
            if (l == null) {
                String name = event.getLoggerName();
                l = Logger.getLogger(name);
                loggers.put(name, l);
            }

            /* Event are filtered by client side, not server side */
            l.callAppenders(event);
        }
    }
}
