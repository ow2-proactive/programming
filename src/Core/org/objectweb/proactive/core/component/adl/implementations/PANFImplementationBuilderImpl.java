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
package org.objectweb.proactive.core.component.adl.implementations;

import java.util.Map;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;


/**
 * TO CHECK: Is this class being used ??????????
 * 
 * @author The ProActive Team
 */
public class PANFImplementationBuilderImpl extends PAImplementationBuilderImpl {
    //@Override
    public Object createComponent(Object type, String name, String definition,
            ControllerDescription controllerDesc, ContentDescription contentDesc, VirtualNode adlVN,
            Map<Object, Object> context) throws Exception {
        ObjectsContainer obj = commonCreation(type, name, definition, contentDesc, adlVN, context);
        return createNFComponent(type, obj.getNodesContainer(), controllerDesc, contentDesc, adlVN, obj
                .getBootstrapComponent());
    }

    private Component createNFComponent(Object type, Object nodesContainer,
            ControllerDescription controllerDesc, ContentDescription contentDesc, VirtualNode adlVN,
            Component bootstrap) throws Exception {
        Component result = newNfFcInstance(bootstrap, (ComponentType) type, controllerDesc, contentDesc,
                nodesContainer);
        //        registry.addComponent(result); // the registry can handle groups
        return result;
    }

    private Component newNfFcInstance(Component bootstrap, Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Object nodesContainer) throws Exception {
        PAGenericFactory genericFactory = Utils.getPAGenericFactory(bootstrap);
        return genericFactory.newNfFcInstance(type, controllerDesc, contentDesc, ADLNodeProvider
                .getNode(nodesContainer));
    }

}
