/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.plugtest;

import java.io.File;
import java.util.List;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;


/**
 * @author The ProActive Team
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MTest {
    public static void main(String[] args) {
        try {
            //lecture du descripteur
            GCMApplication pad = PAGCMDeployment.loadApplicationDescriptor(new File(args[0]));
            pad.startDeployment();
            GCMVirtualNode mTest = pad.getVirtualNode("plugtest");
            mTest.waitReady();

            List<Node> noeuds = mTest.getCurrentNodes();
            System.out.println("Il y a " + noeuds.size() + " noeuds.");
            ObjA[] arrayA = new ObjA[noeuds.size()];
            ObjB b = org.objectweb.proactive.api.PAActiveObject.newActive(ObjB.class, new Object[] { "B" });

            int i = 0;
            for (Node node : noeuds) {
                arrayA[i] = org.objectweb.proactive.api.PAActiveObject.newActive(ObjA.class, new Object[] {
                        "object" + i, b }, node);
                ++i;
            }

            ObjA a = arrayA[0];
            System.out.println("Getting information " + a.getInfo());
            System.out.println("Calling toString " + a.toString());
            System.out.println("Calling getNumber " + a.getNumber());
            System.out.println("Calling getB " + a.getB().sayHello());
            System.out.println("Calling sayHello " + a.sayHello());

            for (i = 0; i < arrayA.length; i++)
                System.out.println("\nI'm " + arrayA[i].getInfo() + " and I say " + arrayA[i].sayHello() +
                    " on " + arrayA[i].getNode());

            printMessageAndWait(pad);
            pad.kill();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printMessageAndWait(GCMApplication pad) {
        java.io.BufferedReader d = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        System.out.println("   --> Press <return> to continue");

        try {
            d.readLine();
            pad.kill();
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //     System.out.println("---- GO ----");
    }
}
