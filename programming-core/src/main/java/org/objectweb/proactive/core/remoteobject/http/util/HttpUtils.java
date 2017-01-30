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
package org.objectweb.proactive.core.remoteobject.http.util;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;


/**
 * @author The ProActive Team
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class HttpUtils {
    public static final String SERVICE_REQUEST_CONTENT_TYPE = "application/java";

    //    public static final String SERVICE_REQUEST_URI = "/ProActiveHTTP";

    /**
     *  Search a Body matching with a given unique ID
     * @param id The unique id of the body we are searching for
     * @return The body associated with the ID
     */
    public static Body getBody(UniqueID id) {
        LocalBodyStore bodyStore = LocalBodyStore.getInstance();

        // check if the id corresponds to a local body

        Body body = bodyStore.getLocalBody(id);
        if (body != null) {
            return body;
        }

        // the reference does not belong to an active object
        // looking for an half body

        body = bodyStore.getLocalHalfBody(id);
        if (body != null) {
            return body;
        }

        // the id does not correspond to a local body
        // neither a half body, could be a forwarder

        body = bodyStore.getForwarder(id);
        return body;
    }
}
