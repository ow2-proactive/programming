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
