/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
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
package org.objectweb.proactive.core.component.factory;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.node.Node;


/**
 * A factory for instantiating components on remote nodes.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface PAGenericFactory extends GenericFactory {
    /**
     * Creates a non-functional component.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the component to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the component to be created. This description is
     * implementation specific.
     * @return The {@link Component} interface of the created component.
     * @throws InstantiationException If the component cannot be created.
     */
    Component newNfFcInstance(Type type, Object controllerDesc, Object contentDesc)
            throws InstantiationException;

    /**
     * Creates components in parallel.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the components to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the components to be created. This description is
     * implementation specific.
     * @param nbComponents The number of components to create in parallel.
     * @return The {@link Component} interfaces of the created components.
     * @throws InstantiationException If the components cannot be created.
     */
    Component[] newFcInstanceInParallel(Type type, Object controllerDesc, Object contentDesc, int nbComponents)
            throws InstantiationException;

    /**
     * Creates non-functional components in parallel.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the components to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the components to be created. This description is
     * implementation specific.
     * @param nbComponents The number of components to create in parallel.
     * @return The {@link Component} interfaces of the created components.
     * @throws InstantiationException If the components cannot be created.
     */
    Component[] newNfFcInstanceInParallel(Type type, Object controllerDesc, Object contentDesc,
            int nbComponents) throws InstantiationException;

    /**
     * Creates a component.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the component to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the component to be created. This description is
     * implementation specific.
     * @return The {@link Component} interface of the created component.
     * @throws InstantiationException If the component cannot be created.
     */
    Component newFcInstance(Type type, ControllerDescription controllerDesc, ContentDescription contentDesc)
            throws InstantiationException;

    /**
     * Creates a non-functional component.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the component to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the component to be created. This description is
     * implementation specific.
     * @return The {@link Component} interface of the created component.
     * @throws InstantiationException If the component cannot be created.
     */
    Component newNfFcInstance(Type type, ControllerDescription controllerDesc, ContentDescription contentDesc)
            throws InstantiationException;

    /**
     * Creates components in parallel.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the components to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the components to be created. This description is
     * implementation specific.
     * @param nbComponents The number of components to create in parallel.
     * @return The {@link Component} interfaces of the created components.
     * @throws InstantiationException If the components cannot be created.
     */
    Component[] newFcInstanceInParallel(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, int nbComponents) throws InstantiationException;

    /**
     * Creates non-functional components in parallel.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the components to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the components to be created. This description is
     * implementation specific.
     * @param nbComponents The number of components to create in parallel.
     * @return The {@link Component} interfaces of the created components.
     * @throws InstantiationException If the components cannot be created.
     */
    Component[] newNfFcInstanceInParallel(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, int nbComponents) throws InstantiationException;

    /**
     * Creates a component on a given node.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the component to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the component to be created. This description is
     * implementation specific.
     * @param node The node where to create the component.
     * @return The {@link Component} interface of the created component.
     * @throws InstantiationException If the component cannot be created.
     */
    Component newFcInstance(Type type, ControllerDescription controllerDesc, ContentDescription contentDesc,
            Node node) throws InstantiationException;

    /**
     * Creates a non-functional component on a given node.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the component to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the component to be created. This description is
     * implementation specific.
     * @param node The node where to create the component.
     * @return The {@link Component} interface of the created component.
     * @throws InstantiationException If the component cannot be created.
     */
    Component newNfFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node node) throws InstantiationException;

    /**
     * Creates components in parallel on the given nodes.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the components to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the components to be created. This description is
     * implementation specific.
     * @param nbComponents The number of components to create in parallel.
     * @param nodes The nodes where to create the components.
     * @return The {@link Component} interfaces of the created components.
     * @throws InstantiationException If the components cannot be created.
     */
    Component[] newFcInstanceInParallel(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, int nbComponents, Node[] nodes) throws InstantiationException;

    /**
     * Creates non-functional components in parallel on the given nodes.
     *
     * @param type An arbitrary component type.
     * @param controllerDesc A description of the controller part of the components to be created. This description
     * is implementation specific. If it is <tt>null</tt> then a "default" controller part will be used.
     * @param contentDesc A description of the content part of the components to be created. This description is
     * implementation specific.
     * @param nbComponents The number of components to create in parallel.
     * @param nodes The nodes where to create the components.
     * @return The {@link Component} interfaces of the created components.
     * @throws InstantiationException If the components cannot be created.
     */
    Component[] newNfFcInstanceInParallel(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, int nbComponents, Node[] nodes) throws InstantiationException;
}
