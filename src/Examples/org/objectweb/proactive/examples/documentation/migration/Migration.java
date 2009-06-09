package org.objectweb.proactive.examples.documentation.migration;

import java.io.File;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.examples.documentation.classes.SimpleAgent;
import org.objectweb.proactive.examples.documentation.classes.UnSerializableAgent;
import org.objectweb.proactive.examples.documentation.classes.UnSerializableAgent2;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


public class Migration {

    //@snippet-start migration_simple
    public static void simpleMigration(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Usage: java org.objectweb.proactive.examples.documentation.migration.Migration GCMA.xml");
            System.exit(-1);
        }

        // Gets a node from a deployment and
        // creates the SimpleAgent on this node

        /***** GCM Deployment *****/
        File applicationDescriptor = new File(args[0]);

        GCMApplication gcmad;
        try {
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);
        } catch (ProActiveException e) {
            e.printStackTrace();
            return;
        }
        gcmad.startDeployment();

        // Take a node from the available nodes of VN
        GCMVirtualNode vn = gcmad.getVirtualNode("VN");
        vn.waitReady();
        Node node = vn.getANode();
        /**************************/

        SimpleAgent simpleAgent;
        try {
            // Creates the SimpleAgent in this JVM
            simpleAgent = (SimpleAgent) PAActiveObject.newActive(SimpleAgent.class.getName(), null, node);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("The Active Object has been created on " + simpleAgent.whereAreYou());

        // Migrates the SimpleAgent to another node
        node = vn.getANode();
        simpleAgent.moveTo(node.getNodeInformation().getURL());
        System.out.println("The Active Object is now on " + simpleAgent.whereAreYou());
    }

    //@snippet-end migration_simple

    //@snippet-start migration_unserializable
    public static void unserializableMigration(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Usage: java org.objectweb.proactive.examples.documentation.migration.Migration GCMA.xml");
            System.exit(-1);
        }

        // Gets a node from a deployment and
        // creates the SimpleAgent on this node

        /***** GCM Deployment *****/
        File applicationDescriptor = new File(args[0]);

        GCMApplication gcmad;
        try {
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);
        } catch (ProActiveException e) {
            e.printStackTrace();
            return;
        }
        gcmad.startDeployment();

        // Take a node from the available nodes of VN
        GCMVirtualNode vn = gcmad.getVirtualNode("VN");
        vn.waitReady();
        Node node = vn.getANode();
        /**************************/

        UnSerializableAgent unserializableAgent;
        try {
            // Creates the SimpleAgent in this JVM
            Object[] params = new Object[] { 100 };
            unserializableAgent = (UnSerializableAgent) PAActiveObject.newActive(UnSerializableAgent.class
                    .getName(), params, node);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("The Active Object has been created on " + unserializableAgent.whereAreYou());
        unserializableAgent.displayArray();

        // Migrates the SimpleAgent to another node
        node = vn.getANode();
        unserializableAgent.moveTo(node.getNodeInformation().getURL());
        System.out.println("The Active Object is now on " + unserializableAgent.whereAreYou());
        unserializableAgent.displayArray();
    }

    //@snippet-end migration_unserializable

    public static void unserializableMigration2(String[] args) {
        if (args.length != 1) {
            System.out
                    .println("Usage: java org.objectweb.proactive.examples.documentation.migration.Migration GCMA.xml");
            System.exit(-1);
        }

        // Gets a node from a deployment and
        // creates the SimpleAgent on this node

        /***** GCM Deployment *****/
        File applicationDescriptor = new File(args[0]);

        GCMApplication gcmad;
        try {
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);
        } catch (ProActiveException e) {
            e.printStackTrace();
            return;
        }
        gcmad.startDeployment();

        // Take a node from the available nodes of VN
        GCMVirtualNode vn = gcmad.getVirtualNode("VN");
        vn.waitReady();
        Node node = vn.getANode();
        /**************************/

        UnSerializableAgent2 unserializableAgent;
        try {
            // Creates the SimpleAgent in this JVM
            Object[] params = new Object[] { 100 };
            unserializableAgent = (UnSerializableAgent2) PAActiveObject.newActive(UnSerializableAgent2.class
                    .getName(), params, node);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        System.out.println("The Active Object has been created on " + unserializableAgent.whereAreYou());
        unserializableAgent.displayArray();

        // Migrates the SimpleAgent to another node
        node = vn.getANode();
        unserializableAgent.moveTo(node.getNodeInformation().getURL());
        System.out.println("The Active Object is now on " + unserializableAgent.whereAreYou());
        unserializableAgent.displayArray();
    }

    public static void main(String[] args) {
        simpleMigration(args);
        unserializableMigration(args);
        unserializableMigration2(args);
    }
}
