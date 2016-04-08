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
package org.objectweb.proactive.core.body.tags;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.BodyImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Abstract Tag class for Request Tagging
 * 
 * You have to create a subclass implementing the abstract
 * method apply to create a tag doing what you want at each
 * propagation.
 */
public abstract class Tag implements Serializable {

    private static final long serialVersionUID = 60L;

    /** Message Tagging Logger */
    private static Logger logger = ProActiveLogger.getLogger(Loggers.MESSAGE_TAGGING);

    /** Identifier of the tag */
    protected String id;

    /** User Data attached to this tag */
    protected Object data;

    private String cachedToString;

    /**
     * Tag constructor
     * @param id     - Identifier of the tag
     * @param data   - User Data Content
     */
    public Tag(String id, Object data) {
        this.id = id;
        this.data = data;
        if (logger.isDebugEnabled()) {
            logger.debug("Creation of a new tag : " + id);
        }
    }

    /**
     * Tag constructor
     * @param id     - Identifier of the tag
     */
    public Tag(String id) {
        this(id, null);
    }

    /**
     * Do the current Tag jobs and then return the Tag for the next
     * propagation. It can be itself (this) or a new Tag or null to 
     * cancel the tag.
     * 
     * @return the next propagate TAG
     */
    abstract public Tag apply();

    /**
     * Return the local memory space of this Tag on the current Active Object
     * if the lease has not exceeded, null otherwise.
     * 
     * Each acces to the memory renew the lease.
     * 
     * @return the LocaLMemoryTag of this tag on the current ActiveObject if it exist
     */
    final public LocalMemoryTag getLocalMemory() {
        Body body = PAActiveObject.getBodyOnThis();
        if (body instanceof BodyImpl) {
            return ((BodyImpl) body).getLocalMemoryTag(this.id);
        } //else
        return null;
    }

    /**
     * Create a local memory space for this Tag on the current Active Object
     * with the specified lease period if inferior to the max lease period of
     * the PAProperties.
     * @param lease - Lease Period
     * @return the LocaLMemoryTag of this tag on the current ActiveObject
     */
    final public LocalMemoryTag createLocalMemory(int lease) {
        Body body = PAActiveObject.getBodyOnThis();
        if (body instanceof BodyImpl) {
            return ((BodyImpl) body).createLocalMemoryTag(this.id, lease);
        } //else
        return null;
    }

    /**
     * Clear the local memory attached to this tag
     */
    final public void clearLocalMemory() {
        Body body = PAActiveObject.getBodyOnThis();
        if (body instanceof BodyImpl) {
            ((AbstractBody) body).clearLocalMemoryTag(this.id);
        }
    }

    /**
     * To get the Id of this tag
     * @return Tag Id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Return the User Data attached to this tag
     * @return - User Data
     */
    public Object getData() {
        return this.data;
    }

    /**
     * Attach a user data on this tag
     * @param data - The User Data
     * @param toStringChanged - if the toString of the Data has been changed for
     * the notification
     */
    public void setData(Object data, boolean toStringChanged) {
        this.data = data;
        if (toStringChanged) {
            this.cachedToString = null;
        }
    }

    /**
     * Attach a user data on this tag
     * @param data - The User Data
     */
    public void setData(Object data) {
        setData(data, false);
    }

    /**
     * Display Tag Information
     */
    public String toString() {
        if (cachedToString == null) {
            synchronized (this) {
                if (cachedToString == null) {
                    this.cachedToString = "[TAG]" + id + "[DATA]" + data + "[END]";
                }
            }
        }
        return cachedToString;
    }

    /**
     * Return the JMX Notification String for this Tag composed of
     * the tag id and the toString of the Data
     * @return jmx notification String
     */
    public String getNotificationMessage() {
        return this.toString();
    }
}
