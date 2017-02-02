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

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.http.HTTPRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.http.HttpRemoteObjectImpl;
import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;
import org.objectweb.proactive.core.util.URIBuilder;


/**
 * This classes represents a HTTPMessage. When processed, this message performs a lookup thanks to the urn.
 * @author The ProActive Team
 * @see HttpMessage
 */
public class HttpRemoteObjectLookupMessage extends HttpMessage implements Serializable {
    //Caller Side

    /**
     * Constructs an HTTP Message
     * @param urn The urn of the Object (it can be an active object or a runtime).
     */
    public HttpRemoteObjectLookupMessage(String url) {
        super(url);
    }

    /**
     * Get the returned object.
     * @return the returned object
     */
    public RemoteRemoteObject getReturnedObject() {
        return (RemoteRemoteObject) this.returnedObject;
    }

    //Callee side

    /**
     * Performs the lookup
     */
    @Override
    public Object processMessage() {
        InternalRemoteRemoteObject irro = HTTPRegistry.getInstance().lookup(URIBuilder.getNameFromURI(url));

        if (irro != null) {
            RemoteRemoteObject rro = null;
            try {
                rro = new HTTPRemoteObjectFactory().newRemoteObject(irro);
                ((HttpRemoteObjectImpl) rro).setURI(URI.create(url));
            } catch (ProActiveException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            this.returnedObject = rro;
        }
        return this.returnedObject;
    }
}
