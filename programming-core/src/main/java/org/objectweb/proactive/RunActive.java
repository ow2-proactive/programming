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
package org.objectweb.proactive;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * <P>
 * RunActive is related to the activity of an active object.
 * When an active object is started, which means that its
 * active thread starts and serves the requests being sent
 * to its request queue, it is possible to define exactly how
 * the activity (the serving of requests amongst others) will
 * be done.
 * </P><P>
 * An object implementing this interface is invoked to run the
 * activity until an event trigger its end. The object being
 * reified as an active object can directly implement this interface
 * or an external class can also be used.
 * </P>
 * <P>
 * It is the role of the body of the active object to perform the
 * call on the object implementing this interface. For an active object
 * to run an activity, the method <code>runActivity</code> must not end
 * before the end of the activity. When the method <code>runActivity</code>
 * ends, the activity ends too and the <code>endActivity</code> can be invoked.
 * </P>
 * <P>
 * Here is an example of a simple implementation of <code>runActivity</code> method
 * doing a FIFO service of the request queue :
 * </P>
 * <pre>
 * public void runActivity(Body body) {
 *   Service service = new Service(body);
 *   while (body.isActive()) {
 *     service.blockingServeOldest();
 *   }
 * }
 * </pre>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/06
 * @since   ProActive 0.9.3
 */
@PublicAPI
public interface RunActive extends Active {

    /**
     * Runs the activity of the active object.
     * @param body the body of the active object being started
     */
    public void runActivity(Body body);
}
