package org.objectweb.proactive.examples.documentation.messagetagging;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMessageTagging;
import org.objectweb.proactive.core.body.tags.LocalMemoryTag;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.node.NodeException;


public class B {

    private C activeC;

    public B() {
    }

    public B(A a) throws ActiveObjectCreationException, NodeException {
        this.activeC = PAActiveObject.newActive(C.class, new Object[] { a });
    }

    public void propagate(Integer maxDepth) {
        MessageTags tags = PAMessageTagging.getCurrentTags();
        IncrementingTag t = (IncrementingTag) tags.getTag("TAG_00");

        System.out.println("\n-----------------------");
        System.out.println("B: Tag depth = " + t.getDepth());

        String str = (String) t.getData() + "-> B";
        t.setData(str);
        System.out.println("B: Path = " + t.getData());

        LocalMemoryTag mem = t.getLocalMemory();
        if (mem == null) {
            t.createLocalMemory(10).put("MEM_00", new Integer(0));
            mem = t.getLocalMemory();

        } else {
            Integer round = (Integer) mem.get("MEM_00");
            mem.put("MEM_00", ++round);
        }
        System.out.println("B: Round number = " + (Integer) mem.get("MEM_00"));
        System.out.println("-----------------------");

        if (t.getDepth() < maxDepth)
            activeC.propagate(maxDepth);
    }
}
