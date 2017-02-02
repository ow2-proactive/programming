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

import java.io.IOException;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;


/**
 * A RemoteObject allows to turn a java object into a remotely accessible object.
 * According to the protocol selected, the remote object is going to register itself
 * on a registry.
 */
public interface RemoteObject<T> {

    /**
     * Send a message containing a reified method call to a remote object
     * @param message the reified method call
     * @return a reply containing the result of the method call
     * @throws ProActiveException
     * @throws IOException if the message transfer has failed
     */
    public Reply receiveMessage(Request message) throws ProActiveException, IOException;

    /**
     * @return return a couple stub + proxy pointing on the current remote object
     * @throws ProActiveException
     */
    public T getObjectProxy() throws ProActiveException;

    /**
     *
     * @param rro
     * @return return a couple stub + proxy pointing on a reference on a remote object identified by rro
     * @throws ProActiveException
     */
    public T getObjectProxy(RemoteRemoteObject rro) throws ProActiveException;

    /**
     * @return return the classname of the reified object
     */
    public String getClassName();

    /**
     * @return return the universal name of this remote object
     */
    public String getName();

    /**
     * @return return the class of the reified object
     */
    public Class<? extends Object> getTargetClass();

    /**
     * @return return the proxy's classname of the reified object
     */
    public String getProxyName();

    /**
     * @see org.objectweb.proactive.core.remoteobject.adapter.Adapter
     * @return return the <code>class</code> of the adapter of this remote object
     */
    public Class<Adapter<T>> getAdapterClass();

    /**
     * @see org.objectweb.proactive.core.remoteobject.adapter.Adapter
     * @return return the <code>class</code> of the adapter of this remote object
     */
    public Adapter<T> getAdapter();

    /**
     * @return the related remoteObjectExposer for this RemoteObject
     */
    public RemoteObjectExposer<T> getRemoteObjectExposer();

    /**
     * Set the related remoteObjectExposer for this RemoteObject
     */
    public void setRemoteObjectExposer(RemoteObjectExposer<T> roe);
}
