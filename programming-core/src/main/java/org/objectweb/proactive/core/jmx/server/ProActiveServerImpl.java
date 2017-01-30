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
package org.objectweb.proactive.core.jmx.server;

import java.io.Serializable;

import javax.management.MBeanServer;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.ProActiveInternalObject;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAVersion;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.jmx.ProActiveConnection;
import org.objectweb.proactive.core.node.NodeException;


/**
 * The active object representing the connector. This object is responsible of creating ProActive JMX Connections
 * @author The ProActive Team
 */
public class ProActiveServerImpl implements Serializable, ProActiveInternalObject {
    private transient MBeanServer mbeanServer;

    private UniqueID id;

    /**
     *  The ProActive Connector version
     * @return
     */
    public String getVersion() {
        return PAVersion.getProActiveVersion();
    }

    /**
     * Returns a new ProActive Connection
     * @return a ProActive Connection that will enables remote calls onto the remote MBean Server
     */
    public ProActiveConnection newClient() {
        ProActiveConnection client = null;
        try {
            client = new ProActiveConnection(this.mbeanServer);
            client = (ProActiveConnection) PAActiveObject.turnActive(client);
            return client;
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        return client;
    }

    /**
     * Sets the MBean server attached to the connector
     * @param mbs The MBean Server bounfd with the connector
     */
    public synchronized void setMBeanServer(MBeanServer mbs) {
        this.mbeanServer = mbs;
    }

    /**
     * @return the Mbean Server bound with  the connector
     */
    public synchronized MBeanServer getMBeanServer() {
        return mbeanServer;
    }

    public UniqueID getUniqueID() {
        if (id == null) {
            id = PAActiveObject.getBodyOnThis().getID();
        }
        return id;
    }
}
