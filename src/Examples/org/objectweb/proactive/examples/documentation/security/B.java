package org.objectweb.proactive.examples.documentation.security;

import java.io.Serializable;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.annotation.ActiveObject;


@ActiveObject
public class B implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 430L;
    String text;

    public B() {
    }

    public B(String text) {
        this.text = text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        try {
            return "Node url = " + PAActiveObject.getNode().getNodeInformation().getURL() +
                System.getProperty("line.separator") + "Text = " + text;
        } catch (NodeException e) {
            // TODO Auto-generated catch block
            return e.getMessage();
        }

    }
}
