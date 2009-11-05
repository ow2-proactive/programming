/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.documentation.activeobjectconcepts;

import java.io.File;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;
import org.objectweb.proactive.examples.documentation.classes.CustomMetaObjectFactory;
import org.objectweb.proactive.examples.documentation.classes.LIFOActivity;
import org.objectweb.proactive.examples.documentation.classes.Pair;
import org.objectweb.proactive.examples.documentation.classes.Worker;
import org.objectweb.proactive.examples.documentation.classes.WorkerFactory;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * @author The ProActive Team
 *
 * Used to instantiate active object
 */
public class Creation {

    /**
     * Shows how to use PAActiveObject.newActive()
     */
    public static void useNewActive() {

        //@snippet-start AO_Creation_1
        // Set the constructor parameters
        Object[] params = new Object[] { new IntWrapper(26), "Charlie" };

        Worker charlie;
        try {
            charlie = PAActiveObject.newActive(Worker.class, params);
            //@snippet-break AO_Creation_1
            System.out.println(charlie.getName() + " is " + charlie.getAge());
            //@snippet-resume AO_Creation_1
        } catch (ActiveObjectCreationException aoExcep) {
            // the creation of ActiveObject failed
            System.err.println(aoExcep.getMessage());
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        }
        //@snippet-end AO_Creation_1
    }

    /**
     * Shows how to use PAActiveObject.newActive() with a node
     */
    public static void useNewActiveWithANode(String gcmaPath) {

        //@snippet-start AO_Creation_6
        /***** GCM Deployment *****/
        File applicationDescriptor = new File(gcmaPath);

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

        // Set the constructor parameters
        Object[] params = new Object[] { new IntWrapper(26), "Charlie" };

        Worker charlie;
        try {
            charlie = PAActiveObject.newActive(Worker.class, params, node);
            //@snippet-break AO_Creation_6
            System.out.println(charlie.getName() + " is " + charlie.getAge());
            //@snippet-resume AO_Creation_6
        } catch (ActiveObjectCreationException aoExcep) {
            // the creation of ActiveObject failed
            System.err.println(aoExcep.getMessage());
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        }
        //@snippet-end AO_Creation_6
    }

    /**
     * Shows how to use PAActiveObject.newActive() with a node URL
     */
    public static void useNewActiveWithANodeURL(String gcmaPath) {

        /***** GCM Deployment *****/
        File applicationDescriptor = new File(gcmaPath);

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

        // Set the constructor parameters
        Object[] params = new Object[] { new IntWrapper(26), "Charlie" };

        Worker charlie;
        try {
            //@snippet-start AO_Creation_7
            String nodeURL = node.getNodeInformation().getURL();
            charlie = PAActiveObject.newActive(Worker.class, params, nodeURL);
            //@snippet-end AO_Creation_7
            System.out.println(charlie.getName() + " is " + charlie.getAge());
        } catch (ActiveObjectCreationException aoExcep) {
            // the creation of ActiveObject failed
            System.err.println(aoExcep.getMessage());
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        }
    }

    /**
     * Shows how to use PAActiveObject.newActive() with an activity Object
     */
    public static void useNewActiveWithActivity() {

        //@snippet-start AO_Creation_8
        // Set the constructor parameters
        Object[] params = new Object[] { new IntWrapper(26), "Charlie" };

        // Instantiate a LIFOActivity object
        LIFOActivity activity = new LIFOActivity();

        Worker charlie;
        try {
            charlie = PAActiveObject.newActive(Worker.class, null, params, null, activity, null);

            System.out.println(charlie.getName() + " is " + charlie.getAge());
            charlie.setAge(new IntWrapper(25));
            charlie.setName("Anonymous");
            System.out.println(charlie.getName() + " is " + charlie.getAge());

            PAActiveObject.terminateActiveObject(charlie, true);
        } catch (ActiveObjectCreationException aoExcep) {
            // the creation of ActiveObject failed
            System.err.println(aoExcep.getMessage());
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        }
        //@snippet-end AO_Creation_8
    }

    /**
     * Shows how to use PAActiveObject.newActiveInParallel()
     *
     * @param gcmaPath Path to the GCMA descriptor
     */
    public static void useNewActiveInParallel(String gcmaPath) {

        //@snippet-start AO_Creation_2
        /***** GCM Deployment *****/
        File applicationDescriptor = new File(gcmaPath);

        GCMApplication gcmad;
        try {
            gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);
        } catch (ProActiveException e) {
            e.printStackTrace();
            return;
        }
        gcmad.startDeployment();

        // Take 2 nodes from the available nodes of VN
        GCMVirtualNode vn = gcmad.getVirtualNode("VN");
        vn.waitReady();
        Node[] nodes = vn.getCurrentNodes().toArray(new Node[(int) vn.getNbCurrentNodes()]);
        /**************************/

        try {
            Worker[] workers = (Worker[]) PAActiveObject.newActiveInParallel(Worker.class.getName(),
                    new Object[nodes.length][], nodes);
            //@snippet-break AO_Creation_2
            System.out.println(workers[0].getName() + " is " + workers[0].getAge());
            //@snippet-resume AO_Creation_2
        } catch (ClassNotFoundException classExcep) {
            System.err.println(classExcep.getMessage());
        }
        //@snippet-end AO_Creation_2
    }

    /**
     * Shows how to use PAActiveObject.turnActive()
     */
    public static void useTurnActive() {

        //@snippet-start AO_Creation_3
        Worker charlie = new Worker(new IntWrapper(19), "Charlie");
        try {
            charlie = (Worker) PAActiveObject.turnActive(charlie);
            //@snippet-break AO_Creation_3
            System.out.println(charlie.getName() + " is " + charlie.getAge());
            //@snippet-resume AO_Creation_3
        } catch (ActiveObjectCreationException aoExcep) {
            // the creation of ActiveObject failed
            System.err.println(aoExcep.getMessage());
        } catch (NodeException nodeExcep) {
            System.err.println(nodeExcep.getMessage());
        }
        //@snippet-end AO_Creation_3

    }

    /**
     * Shows how to manage generic types
     */
    @SuppressWarnings("unchecked")
    public static void useGenerics() {
        //@snippet-start AO_Creation_5
        Worker charlie = new Worker(new IntWrapper(30), "Charlie");
        try {
            // Declaration of the array of Class representing
            // type parameters
            Class<?>[] typeParameters = new Class<?>[] { Worker.class, String.class };

            // Declaration of the constructor parameters
            Object[] constructorParameters = new Object[] { charlie, "Researcher" };

            // Instantiation of the active object
            Pair<Worker, String> pair = (Pair<Worker, String>) PAActiveObject.newActive(Pair.class.getName(),
                    typeParameters, constructorParameters);

            //@snippet-break AO_Creation_5
            System.out.println(pair.getFirst().getName() + ", which is " + pair.getFirst().getAge() +
                ", is a " + pair.getSecond());
            //@snippet-resume AO_Creation_5
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        //@snippet-end AO_Creation_5
    }

    /**
     * Shows how to use a factory pattern
     */
    public static void useWorkerFactory() {
        //@snippet-start AO_Creation_9
        // Creates locally (Node=null) a Worker whose name is Charlie and who is 19
        Worker charlie = WorkerFactory.createActiveWorker(19, "Charlie", null);
        //@snippet-end AO_Creation_9
        System.out.println(charlie.getName() + " is " + charlie.getAge());
    }

    /**
     * Shows how to use a meta-object factory
     */
    public static void useMetaObjectFactory() {
        //@snippet-start AO_Creation_10
        Object[] params = new Object[] { new IntWrapper(26), "Charlie" };
        try {
            Worker charlie = (Worker) PAActiveObject.newActive(Worker.class.getName(), null, params, null,
                    null, CustomMetaObjectFactory.newInstance());
            //@snippet-break AO_Creation_10
            System.out.println(charlie.getName() + " is " + charlie.getAge());
            //@snippet-resume AO_Creation_10
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
        //@snippet-end AO_Creation_10

    }

    /**
     * Main method
     *
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            //            Creation.useNewActive();
            //            Creation.useGenerics();
            //            Creation.useNewActiveWithActivity();
            //            Creation.useWorkerFactory();
            Creation.useMetaObjectFactory();
        } else if (args.length > 1) {
            Creation.useTurnActive();
        } else {
            //            Creation.useNewActiveInParallel(args[0]);
            //            Creation.useNewActiveWithANode(args[0]);
            Creation.useNewActiveWithANodeURL(args[0]);
        }
    }
}
