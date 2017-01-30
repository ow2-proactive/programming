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
package functionalTests.remoteobject.registry;

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


public class DummyProtocol implements RemoteObjectFactory {
    public int getPort() {
        return 452;
    }

    public String getProtocolId() {
        return "dummy";
    }

    public URI[] list(URI url) throws ProActiveException {
        return new URI[] {};
    }

    public <T> RemoteObject<T> lookup(URI url) throws ProActiveException {
        return null;
    }

    public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target) throws ProActiveException {
        return null;
    }

    public RemoteRemoteObject register(InternalRemoteRemoteObject target, URI url, boolean replacePreviousBinding)
            throws ProActiveException {
        return null;
    }

    public void unregister(URI url) throws ProActiveException {
    }

    public void unexport(RemoteRemoteObject rro) throws ProActiveException {
    }

    public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name, boolean rebind)
            throws ProActiveException {
        return null;
    }

    public URI getBaseURI() {
        return null;
    }

    public ObjectInputStream getProtocolObjectInputStream(InputStream in) throws IOException {
        return null;
    }

    public ObjectOutputStream getProtocolObjectOutputStream(OutputStream out) throws IOException {
        return null;
    }

}
