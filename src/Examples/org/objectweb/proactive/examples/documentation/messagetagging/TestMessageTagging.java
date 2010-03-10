package org.objectweb.proactive.examples.documentation.messagetagging;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;


public class TestMessageTagging {

    public static void main(String[] args) {
        try {
            A a = PAActiveObject.newActive(A.class, new Object[] {});
            a.propagate(20);
        } catch (ActiveObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
