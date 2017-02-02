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
package org.objectweb.proactive.extensions.pnp;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;


/**
 * Skeleton of the PNP remote object factory who delegates to {@link PNPRemoteObjectFactoryBackend}.
 *
 * PNP is designed to be extensible. It is possible to inject low level channel handlers
 * to add tunnel or encapsulate PNP into another network protocol. For exemple it is possible
 * to add an SSL layer.
 *
 * Concrete PNP remote object factories must extends this class. They must perform their
 * initialization inside their no args constructor, then create a {@link PNPRemoteObjectFactoryBackend}
 * and call {@link PNPRemoteObjectFactoryAbstract#setBackendRemoteObjectFactory(PNPRemoteObjectFactoryBackend)}.
 * The goal of this class is to automatically delegate all method calls to the backend remote
 * object factory. This design is imposed by how remote object factory works and are initialized.
 *
 *
 * @since ProActive 5.0.0
 */
public abstract class PNPRemoteObjectFactoryAbstract implements RemoteObjectFactory {

    /** The backend rof on which method calls are delegated */
    private PNPRemoteObjectFactoryBackend backendRof;

    public PNPRemoteObjectFactoryAbstract() {
        // Do nothing
    }

    /**
     * Set the backend remote object factory (all method calls are delegated to this rof)
     *
     * @param rof The PNP backend remote object factory
     */
    public void setBackendRemoteObjectFactory(final PNPRemoteObjectFactoryBackend rof) {
        if (this.backendRof != null) {
            throw new IllegalStateException("Real PNP Remote object factory already set");
        }

        this.backendRof = rof;
    }

    public PNPAgent getAgent() {
        return this.backendRof.getAgent();
    }

    // Following methods only delegate method calls to the standard PNP rof

    final public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException {
        return this.backendRof.newRemoteObject(target);
    }

    final public RemoteRemoteObject register(InternalRemoteRemoteObject target, URI url, boolean replacePreviousBinding)
            throws ProActiveException {
        return this.backendRof.register(target, url, replacePreviousBinding);
    }

    final public void unregister(URI url) throws ProActiveException {
        this.backendRof.unregister(url);
    }

    final public URI[] list(URI url) throws ProActiveException {
        return this.backendRof.list(url);
    }

    @SuppressWarnings("unchecked")
    final public <T> RemoteObject<T> lookup(URI url) throws ProActiveException {
        return this.backendRof.lookup(url);
    }

    final public int getPort() {
        return this.backendRof.getPort();
    }

    final public String getProtocolId() {
        return this.backendRof.getProtocolId();
    }

    final public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        this.backendRof.unexport(rro);
    }

    final public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name,
            boolean rebind) throws ProActiveException {
        return this.backendRof.createRemoteObject(remoteObject, name, rebind);
    }

    final public URI getBaseURI() {
        return this.backendRof.getBaseURI();
    }

    final public ObjectInputStream getProtocolObjectInputStream(InputStream in) throws IOException {
        return this.backendRof.getProtocolObjectInputStream(in);
    }

    final public ObjectOutputStream getProtocolObjectOutputStream(OutputStream out) throws IOException {
        return this.backendRof.getProtocolObjectOutputStream(out);
    }
}
