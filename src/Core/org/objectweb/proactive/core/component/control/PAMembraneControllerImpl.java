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
package org.objectweb.proactive.core.component.control;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.NFBinding;
import org.objectweb.proactive.core.component.NFBindings;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.componentcontroller.HostComponentSetter;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.identity.PAComponentImpl;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentativeImpl;
import org.objectweb.proactive.core.component.representative.PANFComponentRepresentative;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The class implementing the membrane controller
 * @author The ProActive Team
 *
 */
public class PAMembraneControllerImpl extends AbstractPAController implements PAMembraneController,
        Serializable, ControllerStateDuplication {
    private Map<String, Component> nfcomponents;
    private String membraneState;
    protected static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    private NFBindings nfBindings;//TODO : This structure has to be updated every time a with the membrane is added or removed

    public PAMembraneControllerImpl(Component owner) {
        super(owner);
        nfcomponents = new HashMap<String, Component>();
        membraneState = MEMBRANE_STOPPED;
        nfBindings = new NFBindings();
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(Constants.MEMBRANE_CONTROLLER,
                    PAMembraneController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller type : " +
                this.getClass().getName());
        }
    }

    private void checkCompatibility(PAGCMInterfaceType clientItfType, Interface serverItf)
            throws IllegalBindingException {
        try {
            if (Utils.isGCMMulticastItf(clientItfType.getFcItfName(), getFcItfOwner())) {
                GCM.getMulticastController(owner).ensureGCMCompatibility(clientItfType, serverItf);
            }

            if (Utils.isGCMGathercastItf(serverItf)) {
                GCM.getGathercastController(owner).ensureGCMCompatibility(clientItfType, serverItf);
            } else if (Utils.isGCMSingletonItf(clientItfType.getFcItfName(), getFcItfOwner())) {
                PAGCMInterfaceType serverItfType = (PAGCMInterfaceType) serverItf.getFcItfType();
                Class<?> cl = Class.forName(clientItfType.getFcItfSignature());
                Class<?> sr = Class.forName(serverItfType.getFcItfSignature());
                if (!cl.isAssignableFrom(sr)) {
                    throw new IllegalBindingException("Signatures of interfaces don't correspond (" +
                        clientItfType.getFcItfSignature() + " and " + serverItfType.getFcItfSignature() + ")");
                }
            }
        } catch (ClassNotFoundException cnfe) {
            throw new IllegalBindingException(cnfe.getMessage());
        } catch (NoSuchInterfaceException nsie) {
            throw new IllegalBindingException(nsie.getMessage());
        }
    }

    public void addNFSubComponent(Component component) throws IllegalContentException,
            IllegalLifeCycleException {
        try {
            if (membraneState.equals(PAMembraneController.MEMBRANE_STARTED) ||
                GCM.getGCMLifeCycleController(owner).getFcState().equals(LifeCycleController.STARTED)) {
                throw new IllegalLifeCycleException(
                    "To perform reconfiguration inside the membrane, the lifecycle and the membrane must be stopped");
            }
        } catch (NoSuchInterfaceException e) {

            /*
             * Without a life cycle controller, the default activity of a GCM component does not
             * work
             */
        }
        checkMembraneIsStarted(component);
        PAComponent ownerRepresentative = owner.getRepresentativeOnThis();
        String name = null;
        if (!(component instanceof PANFComponentRepresentative)) {
            throw new IllegalContentException(
                "Only non-functional components should be added to the membrane");
        }
        try {
            name = GCM.getNameController(component).getFcName();
        } catch (NoSuchInterfaceException e) {
            IllegalContentException ice = new IllegalContentException(
                "The component has to implement the name-controller interface");
            ice.initCause(e);
            throw ice;
        }

        if (nfcomponents.containsKey(name)) {
            throw new IllegalContentException(
                "The name of the component is already assigned to an existing non functional component");
        }

        if (!((PAComponentRepresentativeImpl) ownerRepresentative).isPrimitive()) { /*
         * The host component is composite
         */
            Component[] fcomponents = null;
            try {
                fcomponents = GCM.getContentController(owner).getFcSubComponents();
            } catch (NoSuchInterfaceException e) {
                IllegalContentException ice = new IllegalContentException(
                    "The host component seems to be a composite without content-controller interface!!!");
                ice.initCause(e);
                throw ice;
            }

            for (Component c : fcomponents) { /*
             * Check that the name of the component is not
             * assigned to an existing functional one
             */
                try {
                    try {
                        if (Utils.getPAMembraneController(c).getMembraneState().equals(
                                PAMembraneController.MEMBRANE_STOPPED)) {
                            throw new IllegalLifeCycleException(
                                "While iterating on functional components, it apprears that one of them has its membranje in a stopped state. It should be started.");
                        }
                    } catch (NoSuchInterfaceException e) {
                        //If the component does not have any membrane-controller, it won't have any impact
                    }

                    if (GCM.getNameController(c).getFcName().compareTo(name) == 0) {
                        throw new IllegalContentException(
                            "The name of the component is already assigned to an existing functional component");
                    }
                } catch (NoSuchInterfaceException e) {

                    /*
                     * Do nothing : if the component does not have a name-controller interface, then
                     * it can not be bound with a non functional component
                     */
                }
            }
        } /* end of case with composite */
        try {
            Utils.getPASuperController(component).addParent(ownerRepresentative);
        } catch (NoSuchInterfaceException e) {

            /*
             * Once again, nothing to do, if the component doesn't have the super-controller, it
             * means that it will not reference the host component
             */
        }

        //If the component has the appropriate interface, give it a reference on the host component
        try {
            HostComponentSetter hcs = (HostComponentSetter) component
                    .getFcInterface(Constants.HOST_SETTER_CONTROLLER);
            hcs.setHostComponent(ownerRepresentative);
        } catch (NoSuchInterfaceException e) {
            logger.warn("The non-functional component " + name +
                " doesn't have any reference on its host component");
        }
        //Add the component inside the Map
        nfcomponents.put(name, component);
    }

    private void bindNfServerWithNfClient(String clItf, PAInterface srItf) throws IllegalBindingException {
        PAInterface cl = null;
        try {
            cl = (PAInterface) owner.getFcInterface(clItf);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }
        //Check whether the binding exists
        if (nfBindings.hasBinding("membrane", clItf, "membrane", srItf.getFcItfName())) {
            throw new IllegalBindingException("The binding : membrane." + clItf + "--->" + "membrane." +
                srItf.getFcItfName() + " already exists");
        }
        cl.setFcItfImpl(srItf);
        nfBindings.addNormalBinding(new NFBinding(cl, clItf, srItf, "membrane", "membrane"));

    }

    private void bindNfServerWithNfCServer(String clItf, PAInterface srItf) throws IllegalBindingException {
        PAInterface cl = null;
        Component srOwner = srItf.getFcItfOwner();
        try {
            if (nfBindings.hasBinding("membrane", clItf, GCM.getNameController(srOwner).getFcName(), srItf
                    .getFcItfName())) {
                throw new IllegalBindingException("The binding : membrane." + clItf + "--->" +
                    GCM.getNameController(srOwner).getFcName() + "." + srItf.getFcItfName() +
                    " already exists");
            }
        } catch (NoSuchInterfaceException e1) {
            e1.printStackTrace();
        }

        try {
            cl = (PAInterface) owner.getFcInterface(clItf);
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }

        try {//In this case, the nf component controller gets the state of a controller and duplicates it.
            ControllerStateDuplication dup = (ControllerStateDuplication) srOwner
                    .getFcInterface(Constants.CONTROLLER_STATE_DUPLICATION);
            Object ob = cl.getFcItfImpl();
            if (ob instanceof ControllerStateDuplication) {//The controller is implemented with an object
                dup.duplicateController(((ControllerStateDuplication) ob).getState().getStateObject());
            } else {//The controller is implemented with a NF component??
                if (ob instanceof PAInterface) {
                    Component cmp = ((PAInterface) ob).getFcItfOwner();
                    ControllerStateDuplication duplicated = (ControllerStateDuplication) cmp
                            .getFcInterface(Constants.CONTROLLER_STATE_DUPLICATION);
                    dup.duplicateController(duplicated.getState().getStateObject());
                }

            }
        } catch (NoSuchInterfaceException e) {
            logger.debug("The component controller doesn't have a duplication-controller interface");
        }
        //Check whether the binding exists

        cl.setFcItfImpl(srItf);

        try {
            nfBindings.addServerAliasBinding(new NFBinding(cl, clItf, srItf, "membrane", Fractal
                    .getNameController(srOwner).getFcName()));
        } catch (NoSuchInterfaceException e) {
            logger.warn("Could not add a binding : the component does not not have a Name Controller");
        }
    }

    private void bindNfClientWithFCServer(String clItf, PAInterface srItf) throws IllegalBindingException {

        PAInterface cl = null;
        Component srOwner = null;
        try {
            cl = (PAInterface) owner.getFcInterface(clItf);
        } catch (NoSuchInterfaceException e) {

            e.printStackTrace();
        }
        srOwner = srItf.getFcItfOwner();

        //Check whether the binding exists
        try {
            if (nfBindings.hasBinding("membrane", clItf, GCM.getNameController(srOwner).getFcName(), srItf
                    .getFcItfName())) {
                throw new IllegalBindingException("The binding : membrane." + clItf + "--->" +
                    GCM.getNameController(srOwner).getFcName() + "." + srItf.getFcItfName() +
                    " already exists");
            }
        } catch (NoSuchInterfaceException e1) {
            e1.printStackTrace();
        }

        cl.setFcItfImpl(srItf);
        try {
            nfBindings.addNormalBinding(new NFBinding(cl, clItf, srItf, "membrane", Fractal
                    .getNameController(srOwner).getFcName()));
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }
    }

    private void bindClientNFWithInternalServerNF(String clItf, PAInterface srItf)
            throws IllegalBindingException {
        bindNfServerWithNfClient(clItf, srItf);
    }

    public void bindNFc(String clientItf, String serverItf) throws NoSuchInterfaceException,
            IllegalLifeCycleException, IllegalBindingException, NoSuchComponentException {

        ComponentAndInterface client = getComponentAndInterface(clientItf);
        ComponentAndInterface server = getComponentAndInterface(serverItf);
        PAInterface clItf = (PAInterface) client.getInterface();
        PAGCMInterfaceType clItfType = (PAGCMInterfaceType) clItf.getFcItfType();
        PAInterface srItf = (PAInterface) server.getInterface();
        PAGCMInterfaceType srItfType = (PAGCMInterfaceType) srItf.getFcItfType();

        checkMembraneIsStopped();

        checkCompatibility(clItfType, srItf);

        if (client.getComponent() == null) { //The client interface belongs to the membrane
            if (!clItfType.isFcClientItf()) { //the client interface is a server one (internal or external) belonging to the membrane
                if (server.getComponent() == null) { //The server interface belongs to the membrane
                    if (srItfType.isFcClientItf()) { //The (server) interface is client (internal or external) belonging to the membrane
                        if (clItfType.isInternal()) { //Binding between internal server and internal client is forbidden inside the membrane
                            if (srItfType.isInternal()) {
                                //throw new IllegalBindingException(
                                //  "Internal server interface can not be bound to an internal client");
                                bindNfServerWithNfClient(clItf.getFcItfName(), srItf); //internal NF server with internal NF client
                            } else { //The server interface belongs to the membrane, and is external client
                                bindNfServerWithNfClient(clItf.getFcItfName(), srItf); //internal NF server with external NF client
                            }
                        } else { //The client itf is a NF external server
                            if (srItfType.isInternal()) {
                                bindNfServerWithNfClient(clItf.getFcItfName(), srItf); //external NF server with internal NF client
                            } else { //Trying to bind an external server NF interface with an external client NF interface
                                //throw new IllegalBindingException(
                                //  "External NF server interfaces can not be bound to external NF client interfaces");
                                bindNfServerWithNfClient(clItf.getFcItfName(), srItf); //external NF server with external NF client
                            }
                        }
                    }
                } else { //The server interface belongs to a component. Possible bindings : External/Internal NF server with a server of a NF component
                    checkMembraneIsStarted(server.getComponent());
                    if (srItfType.isFcClientItf() ||
                        !(server.getComponent() instanceof PANFComponentRepresentative) ||
                        srItfType.getFcItfName().endsWith("-controller")) {
                        throw new IllegalBindingException(
                            "NF server interfaces can be bound only to server F interfaces of NF components");
                    } else { //NF server interface with a server interface of a NF component : Server alias binding
                        bindNfServerWithNfCServer(clItf.getFcItfName(), srItf);
                    }
                }
            } else { //The client interface is a NF client one. For this method it can be only an internal NF client. It can be bound only to a NF interface of a F component.
                if (!clItfType.isInternal()) {
                    throw new IllegalBindingException(
                        "With this method, only internal NF client interfaces can be bound");
                }
                if (server.getComponent() == null) {//The server interface belongs to the membrane. In this case, this interface HAS to be an internal NF server
                    if (srItfType.getFcItfName().endsWith("-controller") && srItfType.isInternal()) {
                        bindClientNFWithInternalServerNF(clItfType.getFcItfName(), srItf);//NF internal client ---- NF internal server
                    } else {
                        throw new IllegalBindingException(
                            "Inside the membrane, internal NF interfaces can be bound only with NF internal server of NF interface of F inner components");
                    }

                } else {
                    if ((server.getComponent() instanceof PANFComponentRepresentative) ||
                        !(srItfType.getFcItfName().endsWith("-controller"))) {
                        throw new IllegalBindingException(
                            "With this method, an internal client NF interface can only be bound to a NF interface of a F inner component");
                    } else { //OK for binding client NF internal with NF external of F component
                        bindNfClientWithFCServer(clItf.getFcItfName(), srItf);
                    }
                }
            }
        } else { //The client interface belongs to a (NF or F)component

            if (!clItfType.isFcClientItf()) { //Check that a client interface of a F/NF component is bound
                throw new IllegalBindingException("Only a client interface of a NF/F can be bound");
            } else {
                if (client.getComponent() instanceof PANFComponentRepresentative) { //All possible bindings for client interfaces of NF components
                    if (server.getComponent() == null) { //A client interface of a NF component to a NF external/internal client
                        if (srItfType.isFcClientItf() && srItfType.getFcItfName().endsWith("-controller")) { //Connection to any (internal/external) client NF interface
                            GCM.getBindingController(client.getComponent()).bindFc(clItfType.getFcItfName(),
                                    owner.getRepresentativeOnThis().getFcInterface(srItfType.getFcItfName()));// Alias client binding
                            //Check whether the binding already exist
                            if (nfBindings.hasBinding(GCM.getNameController(client.getComponent())
                                    .getFcName(), client.getInterface().getFcItfName(), "membrane", srItf
                                    .getFcItfName())) {
                                throw new IllegalBindingException("The binding : " +
                                    GCM.getNameController(client.getComponent()).getFcName() + "." + clItf +
                                    "--->" + "membrane" + "." + srItf.getFcItfName() + " already exists");
                            }
                            nfBindings.addClientAliasBinding(new NFBinding(null, clItfType.getFcItfName(),
                                srItf, GCM.getNameController(client.getComponent()).getFcName(), "membrane"));

                        } else { //Exception!!
                            throw new IllegalBindingException(
                                "A NF component can only be bound to client NF interfaces of the membrane");
                        }
                    } else { //Binding of 2 NF components
                        if (!(server.getComponent() instanceof PANFComponentRepresentative)) { //The server component has to be a NF one
                            throw new IllegalBindingException(
                                "A NF component can only be bound to another NF (not F) component");
                        } else { //Last verification before binding
                            if (srItfType.isFcClientItf()) {
                                throw new IllegalBindingException(
                                    "When binding two NF components, a client interface must be bound to a server one");
                            } else { //Call to binding controller of the component that has the client interface
                                GCM.getBindingController(client.getComponent()).bindFc(
                                        clItfType.getFcItfName(),
                                        server.getComponent().getFcInterface(srItfType.getFcItfName()));
                            }
                        }
                    }
                } else { //Binding for NF client interfaces of inner F components
                    if (server.getComponent() == null) {
                        if (!srItfType.isFcClientItf() && srItfType.isInternal()) { //External client NF interface only bound to inner server NF interface
                            //No beed to check the membrane state of Host component
                            Utils.getPAMembraneController(client.getComponent()).bindNFc(
                                    clItfType.getFcItfName(),
                                    owner.getRepresentativeOnThis().getFcInterface(srItfType.getFcItfName()));
                            //Check Whether this binding already exist
                            if (nfBindings.hasBinding(GCM.getNameController(client.getComponent())
                                    .getFcName(), client.getInterface().getFcItfName(), "membrane", srItf
                                    .getFcItfName())) {
                                throw new IllegalBindingException("The binding : " +
                                    GCM.getNameController(client.getComponent()).getFcName() + "." + clItf +
                                    "--->" + "membrane" + "." + srItf.getFcItfName() + " already exists");
                            }
                            nfBindings.addNormalBinding(new NFBinding(clItf, clItfType.getFcItfName(), srItf,
                                GCM.getNameController(client.getComponent()).getFcName(), "membrane"));
                        } else { //Exception
                            throw new IllegalBindingException(
                                "The server interface has to be a NF inner server one");
                        }

                        //Bind only to a NF internal server
                    } else { //Exception!!
                        throw new IllegalBindingException(
                            "An inner F component can only bind its client NF interfaces to inner server NF interfaces");
                    }
                }
            }
        }
    }

    public void bindNFc(String clientItf, Object serverItf) throws NoSuchInterfaceException,
            IllegalLifeCycleException, IllegalBindingException, NoSuchComponentException {//Binds external NF client itf with External NF Server
        serverItf = PAFuture.getFutureValue(serverItf);
        ComponentAndInterface client = getComponentAndInterface(clientItf);
        PAInterface clItf = (PAInterface) client.getInterface();
        PAGCMInterfaceType clItfType = (PAGCMInterfaceType) clItf.getFcItfType();
        PAInterface srItf = (PAInterface) serverItf;
        if (!clItfType.isFcClientItf()) {
            throw new IllegalBindingException("This method only binds NF client interfaces");
        } else {//OK for binding, but first check that types are compatible
            checkMembraneIsStopped();

            checkCompatibility(clItfType, srItf);

            if (nfBindings.hasBinding("membrane", clientItf, null, srItf.getFcItfName())) {
                throw new IllegalBindingException("The binding :" + " membrane." + clientItf +
                    "--> external NF interface already exists");
            }
            PAInterface cl = (PAInterface) owner.getFcInterface(clientItf);
            cl.setFcItfImpl(serverItf);
            nfBindings.addNormalBinding(new NFBinding(clItf, clientItf, srItf, "membrane", null));
        }

    }

    public String getNFcState(String component) throws NoSuchComponentException, NoSuchInterfaceException,
            IllegalLifeCycleException {
        if (!nfcomponents.containsKey(component)) {
            throw new NoSuchComponentException("There is no component named " + component);
        }
        checkMembraneIsStarted(nfcomponents.get(component));
        return GCM.getGCMLifeCycleController(nfcomponents.get(component)).getFcState();
    }

    public Component[] getNFcSubComponents() {
        List<Component> nfSubComponents = new ArrayList<Component>(nfcomponents.values());
        return nfSubComponents.toArray(new Component[nfSubComponents.size()]);
    }

    public String[] listNFc(String component) throws NoSuchComponentException, NoSuchInterfaceException,
            IllegalLifeCycleException {
        if (!nfcomponents.containsKey(component)) {
            throw new NoSuchComponentException("There is no " + component + " inside the membrane");
        }
        checkMembraneIsStarted(nfcomponents.get(component));
        return GCM.getBindingController(nfcomponents.get(component)).listFc();
    }

    public Object lookupNFc(String itfname) throws NoSuchInterfaceException, NoSuchComponentException {
        ComponentAndInterface itf = getComponentAndInterface(itfname);
        PAInterface theItf = (PAInterface) itf.getInterface();
        PAGCMInterfaceType theType = (PAGCMInterfaceType) theItf.getFcItfType();
        if (itf.getComponent() == null) {//The interface has to belong to the membrane and has to be client!!
            theItf = (PAInterface) itf.getInterface();
            theType = (PAGCMInterfaceType) theItf.getFcItfType();
            if (theType.isFcClientItf()) {//OK, We can return its implementation

                return theItf.getFcItfImpl();

            } else {
                throw new NoSuchInterfaceException("The requested interface: " + theItf.getFcItfName() +
                    " is not a client one");
            }

        } else {//The component is either functional or non-functional
            if (itf.getComponent() instanceof PANFComponentRepresentative) {
                return GCM.getBindingController(itf.getComponent()).lookupFc(theItf.getFcItfName());
            } else {//The component is functional, and we are attempting to lookup on a client non-functional external interface
                if (theType.getFcItfName().endsWith("-controller")) {
                    return Utils.getPAMembraneController(itf.getComponent()).lookupNFc(
                            itf.getInterface().getFcItfName());
                }
                //throw new NoSuchComponentException("The specified component: " +
                //  GCM.getNameController(itf.getComponent()).getFcName() + " is not in the membrane");
            }

        }
        return null;
    }

    public void removeNFSubComponent(Component component) throws IllegalContentException,
            IllegalLifeCycleException, NoSuchComponentException {
        try { /* Check the lifecycle of the membrane and the component */
            if (membraneState.equals(MEMBRANE_STARTED) ||
                GCM.getGCMLifeCycleController(owner).getFcState().equals(LifeCycleController.STARTED)) {
                throw new IllegalLifeCycleException(
                    "To perform reconfiguration inside the membrane, the lifecycle and the membrane must be stopped");
            }
        } catch (NoSuchInterfaceException e) {

            /* Without a life cycle controller, a GCM component does not work */
        }
        checkMembraneIsStarted(component);
        String componentname = null;
        try {
            componentname = GCM.getNameController(component).getFcName();
        } catch (NoSuchInterfaceException i) {
            IllegalContentException ice = new IllegalContentException(
                "NF components are identified by their names. The component to remove does not have any.");
            ice.initCause(i);
            throw ice;
        }
        PAComponent ownerRepresentative = owner.getRepresentativeOnThis();

        if (!nfcomponents.containsKey(componentname)) {
            throw new NoSuchComponentException("There is no " + componentname + " inside the membrane");
        }

        Component toRemove = nfcomponents.get(componentname);

        try {
            if (Utils.getPABindingController(toRemove).isBound().booleanValue()) {
                throw new IllegalContentException(
                    "cannot remove a sub component that holds bindings on its external interfaces");
            }
        } catch (NoSuchInterfaceException ignored) {
            // no binding controller
        }

        try {
            Utils.getPASuperController(toRemove).removeParent(ownerRepresentative);
        } catch (NoSuchInterfaceException e) {

            /* No superController */
        }
        //Here, when removing a component on which the host holds bindings, remove those bindings
        nfBindings.removeServerAliasBindingsOn(componentname);
        nfcomponents.remove(componentname);
    }

    public void setControllerObject(String itf, Object controllerclass) throws NoSuchInterfaceException {
        try {
            if (membraneState.equals(PAMembraneController.MEMBRANE_STARTED) ||
                GCM.getGCMLifeCycleController(owner).getFcState().equals(LifeCycleController.STARTED)) {
                throw new IllegalLifeCycleException(
                    "For the moment, to perform reconfiguration inside the membrane, the lifecycle and the membrane must be stopped");
            }
            ((PAComponentImpl) owner).setControllerObject(itf, controllerclass);
        } catch (NoSuchInterfaceException n) {
            throw n;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startNFc(String component) throws IllegalLifeCycleException, NoSuchComponentException,
            NoSuchInterfaceException {
        if (!nfcomponents.containsKey(component)) {
            throw new NoSuchComponentException("There is no component named " + component);
        }

        checkMembraneIsStarted(nfcomponents.get(component));
        GCM.getGCMLifeCycleController(nfcomponents.get(component)).startFc();
    }

    public void stopNFc(String component) throws IllegalLifeCycleException, NoSuchComponentException,
            NoSuchInterfaceException {
        if (!nfcomponents.containsKey(component)) {
            throw new NoSuchComponentException("There is no component named " + component);
        }
        checkMembraneIsStarted(nfcomponents.get(component));

        GCM.getGCMLifeCycleController(nfcomponents.get(component)).stopFc();
    }

    public void unbindNFc(String clientItf) throws NoSuchInterfaceException, IllegalLifeCycleException,
            IllegalBindingException, NoSuchComponentException {//Unbinds client interfaces exposed by the membrane, of client interfaces of non-functional components.

        if (membraneState.equals(PAMembraneController.MEMBRANE_STARTED)) {
            throw new IllegalLifeCycleException(
                "The membrane should be stopped while unbinding non-functional interfaces");
        }
        ComponentAndInterface theItf = getComponentAndInterface(clientItf);
        PAInterface it = (PAInterface) theItf.getInterface();
        PAGCMInterfaceType clItfType = (PAGCMInterfaceType) it.getFcItfType();
        if (theItf.getComponent() == null) {// Unbind a client interface exposed by the membrane, update the structure
            it.setFcItfImpl(null);
            nfBindings.removeNormalBinding("membrane", it.getFcItfName());//Here, we deal only with singleton bindings
        } else {//Unbind the non-functional component's interface
            Component theComp = theItf.getComponent();
            checkMembraneIsStarted(theComp);
            if (theComp instanceof PANFComponentRepresentative) {
                if (clItfType.isFcClientItf()) {
                    GCM.getBindingController(theComp).unbindFc(theItf.getInterface().getFcItfName());
                    nfBindings.removeClientAliasBinding(GCM.getNameController(theComp).getFcName(), it
                            .getFcItfName());
                } else {//The interface is not client. It should be.
                    throw new IllegalBindingException("You should specify a client singleton interface");
                }
            } else {//The component is a functional one. It should not.
                throw new IllegalBindingException(
                    "You should unbind a functional interface of a non-functional component inside the membrane");
            }
        }

    }

    public void startMembrane() throws IllegalLifeCycleException {

        InterfaceType[] itfTypes = ((ComponentType) ((PAComponentImpl) getFcItfOwner()).getNFType())
                .getFcInterfaceTypes();
        for (InterfaceType itfT : itfTypes) {
            if (!itfT.isFcOptionalItf()) {//Are all mandatory interfaces bound??
                try {
                    PAInterface paItf = (PAInterface) getFcItfOwner().getFcInterface(itfT.getFcItfName());
                    if (paItf.getFcItfImpl() == null) {
                        throw new IllegalLifeCycleException(
                            "To start the membrane, all mandatory non-functional interfaces have to be bound. The interface " +
                                itfT.getFcItfName() + " is not.");
                    }
                } catch (NoSuchInterfaceException e) {
                    IllegalLifeCycleException ilce = new IllegalLifeCycleException("The interface " +
                        itfT.getFcItfName() +
                        " declared in the non-functional type was not generated on the server side");
                    ilce.initCause(e);
                    throw ilce;
                }
            }

        }
        for (Component c : nfcomponents.values()) {
            try {
                checkMembraneIsStarted(c);
                GCM.getGCMLifeCycleController(c).startFc();
            } catch (NoSuchInterfaceException nosi) {

                /*
                 * If the component has no lifecycle controller, then it can not be started or
                 * stopped
                 */
            }
        }
        membraneState = MEMBRANE_STARTED;
    }

    public void stopMembrane() throws IllegalLifeCycleException {

        for (Component c : nfcomponents.values()) {
            try {
                checkMembraneIsStarted(c);
                GCM.getGCMLifeCycleController(c).stopFc();

            } catch (NoSuchInterfaceException nosi) {

                try {
                    logger.debug("The component" + GCM.getNameController(c).getFcName() +
                        " has no LifeCycle Controller");
                } catch (NoSuchInterfaceException e) {// If the component has no lifecycle controller, then it can not be started or stopped

                    //No LifeCycle and no name for this component
                }

            }
        }
        membraneState = MEMBRANE_STOPPED;
    }

    /**
     * Check if all non functional components are stopped
     * @return True if all the non functional components are stopped, false if not
     */
    private boolean membraneIsStopped() {
        boolean result = true;
        for (Component c : nfcomponents.values()) {
            try {
                result = result &&
                    (GCM.getGCMLifeCycleController(c).getFcState().compareTo(LifeCycleController.STOPPED) == 0);
            } catch (NoSuchInterfaceException e) {

                /* Without a lifecycle controller, the componnet has no lifecycle state */
            }
        }

        return result;
    }

    /**
     * Returns first occurence of functional components corresponding to the specified name
     * @param name The name of the component
     * @return The first occurence of functional components corresponding to the specified name
     */
    private Component getFunctionalComponent(String name) {
        try {
            Component[] fComponents = GCM.getContentController(owner).getFcSubComponents();

            for (Component c : fComponents) {
                try {
                    if (GCM.getNameController(c).getFcName().compareTo(name) == 0) {
                        return c;
                    }
                } catch (NoSuchInterfaceException e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean hostComponentisPrimitive() {
        try {
            return owner.getComponentParameters().getHierarchicalType().equals(Constants.PRIMITIVE);
        } catch (Exception e) {
        }

        return false;
    }

    public Component getNFcSubComponent(String name) {
        return nfcomponents.get(name);
    }

    public void duplicateController(Object c) {
        if (c instanceof HashMap<?, ?>) {
            nfcomponents = (HashMap<String, Component>) c;
        } else {
            throw new ProActiveRuntimeException(
                "PAMembraneControllerImpl: Impossible to duplicate the controller " + this +
                    " from the controller" + c);
        }
    }

    private ComponentAndInterface getComponentAndInterface(String itf) throws NoSuchInterfaceException {
        String[] itfTab = itf.split("\\.", 2);
        if (itfTab.length == 1) { /*
         * The interface tab has only one element : if it exists, it is
         * an interface of the membrane
         */
            if (itfTab[0].endsWith("-controller")) {
                Interface i = (Interface) owner.getFcInterface(itfTab[0]);

                return new ComponentAndInterface(i);
            } else {//The interface is not a controller one
                throw new NoSuchInterfaceException("The specified interface " + itfTab[0] +
                    " is not non-functional");
            }
        } else { /* Normally, component and its interface are specified */
            if (itfTab[0].equals("membrane")) {
                Interface i = (Interface) owner.getFcInterface(itfTab[1]);
                return new ComponentAndInterface(i);
            }
            Component searchComponent = null;
            try {
                if (!hostComponentisPrimitive()) { //Is it a functional component?
                    searchComponent = getFunctionalComponent(itfTab[0]);
                }

                if (searchComponent == null) {//The component we are looking for is not in the functional content
                    searchComponent = getNFcSubComponent(itfTab[0]); /*
                     * Is it a non functional
                     * component??
                     */
                    if (searchComponent == null) {
                        throw new NoSuchComponentException("There is no : " + itfTab[0] + " component");
                    } else { /* The component is non-functional */
                        return new ComponentAndInterface(searchComponent, (Interface) searchComponent
                                .getFcInterface(itfTab[1]));
                    }
                } else { /* The component is functional */
                    return new ComponentAndInterface(searchComponent, (Interface) searchComponent
                            .getFcInterface(itfTab[1]));
                }
            } catch (NoSuchInterfaceException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private void checkMembraneIsStopped() throws IllegalLifeCycleException {
        if (membraneState.equals(PAMembraneController.MEMBRANE_STARTED)) {
            throw new IllegalLifeCycleException("The membrane should be stopped");
        }
    }

    private void checkMembraneIsStarted(Component comp) throws IllegalLifeCycleException {

        try {
            if (Utils.getPAMembraneController(comp).getMembraneState().equals(
                    PAMembraneController.MEMBRANE_STOPPED)) {
                throw new IllegalLifeCycleException(
                    "The desired operation can not be performed. The membrane of a non-functional component is stopped. It should be started.");
            }
        } catch (NoSuchInterfaceException e) {
            //No impact on the rest of the code without a membrane-controller
        }

    }

    public void checkInternalInterfaces() throws IllegalLifeCycleException {

        InterfaceType[] itfTypes = ((ComponentType) ((PAComponentImpl) getFcItfOwner()).getNFType())
                .getFcInterfaceTypes();
        PAGCMInterfaceType paItfT;
        for (InterfaceType itfT : itfTypes) {
            paItfT = (PAGCMInterfaceType) itfT;
            if (!itfT.isFcOptionalItf() && paItfT.isInternal()) {

                PAInterface paItf;
                try {
                    paItf = (PAInterface) getFcItfOwner().getFcInterface(itfT.getFcItfName());
                    if (paItf.getFcItfImpl() == null) {
                        throw new IllegalLifeCycleException(
                            "When strating the component, all mandatory internal non-functional interfaces have to be bound. The interface " +
                                itfT.getFcItfName() + " is not.");
                    }
                } catch (NoSuchInterfaceException e) {
                    IllegalLifeCycleException ilce = new IllegalLifeCycleException("The interface " +
                        itfT.getFcItfName() +
                        " declared in the non-functional type was not generated on the server side");
                    ilce.initCause(e);
                    throw ilce;
                }

            }

        }

    }

    class ComponentAndInterface {
        private Component theComponent;
        private Interface theInterface;

        public ComponentAndInterface(Component comp, Interface i) {
            theComponent = comp;
            theInterface = i;
        }

        public ComponentAndInterface(Interface i) {
            theComponent = null;
            theInterface = i;
        }

        public Component getComponent() {
            return theComponent;
        }

        public void setComponent(Component theComponent) {
            this.theComponent = theComponent;
        }

        public Interface getInterface() {
            return theInterface;
        }

        public void setInterface(Interface theInterface) {
            this.theInterface = theInterface;
        }
    }

    public ControllerState getState() {
        return new ControllerState((HashMap<String, Component>) nfcomponents);
    }

    public String getMembraneState() {
        return membraneState;
    }

}
