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
