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
package org.objectweb.proactive.core.component.adl.implementations;

import java.util.List;
import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.factory.ProActiveGenericFactory;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;


/**
 * @author The ProActive Team
 */
public class ProActiveNFImplementationBuilderImpl extends ProActiveImplementationBuilderImpl {
    @Override
    public Object createComponent(Object type, String name, String definition,
            ControllerDescription controllerDesc, ContentDescription contentDesc, VirtualNode adlVN,
            Map context) throws Exception {
        ObjectsContainer obj = commonCreation(type, name, definition, contentDesc, adlVN, context);

        return createNFComponent(type, obj.getDvn(), controllerDesc, contentDesc, adlVN, obj
                .getBootstrapComponent());
    }

    private Component createNFComponent(Object type,
            org.objectweb.proactive.core.descriptor.data.VirtualNode deploymentVN,
            ControllerDescription controllerDesc, ContentDescription contentDesc, VirtualNode adlVN,
            Component bootstrap) throws Exception {
        Component result;

        // FIXME : exhaustively specify the behavior
        if ((deploymentVN != null) && VirtualNode.MULTIPLE.equals(adlVN.getCardinality()) &&
            controllerDesc.getHierarchicalType().equals(Constants.PRIMITIVE) && !contentDesc.uniqueInstance()) {

            Object instanceList = newNFcInstanceAsList(bootstrap, (ComponentType) type, controllerDesc,
                    contentDesc, deploymentVN);
            result = (Component) ((Group<?>) instanceList).getGroupByType();
        } else {
            result = newNFcInstance(bootstrap, (ComponentType) type, controllerDesc, contentDesc,
                    deploymentVN);
        }

        //        registry.addComponent(result); // the registry can handle groups
        return result;
    }

    private List<Component> newNFcInstanceAsList(Component bootstrap, Type type,
            ControllerDescription controllerDesc, ContentDescription contentDesc,
            org.objectweb.proactive.core.descriptor.data.VirtualNode virtualNode) throws Exception {

        ProActiveGenericFactory genericFactory = (ProActiveGenericFactory) Fractal
                .getGenericFactory(bootstrap);

        if (virtualNode == null) {
            return genericFactory.newNFcInstanceAsList(type, controllerDesc, contentDesc, (Node[]) null);
        }
        try {
            virtualNode.activate();
            return genericFactory.newNFcInstanceAsList(type, controllerDesc, contentDesc, virtualNode
                    .getNodes());
        } catch (NodeException e) {
            InstantiationException ie = new InstantiationException(
                "could not instantiate components due to a deployment problem : " + e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    private Component newNFcInstance(Component bootstrap, Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc,
            org.objectweb.proactive.core.descriptor.data.VirtualNode virtualNode) throws Exception {

        ProActiveGenericFactory genericFactory = (ProActiveGenericFactory) Fractal
                .getGenericFactory(bootstrap);

        if (virtualNode == null) {
            return genericFactory.newNFcInstance(type, controllerDesc, contentDesc, (Node) null);
        }
        try {
            virtualNode.activate();
            if (virtualNode.getNodes().length == 0) {
                throw new InstantiationException(
                    "Cannot create component on virtual node as no node is associated with this virtual node");
            }
            return genericFactory.newNFcInstance(type, controllerDesc, contentDesc, virtualNode.getNode());
        } catch (NodeException e) {
            InstantiationException ie = new InstantiationException(
                "could not instantiate components due to a deployment problem : " + e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

}
