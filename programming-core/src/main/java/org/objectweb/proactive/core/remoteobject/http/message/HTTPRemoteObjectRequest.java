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

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.remoteobject.http.util.HTTPRegistry;
import org.objectweb.proactive.core.remoteobject.http.util.HttpMessage;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.Sleeper;


public class HTTPRemoteObjectRequest extends HttpMessage implements Serializable {
    private Request request;

    public HTTPRemoteObjectRequest(Request request, String url) {
        super(url);
        this.request = request;
    }

    public Object getReturnedObject() {
        return this.returnedObject;
    }

    @Override
    public boolean isOneWay() {
        return this.request.isOneWay();
    }

    /**
     *
     */
    @Override
    public Object processMessage() {
        try {
            InternalRemoteRemoteObject ro = HTTPRegistry.getInstance().lookup(URIBuilder.getNameFromURI(url));
            int max_retry = 5;

            if (ro == null) {
                // this case happens when a method call has been performed while the
                // registration in the registry has not yet been completed.
                // this mostly appears in multithreaded code.
                Sleeper sleeper = new Sleeper(1000, ProActiveLogger.getLogger(Loggers.SLEEPER));
                while ((ro == null) && (max_retry > 0)) {
                    sleeper.sleep();
                    ro = HTTPRegistry.getInstance().lookup(URIBuilder.getNameFromURI(url));
                    max_retry--;
                }
            }

            Object o = ro.receiveMessage(this.request);

            return o;
        } catch (Throwable e) {
            // this point is mostly reached when the remote object is unknown by the registry.
            // functional exceptions have already been caught deeper in the remote object code.
            // Due to the current implementation, the only way to return the exception is by wrapping
            // it into a methodcallresult inside a synchronousreply.
            // this solves PROACTIVE-717
            return new SynchronousReplyImpl(new MethodCallResult(null,
                                                                 new IOException("remote object " + url +
                                                                                 "not found")));
        }
    }
}
