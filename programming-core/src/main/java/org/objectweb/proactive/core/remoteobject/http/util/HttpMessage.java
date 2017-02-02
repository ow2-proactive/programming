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

import java.io.Serializable;

import org.objectweb.proactive.core.remoteobject.http.util.exceptions.HTTPRemoteException;


/**
 * This interface is used to encapsulate any kind of HTTP message.
 * @author The ProActive Team
 * @see java.io.Serializable
 */
public abstract class HttpMessage implements Serializable {
    protected Object returnedObject;

    protected String url;

    public HttpMessage(String url) {
        this.url = url;
    }

    /**
     * Processes the message.
     * @return an object as a result of the execution of the message
     */
    public abstract Object processMessage();

    public boolean isOneWay() {
        return false;
    }

    /**
     * @throws HTTPRemoteException
     */
    public final void send() throws HTTPRemoteException {
        HttpMessageSender hms = new HttpMessageSender(this.url);
        this.returnedObject = hms.sendMessage(this);
    }
}
