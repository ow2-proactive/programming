package org.objectweb.proactive.api;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.body.tags.MessageTags;


/**
 * This class provide acces to the messages tags.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class PAMessageTagging {

    /**
     * Return the tags attached to the current Message(Request/Reply)
     * served.
     * 
     * This method must be called from an ActiveObject.
     * 
     * @return the tags of the current message served.
     */
    public static MessageTags getCurrentTags() {
        return PAActiveObject.getContext().getCurrentRequest().getTags();
    }

}
