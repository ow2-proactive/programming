/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
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

    public ProActiveLogCollectorDeployer(String name) {
        this.collector = new ProActiveLogCollector();
        this.roe = PARemoteObject.newRemoteObject(ProActiveLogCollector.class.getName(), this.collector);
        this.roe.createRemoteObject(name);
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
