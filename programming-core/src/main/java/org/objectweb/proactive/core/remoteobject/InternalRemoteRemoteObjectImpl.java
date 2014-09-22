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
package org.objectweb.proactive.core.remoteobject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.mop.MethodCallExecutionFailedException;


/**
 * An InternalRemoteRemoteObject is a generic object owns by any remote remote object.
 * It seats between the protocol dependent part of the remote object on the server
 * side (the XXXRemoteObjectImpl) and the remote object implementation.
 * It handles all the requests related to the communication protocol of a remote remote object
 * that acts as a proxy for a remote object.
 * Whereas it can be seen as a protocol dependent object, the internal remote remote object behaviour is the same
 * for all the implementation of any protocol. This is why it is an internal remote remote
 * object, hidden to the level on the protocol dependent part of the remote object it represents
 * that only provides a transport layer
 */
public class InternalRemoteRemoteObjectImpl implements InternalRemoteRemoteObject {

    private static final long serialVersionUID = 60L;

    /**
     * the remote remote object of the internal remote remote object.
     * Remote method calls are received by this remote remote object,
     * go through the internal remote remote object.
     */
    private RemoteRemoteObject remoteRemoteObject;

    /**
     * the URI where the remote remote object is bound
     */
    private URI uri;

    /**
     * the remote object that contains the reified object
     */
    private transient RemoteObject<?> remoteObject;

    //    public InternalRemoteRemoteObjectImpl() {
    //    }
    //
    //    public InternalRemoteRemoteObjectImpl(RemoteObject<?> ro) {
    //        this.remoteObject = ro;
    //    }

    public InternalRemoteRemoteObjectImpl(RemoteObject<?> ro, URI uri) {
        this.remoteObject = ro;
        this.uri = uri;
    }

    /**
     * Constructor for an internal remote remote object
     * @param ro the remote object to represent
     * @param uri the uri where the remote remote object is bound
     * @param rro the remote remote object activated on a given URI for a given
     * protocol
     */
    public InternalRemoteRemoteObjectImpl(RemoteObject<?> ro, URI uri, RemoteRemoteObject rro) {
        this.remoteObject = ro;
        this.uri = uri;
        this.remoteRemoteObject = rro;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#getURI()
     */
    public URI getURI() {
        return this.uri;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#setURI(java.net.URI)
     */
    public void setURI(URI uri) throws ProActiveException, IOException {
        this.uri = uri;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.RemoteRemoteObject#receiveMessage(org.objectweb.proactive.core.body.request.Request)
     */
    public Reply receiveMessage(Request message) throws ProActiveException, IOException {
        if (message instanceof InternalRemoteRemoteObjectRequest) {
            try {
                Object o = ((InternalRemoteRemoteObjectRequest) message).execute(this);
                return new SynchronousReplyImpl(new MethodCallResult(o, null));
            } catch (MethodCallExecutionFailedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return this.remoteObject.receiveMessage(message);
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#getRemoteObject()
     */
    public RemoteObject getRemoteObject() {
        return this.remoteObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#getRemoteRemoteObject()
     */
    public RemoteRemoteObject getRemoteRemoteObject() {
        return this.remoteRemoteObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#setRemoteRemoteObject(org.objectweb.proactive.core.remoteobject.RemoteRemoteObject)
     */
    public void setRemoteRemoteObject(RemoteRemoteObject remoteRemoteObject) {
        this.remoteRemoteObject = remoteRemoteObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#setRemoteObject(org.objectweb.proactive.core.remoteobject.RemoteObject)
     */
    public void setRemoteObject(RemoteObject<?> remoteObject) {
        this.remoteObject = remoteObject;
    }

    /* (non-Javadoc)
     * @see org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject#getObjectProxy()
     */
    public Object getObjectProxy() {
        try {
            return this.remoteObject.getObjectProxy();
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        return null;
    }

    public RemoteObjectSet getRemoteObjectSet() throws IOException {
        RemoteObjectSet ros = this.remoteObject.getRemoteObjectExposer().getRemoteObjectSet(
                this.remoteRemoteObject);
        return ros;
    }
}
