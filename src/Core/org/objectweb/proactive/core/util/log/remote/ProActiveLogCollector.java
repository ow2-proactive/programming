/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
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
