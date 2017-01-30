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
package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.body.request.RequestImpl;
import org.objectweb.proactive.core.mop.ConstructorCall;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;


/**
 * A synchrounous proxy send reified method calls in a synchronous fashion.
 * Two methods are available for that :
 *  1 - the reify method that expects a MethodCall as parameter and that is capable
 *  of creating the corresponding request
 *  2 - the receiveMessage method only pass an already created request to the remote target.
 *  This last method is used to avoid a double reification when an active object sends a request
 *  (a request containing an other request). It could also be used by the forwarder mechanism.
 *  @author The ProActive Team
 */
public class SynchronousProxy implements Proxy, Serializable {
    protected RemoteObject remoteObject;

    public SynchronousProxy(ConstructorCall contructorCall, Object[] params) throws ProActiveException {
        Object p0 = params[0];
        if (p0 instanceof RemoteObject) {
            this.remoteObject = (RemoteObject) p0;
        } else if (p0 instanceof RemoteRemoteObject) {
            this.remoteObject = new RemoteObjectAdapter((RemoteRemoteObject) p0);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.objectweb.proactive.core.mop.Proxy#reify(org.objectweb.proactive.core.mop.MethodCall)
     */
    public Object reify(MethodCall c) throws Throwable {
        Request r = new RequestImpl(c, c.isOneWayCall());

        SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(r);

        if (reply != null) {
            MethodCallResult rr = reply.getResult();
            if (rr.getException() != null) {
                throw rr.getException();
            }

            return reply.getResult().getResult();
        }

        return null;
    }

    /**
     * this method forward the request passed as parameter to the target without
     * encapsulating it within a new request.
     * @param request the request to send
     * @return the result of the method call
     * @throws Throwable if the result was an exception or if the method call raised
     * an exception then this exception is thrown
     */
    public Object receiveMessage(Request request) throws Throwable {
        SynchronousReplyImpl reply = (SynchronousReplyImpl) this.remoteObject.receiveMessage(request);

        if (reply != null) {
            MethodCallResult rr = reply.getResult();
            if (rr.getException() != null) {
                throw rr.getException();
            }

            return reply.getResult().getResult();
        }

        return null;
    }

    /**
     * Sets the remote remote object identified as target by this proxy
     * @param rro the remote remote object identified as target by this proxy
     * @throws ProActiveException if the remote remote object does not exist
     */
    public void setRemoteObject(RemoteRemoteObject rro) throws ProActiveException {
        this.remoteObject = new RemoteObjectAdapter(rro);
    }

    /**
     * set the remote object identified as target of this proxy
     * @param ro the remote object identified as target by this proxy
     */
    public void setRemoteObject(RemoteObject ro) {
        this.remoteObject = ro;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((remoteObject == null) ? 0 : remoteObject.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SynchronousProxy other = (SynchronousProxy) obj;
        if (remoteObject == null) {
            if (other.remoteObject != null) {
                return false;
            }
        } else if (!remoteObject.equals(other.remoteObject)) {
            return false;
        }
        return true;
    }

    public RemoteObject getRemoteObject() {
        return this.remoteObject;
    }
}
