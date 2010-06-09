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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.runtime.broadcast;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;

import java.net.URI;
import java.net.URISyntaxException;


class RTBroadcastActionDefaultImpl implements RTBroadcastAction {

    //
    // -- CONSTRUCTOR -----------------------------------------------
    //
    public RTBroadcastActionDefaultImpl() {
    }

    //
    // -- NEW JVM -----------------------------------------------
    //
    public void creationHandler(String url) {
        //  just do nothing by default
    }

    //
    // -- DISCOVER -----------------------------------------------
    //
    public void discoverHandler(String url) {
        //--Check if the message is coming from me. in this case -> no action
        if (url.equals(RTBroadcaster.getInstance().getCallbackUri().toString())) {
            // we are the sender of the notification
            // just skip it
            return;
        }

        //--New remote object runtime callback
        BTCallback rtCallback = null;
        try {
            rtCallback = (BTCallback) PARemoteObject.lookup(new URI(url));
            rtCallback.register(ProActiveRuntimeImpl.getProActiveRuntime().getURL());
        } catch (ProActiveException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
