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
package org.objectweb.proactive.core.mop;

import org.objectweb.proactive.annotation.PublicAPI;


/**
 * References on an active object are indirect link to the active object. There
 * is some interposition objects between the caller and the targeted active
 * object like a StubObject and a Proxy object. It is possible to know if an
 * object is a reference onto an active object by checking if the object
 * implements StubObject. A reference can be either on a local (on the same
 * runtime) or on a distant (on a remote runtime) active object. if an object is
 * a reference onto an active object, it implements StubObject but also the
 * class of the active object allowing to perform method call as if the method
 * call was made on the active object
 */
@PublicAPI
public interface StubObject {

    /**
     * set the proxy to the active object
     */
    public void setProxy(Proxy p);

    /**
     * return the proxy to the active object
     */
    public Proxy getProxy();
}
