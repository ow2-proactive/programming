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
package org.objectweb.proactive.core.component.control;

import java.util.List;

import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.exceptions.IllegalInterceptorException;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;
import org.objectweb.proactive.core.component.interception.Interceptor;


/**
 * Controller for the {@link Interceptor interceptors} of the component.
 * <br>
 * Each {@link Interceptor} is represented by an ID which can correspond to:
 * <ul>
 * <li>
 * The controller interface name of a controller object implementing the {@link Interceptor}
 * interface. For instance: {@code controllername-controller}.
 * </li>
 * <li>
 * Or the name of a Non Functional component of the membrane followed by the name of one of its
 * functional server interfaces implementing the {@link Interceptor} interface. For instance:
 * {@code nfComponentName.interfaceName}.
 * </li>
 * <br>
 * Each {@link Interceptor} can be attached to one or several client and/or server
 * functional interfaces.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface PAInterceptorController {
    //@snippet-start component_userguide_15
    /**
     * Returns the IDs of the {@link Interceptor interceptors} attached to the interface with the specified name.
     * 
     * @param interfaceName Name of the interface on which to get the IDs of its {@link Interceptor interceptors}.
     * @return The IDs of the {@link Interceptor interceptors} attached to the interface with the specified name.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public List<String> getInterceptorIDsFromInterface(String interfaceName) throws NoSuchInterfaceException;

    /**
     * Adds at the specified position the {@link Interceptor interceptor} with the specified ID to the interface
     * with the specified name.
     * 
     * @param interfaceName Name of the interface on which to add the {@link Interceptor interceptor}.
     * @param interceptorID ID of the {@link Interceptor interceptor} to add.
     * @param index Position on which to add the {@link Interceptor interceptor}.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If there is no such interface or if the ID does not represent a
     * controller interface nor a server interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor} or if the index is invalid.
     */
    public void addInterceptorOnInterface(String interfaceName, String interceptorID, int index)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException;

    /**
     * Adds at the last position the {@link Interceptor interceptor} with the specified ID to the interface
     * with the specified name.
     * 
     * @param interfaceName Name of the interface on which to add the {@link Interceptor interceptor}.
     * @param interceptorID ID of the {@link Interceptor interceptor} to add.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If there is no such interface or if the ID does not represent a
     * controller interface nor a server interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void addInterceptorOnInterface(String interfaceName, String interceptorID)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException;

    /**
     * Adds the {@link Interceptor interceptors} with the specified IDs to the interface with the specified
     * name.
     * 
     * @param interfaceName Name of the interface on which to add the {@link Interceptor interceptor}.
     * @param interceptorIDs IDs of the {@link Interceptor interceptors} to add.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If there is no such interface or if one of the ID does not represent a
     * controller interface nor a server interface of a NF component.
     * @throws NoSuchComponentException If one the IDs matches the format of the concatenation of a NF component
     * name and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void addInterceptorsOnInterface(String interfaceName, List<String> interceptorIDs)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException;

    /**
     * Adds at the last position the {@link Interceptor interceptor} with the specified ID to all the
     * server interfaces.
     * 
     * @param interceptorID ID of the {@link Interceptor interceptor} to add.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If the ID does not represent a controller interface nor a server
     * interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void addInterceptorOnAllServerInterfaces(String interceptorID) throws IllegalLifeCycleException,
            NoSuchInterfaceException, NoSuchComponentException, IllegalInterceptorException;

    /**
     * Adds at the last position the {@link Interceptor interceptor} with the specified ID to all the
     * client interfaces.
     * 
     * @param interceptorID ID of the {@link Interceptor interceptor} to add.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If the ID does not represent a controller interface nor a server
     * interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void addInterceptorOnAllClientInterfaces(String interceptorID) throws IllegalLifeCycleException,
            NoSuchInterfaceException, NoSuchComponentException, IllegalInterceptorException;

    /**
     * Adds at the last position the {@link Interceptor interceptor} with the specified ID to all the
     * client and server interfaces.
     * 
     * @param interceptorID ID of the {@link Interceptor interceptor} to add.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If the ID does not represent a controller interface nor a server
     * interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void addInterceptorOnAllInterfaces(String interceptorID) throws IllegalLifeCycleException,
            NoSuchInterfaceException, NoSuchComponentException, IllegalInterceptorException;

    /**
     * Removes the {@link Interceptor interceptor} located at the specified position from the interface
     * with the specified name.
     * 
     * @param interfaceName Name of the interface on which to remove the {@link Interceptor interceptor}.
     * @param index Position of the {@link Interceptor interceptor} to remove.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If there is no such interface.
     * @throws IllegalInterceptorException If the index is invalid.
     */
    public void removeInterceptorFromInterface(String interfaceName, int index)
            throws IllegalLifeCycleException, NoSuchInterfaceException, IllegalInterceptorException;

    /**
     * Removes the first occurrence of the {@link Interceptor interceptor} with the specified ID from the
     * interface with the specified name.
     * 
     * @param interfaceName Name of the interface on which to remove the {@link Interceptor interceptor}.
     * @param interceptorID ID of the {@link Interceptor interceptor} to remove.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If there is no such interface or if the ID does not represent a
     * controller interface nor a server interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void removeInterceptorFromInterface(String interfaceName, String interceptorID)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException;

    /**
     * Removes all the {@link Interceptor interceptors} from the interface with the specified name.
     * 
     * @param interfaceName Name of the interface on which to remove the {@link Interceptor interceptor}.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public void removeAllInterceptorsFromInterface(String interfaceName) throws IllegalLifeCycleException,
            NoSuchInterfaceException;

    /**
     * Removes the first occurrence of the {@link Interceptor interceptor} with the specified ID from all the
     * server interfaces.
     * 
     * @param interceptorID ID of the {@link Interceptor interceptor} to remove.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If the ID does not represent a controller interface nor a server
     * interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void removeInterceptorFromAllServerInterfaces(String interceptorID)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException;

    /**
     * Removes the first occurrence of the {@link Interceptor interceptor} with the specified ID from all the
     * client interfaces.
     * 
     * @param interceptorID ID of the {@link Interceptor interceptor} to remove.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If the ID does not represent a controller interface nor a server
     * interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void removeInterceptorFromAllClientInterfaces(String interceptorID)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException;

    /**
     * Removes the first occurrence of the {@link Interceptor interceptor} with the specified ID from all the
     * client and server interfaces.
     * 
     * @param interceptorID ID of the {@link Interceptor interceptor} to remove.
     * @throws IllegalLifeCycleException If the component is not in the stopped state.
     * @throws NoSuchInterfaceException If the ID does not represent a controller interface nor a server
     * interface of a NF component.
     * @throws NoSuchComponentException If the ID matches the format of the concatenation of a NF component name
     * and a server interface name but there is no NF component with such a name.
     * @throws IllegalInterceptorException If there is a problem with the ID or with the
     * {@link Interceptor interceptor}.
     */
    public void removeInterceptorFromAllInterfaces(String interceptorID) throws IllegalLifeCycleException,
            NoSuchInterfaceException, NoSuchComponentException, IllegalInterceptorException;
    //@snippet-end component_userguide_15
}
