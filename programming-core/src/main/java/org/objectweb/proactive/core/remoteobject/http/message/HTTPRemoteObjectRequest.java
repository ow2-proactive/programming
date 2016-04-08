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
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
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

    private static final long serialVersionUID = 60L;
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
            return new SynchronousReplyImpl(new MethodCallResult(null, new IOException("remote object " +
                url + "not found")));
        }
    }
}
