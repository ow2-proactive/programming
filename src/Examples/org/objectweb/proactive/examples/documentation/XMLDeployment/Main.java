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
package org.objectweb.proactive.examples.documentation.XMLDeployment;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PADeployment;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
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
