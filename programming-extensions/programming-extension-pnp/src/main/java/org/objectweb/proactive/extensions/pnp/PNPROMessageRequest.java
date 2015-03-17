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
package org.objectweb.proactive.extensions.pnp;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.SynchronousReplyImpl;
import org.objectweb.proactive.core.util.URIBuilder;


/** Represent a {@link Request} */

class PNPROMessageRequest extends PNPROMessage implements Serializable {

    private static final long serialVersionUID = 61L;
    private Request request;

    /** Construct a request message
     *
     * @param request the request to be send
     * @param uri the recipient (aka the remote object) of the request
     * @param agent the local agent to use to send this message
     */
    public PNPROMessageRequest(Request request, URI uri, PNPAgent agent) {
        super(uri, agent);
        this.request = request;

        // this statement is not needed but someone will want to change it in:
        //   this.asynchronous = request.isOneWay().
        //
        // IT DOES NOT WORK. Even if a ProActive Request is one way, we must perform the
        // rendez-vous and wait for the answer. asynchronous messages and one way request ARE NOT
        // THE SAME THING.
        this.isAsynchronous = isAsynchronous(request);
    }

    private boolean isAsynchronous(Request request) {
        // TODO FIXME UGLY UGLY UGLY UGLY UGLY HACK
        // Chuck norris will kill your mother if not fixed before the merge
        return "killRT".equals(request.getMethodName());
    }

    /** Get the response of this request */
    // client side
    public Object getReturnedObject() {
        return this.returnedObject;
    }

    @Override
    // server side
    public Object processMessage() {
        if (logger.isTraceEnabled()) {
            logger.trace("Executing the request message " + this.request + " on " + uri);
        }

        try {
            InternalRemoteRemoteObject ro;

            ro = PNPRegistry.singleton.lookup(URIBuilder.getNameFromURI(uri));
            if (ro == null) {
                return new SynchronousReplyImpl(new MethodCallResult(null, new IOException("remote object " +
                    uri + " not found. Message " + request + " cannot be processed ")));
            } else {
                return ro.receiveMessage(this.request);
            }
        } catch (Throwable e) {
            return new SynchronousReplyImpl(new MethodCallResult(null, new IOException6(uri +
                " failed to process message " + this.request, e)));
        }
    }
}
