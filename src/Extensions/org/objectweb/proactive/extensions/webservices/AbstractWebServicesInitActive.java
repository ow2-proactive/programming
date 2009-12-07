package org.objectweb.proactive.extensions.webservices;

import java.io.Serializable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


public abstract class AbstractWebServicesInitActive implements InitActive, Serializable {

    public void initServlet(Node node) throws ActiveObjectCreationException, NodeException {
        PAActiveObject.newActive(this.getClass().getName(), null, node);
    }

}
