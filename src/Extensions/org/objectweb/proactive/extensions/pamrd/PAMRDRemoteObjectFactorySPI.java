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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.pamrd;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.AbstractRemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.InternalRemoteRemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObjectFactorySPI;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;


/** 
 *
 * @since ProActive 5.0.0
 */
public class PAMRDRemoteObjectFactorySPI implements RemoteObjectFactorySPI {

    static final public String PROTO_ID = "pamrd";

    public Class<? extends RemoteObjectFactory> getFactoryClass() {
        return PAMRDRemoteObjectFactory.class;
    }

    public String getProtocolId() {
        return PROTO_ID;
    }

    public static class PAMRDRemoteObjectFactory extends AbstractRemoteObjectFactory {

        public PAMRDRemoteObjectFactory() throws ProActiveException {
            throw new ProActiveException(
                "PAMRD not yet available, will be released in the next maintenance version");
        }

        public RemoteRemoteObject newRemoteObject(InternalRemoteRemoteObject target)
                throws ProActiveException {
            return null;
        }

        public void unregister(URI url) throws ProActiveException {

        }

        public URI[] list(URI url) throws ProActiveException {
            return null;
        }

        public <T> RemoteObject<T> lookup(URI url) throws ProActiveException {
            return null;
        }

        public int getPort() {
            return 0;
        }

        public String getProtocolId() {
            return null;
        }

        public void unexport(RemoteRemoteObject rro) throws ProActiveException {
        }

        public InternalRemoteRemoteObject createRemoteObject(RemoteObject<?> remoteObject, String name,
                boolean rebind) throws ProActiveException {
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

        public RemoteRemoteObject register(InternalRemoteRemoteObject target, URI url,
                boolean replacePreviousBinding) throws ProActiveException {
            return null;
        }

    }
}
