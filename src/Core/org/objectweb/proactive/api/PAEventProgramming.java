/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
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
package org.objectweb.proactive.api;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.future.FutureProxy;
import org.objectweb.proactive.core.mop.StubObject;


/**
 * This class provides methods to react when a future is updated.
 */
@PublicAPI
public class PAEventProgramming {
    /**
     * Register a method in the calling active object to be called when the specified future is
     * updated. The registered method takes a java.util.concurrent.Future as parameter.
     * 
     * This method must be called from a Body.
     * 
     * @param future
     *            the future to watch
     * @param methodName
     *            the name of the method to call on the current active object
     * @throws IllegalArgumentException
     *             if the first argument is not a future or if the method could not be found
     * @throws IllegalStateException
     *             if the caller is not a Body
     */
    public static void addActionOnFuture(Object future, String methodName) {
        FutureProxy f;
        try {
            f = (FutureProxy) ((StubObject) future).getProxy();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected a future, got a " + future.getClass());
        }

        f.addCallback(methodName);
    }
}
