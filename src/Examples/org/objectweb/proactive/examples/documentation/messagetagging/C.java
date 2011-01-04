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

import org.objectweb.proactive.api.PAMessageTagging;
import org.objectweb.proactive.core.body.tags.LocalMemoryTag;
import org.objectweb.proactive.core.body.tags.MessageTags;


public class C {

    private A activeA;

    public C() {
    }

    public C(A a) {
        this.activeA = a;
    }

    public void propagate(Integer maxDepth) {
        MessageTags tags = PAMessageTagging.getCurrentTags();
        IncrementingTag t = (IncrementingTag) tags.getTag("TAG_00");

        System.out.println("\n-----------------------");
        System.out.println("C: Tag depth = " + t.getDepth());

        String str = (String) t.getData() + "-> C";
        t.setData(str);
        System.out.println("C: Path = " + t.getData());

        LocalMemoryTag mem = t.getLocalMemory();
        if (mem == null) {
            t.createLocalMemory(10).put("MEM_00", new Integer(0));
            mem = t.getLocalMemory();
        } else {
            Integer round = (Integer) mem.get("MEM_00");
            mem.put("MEM_00", ++round);
        }
        System.out.println("C: Round number = " + (Integer) mem.get("MEM_00"));
        System.out.println("-----------------------");

        if (t.getDepth() < maxDepth)
            activeA.propagate(maxDepth);
    }

}
