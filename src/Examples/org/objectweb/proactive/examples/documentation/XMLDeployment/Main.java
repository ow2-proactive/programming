package org.objectweb.proactive.examples.documentation.XMLDeployment;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorImpl;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.examples.documentation.classes.A;


public class Main {

    /**
     * @param args
     * @throws ProActiveException
     * @throws ClassNotFoundException
     * @throws ClassNotReifiableException
     */
    public static void main(String[] args) throws ProActiveException, ClassNotReifiableException,
            ClassNotFoundException {

        //@snippet-start XMLDescriptor_3
        String descriptorFile = Main.class.getResource(
                "/org/objectweb/proactive/examples/documentation/XMLDeployment/SSHDescriptor.xml").getPath();

        // Creates the ProActiveDescriptor corresponding to the descriptor file
        ProActiveDescriptor proActiveDescriptor = PADeployment.getProactiveDescriptor(descriptorFile);

        // Gets the virtual node named VN1 described in the descriptor file.
        VirtualNode virtualNode = proActiveDescriptor.getVirtualNode("VN1");

        // Activates the virtual node.
        // For activating several virtual node at once, you can use
        // proActiveDescriptorAgent.activateMappings()
        virtualNode.activate();

        String className = A.class.getName();
        Object[] constructorParameters = new Object[] {};

        // Gets a node on which the active object will be created
        Node node = virtualNode.getNode();

        // Creates the active object
        A a = (A) PAActiveObject.newActive(className, constructorParameters, node);
        //@snippet-end XMLDescriptor_3
        a.display();

        //@snippet-start XMLDescriptor_1
        PAActiveObject.newActive(className, constructorParameters, node);
        //@snippet-end XMLDescriptor_1
    }
}
