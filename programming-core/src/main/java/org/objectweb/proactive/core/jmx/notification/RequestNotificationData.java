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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.jmx.notification;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;


public class RequestNotificationData implements Serializable {

    private static final long serialVersionUID = 60L;
    private UniqueID source;
    private UniqueID destination;
    private String methodName;
    private int requestQueueLength;
    private String sourceNode;
    private String destinationNode;
    private long sequenceNumber;
    private String tags;

    /**
     * Creates a new requestData used by the JMX user data.
     * @param source The source of the request
     * @param destination The destination of the request
     * @param methodName The name of the method called
     * @param requestQueueLength The request queue length of the destination active object
     * @param sequenceNumber The Sequence Number of the JMX Notification
     * @param tags The tags binded to the request
     */
    public RequestNotificationData(UniqueID source, String sourceNode, UniqueID destination,
            String destinationNode, String methodName, int requestQueueLength, long sequenceNumber,
            String tags) {
        this.source = source;
        this.sourceNode = sourceNode;
        this.destination = destination;
        this.destinationNode = destinationNode;
        this.methodName = methodName;
        this.requestQueueLength = requestQueueLength;
        this.sequenceNumber = sequenceNumber;
        this.tags = tags;
    }

    /**
     * Returns the id of the source object.
     * @return the id of the source object.
     */
    public UniqueID getSource() {
        return this.source;
    }

    /**
     * Returns the id of the destination object.
     * @return the if of the destination object.
     */
    public UniqueID getDestination() {
        return this.destination;
    }

    /**
     * Returns the URL of the source node object.
     * @return the URL of the source node object.
     */
    public String getSourceNode() {
        return this.sourceNode;
    }

    /**
     * Returns the URL of the destination node object.
     * @return the URL of the destination node object.
     */
    public String getDestinationNode() {
        return this.destinationNode;
    }

    /**
     * Returns the method name called on the destination object.
     * @return the method name called on the destination object.
     */
    public String getMethodName() {
        return this.methodName;
    }

    /**
     * Returns the request queue length of the destination object.
     * @return
     */
    public int getRequestQueueLength() {
        return this.requestQueueLength;
    }

    @Override
    public String toString() {
        return "Request source: " + source + ", destination: " + destination + ", methodName: " + methodName +
            ", destination request queue length: " + requestQueueLength;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public String getTags() {
        return tags;
    }
}
