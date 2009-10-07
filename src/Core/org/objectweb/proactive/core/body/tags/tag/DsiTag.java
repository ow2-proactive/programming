package org.objectweb.proactive.core.body.tags.tag;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.tags.Tag;


/**
 * DsiTag provide a tag to follow the Distributed Service flow
 * by propagating the same UniqueID during a flow execution. 
 */
public class DsiTag extends Tag {

    public static final String IDENTIFIER = "PA_TAG_DSI";

    /**
     * Constructor setting the Tag name "PA_TAG_DSI"
     * and an UniqueID concatened with the sequence number of the request
     * as the tag DATA.
     */
    public DsiTag(UniqueID id, long seq) {
        super(IDENTIFIER, "" + id.getCanonString() + "::" + seq + "::" + null + "::" + 0);
    }

    /**
     * This tag return itself at each propagation.
     */
    public Tag apply() {
        // Set the current body and seq number as the parent for the future request
        String[] dataInfos = data.toString().split("::");
        String currentId = PAActiveObject.getBodyOnThis().getID().getCanonString();
        long currentSeq = PAActiveObject.getContext().getCurrentRequest().getSequenceNumber();
        setData(dataInfos[0] + "::" + dataInfos[1] + "::" + currentId + "::" + currentSeq, true);
        return this;
    }

}
