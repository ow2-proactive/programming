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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.remoteobject;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.core.remoteobject.adapter.Adapter;


public class RemoteObjectProperties implements Serializable {

    private static final long serialVersionUID = 60L;

    protected String className;
    protected Class<?> targetClass;
    protected String proxyName;
    protected Class<?> adapterClass;
    protected Adapter<?> adapter;
    protected URI uri;
    protected Object stub;

    public RemoteObjectProperties() {
    }

    public RemoteObjectProperties(Object stub, URI uri, String className, Class<?> targetClass,
            String proxyName, Class<?> adapterClass, Adapter<?> adapter) {
        this.className = className;
        this.targetClass = targetClass;
        this.proxyName = proxyName;
        this.adapterClass = adapterClass;
        this.adapter = adapter;
        this.uri = uri;
        this.stub = stub;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        this.targetClass = targetClass;
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public Class<?> getAdapterClass() {
        return adapterClass;
    }

    public void setAdapterClass(Class<?> adapterClass) {
        this.adapterClass = adapterClass;
    }

    public Adapter<?> getAdapter() {
        return adapter;
    }

    public void setAdapter(Adapter<?> adapter) {
        this.adapter = adapter;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public Object getStub() {
        return stub;
    }

    public void setStub(Object stub) {
        this.stub = stub;
    }

}
