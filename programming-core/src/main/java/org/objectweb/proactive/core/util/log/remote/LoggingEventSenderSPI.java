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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.spi.LoggingEvent;
import org.objectweb.proactive.core.config.PAProperties;


/**
 * A logging event sender service.
 * 
 * To ease the deployment of distributed applications, a logging event sender
 * service can be used to centralize all the log4j logging events on a given
 * machine. Usually the machine who loaded the GCM Application descriptor also
 * host the log collector.
 * 
 * This service allows to use a custom service provider. It can be useful if the
 * default service provider does not match your expectation in term of
 * reliability, performances, or functionalities.
 * 
 * Each time a logging event is received by a {@link LoggingEventSenderSPI}, it
 * is forwarded to the logging event sender service through the
 * {@link LoggingEventSenderSPI#append(LoggingEvent)} method. This
 * event can be send to the collector immediately or buffered for later
 * delivery.
 * 
 * Since immediate sending has a dramatic impact on performances; it is often
 * better to send the logging events to collector in batch. It can be done in
 * the {@link LoggingEventSenderSPI#append(LoggingEvent)} method when a
 * condition is meet, or by using a dedicated daemon thread. A such thread is
 * started by the {@link ProActiveAppender}. Service providers must override
 * {@link LoggingEventSenderSPI#run} to define a flushing policy. If the daemon
 * flushing thread is not useful, then the method can do nothing.
 * 
 * It is critical to flush all known logging events before the JVM exit. A
 * shutdown hook will call the {@link LoggingEventSenderSPI#terminate()} method
 * to allow the service provider to flush all buffered logging events. This
 * method and the flushing thread can run concurrently. They must be properly
 * synchronized
 * 
 * @see ProActiveAppender
 *@see ProActiveLogCollector
 *@see PAProperties#PA_LOG4J_APPENDER_PROVIDER
 */
public abstract class LoggingEventSenderSPI {

    private AtomicReference<ProActiveLogCollector> collector;

    public LoggingEventSenderSPI() {
        this.collector = new AtomicReference<ProActiveLogCollector>();
    }

    /**
     * Add a new logging event to send
     * 
     * Called each time {@link ProActiveAppender#append(LoggingEvent)} is called
     * 
     * Must be thread safe and should be lightweight since it is called from
     * user code.
     * 
     * Performing a remote call in this method has a major impact on
     * performances
     * 
     */
    abstract public void append(LoggingEvent event);

    /**
     * Sends the logging event to the collector, in a daemon thread
     * 
     * {@link ProActiveAppender} starts a thread and invokes this method. It
     * can, and should, be used to send the logging events to the collector in
     * batch according a flushing policy.
     * 
     */
    abstract public void run();

    /**
     * Called from a shutdown hook
     * 
     * To avoid to loose logging events when on exit, a shutdown hook notify the
     * service provider that it should flush all the buffered logging events.
     * 
     * The flushing thread can still be running. So
     * {@link LoggingEventSenderSPI#terminate()} and
     * {@link LoggingEventSenderSPI#run()} methods must be properly
     * synchronized.
     */
    abstract public void terminate();

    /**
     * Set the collector
     * 
     * Can be called once and only once
     * 
     * @throws IllegalStateException
     *             if the collector is already set
     * 
     */
    final void setCollector(ProActiveLogCollector collector) {
        if (this.collector.get() != null) {
            throw new IllegalStateException("Log collector already set");
        }

        this.collector.set(collector);
    }

    /**
     * Get the collector
     * 
     * @return the collector or null if not yet available
     */
    final protected ProActiveLogCollector getCollector() {
        return this.collector.get();
    }
}
