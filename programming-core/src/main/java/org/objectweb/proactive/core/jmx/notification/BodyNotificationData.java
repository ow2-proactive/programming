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
package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


/**
 * Used in the JMX notifications
 * @author The ProActive Team
 */
public class BodyNotificationData implements Serializable {

    /** The unique id of the body */
    private UniqueID id;

    /** The jobID */
    private String jobID;

    /** The nodeUrl */
    private String nodeUrl;

    /** The className */
    private String className;

    public BodyNotificationData() {
        // No args constructor
    }

    /**
     * Creates a new BodyNotificationData.
     * @param bodyID Id of the new active object.
     * @param nodeURL Url of the node containing this active object.
     * @param className Name of the classe used to create the active object.
     */
    public BodyNotificationData(UniqueID bodyID, String nodeURL, String className) {
        this.id = bodyID;
        this.nodeUrl = nodeURL;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public UniqueID getId() {
        return id;
    }

    public String getJobID() {
        return jobID;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }
}
