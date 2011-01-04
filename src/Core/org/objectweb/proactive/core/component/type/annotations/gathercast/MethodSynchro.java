/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.type.annotations.gathercast;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for specifying timeouts and waitForAll mode at the level of methods.
 * <br>
 * Timeouts are only handled for invocations of methods that return a result. When a timeout is detected, the
 * default behavior is to throw a GathercastTimeoutException to the clients.
 * <br>
 * If waitForAll is set to true (default behaviour), the method will wait for the requests of all components binded
 * on the gathercast interface before to be executed. If waitForAll is set to false, the method will be executed
 * immediately on the first request received (therefore will not wait for requests from other components binded on
 * the gathercast interface).
 * <br>
 * Also, if waitForAll is set to false then it could not be combined with a timeout (throw an
 * {@link InstantiationException}).
 *
 * @author The ProActive Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MethodSynchro {
    public long DEFAULT_TIMEOUT = -1;

    /**
     * @return The timeout in milliseconds
     */
    long timeout() default DEFAULT_TIMEOUT;

    /**
     * @return True if the method will wait for the requests of all components binded on the gathercast interface
     * before to be executed, false otherwise
     */
    boolean waitForAll() default true;
}
