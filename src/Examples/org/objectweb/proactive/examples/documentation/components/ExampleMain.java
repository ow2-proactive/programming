package org.objectweb.proactive.examples.documentation.components;

import java.io.File;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class ExampleMain {

    public void standardInstantiation() {
        //@snippet-start component_examples_3
        A object = new AImpl();
        //@snippet-end component_examples_3
    }

    public void activeObjectInstantiation(String descriptorPath) throws ProActiveException {
        File applicationDescriptor = new File(descriptorPath);

        GCMApplication gcmad;

        // Loads the application descriptor file
        gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);

        // Starts the deployment
        gcmad.startDeployment();

        GCMVirtualNode vn = gcmad.getVirtualNode("MyVirtualNode");
        Node aNode = vn.getANode();

        //@snippet-start component_examples_4
        A active_object = (A) PAActiveObject.newActive(AImpl.class.getName(), // signature of the base class
                new Object[] {}, // Object[]
                aNode // location, could also be a virtual node
                );
        //@snippet-end component_examples_4
    }

    public void componentInstantiation(String descriptorPath) throws ProActiveException,
            InstantiationException, NoSuchInterfaceException {
        File applicationDescriptor = new File(descriptorPath);

        GCMApplication gcmad;

        // Loads the application descriptor file
        gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);

        // Starts the deployment
        gcmad.startDeployment();

        GCMVirtualNode vn = gcmad.getVirtualNode("MyVirtualNode");
        Node aNode = vn.getANode();

        //@snippet-start component_examples_5
        Component boot = Fractal.getBootstrapComponent();
        //@snippet-end component_examples_5

        //@snippet-start component_examples_6
        TypeFactory tf = (TypeFactory) boot.getFcInterface("type-factory");
        //@snippet-end component_examples_6

        //@snippet-start component_examples_7
        // type of the "a" component
        ComponentType aType = tf.createFcType(new InterfaceType[] { tf.createFcItfType("a", "A", false,
                false, false) });
        //@snippet-end component_examples_7

        //@snippet-start component_examples_8
        ContentDescription contentDesc = new ContentDescription(AImpl.class.getName(), // signature of the base class
            new Object[] {} // Object[]
        );
        //@snippet-end component_examples_8

        //@snippet-start component_examples_9
        ControllerDescription controllerDesc = new ControllerDescription("myName", // name of the component
            Constants.PRIMITIVE // the hierarchical type of the component
        // it could be PRIMITIVE, COMPOSITE, or PARALLEL
        );
        //@snippet-end component_examples_9

        //@snippet-start component_examples_10
        ProActiveGenericFactory componentFactory = (ProActiveGenericFactory) Fractal.getGenericFactory(boot);
        Component component = componentFactory.newFcInstance(aType, // type of the component (defining the client and server interfaces)
                controllerDesc, // implementation-specific description for the controller
                contentDesc, // implementation-specific description for the content
                aNode // location, could also be a virtual node
                );
        //@snippet-end component_examples_10 

    }

    public void interfacesExamples() throws InstantiationException, NoSuchInterfaceException {
        Component boot = Fractal.getBootstrapComponent();

        TypeFactory tf = (TypeFactory) boot.getFcInterface("type-factory");

        //@snippet-start component_examples_11
        // type of the "a" component
        ComponentType aType = tf.createFcType(new InterfaceType[] {
                tf.createFcItfType("a", "A", false, false, false),
                tf.createFcItfType("requiredService", "A", true, false, false) });
        //@snippet-end component_examples_11
    }
}
