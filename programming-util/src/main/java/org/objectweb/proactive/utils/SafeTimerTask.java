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
package org.objectweb.proactive.utils;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * A safer version of {@link TimerTask}.
 * 
 * <p>{@link Timer} and {@link TimerTask} are error prone since they do not 
 * handle exceptions thrown by {@link TimerTask#run()} implementations. It means
 * that if a <i>run</i> method throws an exception the whole timer is killed.</p>
 * 
 * <p>This wrapper offers a safer API. If an exception is thrown an error message
 * is printed but the timer is not killed.</p> 
 *  
 * @author ProActive team
 * @since  ProActive 5.1.0
 */
public abstract class SafeTimerTask extends TimerTask {
    /** Logger to use or null. */
    final private Logger logger;

    /** Level at which the message is logged */
    final private Level level;

    /**
     * Create a new safe timer task.
     * 
     * Like with TimerTask, the stack trace is printed on stderr; but the thread 
     * is not killed by the exception.
     */
    public SafeTimerTask() {
        this.logger = null;
        this.level = Level.INFO;
    }

    /**
     * Create a new safe timer task with specific logger.
     * 
     * Instead of printing the stack trace on stderr, an error message is send
     * to a specific logger at INFO level.
     * 
     * @param logger
     *    The logger to be used
     * @throws NullPointerException
     *    If logger is null
     */
    public SafeTimerTask(Logger logger) {
        ArgCheck.requireNonNull(logger);
        this.logger = logger;
        this.level = Level.INFO;
    }

    /**
     * Create a new safe timer task with specific logger.
     * 
     * Instead of printing the stack trace on stderr, an error message is send
     * to a specific logger at a specific level.
     * 
     * @param logger
     *    The logger to be used
     * @param level
     *    The level at which the message is logged
     * @throws NullPointerException
     *    If logger or level is null.
     */
    public SafeTimerTask(Logger logger, Level level) {
        ArgCheck.requireNonNull(logger);
        ArgCheck.requireNonNull(level);
        this.logger = logger;
        this.level = level;
    }

    @Override
    final public void run() {
        try {
            this.safeRun();
        } catch (Throwable t) {
            if (this.logger == null) {
                t.printStackTrace();
            } else {
                this.logger.log(this.level, "Exception caught in safeRun() method: ", t);
            }
        }
    }

    /**
     * The action to be performed by this safe timer task.
     */
    abstract public void safeRun();
}
