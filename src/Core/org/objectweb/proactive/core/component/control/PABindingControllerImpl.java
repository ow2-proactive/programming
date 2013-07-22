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

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.etsi.uri.gcm.api.type.GCMInterfaceType;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Binding;
import org.objectweb.proactive.core.component.Bindings;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ItfStubObject;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.gen.GatherItfAdapterProxy;
import org.objectweb.proactive.core.component.gen.OutputInterceptorClassGenerator;
import org.objectweb.proactive.core.component.gen.WSProxyClassGenerator;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.type.PAComponentType;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceTypeImpl;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;
import org.objectweb.proactive.core.component.type.WSComponent;
import org.objectweb.proactive.core.component.webservices.WSInfo;


/**
 * Implementation of the {@link BindingController binding controller}.
 *
 * @author The ProActive Team
 */
public class PABindingControllerImpl extends AbstractPAController implements PABindingController,
        Serializable, ControllerStateDuplication {
    protected Bindings bindings; // key = clientInterfaceName ; value = Binding

    /**
     * Creates a {@link PABindingControllerImpl}.
     * 
     * @param owner Component owning the controller.
     */
    public PABindingControllerImpl(Component owner) {
        super(owner);
        bindings = new Bindings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(Constants.BINDING_CONTROLLER,
                    PABindingController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller type for controller " +
                this.getClass().getName());
        }
    }

    public void addBinding(Binding binding) {
        bindings.add(binding);
    }

    protected void checkBindability(String clientItfName, Interface serverItf)
            throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        if (!(serverItf instanceof PAInterface)) {
            throw new IllegalBindingException("Can only bind interfaces of type PAInterface");
        }

        //        if (((InterfaceType) serverItf.getFcItfType()).isFcClientItf())
        //            throw new IllegalBindingException("The provided server interface is a client interface");

        PAGCMInterfaceType clientItfType = (PAGCMInterfaceType) ((PAComponentType) owner.getFcType())
                .getAllFcInterfaceType(clientItfName);

        // TODO handle internal interfaces
        // if (server_itf_type.isFcClientItf()) {
        // throw new IllegalBindingException("cannot bind client interface " +
        // clientItfName + " to other client interface "
        // +server_itf_type.getFcItfName() );
        // }
        // if (!client_itf_type.isFcClientItf()) {
        // throw new IllegalBindingException("cannot bind client interface " +
        // clientItfName + " to other client interface "
        // +server_itf_type.getFcItfName() );
        // }
        if (!(GCM.getGCMLifeCycleController(getFcItfOwner())).getFcState()
                .equals(LifeCycleController.STOPPED)) {
            throw new IllegalLifeCycleException("component has to be stopped to perform binding operations");
        }

        // multicast interfaces : interfaces must be compatible
        // (rem : itf is null when it is a single itf not yet bound
        if (Utils.isGCMMulticastItf(clientItfName, getFcItfOwner())) {
            GCM.getMulticastController(owner).ensureGCMCompatibility(clientItfType, serverItf);

            // ensure multicast interface of primitive component is initialized
            if (isPrimitive()) {
                BindingController userBindingController = (BindingController) (owner)
                        .getReferenceOnBaseObject();

                if ((userBindingController.lookupFc(clientItfName) == null) ||
                    !(PAGroup.isGroup(userBindingController.lookupFc(clientItfName)))) {
                    userBindingController.bindFc(clientItfName, owner.getFcInterface(clientItfName));
                }
            }
        }

        if (Utils.isGCMGathercastItf(serverItf)) {
            GCM.getGathercastController(owner).ensureGCMCompatibility(clientItfType, serverItf);
        }
        //  TODO type checkings for other cardinalities
        else if (Utils.isGCMSingletonItf(clientItfName, getFcItfOwner())) {
            InterfaceType sType = (InterfaceType) serverItf.getFcItfType();

            //InterfaceType cType = (InterfaceType)((PAInterface)owner.getFcInterface(clientItfName)).getFcItfType();
            InterfaceType cType = ((PAComponentType) owner.getFcType()).getAllFcInterfaceType(clientItfName);

            try {
                Class<?> s = Class.forName(sType.getFcItfSignature());
                Class<?> c = Class.forName(cType.getFcItfSignature());
                if (!c.isAssignableFrom(s)) {
                    throw new IllegalBindingException("The server interface type " + s.getName() +
                        " is not a subtype of the client interface type " + c.getName());
                }
            } catch (ClassNotFoundException e) {
                IllegalBindingException ibe = new IllegalBindingException("Cannot find type of interface : " +
                    e.getMessage());
                ibe.initCause(e);
                throw ibe;
            }
        }

        // check for binding primitive component can only be performed in the
        // primitive component
        if (!isPrimitive()) {
            // removed the following checkings as they did not consider composite server itfs
            //            checkClientInterfaceName(clientItfName);
            if (existsBinding(clientItfName)) {
                if (!((PAGCMInterfaceTypeImpl) ((Interface) getFcItfOwner().getFcInterface(clientItfName))
                        .getFcItfType()).isFcCollectionItf()) {
                    // binding from a single client interface : only 1 binding
                    // is allowed
                    controllerLogger.warn(GCM.getNameController(getFcItfOwner()).getFcName() + "." +
                        clientItfName + " is already bound");

                    throw new IllegalBindingException(clientItfName + " is already bound");
                } else {
                    // binding from a collection interface
                    if (((InterfaceType) serverItf.getFcItfType()).isFcClientItf()) {
                        // binding to a client(external) interface --> not OK
                        throw new IllegalBindingException(serverItf.getFcItfName() +
                            " is not a server interface");
                    }
                }
            }
        }

        // TODO : check bindings between external client interfaces
        // see next, but need to consider internal interfaces (i.e. valid if
        // server AND internal)
        // TODO : check bindings crossing composite membranes
    }

    protected void checkUnbindability(String clientItfName) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        checkLifeCycleIsStopped();
        checkClientInterfaceName(clientItfName);

        if (((ComponentType) owner.getFcType()).getFcInterfaceType(clientItfName).isFcCollectionItf()) {
            throw new IllegalBindingException(
                "In this implementation, for coherency reasons, it is not possible to unbind members of a collection interface");
        }
    }

    /**
     *
     * @param clientItfName
     *            the name of the client interface
     * @return a Binding object if single binding, List of Binding objects
     *         otherwise
     */
    public Object removeBinding(String clientItfName) {
        return bindings.remove(clientItfName);
    }

    /**
     *
     * @param clientItfName
     *            the name of the client interface
     * @return a Binding object if single binding, List of Binding objects
     *         otherwise
     */
    public Object getBinding(String clientItfName) {
        return bindings.get(clientItfName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object lookupFc(String clientItfName) throws NoSuchInterfaceException {
        Object itf = null;
        if (isPrimitive()) {
            itf = ((BindingController) ((PAComponent) getFcItfOwner()).getReferenceOnBaseObject())
                    .lookupFc(clientItfName);
        } else if (existsBinding(clientItfName)) {
            itf = ((Binding) getBinding(clientItfName)).getServerInterface();
        }
        if (itf == null) {
            //check if the interface name exist, if not we must throw a NoSuchInterfaceException
            checkClientInterfaceExist(clientItfName);
        }
        return itf;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindFc(String clientItfName, Object serverItf) throws NoSuchInterfaceException,
            IllegalBindingException, IllegalLifeCycleException {
        // get value of (eventual) future before casting
        serverItf = PAFuture.getFutureValue(serverItf);

        PAInterface sItf = null;
        if (serverItf instanceof PAInterface) {
            sItf = (PAInterface) serverItf;

            //        if (controllerLogger.isDebugEnabled()) {
            //            String serverComponentName;
            //
            //            if (PAGroup.isGroup(serverItf)) {
            //                serverComponentName = "a group of components ";
            //            } else {
            //                serverComponentName = GCM.getNameController((sItf).getFcItfOwner()).getFcName();
            //            }
            //
            //            controllerLogger.debug("binding " + GCM.getNameController(getFcItfOwner()).getFcName() + "." +
            //                clientItfName + " to " + serverComponentName + "." + (sItf).getFcItfName());
            //        }

            checkBindability(clientItfName, (Interface) serverItf);

            ((ItfStubObject) serverItf).setSenderItfID(new ItfID(clientItfName,
                ((PAComponent) getFcItfOwner()).getID()));
        }
        // binding on a web service
        else if (serverItf instanceof WSInfo) {
            PAGCMInterfaceType serverItfType = null;
            try {
                serverItfType = new PAGCMInterfaceTypeImpl(clientItfName, ((ComponentType) owner.getFcType())
                        .getFcInterfaceType(clientItfName).getFcItfSignature(), TypeFactory.SERVER,
                    TypeFactory.MANDATORY, GCMTypeFactory.SINGLETON_CARDINALITY);
            } catch (InstantiationException e) {
                // should never append
                controllerLogger.error("could not generate ProActive interface type for " + clientItfName +
                    ": " + e.getMessage());
                IllegalBindingException ibe = new IllegalBindingException(
                    "could not generate ProActive interface type for " + clientItfName + ": " +
                        e.getMessage());
                ibe.initCause(e);
                throw ibe;
            }
            try {
                // generate a proxy implementing the Java client interface and calling the web service
                sItf = WSProxyClassGenerator.instance().generateFunctionalInterface(
                        serverItfType.getFcItfName(), new WSComponent((WSInfo) serverItf), serverItfType);
            } catch (InterfaceGenerationFailedException e) {
                controllerLogger.error("could not generate web service proxy for client interface " +
                    clientItfName + ": " + e.getMessage());
                IllegalBindingException ibe = new IllegalBindingException(
                    "could not generate web service proxy for client interface " + clientItfName + ": " +
                        e.getMessage());
                ibe.initCause(e);
                throw ibe;
            }
        }
        // binding on a web service
        else if (serverItf instanceof String) {
            bindFc(clientItfName, new WSInfo((String) serverItf));
            return;
        }

        // checks binding from internal client interface of composite component to server interface of subcomponent
        if (isComposite() && !Utils.isGCMClientItf(clientItfName, owner)) {
            Component sComponent = sItf.getFcItfOwner();
            Component[] subComponents = GCM.getContentController(owner).getFcSubComponents();
            boolean isSubComponent = false;
            for (Component subComponent : subComponents) {
                if (subComponent.equals(sComponent)) {
                    isSubComponent = true;
                    break;
                }
            }
            if (!isSubComponent) {
                throw new IllegalBindingException(
                    "could not bind internal client interface " +
                        clientItfName +
                        " on server interface " +
                        sItf.getFcItfName() +
                        " : the component owner of the server interface is not a subcomponent of the component owner of the internal client interface.");
            }
        }

        // If output interceptors may be defined (ie the component has an interceptor controller)
        // TODO check with groups : interception is here done at the beginning of the group invocation,
        // not for each element of the group.
        try {
            PAInterceptorControllerImpl interceptorController = (PAInterceptorControllerImpl) ((PAInterface) Utils
                    .getPAInterceptorController(owner)).getFcItfImpl();

            try {
                // replace server itf with an interface of the same type+same proxy, but with interception code
                sItf = OutputInterceptorClassGenerator.instance().generateInterface(sItf,
                        interceptorController, clientItfName);
            } catch (InterfaceGenerationFailedException e) {
                controllerLogger.error("could not generate output interceptor for client interface " +
                    clientItfName + " : " + e.getMessage());

                IllegalBindingException ibe = new IllegalBindingException(
                    "could not generate output interceptor for client interface " + clientItfName + " : " +
                        e.getMessage());
                ibe.initCause(e);
                throw ibe;
            }
        } catch (NoSuchInterfaceException nsie) {
            // No PAInterceptorController, nothing to do
        }

        // Multicast bindings are handled here
        if (Utils.isGCMMulticastItf(clientItfName, owner)) {
            ((PAMulticastControllerImpl) ((PAInterface) GCM.getMulticastController(owner)).getFcItfImpl())
                    .bindFc(clientItfName, sItf);
            if (Utils.isGCMGathercastItf(sItf)) {
                // add a callback ref in the server gather interface
                // TODO should throw a binding event
                try {
                    if (Utils.getPAMembraneController((sItf).getFcItfOwner()).getMembraneState().equals(
                            PAMembraneController.MEMBRANE_STOPPED)) {
                        throw new IllegalLifeCycleException(
                            "The membrane of the owner of the server interface should be started");
                    }
                } catch (NoSuchInterfaceException e) {
                    //If the component doesn't have a PAMembraneController, it won't have any impact on the rest of the method.
                }
                GCM.getGathercastController((sItf).getFcItfOwner()).notifyAddedGCMBinding(
                        sItf.getFcItfName(), (owner).getRepresentativeOnThis(), clientItfName);
            }
            return;
        }

        if (isPrimitive()) {
            checkClientInterfaceExist(clientItfName);
            // binding operation is delegated
            if (Utils.isGCMGathercastItf(sItf)) {
                primitiveBindFc(clientItfName, getGathercastAdaptor(clientItfName, serverItf, sItf));
                // add a callback ref in the server gather interface
                // TODO should throw a binding event
                try {
                    if (Utils.getPAMembraneController((sItf).getFcItfOwner()).getMembraneState().equals(
                            PAMembraneController.MEMBRANE_STOPPED)) {
                        throw new IllegalLifeCycleException(
                            "The membrane of the owner of the server interface should be started");
                    }
                } catch (NoSuchInterfaceException e) {
                    //If the component doesn't have a PAMembraneController, it won't have any impact on the rest of the method.
                }
                GCM.getGathercastController((sItf).getFcItfOwner()).notifyAddedGCMBinding(
                        sItf.getFcItfName(), (owner).getRepresentativeOnThis(), clientItfName);
            } else {
                primitiveBindFc(clientItfName, sItf);
            }
            return;
        }

        InterfaceType client_itf_type;

        client_itf_type = ((ComponentType) owner.getFcType()).getFcInterfaceType(clientItfName);

        if (isComposite()) {
            if (Utils.isGCMGathercastItf(sItf)) {
                compositeBindFc(clientItfName, client_itf_type, getGathercastAdaptor(clientItfName,
                        serverItf, sItf));
                // add a callback ref in the server gather interface
                // TODO should throw a binding event
                try {
                    if (Utils.getPAMembraneController((sItf).getFcItfOwner()).getMembraneState().equals(
                            PAMembraneController.MEMBRANE_STOPPED)) {
                        throw new IllegalLifeCycleException(
                            "The membrane of the owner of the server interface should be started");
                    }
                } catch (NoSuchInterfaceException e) {
                    //If the component doesn't have a PAMembraneController, it won't have any impact on the rest of the method.
                }
                GCM.getGathercastController(sItf.getFcItfOwner()).notifyAddedGCMBinding(sItf.getFcItfName(),
                        owner.getRepresentativeOnThis(), clientItfName);
            } else {
                compositeBindFc(clientItfName, client_itf_type, sItf);
            }
        }
    }

    private PAInterface getGathercastAdaptor(String clientItfName, Object serverItf, PAInterface sItf) {
        if (!Proxy.isProxyClass(sItf.getClass())) {
            // add an adaptor proxy for matching interface types
            Class<?> clientItfClass = null;
            try {
                InterfaceType[] cItfTypes = ((ComponentType) owner.getFcType()).getFcInterfaceTypes();
                for (int i = 0; i < cItfTypes.length; i++) {
                    if (clientItfName.equals(cItfTypes[i].getFcItfName()) ||
                        (cItfTypes[i].isFcCollectionItf() && clientItfName.startsWith(cItfTypes[i]
                                .getFcItfName()))) {
                        clientItfClass = Class.forName(cItfTypes[i].getFcItfSignature());
                    }
                }
                if (clientItfClass == null) {
                    throw new ProActiveRuntimeException("could not find type of client interface " +
                        clientItfName);
                }
            } catch (ClassNotFoundException e) {
                throw new ProActiveRuntimeException(
                    "cannot find client interface class for client interface : " + clientItfName);
            }
            PAInterface itfProxy = (PAInterface) Proxy.newProxyInstance(Thread.currentThread()
                    .getContextClassLoader(), new Class<?>[] { PAInterface.class, ItfStubObject.class,
                    clientItfClass }, new GatherItfAdapterProxy(serverItf));
            return itfProxy;
        } else {
            return sItf;
        }
    }

    private void primitiveBindFc(String clientItfName, PAInterface serverItf)
            throws NoSuchInterfaceException, IllegalBindingException, IllegalLifeCycleException {
        // delegate binding operation to the reified object
        BindingController user_binding_controller = (BindingController) ((PAComponent) getFcItfOwner())
                .getReferenceOnBaseObject();

        // serverItf cannot be a Future (because it has to be casted) => make
        // sure if binding to a composite's internal interface
        serverItf = PAFuture.getFutureValue(serverItf);
        //if not already bound
        if (user_binding_controller.lookupFc(clientItfName) == null ||
            ((GCMInterfaceType) ((Interface) user_binding_controller.lookupFc(clientItfName)).getFcItfType())
                    .isGCMMulticastItf()) {
            user_binding_controller.bindFc(clientItfName, serverItf);
            //        addBinding(new Binding(clientItf, clientItfName, serverItf));
        } else {
            throw new IllegalBindingException("The client interface '" + serverItf + "' is already bound!");
        }
    }

    /*
     * binding method enforcing Interface type for the server interface, for composite components
     */
    private void compositeBindFc(String clientItfName, InterfaceType clientItfType, Interface serverItf)
            throws NoSuchInterfaceException {
        PAInterface clientItf = null;
        clientItf = (PAInterface) getFcItfOwner().getFcInterface(clientItfName);
        // TODO remove this as we should now use multicast interfaces for this purpose
        // if we have a collection interface, the impl object is actually a
        // group of references to interfaces
        // Thus we have to add the link to the new interface in this group
        if (clientItfType.getFcItfName().equals(clientItfName)) {
            // single binding
            clientItf.setFcItfImpl(serverItf);
        } else {
            if (((ComponentType) owner.getFcType()).getFcInterfaceType(clientItfName).isFcCollectionItf()) {
                clientItf.setFcItfImpl(serverItf);
            } else {
                throw new NoSuchInterfaceException("Cannot bind interface " + clientItfName +
                    " because it does not correspond to the specified type");
            }
        }
        addBinding(new Binding(clientItf, clientItfName, serverItf));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*
     * CAREFUL : unbinding action on collective interfaces will remove all the bindings to this
     * interface.
     */
    public void unbindFc(String clientItfName) throws NoSuchInterfaceException, IllegalBindingException,
            IllegalLifeCycleException {

        if (!existsBinding(clientItfName)) {
            throw new IllegalBindingException(clientItfName + " is not yet bound");
        }

        // remove from bindings and set impl object to null
        if (isPrimitive()) {
            checkLifeCycleIsStopped();
            // delegate to primitive component
            BindingController user_binding_controller = (BindingController) ((PAComponent) getFcItfOwner())
                    .getReferenceOnBaseObject();
            PAInterface sItf = (PAInterface) user_binding_controller.lookupFc(clientItfName);
            if ((sItf != null) && Utils.isGCMGathercastItf(sItf.getFcItfName(), sItf.getFcItfOwner())) {
                try {
                    if (Utils.getPAMembraneController((sItf).getFcItfOwner()).getMembraneState().equals(
                            PAMembraneController.MEMBRANE_STOPPED)) {
                        throw new IllegalLifeCycleException(
                            "The client interface is bound to a component that has its membrane in a stopped state. It should be strated, as this method could interact with its controllers.");
                    }
                } catch (NoSuchInterfaceException e) {
                    //If the component doesn't have a PAMembraneController, it won't have any impact on the rest of the method.
                }

                GCM.getGathercastController(sItf.getFcItfOwner()).notifyRemovedGCMBinding(
                        sItf.getFcItfName(), owner.getRepresentativeOnThis(), clientItfName);
            }
            user_binding_controller.unbindFc(clientItfName);
        } else {
            checkUnbindability(clientItfName);
        }
        removeBinding(clientItfName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public String[] listFc() {
        if (isPrimitive()) {
            return ((BindingController) ((PAComponent) getFcItfOwner()).getReferenceOnBaseObject()).listFc();
        }

        InterfaceType[] itfs_types = ((ComponentType) getFcItfOwner().getFcType()).getFcInterfaceTypes();
        List<String> client_itfs_names = new ArrayList<String>();

        for (int i = 0; i < itfs_types.length; i++) {
            if (itfs_types[i].isFcClientItf()) {
                if (itfs_types[i].isFcCollectionItf()) {
                    List<Interface> collection_itfs = (List<Interface>) bindings.get(itfs_types[i]
                            .getFcItfName());

                    if (collection_itfs != null) {
                        Iterator<Interface> it = collection_itfs.iterator();

                        while (it.hasNext()) {
                            client_itfs_names.add((it.next()).getFcItfName());
                        }
                    }
                } else {
                    client_itfs_names.add(itfs_types[i].getFcItfName());
                }
            }
        }

        return client_itfs_names.toArray(new String[client_itfs_names.size()]);
    }

    protected boolean existsBinding(String clientItfName) throws NoSuchInterfaceException {
        if (isPrimitive() &&
            !(((GCMInterfaceType) ((PAComponentType) owner.getFcType()).getAllFcInterfaceType(clientItfName))
                    .isGCMMulticastItf())) {
            return (((BindingController) ((PAComponent) getFcItfOwner()).getReferenceOnBaseObject())
                    .lookupFc(clientItfName) != null);
        } else {
            return bindings.containsBindingOn(clientItfName);
        }
    }

    protected void checkClientInterfaceName(String clientItfName) throws NoSuchInterfaceException {
        if (Utils.isGCMSingletonItf(clientItfName, owner)) {
            return;
        }

        if (Utils.isGCMCollectionItf(clientItfName, owner)) {
            return;
        }

        if (Utils.isGCMMulticastItf(clientItfName, owner)) {
            return;
        }

        throw new NoSuchInterfaceException(clientItfName +
            " does not correspond to a single nor a collective interface");
    }

    /**
     * Throws a NoSuchInterfaceException exception if there is no client interface with this name
     * defined in the owner component type. It's applicable only on primitive.
     *
     * @throws NoSuchInterfaceException
     */
    private void checkClientInterfaceExist(String clientItfName) throws NoSuchInterfaceException {
        InterfaceType[] itfTypes = ((PAComponentType) getFcItfOwner().getFcType()).getAllFcInterfaceTypes();
        for (InterfaceType interfaceType : itfTypes) {
            if (clientItfName.startsWith(interfaceType.getFcItfName())) {
                return;
            }
        }

        throw new NoSuchInterfaceException(clientItfName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isBound() {
        String[] client_itf_names = listFc();

        for (int i = 0; i < client_itf_names.length; i++) {
            try {
                if (existsBinding(client_itf_names[i])) {
                    return true;
                }
            } catch (NoSuchInterfaceException logged) {
                controllerLogger.error("cannot find interface " + client_itf_names[i] + " : " +
                    logged.getMessage());
            }
        }

        return Boolean.valueOf(false);
    }

    public Bindings getBindings() {
        return bindings;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void duplicateController(Object c) {
        if (c instanceof Bindings) {
            bindings = (Bindings) c;
        } else {
            throw new ProActiveRuntimeException(
                "PABindingControllerImpl: Impossible to duplicate the controller " + this +
                    " from the controller" + c);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ControllerState getState() {
        return new ControllerState(bindings);
    }

    private Interface[] filterServerItfs(Object[] itfs) {
        ArrayList<Interface> newListItfs = new ArrayList<Interface>();
        for (int i = 0; i < itfs.length; i++) {
            Interface curItf = (Interface) itfs[i];
            if (!((GCMInterfaceType) curItf.getFcItfType()).isFcClientItf())
                newListItfs.add(curItf);
        }
        return newListItfs.toArray(new Interface[] {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean isBoundTo(Component component) {
        Object[] serverItfsComponent = filterServerItfs(component.getFcInterfaces());
        Object[] itfs = getFcItfOwner().getFcInterfaces();
        for (int i = 0; i < itfs.length; i++) {
            Interface curItf = (Interface) itfs[i];
            if (!((GCMInterfaceType) curItf.getFcItfType()).isGCMMulticastItf()) {
                for (int j = 0; j < serverItfsComponent.length; j++) {
                    Interface curServerItf = (Interface) serverItfsComponent[j];
                    Binding binding = (Binding) getBinding(curItf.getFcItfName());
                    if ((binding != null) &&
                        binding.getServerInterface().getFcItfOwner().equals(curServerItf.getFcItfOwner()) &&
                        binding.getServerInterface().getFcItfType().equals(curServerItf.getFcItfType())) {
                        return Boolean.valueOf(true);
                    }
                }
            } else {
                try {
                    PAMulticastController mc = Utils.getPAMulticastController(getFcItfOwner());
                    if (mc.isBoundTo(curItf.getFcItfName(), serverItfsComponent))
                        return Boolean.valueOf(true);
                } catch (NoSuchInterfaceException e) {
                    // TODO: handle exception
                }
            }
        }
        return Boolean.valueOf(false);
    }
}
