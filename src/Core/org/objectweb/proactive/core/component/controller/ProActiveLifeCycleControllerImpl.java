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
package org.objectweb.proactive.core.component.controller;

import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Fractive;
import org.objectweb.proactive.core.component.ProActiveInterface;
import org.objectweb.proactive.core.component.group.ProxyForComponentInterfaceGroup;
import org.objectweb.proactive.core.component.identity.ProActiveComponent;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.type.ProActiveInterfaceType;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactory;
import org.objectweb.proactive.core.component.type.ProActiveTypeFactoryImpl;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Implementation of the {@link org.objectweb.fractal.api.control.LifeCycleController} interface.<br>
 *
 * @author The ProActive Team
 *
 */
public class ProActiveLifeCycleControllerImpl extends AbstractProActiveController implements
        ProActiveLifeCycleController, Serializable, ControllerStateDuplication {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_CONTROLLERS);
    protected String fcState = LifeCycleController.STOPPED;

    public ProActiveLifeCycleControllerImpl(Component owner) {
        super(owner);
    }

    @Override
    protected void setControllerItfType() {
        try {
            setItfType(ProActiveTypeFactoryImpl.instance().createFcItfType(Constants.LIFECYCLE_CONTROLLER,
                    ProActiveLifeCycleController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.LifeCycleController#getFcState()
     */
    public String getFcState() {
        return fcState;
        //        return getRequestQueue().isStarted() ? LifeCycleController.STARTED
        //                                             : LifeCycleController.STOPPED;
    }

    /**
     * @see org.objectweb.fractal.api.control.LifeCycleController#startFc() recursive if composite (
     *      recursivity is allowed here as we do not implement sharing )
     */
    public void startFc() throws IllegalLifeCycleException {
        try {
            //check that all mandatory client interfaces are bound
            InterfaceType[] itfTypes = ((ComponentType) getFcItfOwner().getFcType()).getFcInterfaceTypes();

            for (int i = 0; i < itfTypes.length; i++) {
                if ((itfTypes[i].isFcClientItf() && !itfTypes[i].isFcOptionalItf()) ||
                    (isComposite() && !itfTypes[i].isFcClientItf() && !itfTypes[i].isFcOptionalItf() && !AttributeController.class
                            .isAssignableFrom(Class.forName(itfTypes[i].getFcItfSignature())))) {
                    if (itfTypes[i].isFcCollectionItf()) {
                        // look for collection members
                        Object[] itfs = owner.getFcInterfaces();
                        for (int j = 0; j < itfs.length; j++) {
                            Interface itf = (Interface) itfs[j];
                            if (itf.getFcItfName().startsWith(itfTypes[i].getFcItfName())) {
                                if (itf.getFcItfName().equals(itfTypes[i].getFcItfName())) {
                                    throw new IllegalLifeCycleException(
                                        "invalid collection interface name at runtime (suffix required)");
                                }
                                if (Fractal.getBindingController(owner).lookupFc(itf.getFcItfName()) == null) {
                                    if (itfTypes[i].isFcClientItf()) {
                                        throw new IllegalLifeCycleException(
                                            "compulsory collection client interface " +
                                                itfTypes[i].getFcItfName() + " in component " +
                                                Fractal.getNameController(getFcItfOwner()).getFcName() +
                                                " is not bound.");
                                    } else { // itfTypes[i] is a server interface of a composite
                                        throw new IllegalLifeCycleException(
                                            "compulsory collection server interface " +
                                                itfTypes[i].getFcItfName() + " in composite component " +
                                                Fractal.getNameController(getFcItfOwner()).getFcName() +
                                                " is not bound to any sub component.");
                                    }
                                }
                            }
                        }
                    } else if (((ProActiveInterfaceType) itfTypes[i]).isFcMulticastItf() &&
                        !itfTypes[i].isFcOptionalItf()) {
                        ProxyForComponentInterfaceGroup<?> delegatee = Fractive.getMulticastController(
                                getFcItfOwner()).lookupFcMulticast(itfTypes[i].getFcItfName()).getDelegatee();
                        if ((delegatee == null) || delegatee.isEmpty()) {
                            if (itfTypes[i].isFcClientItf()) {
                                throw new IllegalLifeCycleException("compulsory multicast client interface " +
                                    itfTypes[i].getFcItfName() + " in component " +
                                    Fractal.getNameController(getFcItfOwner()).getFcName() + " is not bound.");
                            } else { // itfTypes[i] is a server interface of a composite
                                throw new IllegalLifeCycleException("compulsory multicast server interface " +
                                    itfTypes[i].getFcItfName() + " in composite component " +
                                    Fractal.getNameController(getFcItfOwner()).getFcName() +
                                    " is not bound to any sub component.");
                            }
                        }
                    } else if ((((ProActiveInterfaceType) itfTypes[i]).getFcCardinality().equals(
                            ProActiveTypeFactory.SINGLETON_CARDINALITY) || ((ProActiveInterfaceType) itfTypes[i])
                            .getFcCardinality().equals(ProActiveTypeFactory.GATHER_CARDINALITY)) &&
                        (Fractal.getBindingController(getFcItfOwner()).lookupFc(itfTypes[i].getFcItfName()) == null)) {
                        if (itfTypes[i].isFcClientItf()) {
                            throw new IllegalLifeCycleException("compulsory client interface " +
                                itfTypes[i].getFcItfName() + " in component " +
                                Fractal.getNameController(getFcItfOwner()).getFcName() + " is not bound.");
                        } else { // itfTypes[i] is a server interface of a composite
                            throw new IllegalLifeCycleException("compulsory server interface " +
                                itfTypes[i].getFcItfName() + " in composite component " +
                                Fractal.getNameController(getFcItfOwner()).getFcName() +
                                " is not bound to any sub component.");
                        }
                    }

                    // tests for internal client interface of composite component
                    if (isComposite() && itfTypes[i].isFcClientItf() && !itfTypes[i].isFcOptionalItf()) {
                        if (itfTypes[i].isFcCollectionItf()) {
                            // TODO Check binding
                        } else if (((ProActiveInterfaceType) itfTypes[i]).getFcCardinality().equals(
                                ProActiveTypeFactory.GATHER_CARDINALITY)) {
                            List<ItfID> connectedClientItfs = Fractive.getGathercastController(
                                    getFcItfOwner()).getConnectedClientItfs(itfTypes[i].getFcItfName());
                            if ((connectedClientItfs == null) || connectedClientItfs.isEmpty()) {
                                throw new IllegalLifeCycleException(
                                    "compulsory gathercast client interface " + itfTypes[i].getFcItfName() +
                                        " in composite component " +
                                        Fractal.getNameController(getFcItfOwner()).getFcName() +
                                        " is not bound to any sub component.");
                            }
                        } else { // client interface is a single or multicast interface
                            boolean isBound = false;
                            Component[] subComponents = Fractal.getContentController(getFcItfOwner())
                                    .getFcSubComponents();
                            for (int j = 0; (j < subComponents.length) && !isBound; j++) {
                                try {
                                    String[] subComponentItfs = Fractal
                                            .getBindingController(subComponents[j]).listFc();
                                    for (int k = 0; k < subComponentItfs.length; k++) {
                                        if (((ProActiveInterfaceType) ((Interface) subComponents[j]
                                                .getFcInterface(subComponentItfs[k])).getFcItfType())
                                                .isFcMulticastItf()) {
                                            isBound = true;
                                            break;
                                            //                                            ProxyForComponentInterfaceGroup<?> delegatee = Fractive
                                            //                                                    .getMulticastController(subComponents[j])
                                            //                                                    .lookupFcMulticast(subComponentItfs[k])
                                            //                                                    .getDelegatee();
                                            //                                            if ((delegatee != null) && !delegatee.isEmpty()) {
                                            //                                                ProActiveInterface[] delegatees = delegatee
                                            //                                                        .toArray(new ProActiveInterface[] {});
                                            //                                                for (int l = 0; l < delegatees.length; l++) {
                                            //                                                    if (((ProActiveComponent) delegatees[l].getFcItfOwner())
                                            //                                                            .getID().equals(owner.getID())) {
                                            //                                                        isBound = true;
                                            //                                                        break;
                                            //                                                    }
                                            //                                                }
                                            //                                            }
                                        } else {
                                            Object subComponentItfImpl = null;
                                            try {
                                                subComponentItfImpl = Fractal.getBindingController(
                                                        subComponents[j]).lookupFc(subComponentItfs[k]);
                                            } catch (NoSuchInterfaceException nsie) {
                                                // should never happen
                                                logger.error("Interface " + subComponentItfs[k] +
                                                    " in component " + subComponents[j] + " does not exist",
                                                        nsie);
                                            }
                                            if (subComponentItfImpl != null) {
                                                if (((ProActiveComponent) ((ProActiveInterface) subComponentItfImpl)
                                                        .getFcItfOwner()).getID().equals(owner.getID())) {
                                                    isBound = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                } catch (NoSuchInterfaceException nsie) {
                                    // sub component does not have a binding controller
                                }
                            }
                            if (!isBound) {
                                throw new IllegalLifeCycleException("compulsory client interface " +
                                    itfTypes[i].getFcItfName() + " in composite component " +
                                    Fractal.getNameController(getFcItfOwner()).getFcName() +
                                    " is not bound to any sub component.");
                            }
                        }
                    }
                }
            }

            //try {
            //   ProActiveInterface it = (ProActiveInterface) Fractive.getMembraneController(getFcItfOwner());
            //   Object obj = it.getFcItfImpl();
            //   ((MembraneControllerImpl) obj).checkInternalInterfaces();
            //} catch (NoSuchInterfaceException nsie) {
            //Nothing to do, if the component does not have any membrane controller
            //}

            String hierarchical_type = owner.getComponentParameters().getHierarchicalType();
            if (hierarchical_type.equals(Constants.COMPOSITE)) {
                // start all inner components
                Component[] inner_components = Fractal.getContentController(getFcItfOwner())
                        .getFcSubComponents();
                if (inner_components != null) {
                    for (int i = 0; i < inner_components.length; i++) {
                        try {
                            if (Fractive.getMembraneController(inner_components[i]).getMembraneState()
                                    .equals(MembraneController.MEMBRANE_STOPPED)) {
                                throw new IllegalLifeCycleException(
                                    "Before starting all subcomponents, make sure that the membrane of all of them is started");
                            }
                        } catch (NoSuchInterfaceException e) {
                            //the subComponent doesn't have a MembraneController, no need to check what's is in the previous try block
                        }
                        ((LifeCycleController) inner_components[i]
                                .getFcInterface(Constants.LIFECYCLE_CONTROLLER)).startFc();
                    }
                }
            }

            //getRequestQueue().start();
            fcState = LifeCycleController.STARTED;
            if (logger.isDebugEnabled()) {
                logger.debug("started " + Fractal.getNameController(owner).getFcName());
            }
        } catch (ClassNotFoundException cnfe) {
            logger.error("class not found : " + cnfe.getMessage(), cnfe);
        } catch (NoSuchInterfaceException nsie) {
            logger.error("interface not found : " + nsie.getMessage(), nsie);
        }
    }

    /*
     * @see org.objectweb.fractal.api.control.LifeCycleController#stopFc() recursive if composite
     */
    public void stopFc() {
        try {
            String hierarchical_type = owner.getComponentParameters().getHierarchicalType();
            if (hierarchical_type.equals(Constants.COMPOSITE)) {
                // stop all inner components
                Component[] inner_components = Fractal.getContentController(getFcItfOwner())
                        .getFcSubComponents();
                if (inner_components != null) {
                    for (int i = 0; i < inner_components.length; i++) {
                        try {
                            if (Fractive.getMembraneController(inner_components[i]).getMembraneState()
                                    .equals(MembraneController.MEMBRANE_STOPPED)) {
                                throw new IllegalLifeCycleException(
                                    "Before stopping all subcomponents, make sure that the membrane of all them is started");
                            }
                        } catch (NoSuchInterfaceException e) {
                            //the subComponent doesn't have a MembraneController, no need to check what's is in the previous try block
                        }
                        ((LifeCycleController) inner_components[i]
                                .getFcInterface(Constants.LIFECYCLE_CONTROLLER)).stopFc();
                    }
                }
            }

            //getRequestQueue().stop();
            fcState = LifeCycleController.STOPPED;
            if (logger.isDebugEnabled()) {
                logger.debug("stopped" + Fractal.getNameController(owner).getFcName());
            }
        } catch (NoSuchInterfaceException nsie) {
            logger.error("interface not found : " + nsie.getMessage());
        } catch (IllegalLifeCycleException ilce) {
            logger.error("illegal life cycle operation : " + ilce.getMessage());
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#getFcState(short)
     */
    public String getFcState(short priority) {
        return getFcState();
    }

    /**
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#startFc(short)
     */
    public void startFc(short priority) throws IllegalLifeCycleException {
        startFc();
    }

    /*
     * @see org.objectweb.proactive.core.component.controller.ProActiveLifeCycleController#stopFc(short)
     */
    public void stopFc(short priority) {
        stopFc();
    }

    public void duplicateController(Object c) {
        if (c instanceof String) {
            fcState = (String) c;

        } else {
            throw new ProActiveRuntimeException(
                "ProActiveLifeCycleControllerImpl : Impossible to duplicate the controller " + this +
                    " from the controller" + c);
        }

    }

    public ControllerState getState() {

        return new ControllerState(fcState);
    }
}
