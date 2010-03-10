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
