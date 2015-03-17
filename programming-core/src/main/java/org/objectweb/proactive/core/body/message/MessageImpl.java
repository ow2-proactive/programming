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
package org.objectweb.proactive.core.body.message;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.tags.MessageTags;


/**
 * <p>
 * Implements a simple message encapsulating a method call between two
 * active objects.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2001/10/23
 * @since   ProActive 0.9
 *
 */

public class MessageImpl implements Message, java.io.Serializable {

    private static final long serialVersionUID = 61L;

    /** The name of the method called */
    protected String methodName;

    /** The UniqueID of the body sending the message */
    protected UniqueID sourceID;

    /** The unique sequence number for the message */
    protected long sequenceNumber;

    /** the time the message has been issued or deserialized */
    protected transient long timeStamp;
    protected boolean isOneWay;

    // DSI
    protected MessageTags tags;

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    public MessageImpl() {
    }

    /**
     * Creates a new Message based on the given information.
     * @param sourceID the id of the sender of this message
     * @param sequenceNumber the unique sequence number of this message
     * @param isOneWay <code>true</code> if oneWay
     * @param methodName the method name of the method call
     * @param tags container of all tags for this message
     */
    public MessageImpl(UniqueID sourceID, long sequenceNumber, boolean isOneWay, String methodName,
            MessageTags tags) {
        this.sourceID = sourceID;
        this.sequenceNumber = sequenceNumber;
        this.timeStamp = System.currentTimeMillis();
        this.isOneWay = isOneWay;
        this.methodName = methodName;
        this.tags = tags;
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("method=").append(methodName);
        sb.append(", sender=").append(sourceID);
        sb.append(", sequenceNumber=").append(sequenceNumber);
        return sb.toString();
    }

    //
    // -- implements Message -----------------------------------------------
    //
    public UniqueID getSourceBodyID() {
        return this.sourceID;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public boolean isOneWay() {
        return isOneWay;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.timeStamp = System.currentTimeMillis();
    }

    public MessageTags getTags() {
        if (this.tags == null) {
            // Check if there is already a tag container attached on this message
            // otherwise, create it.
            // TODO : use the Metaobject Factory to create it instead of a direct creation
            this.tags = new MessageTags();
        }
        return tags;
    }
}
