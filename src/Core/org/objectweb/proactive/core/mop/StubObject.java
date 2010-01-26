/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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
