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

/**
 * <P>
 * An object instance of this class is to be returned when a method of an active
 * object needs to be called synchronously. No real data is expected as a result,
 * but the caller needs to wait the actual execution (service) of the request.
 * Because the class is final, the reified call cannot return a future and will
 * do a synchronous call.
 * </P><P>
 * This type can be used instead of void to perform the synchronous call. Note that
 * any final object will result in the same behaviour.
 * </P>
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */
public final class ObjectForSynchronousCall extends Object implements java.io.Serializable {

    /**
     * No arg constructor for Serializable
     */
    public ObjectForSynchronousCall() {
    }
}
