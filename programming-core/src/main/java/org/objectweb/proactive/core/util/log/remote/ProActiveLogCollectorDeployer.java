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
package org.objectweb.proactive.core.util.log.remote;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;


/**
 * Deploys a {@link ProActiveLogCollector} on the local runtime
 * 
 * It is a simple wrapper to easily get the URL at which the collector is bound.
 */
final public class ProActiveLogCollectorDeployer {
    /** URL of the remote object */
    final private String url;

    /** The collector */
    final private ProActiveLogCollector collector;

    final RemoteObjectExposer<ProActiveLogCollector> roe;

    public ProActiveLogCollectorDeployer(String name) throws ProActiveException {
        this.collector = new ProActiveLogCollector();
        this.roe = PARemoteObject.newRemoteObject(ProActiveLogCollector.class.getName(), this.collector);
        this.roe.createRemoteObject(name, false);
        this.url = roe.getURL();
    }

    /** Get the local log collector*/
    public ProActiveLogCollector getCollector() {
        return this.collector;
    }

    /** Get the log collector as a remote object */
    public ProActiveLogCollector getRemoteObject() throws ProActiveException {
        return (ProActiveLogCollector) RemoteObjectHelper.generatedObjectStub(this.roe.getRemoteObject());
    }

    /** Get the URL of the local log collector */
    public String getCollectorURL() {
        return this.url;
    }

    /** Unexport the remote object
     * @throws ProActiveException  
     */
    public void terminate() throws ProActiveException {
        roe.unexportAll();
    }
}
