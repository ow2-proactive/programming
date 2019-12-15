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
package org.objectweb.proactive.core.runtime.broadcast;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


class RTBroadcastActionDefaultImpl implements RTBroadcastAction {

    public static Logger logger = ProActiveLogger.getLogger(Loggers.RUNTIME);

    //
    // -- CONSTRUCTOR -----------------------------------------------
    //
    public RTBroadcastActionDefaultImpl() {
    }

    //
    // -- NEW JVM -----------------------------------------------
    //
    public void creationHandler(String url) throws BroadcastDisabledException {
        //  just do nothing by default
    }

    //
    // -- DISCOVER -----------------------------------------------
    //
    public void discoverHandler(String url) throws BroadcastDisabledException {
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
        } catch (ProActiveException | URISyntaxException e) {
            logger.error("", e);
        }
    }

}
