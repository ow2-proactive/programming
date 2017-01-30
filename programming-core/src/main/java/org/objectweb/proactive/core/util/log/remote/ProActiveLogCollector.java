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
