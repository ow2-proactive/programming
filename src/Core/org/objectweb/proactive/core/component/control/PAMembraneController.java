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

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;


/**
 * Controller for the membrane of the component.
 *
 * @author The ProActive Team
 */
@PublicAPI
public interface PAMembraneController {
    public final static String MEMBRANE_STOPPED = "MEMBRANE_STOPPED";
    public final static String MEMBRANE_STARTED = "MEMBRANE_STARTED";

    //@snippet-start component_userguide_16
    /**
     * Adds non-functional components inside the membrane.
     *
     * @param component The non-functional component to add.
     * @throws IllegalContentException If the component to add is not a non-functional component.
     */
    void nfAddFcSubComponent(Component component) throws IllegalContentException, IllegalLifeCycleException;

    /**
     * Removes the specified component from the membrane.
     *
     * @param componentname The name of the component to remove.
     * @throws IllegalContentException If the specified component can not be removed.
     */
    void nfRemoveFcSubComponent(Component componentname) throws IllegalContentException,
            IllegalLifeCycleException, NoSuchComponentException;

    /**
     * Returns an array containing all the components inside the membrane.
     *
     * @return All the non-functional components.
     */
    Component[] nfGetFcSubComponents();

    /**
     * Returns the non functional component specified by the name parameter.
     *
     * @return The non functional component specified by the name parameter.
     */
    Component nfGetFcSubComponent(String name) throws NoSuchComponentException;

    /**
     * Sets a new controller object implementing the specified interface.
     *
     * @param itf The name of interface the new object has to implement.
     * @param controllerclass The controller object.
     * @throws NoSuchInterfaceException If the specified interface does not exist.
     */
    void setControllerObject(String itf, Object controllerclass) throws NoSuchInterfaceException;

    /**
     * Starts all non functional components inside the membrane.
     *
     * @throws IllegalLifeCycleException If one of the non functional components is in inconsistent lifecycle state.
     */
    public void startMembrane() throws IllegalLifeCycleException;

    /**
     * Stops all non functional components inside the membrane.
     *
     * @throws IllegalLifeCycleException If one of the non functional components is in inconsistent lifecycle state.
     */
    public void stopMembrane() throws NoSuchInterfaceException, IllegalLifeCycleException;

    /**
     * Returns the state of the membrane (by default, started or stopped).
     *
     * @return The current state of the membrane.
     */
    public String getMembraneState();

    //@snippet-end component_userguide_16

    //@snippet-start component_userguide_17
    /**
     * Performs bindings inside the membrane, and with non-functional interfaces of internal functional components.
     *
     * @param clientItf The client interface, referenced by a string of the form "component.interface", where
     * component is the name of the component, and interface the name of its client interface. If the component name
     * is "membrane", it means that interface must be the name of a non-functional external/internal interface of
     * the host component.
     * @param serverItf The server interface, referenced by a string of the form "component.interface", where
     * component is the name of the component, and interface the name of its server interface. If the component name
     * is "membrane", it means that interface must be the name of a non-functional external/internal interface of
     * the host component.
     * @throws NoSuchInterfaceException If one of the specified interfaces does not exist.
     * @throws IllegalLifeCycleException If one of the component is in inconsistent lifecycle state.
     * @throws IllegalBindingException If the type of the interfaces does not match.
     */
    void nfBindFc(String clientItf, String serverItf) throws NoSuchInterfaceException,
            IllegalLifeCycleException, IllegalBindingException, NoSuchComponentException;

    /**
     * Performs bindings with non-functional external interfaces.
     *
     * @param clientItf The client interface, referenced by a string of the form "component.interface", where
     * component is the name of the component, and interface the name of its client interface. If the component name
     * is "membrane", it means that interface must be the name of a non-functional external interface of the host
     * component.
     * @param serverItf The non-functional external server interface.
     * @throws NoSuchInterfaceException If one of the specified client interface does not exist.
     * @throws IllegalLifeCycleException If one of the component is in inconsistent lifecycle state.
     * @throws IllegalBindingException If the type of the interfaces does not match.
     */
    void nfBindFc(String clientItf, Object serverItf) throws NoSuchInterfaceException,
            IllegalLifeCycleException, IllegalBindingException, NoSuchComponentException;

    /**
     * Removes non-functional bindings.
     *
     * @param clientItf The client interface that will have its binding removed. It is a string of the form
     * "component.interface", where component is the name of the component, and interface the name of its server
     * interface. If the component name is "membrane", it means that interface must be the name of a non-functional
     * external/internal interface of the host component.
     * @throws NoSuchInterfaceException If the client interface does not exist.
     * @throws IllegalLifeCycleException If the component is in inconsistent lifecycle state.
     * @throws IllegalBindingException If the binding can not be removed.
     */
    void nfUnbindFc(String clientItf) throws NoSuchInterfaceException, IllegalLifeCycleException,
            IllegalBindingException, NoSuchComponentException;

    /**
     * Returns the names of the client interfaces belonging to the component, which name is passed as argument.
     *
     * @param component The name of the component.
     * @return An array containing the name of the client interfaces(we suppose that non-functional components don't
     * have client NF interfaces).
     */
    String[] nfListFc(String component) throws NoSuchComponentException, NoSuchInterfaceException,
            IllegalLifeCycleException;

    /**
     * Returns the stub and proxy of the server interface the client interface is connected to (for components
     * inside the membrane).
     *
     * @param itfname The client interface, referenced by a string of the form "component.interface", where
     * component is the name of the component, and interface the name of its client interface.
     * @return The stubs and the proxy of the server interface the client interface is connected to.
     * @throws NoSuchInterfaceException If the specified interface does not exist.
     */
    Object nfLookupFc(String itfname) throws NoSuchInterfaceException, NoSuchComponentException;

    /**
     * Starts the specified component.
     *
     * @param component The name of the component to stop.
     * @throws IllegalLifeCycleException If the lifecycle state is inconsistent.
     */
    void nfStartFc(String component) throws IllegalLifeCycleException, NoSuchComponentException,
            NoSuchInterfaceException;

    /**
     * Stops the specified component.
     *
     * @param component The name of the component to stop.
     * @throws IllegalLifeCycleException If the lifecycle state is inconsistent.
     */
    void nfStopFc(String component) throws IllegalLifeCycleException, NoSuchComponentException,
            NoSuchInterfaceException;

    /**
     * Returns the state of the specified non-functional component.
     *
     * @param component The name of the component.
     * @return The current state of the specified component.
     */
    String nfGetFcState(String component) throws NoSuchComponentException, NoSuchInterfaceException,
            IllegalLifeCycleException;
    //@snippet-end component_userguide_17
}
