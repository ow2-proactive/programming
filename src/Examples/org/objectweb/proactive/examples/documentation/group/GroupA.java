/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.documentation.group;

import java.io.File;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.mop.ClassNotReifiableException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.examples.documentation.classes.A;
import org.objectweb.proactive.examples.documentation.classes.B;


/**
 * @author ffonteno
 *
 */
public class GroupA {

    /**
     * @param args
     */
    public static void main(String[] args) {

        boolean GCMDeployment = false;
        boolean sameParams = true;
        if (args.length >= 1) {
            GCMDeployment = true;
            if (args.length == 2) {
                sameParams = false;
            }
        }

        //@snippet-start group_A_0
        //@snippet-start group_A_1
        try {

            //@snippet-break group_A_0
            //@snippet-break group_A_1
            if (GCMDeployment) {
                //@snippet-resume group_A_1
                /***** GCM Deployment *****/
                File applicationDescriptor = new File(args[0]);
                GCMApplication gcmad = PAGCMDeployment.loadApplicationDescriptor(applicationDescriptor);
                gcmad.startDeployment();

                GCMVirtualNode vn = gcmad.getVirtualNode("VN");
                Node node1 = vn.getANode();
                Node node2 = vn.getANode();
                Node[] nodes = new Node[] { node1, node2 };
                /**************************/

                A groupA;
                if (sameParams) {
                    /**
                     * Same parameters will be used for object creations
                     */
                    Object[] params = new Object[] { "Node" };
                    groupA = (A) PAGroup.newGroup(A.class.getName(), params, nodes);
                } else {
                    /**
                     * Different parameters will be used for object creations
                     */
                    Object[][] params = new Object[][] { new Object[] { "Node 1" }, new Object[] { "Node 2" } };
                    groupA = (A) PAGroup.newGroup(A.class.getName(), params, nodes);
                }
                //@snippet-break group_A_1
                //@snippet-start group_A_2
                /**
                 * Retrieve the two first members of groupA
                 */
                A firstMember = (A) PAGroup.get(groupA, 0);
                A secondMember = (A) PAGroup.get(groupA, 1);
                //@snippet-end group_A_2
                groupA.display();

                //@snippet-start group_turnActive
                /**
                 * Turn groupA into an active object
                 */
                PAGroup.turnActiveGroup(groupA, node1);
                //@snippet-end group_turnActive
            } else {
                //@snippet-resume group_A_0
                A groupA = (A) PAGroup.newGroup(A.class.getName());
                //@snippet-break group_A_0
                //@snippet-start group_A_3

                /**
                 * Definition of one standard Java object and two active objects
                 */
                A a1 = new A();
                A a2 = PAActiveObject.newActive(A.class, new Object[] { "A Object" });
                B b = PAActiveObject.newActive(B.class, new Object[] { "B Object" });

                /**
                 * For management purposes, get the management representation
                 */
                Group<A> grp = PAGroup.getGroup(groupA);

                /**
                 * Add nodes into the group
                 * Note: B extends A
                 * Note: Objects and active objects are mixed into the group
                 */
                grp.add(a1);
                grp.add(a2);
                grp.add(b);

                /**
                 * Call the display method on every group member
                 * Note: only methods of A can be called but we can override
                 * a method of A in B as it is done for the display method.
                 */
                groupA.display();

                /**
                 * A new reference to the typed group can also be built as follows
                 */
                A groupAnew = (A) grp.getGroupByType();
                //@snippet-end  group_A_3

                //@snippet-start group_A_4
                /**
                 * Call the getB method on groupA which returns a group of B
                 */
                B groupB = groupA.getB();

                /**
                 * Call the display method on groupB.
                 * A wait-by-necessity process is therefore launch in order to
                 * wait the end of every getB method previously called on each
                 * A member of groupA.
                 */
                groupB.display();
                //@snippet-end group_A_4

                //@snippet-start group_A_5
                /**
                 * Call the getB method on groupA which returns a group of B
                 */
                B groupB2 = groupA.getB();

                /**
                 * Wait and capture the first returned member of groupB2
                 */
                B firstB = (B) PAGroup.waitAndGetOne(groupB2);

                /**
                 * To wait all the members of vg are arrived
                 */
                PAGroup.waitAll(groupB2);
                //@snippet-end group_A_5

                //@snippet-start group_A_6
                /**
                 * Create a group of parameters
                 */
                Object[][] params = new Object[][] { new Object[] { "Param 1" }, new Object[] { "Param 2" } };
                StringWrapper strWrapper = (StringWrapper) PAGroup.newGroup(StringWrapper.class.getName(),
                        params);

                /**
                 * Call the setScatterGroup method on this group
                 */
                PAGroup.setScatterGroup(strWrapper);

                /**
                 * Dispatch parameters
                 */
                A resultGroup = groupA.setStrWrapper(strWrapper);

                /**
                 * Call the unsetScatterGroup method on the parameter group
                 */
                PAGroup.unsetScatterGroup(strWrapper);

                /**
                 * Call the display method on the result group which displays:
                 *
                 * A display =====> Param 1
                 * A display =====> Param 2
                 */
                resultGroup.display();

                /**
                 * Call the display method on the whole group which displays:
                 *
                 * A display =====> Param 1
                 * A display =====> Param 2
                 * B display =====> B Object
                 */
                groupA.display();
                //@snippet-end group_A_6

                //@snippet-start group_A_7
                A a3 = PAActiveObject.newActive(A.class, new Object[] { "Another A object" });

                /**
                 * Using the previous management representation, we insert
                 * a new A element with the name "nameA"
                 */
                grp.addNamedElement("namedA", a3);

                /**
                 * Retrieve this element using its name
                 */
                A named_a = (A) grp.getNamedElement("namedA");

                /**
                 * Display it to be sure it's the good one. Returns:
                 *
                 * A display =====> Another A object
                 */
                named_a.display();
                //@snippet-end group_A_7

                //@snippet-start group_A_8
                /**
                 * Set unique serialization
                 */
                PAGroup.setUniqueSerialization(groupA);

                /**
                 * Call the display method
                 */
                groupA.display();

                /**
                 * Unset unique serialization
                 */
                PAGroup.unsetUniqueSerialization(groupA);
                //@snippet-end group_A_8
            }
            //@snippet-resume group_A_1
            //@snippet-resume group_A_0

        } catch (ClassNotReifiableException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            //@snippet-break group_A_0
            //@snippet-break group_A_1
        } catch (ProActiveException e) {
            e.printStackTrace();
            //@snippet-resume group_A_0
            //@snippet-resume group_A_1
        }
        //@snippet-end group_A_0
        //@snippet-end group_A_1
    }
}
