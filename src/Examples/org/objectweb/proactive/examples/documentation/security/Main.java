package org.objectweb.proactive.examples.documentation.security;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;


public class Main {

    /**
     * @param args
     * @throws ProActiveException
     * @throws ClassNotFoundException
     * @throws ClassNotReifiableException
     */
    public static void main(String[] args) throws ProActiveException, ClassNotReifiableException,
            ClassNotFoundException {

        String descriptorFile = Main.class.getResource(
                "/org/objectweb/proactive/examples/documentation/security/SSHDescriptor.xml").getPath();

        // Creates the ProActiveDescriptor corresponding to the descriptor file
        ProActiveDescriptor proActiveDescriptor = PADeployment.getProactiveDescriptor(descriptorFile);

        // Gets the virtual nodes described in the descriptor file.
        VirtualNode virtualNode_A = proActiveDescriptor.getVirtualNode("VN_A");
        VirtualNode virtualNode_B = proActiveDescriptor.getVirtualNode("VN_B");

        // Activates the virtual node.
        // proActiveDescriptor.activateMappings();
        virtualNode_A.activate();
        virtualNode_B.activate();

        String classNameB = B.class.getName();
        Object[] constructorParametersB = new Object[] { "Hello ProActive Team !" };

        Node nodeB = virtualNode_B.getNode();

        // Creates the active object
        B b = (B) PAActiveObject.newActive(classNameB, constructorParametersB, nodeB);

        String classNameA = A.class.getName();
        Object[] constructorParametersA = new Object[] { b };

        Node nodeA = virtualNode_A.getNode();

        // Creates the active object
        A a = (A) PAActiveObject.newActive(classNameA, constructorParametersA, nodeA);

        for (int i = 0; i < 25; i++) {
            b.setText("Hello ProActive Team ! (n = " + i + ")");
            a.displayB();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }
}
