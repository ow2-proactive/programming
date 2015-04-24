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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Map of tag attached on a message. At each sendrequest, the "apply" method
 * of each tag is called, and the tags resulting are attached to the request which
 * will be sent.
 */
public class MessageTags implements Serializable {

    private static final long serialVersionUID = 62L;

    /** Message Tagging Logger */
    private static Logger logger = ProActiveLogger.getLogger(Loggers.MESSAGE_TAGGING);

    /**  Map of all tags/policy-data associated with a request */
    protected Map<String, Tag> messagestags;

    /**
     * Constructor
     */
    public MessageTags() {
        this.messagestags = new HashMap<String, Tag>();
    }

    /**
     * Add a tag on the request.
     * @param tag - the new tag.
     * @return the new tag
     */
    public Tag addTag(Tag tag) {
        String id = tag.getId();
        this.messagestags.put(id, tag);
        if (logger.isDebugEnabled()) {
            logger.debug("Adding the tag : " + tag);
        }
        return tag;
    }

    /**
     * Remove the tag with this identifier from this request.
     * @param id Tag identifier
     * @return the Tag removed
     */
    public Tag removeTag(String id) {
        if (logger.isDebugEnabled()) {
            logger.debug("Remove the tag : " + id);
        }
        return this.messagestags.remove(id);
    }

    /**
     * Return all the Tags 
     * @return Collection of Tags
     */
    public Collection<Tag> getTags() {
        return this.messagestags.values();
    }

    /**
     * Return the Tag with the specified Id
     * @param id - Tag identifier
     * @return The Tag
     */
    public Tag getTag(String id) {
        return messagestags.get(id);
    }

    /**
     * Return the user data content attached to this tag.
     * @param id - Identifier of the tag
     * @return User data content
     */
    public Object getData(String id) {
        return messagestags.get(id).getData();
    }

    /**
     * Return all Tags Name setted.
     * @return Set of tag name setted
     */
    public Set<String> getAllTagsID() {
        return messagestags.keySet();
    }

    /**
     * Return true if the tag exist, false otherwise.
     * @param id - Tag identifier
     * @return true if the tag exist, false otherwise
     */
    public boolean check(String id) {
        return messagestags.get(id) != null;
    }

    /**
     * Display informations of all tags
     */
    public String toString() {
        String res = "";
        for (Entry<String, Tag> e : messagestags.entrySet()) {
            res += e.getKey() + "" + e.getValue() + "\n";
        }
        return res;
    }

}
