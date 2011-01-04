/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.examples.documentation.messagetagging;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMessageTagging;
import org.objectweb.proactive.core.body.tags.LocalMemoryTag;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.node.NodeException;


public class A implements InitActive {

    private B activeB;

    public A() {
    }

    public void initActivity(Body body) {
        try {
            this.activeB = PAActiveObject.newActive(B.class, new Object[] { (A) PAActiveObject
                    .getStubOnThis() });
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //@snippet-start Tag_propagate
    public void propagate(Integer maxDepth) {

        MessageTags tags = PAMessageTagging.getCurrentTags();

        // Gets or creates the tag 'Tag_00'
        IncrementingTag t = (IncrementingTag) tags.getTag("TAG_00");
        if (t == null)
            t = (IncrementingTag) tags.addTag(new IncrementingTag("TAG_00"));

        // Displays the tag depth
        System.out.println("\n-----------------------");
        System.out.println("A: Tag depth = " + t.getDepth());

        // Gets the tag data and adds the current class to the path
        String str = (String) t.getData();
        if (str == null)
            str = "A";
        else
            str += " -> A";
        t.setData(str);
        System.out.println("A: Path = " + t.getData());

        // Gets or creates the memory 'MEM_00'
        // and stores in it the round number.
        LocalMemoryTag mem = t.getLocalMemory();
        if (mem == null) {
            t.createLocalMemory(10).put("MEM_00", new Integer(0));
            mem = t.getLocalMemory();

        } else {
            Integer round = (Integer) mem.get("MEM_00");
            mem.put("MEM_00", ++round);
        }
        System.out.println("A: Round number = " + (Integer) mem.get("MEM_00"));
        System.out.println("-----------------------");

        // Propagates to the active object 'B'
        if (t.getDepth() < maxDepth)
            activeB.propagate(maxDepth);
    }
    //@snippet-end Tag_propagate
}
