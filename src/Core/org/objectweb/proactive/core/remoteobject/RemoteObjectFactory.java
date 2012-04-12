/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;


public interface RemoteObjectFactory {

    /**
     * Return a remote part of a remote object according to the factory i.e the
     * protocol
     *
     * @param target
     *            the RemoteObject to expose
     * @return the remote part of the remote object
     * @throws ProActiveException
     */
    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException;

    /**
     * Bind a remote object to the registry used by the factory and return the
     * remote remote object corresponding to this bind
     *
     * @param target
     *            the remote object to register
     * @param url
     *            the url associated to the remote object
     * @param replacePreviousBinding
     *            if true replace an existing remote object by the new one
     * @return a reference to the remote remote object
     * @throws ProActiveException
     *             throws a ProActiveException if something went wrong during
     *             the registration
     */
    public RemoteRemoteObject register(InternalRemoteRemoteObject target, URI url,
            boolean replacePreviousBinding) throws ProActiveException;

    /**
     * unregister the remote remote object located at a given
     *
     * @param url
     *            the url
     * @throws ProActiveException
     *             throws a ProActiveException if something went wrong during
     *             the unregistration
     */
    public void unregister(URI url) throws ProActiveException;

    /**
     * list all the remote objects register into a registry located at the url
     *
     * @param url
     *            the location of the registry
     * @throws ProActiveException
     */
    public URI[] list(URI url) throws ProActiveException;

    /**
     * Returns a reference, a stub, for the remote object associated with the
     * specified url.
     *
     * @param url
     * @return
     * @throws ProActiveException
     */
    public <T> RemoteObject<T> lookup(URI url) throws ProActiveException;

    /**
     * @return return the port number
     */
    public int getPort();

    /**
     *  the id string of the protocol
     * @return returns the id string of the protocol (i.e. rmi, ibis, ...)
     */
    public String getProtocolId();

    /**
     * Unexports the remote object if the protocol supports it.  
     * @param rro the Remote Remote Object
     * @throws ProActiveException if the unexport fails
     */
    public void unexport(RemoteRemoteObject rro) throws ProActiveException;

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name,
            boolean rebind) throws ProActiveException;

    /**
     * To lookup an object on this host, caller can use this base URI and just set the name part.
     * @return the base URI for all object in this {@link RemoteObjectFactory}
     * @throws ProActiveException  
     */
    public URI getBaseURI() throws ProActiveException;

    /**
     * Return an input Stream which will handle the deserialization of the RemoteRemoteObject created
     * by this factory. By default PAObjectInputStream class could be use.
     */
    public ObjectInputStream getProtocolObjectInputStream(InputStream in) throws IOException;

    /**
     * Return an output Stream which will handle the serialization of the RemoteRemoteObject created
     * by this factory. By default PAObjectOutputStream class could be use.
     */
    public ObjectOutputStream getProtocolObjectOutputStream(OutputStream out) throws IOException;
}
