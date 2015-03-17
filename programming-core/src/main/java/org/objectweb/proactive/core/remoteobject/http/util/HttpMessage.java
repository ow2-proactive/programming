/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
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

    private static final long serialVersionUID = 61L;
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
