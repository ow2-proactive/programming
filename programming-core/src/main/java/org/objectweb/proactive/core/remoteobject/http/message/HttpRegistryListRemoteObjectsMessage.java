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
package org.objectweb.proactive.core.remoteobject.http.message;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;


/**
 * This classes represents a HTTPMessage. When processed, this message performs a lookup thanks to the urn.
 * @author The ProActive Team
 * @see HttpMessage
 */
public class HttpRegistryListRemoteObjectsMessage extends HttpMessage implements Serializable {
    private String urn;

    //Caller Side

    /**
     * Constructs an HTTP Message
     * @param urn The urn of the Object (it can be an active object or a runtime).
     */
    public HttpRegistryListRemoteObjectsMessage(URI url) {
        super(url.toString());
    }

    /**
     * Get the returned object.
     * @return the returned object
     */
    public String[] getReturnedObject() {
        return (String[]) this.returnedObject;
    }

    //Callee side

    /**
     * Performs the lookup
     * @return The Object associated with the urn
     */
    @Override
    public Object processMessage() {
        String[] uri = HTTPRegistry.getInstance().list();
        //            System.out.println("HttpRemoteObjectLookupMessage.processMessage() ++ ro at " + url +" : " +ro) ;
        this.returnedObject = uri;
        return this.returnedObject;
    }
}
