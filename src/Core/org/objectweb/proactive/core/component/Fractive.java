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
package org.objectweb.proactive.core.component;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.AttributeController;
import org.objectweb.fractal.api.factory.Factory;
import org.objectweb.fractal.api.factory.GenericFactory;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.component.body.ComponentBody;
import org.objectweb.proactive.core.component.factory.PAGenericFactory;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentative;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.type.Composite;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.utils.NamedThreadFactory;


/**
 * This class is used for creating components. It acts as :
 * <ol>
 * <li> a bootstrap component</li>
 * <li> a specialized GenericFactory for instantiating new components on remote nodes ({@link PAGenericFactory})</li>
 * <li> a utility class providing static methods to create collective interfaces</li>
 * </ol>
 *
 * @author The ProActive Team
 */
@PublicAPI
public class Fractive implements PAGenericFactory, Component, Factory {
    private GCMTypeFactory typeFactory = PAGCMTypeFactoryImpl.instance();
    private Type type = null;
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    /**
     * no-arg constructor (used by Fractal to get a bootstrap component)
     */
    public Fractive() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component newFcInstance() throws InstantiationException {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component newFcInstance(Type type, Object controllerDesc, Object contentDesc)
            throws InstantiationException {
        try {
            return newFcInstance(type, (ControllerDescription) controllerDesc,
                    (ContentDescription) contentDesc);
        } catch (ClassCastException e) {
            if ((type == null) && (controllerDesc == null) && (contentDesc instanceof Map)) {
                // for compatibility with the new
                // org.objectweb.fractal.util.Fractal class
                return this;
            }
            if ((controllerDesc instanceof ControllerDescription) &&
                ((contentDesc instanceof String) || (contentDesc == null))) {
                // for the ADL, when only type and ControllerDescription are
                // given
                return newFcInstance(type, controllerDesc, (contentDesc == null) ? null
                        : new ContentDescription((String) contentDesc));
            }

            // code compatibility with Julia
            if ("composite".equals(controllerDesc)) {
                try {
                    if (contentDesc == null) {
                        return newFcInstance(type, new ControllerDescription(null, Constants.COMPOSITE), null);
                    } else if ((contentDesc instanceof String) &&
                        (AttributeController.class.isAssignableFrom(Class.forName((String) contentDesc)))) {
                        return newFcInstance(type, new ControllerDescription(null, Constants.COMPOSITE),
                                new ContentDescription((String) contentDesc));
                    }
                } catch (ClassNotFoundException cnfe) {
                    InstantiationException ie = new InstantiationException("cannot find classe " +
                        contentDesc + " : " + cnfe.getMessage());
                    ie.initCause(cnfe);
                    throw ie;
                }

            }
            if ("primitive".equals(controllerDesc) && (contentDesc instanceof String)) {
                return newFcInstance(type, new ControllerDescription(null, Constants.PRIMITIVE),
                        new ContentDescription((String) contentDesc));
            }

            // any other case
            throw new InstantiationException(
                "With this implementation, parameters must be of respective types : " + Type.class.getName() +
                    ',' + ControllerDescription.class.getName() + ',' + ContentDescription.class.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component newNfFcInstance(Type type, Object controllerDesc, Object contentDesc)
            throws InstantiationException {
        try {
            return newNfFcInstance(type, (ControllerDescription) controllerDesc,
                    (ContentDescription) contentDesc);
        } catch (ClassCastException e) {
            if ((type == null) && (controllerDesc == null) && (contentDesc instanceof Map)) {
                // for compatibility with the new
                // org.objectweb.fractal.util.Fractal class
                return this;
            }
            if ((controllerDesc instanceof ControllerDescription) &&
                ((contentDesc instanceof String) || (contentDesc == null))) {
                // for the ADL, when only type and ControllerDescription are
                // given
                return newNfFcInstance(type, controllerDesc, (contentDesc == null) ? null
                        : new ContentDescription((String) contentDesc));
            }

            // code compatibility with Julia
            if ("composite".equals(controllerDesc)) {
                try {
                    if (contentDesc == null) {
                        return newFcInstance(type, new ControllerDescription(null, Constants.COMPOSITE), null);
                    } else if ((contentDesc instanceof String) &&
                        (AttributeController.class.isAssignableFrom(Class.forName((String) contentDesc)))) {
                        return newFcInstance(type, new ControllerDescription(null, Constants.COMPOSITE),
                                new ContentDescription((String) contentDesc));
                    }
                } catch (ClassNotFoundException cnfe) {
                    InstantiationException ie = new InstantiationException("cannot find classe " +
                        contentDesc + " : " + cnfe.getMessage());
                    ie.initCause(cnfe);
                    throw ie;
                }

            }
            if ("primitive".equals(controllerDesc) && (contentDesc instanceof String)) {
                return newNfFcInstance(type, new ControllerDescription(null, Constants.PRIMITIVE),
                        new ContentDescription((String) contentDesc));
            }

            // any other case
            throw new InstantiationException(
                "With this implementation, parameters must be of respective types : " + Type.class.getName() +
                    ',' + ControllerDescription.class.getName() + ',' + ContentDescription.class.getName());
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component[] newFcInstanceInParallel(final Type type, final Object controllerDesc,
            final Object contentDesc, int nbComponents) throws InstantiationException {
        return newFcInstanceInParallel(type, controllerDesc, contentDesc, nbComponents, null, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component[] newNfFcInstanceInParallel(final Type type, final Object controllerDesc,
            final Object contentDesc, int nbComponents) throws InstantiationException {
        return newFcInstanceInParallel(type, controllerDesc, contentDesc, nbComponents, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component newFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc) throws InstantiationException {
        return newFcInstance(type, controllerDesc, contentDesc, (Node) null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component newNfFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc) throws InstantiationException {
        return newNfFcInstance(type, controllerDesc, contentDesc, (Node) null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component[] newFcInstanceInParallel(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, int nbComponents) throws InstantiationException {
        return newFcInstanceInParallel(type, controllerDesc, contentDesc, nbComponents, (Node[]) null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component[] newNfFcInstanceInParallel(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, int nbComponents) throws InstantiationException {
        return newNfFcInstanceInParallel(type, controllerDesc, contentDesc, nbComponents, (Node[]) null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component newFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node node) throws InstantiationException {
        try {
            ActiveObjectWithComponentParameters container = commonInstanciation(type, controllerDesc,
                    contentDesc, node);
            return fComponent(type, container);
        } catch (ActiveObjectCreationException e) {
            InstantiationException ie = new InstantiationException(e.getMessage());
            ie.initCause(e);
            throw ie;
        } catch (NodeException e) {
            InstantiationException ie = new InstantiationException(e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component newNfFcInstance(Type type, ControllerDescription controllerDesc,
            ContentDescription contentDesc, Node node) throws InstantiationException {
        try {
            ActiveObjectWithComponentParameters container = commonInstanciation(type, controllerDesc,
                    contentDesc, node);
            return nfComponent(type, container);
        } catch (ActiveObjectCreationException e) {
            InstantiationException ie = new InstantiationException(e.getMessage());
            ie.initCause(e);
            throw ie;
        } catch (NodeException e) {
            InstantiationException ie = new InstantiationException(e.getMessage());
            ie.initCause(e);
            throw ie;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component[] newFcInstanceInParallel(final Type type, final ControllerDescription controllerDesc,
            final ContentDescription contentDesc, int nbComponents, final Node[] nodes)
            throws InstantiationException {
        return newFcInstanceInParallel(type, controllerDesc, contentDesc, nbComponents, nodes, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component[] newNfFcInstanceInParallel(final Type type, final ControllerDescription controllerDesc,
            final ContentDescription contentDesc, int nbComponents, final Node[] nodes)
            throws InstantiationException {
        return newFcInstanceInParallel(type, controllerDesc, contentDesc, nbComponents, nodes, false);
    }

    private Component[] newFcInstanceInParallel(final Type type, final Object controllerDesc,
            final Object contentDesc, int nbComponents, final Node[] nodes, final boolean isFunctional)
            throws InstantiationException {
        ThreadFactory threadFactory = new NamedThreadFactory("Fractive thread pool");
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime()
                .availableProcessors(), threadFactory);
        List<Future<Component>> futures = new ArrayList<Future<Component>>();

        for (int i = 0; i < nbComponents; i++) {
            final Node node = (nodes == null) ? null : nodes[i];

            futures.add(executorService.submit(new Callable<Component>() {
                @Override
                public Component call() throws Exception {
                    if (node != null) {
                        if (isFunctional) {
                            return newFcInstance(type, (ControllerDescription) controllerDesc,
                                    (ContentDescription) contentDesc, node);
                        } else {
                            return newNfFcInstance(type, (ControllerDescription) controllerDesc,
                                    (ContentDescription) contentDesc, node);
                        }
                    } else {
                        if (isFunctional) {
                            return newFcInstance(type, (ControllerDescription) controllerDesc,
                                    (ContentDescription) contentDesc);
                        } else {
                            return newNfFcInstance(type, controllerDesc, contentDesc);
                        }
                    }
                }
            }));
        }

        Component[] components = new Component[nbComponents];

        for (int i = 0; i < nbComponents; i++) {
            try {
                components[i] = futures.get(i).get();
            } catch (InterruptedException e) {
                throw new InstantiationException(e.getMessage());
            } catch (ExecutionException e) {
                throw new InstantiationException(e.getMessage());
            }
        }

        return components;
    }

    /*
     * @see org.objectweb.fractal.api.Component#getFcInterface(java.lang.String)
     */
    public Object getFcInterface(String itfName) throws NoSuchInterfaceException {
        if (Constants.GENERIC_FACTORY.equals(itfName)) {
            return this;
        } else if (Constants.TYPE_FACTORY.equals(itfName)) {
            return typeFactory;
        } else {
            throw new NoSuchInterfaceException(itfName);
        }
    }

    /*
     * @see org.objectweb.fractal.api.Component#getFcInterfaces()
     */
    public Object[] getFcInterfaces() {
        return null;
    }

    /*
     * @see org.objectweb.fractal.api.Component#getFcType()
     */
    public Type getFcType() {
        if (type == null) {
            try {
                return type = typeFactory.createFcType(new InterfaceType[] {
                        typeFactory.createFcItfType(Constants.GENERIC_FACTORY,
                                GenericFactory.class.getName(), false, false, false),
                        typeFactory.createFcItfType(Constants.TYPE_FACTORY, TypeFactory.class.getName(),
                                false, false, false) });
            } catch (InstantiationException e) {
                logger.error(e.getMessage());
                return null;
            }
        } else {
            return type;
        }
    }

    /*
     * @see org.objectweb.fractal.api.factory.Factory#getFcContentDesc()
     */
    public Object getFcContentDesc() {
        return null;
    }

    /*
     * @see org.objectweb.fractal.api.factory.Factory#getFcControllerDesc()
     */
    public Object getFcControllerDesc() {
        return null;
    }

    /*
     * @see org.objectweb.fractal.api.factory.Factory#getFcInstanceType()
     */
    public Type getFcInstanceType() {
        return null;
    }

    /**
     * Returns a component representative pointing to the component associated to the component whose active
     * thread is calling this method. It can be used for a component to pass callback references to itself.
     *
     * @return Component representative for the component in which the current thread is running.
     */
    public static Component getComponentRepresentativeOnThis() {
        ComponentBody componentBody;
        try {
            componentBody = (ComponentBody) PAActiveObject.getBodyOnThis();
        } catch (ClassCastException e) {
            logger
                    .error("Cannot get a component representative from the current object, because this object is not a component");
            return null;
        }
        PAComponent currentComponent = componentBody.getPAComponentImpl();
        return currentComponent.getRepresentativeOnThis();
    }

    /**
     * Registers a reference on a component with a name.
     *
     * @param ref a reference on a component (it should be an instance of
     *            PAComponentRepresentative).
     * @param name the name of the component.
     * @return The URI at which the component is bound.
     * @throws ProActiveException if the component cannot be registered.
     */
    public static String registerByName(Component ref, String name) throws ProActiveException {
        if (!(ref instanceof PAComponentRepresentative)) {
            throw new IllegalArgumentException("This method can only register ProActive components");
        }
        return PAActiveObject.registerByName(ref, name);
    }

    /**
     * Unregisters a component previously registered into a registry.
     * 
     * @param url the url under which the component is registered.
     * @throws IOException if the remote component cannot be removed from the registry.
     */
    public static void unregister(String url) throws IOException {
        PAActiveObject.unregister(url);
    }

    /**
     * Returns a reference on a component (a component representative) for the
     * component associated with the specified name.<br>
     *
     * @param url the registered location of the component.
     * @return a reference on the component corresponding to the given name.
     * @throws IOException if there is a communication problem with the registry.
     * @throws NamingException if a reference on a component could not be found at the
     *             specified URL.
     */
    public static PAComponentRepresentative lookup(String url) throws IOException, NamingException {
        UniversalBody b = null;
        RemoteObject<?> rmo;
        URI uri = RemoteObjectHelper.expandURI(URI.create(url));

        try {
            rmo = RemoteObjectHelper.lookup(uri);

            b = (UniversalBody) RemoteObjectHelper.generatedObjectStub(rmo);

            StubObject stub = (StubObject) MOP.createStubObject(PAComponentRepresentative.class.getName(), b);

            return PAComponentRepresentativeFactory.instance().createComponentRepresentative(stub.getProxy());
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("Could not perform lookup for component at URL: " + url +
                ", because construction of component representative failed." + t.toString());
            throw new NamingException("Could not perform lookup for component at URL: " + url +
                ", because construction of component representative failed.");
        }
    }

    /**
     * Common instantiation method called during creation both functional and non functional components
     * @param type
     * @param controllerDesc
     * @param contentDesc
     * @param node
     * @return A container object, containing objects for the generation of the component representative
     * @throws InstantiationException
     * @throws ActiveObjectCreationException
     * @throws NodeException
     */
    private ActiveObjectWithComponentParameters commonInstanciation(Type type,
            ControllerDescription controllerDesc, ContentDescription contentDesc, Node node)
            throws InstantiationException, ActiveObjectCreationException, NodeException {
        ComponentType cType = null;
        // type must be a component type
        if (!(type instanceof ComponentType)) {
            throw new InstantiationException("Argument type must be an instance of ComponentType");
        } else {
            cType = (ComponentType) type;
        }

        if (contentDesc == null) {
            // a composite component, no activity/factory/node specified
            if (Constants.COMPOSITE.equals(controllerDesc.getHierarchicalType())) {
                contentDesc = new ContentDescription(Composite.class.getName());
            } else {
                throw new InstantiationException(
                    "Content can be null only if the hierarchical type of the component is composite");
            }
        } else {
            Class<?> contentClass;
            try {
                contentClass = Class.forName(contentDesc.getClassName());
            } catch (ClassNotFoundException e) {
                InstantiationException ie = new InstantiationException(
                    "Cannot find interface defined in component content : " + e.getMessage());
                ie.initCause(e);
                throw ie;
            }

            if (Constants.COMPOSITE.equals(controllerDesc.getHierarchicalType())) {
                if ((!contentClass.equals(Composite.class)) &&
                    (!AttributeController.class.isAssignableFrom(contentClass))) {
                    throw new InstantiationException(
                        "Content can be not null for composite component only if it extends AttributeControler");
                }
            } else if (Constants.PRIMITIVE.equals(controllerDesc.getHierarchicalType())) {
                // Check that the provided content class implements all defined interfaces (server, functional and not mandatory)
                for (InterfaceType itfType : cType.getFcInterfaceTypes()) {
                    if (!itfType.isFcClientItf() && !itfType.isFcOptionalItf() &&
                        !Utils.isControllerItfName(itfType.getFcItfName())) {
                        try {
                            if (!Class.forName(itfType.getFcItfSignature()).isAssignableFrom(contentClass)) {
                                throw new InstantiationException(
                                    "The provided content class does not implement the " +
                                        itfType.getFcItfName() + " (" + itfType.getFcItfSignature() +
                                        ") interface, cancel component creation.");
                            }
                        } catch (ClassNotFoundException e) {
                            logger.debug("Class " + itfType.getFcItfSignature() +
                                " can not be found; skip verification.", e);
                        }
                    }
                }
            }
        }
        // instantiate the component metaobject factory with parameters of
        // the component

        ComponentParameters componentParameters = new ComponentParameters((ComponentType) type,
            controllerDesc);
        if (contentDesc.getFactory() == null) {
            // first create a map with the parameters
            Map<String, Object> factory_params = new Hashtable<String, Object>(1);

            factory_params.put(ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY, componentParameters);
            if (controllerDesc.isSynchronous() &&
                (Constants.COMPOSITE.equals(controllerDesc.getHierarchicalType()))) {
                factory_params.put(ProActiveMetaObjectFactory.SYNCHRONOUS_COMPOSITE_COMPONENT_KEY,
                        Constants.SYNCHRONOUS);
            }
            contentDesc.setFactory(new ProActiveMetaObjectFactory(factory_params));
            // factory =
            // PAComponentMetaObjectFactory.newInstance(componentParameters);
        }

        // TODO : add controllers in the component metaobject factory?
        Object ao = null;

        // 3 possibilities : either the component is created on a node (or
        // null), it is created on a virtual node, or on multiple nodes
        ao = PAActiveObject.newActive(contentDesc.getClassName(), null, contentDesc
                .getConstructorParameters(), node, contentDesc.getActivity(), contentDesc.getFactory());

        return new ActiveObjectWithComponentParameters((StubObject) ao, componentParameters);
    }

    /**
     * Creates a component representative for a functional component (to be used with commonInstanciation method)
     * @param container The container containing objects for the generation of component representative
     * @return The created component
     */
    private PAComponentRepresentative fComponent(Type type, ActiveObjectWithComponentParameters container) {
        ComponentParameters componentParameters = container.getParameters();
        StubObject ao = container.getActiveObject();
        org.objectweb.proactive.core.mop.Proxy myProxy = (ao).getProxy();
        if (myProxy == null) {
            throw new ProActiveRuntimeException("Cannot find a Proxy on the stub object: " + ao);
        }
        PAComponentRepresentative representative = PAComponentRepresentativeFactory.instance()
                .createComponentRepresentative(componentParameters, myProxy);
        representative.setStubOnBaseObject(ao);
        return representative;
    }

    /**
     * Creates a component representative for a non functional component (to be used with commonInstanciation method)
     * @param container The container containing objects for the generation of component representative
     * @return The created component
     */
    private PAComponentRepresentative nfComponent(Type type, ActiveObjectWithComponentParameters container) {
        ComponentParameters componentParameters = container.getParameters();
        StubObject ao = container.getActiveObject();
        org.objectweb.proactive.core.mop.Proxy myProxy = (ao).getProxy();
        if (myProxy == null) {
            throw new ProActiveRuntimeException("Cannot find a Proxy on the stub object: " + ao);
        }
        PAComponentRepresentative representative = PAComponentRepresentativeFactory.instance()
                .createNFComponentRepresentative(componentParameters, myProxy);
        representative.setStubOnBaseObject(ao);
        return representative;
    }

    private static class ActiveObjectWithComponentParameters {
        StubObject activeObject;
        ComponentParameters parameters;

        public ActiveObjectWithComponentParameters(StubObject ao, ComponentParameters par) {
            this.activeObject = ao;
            this.parameters = par;
        }

        public StubObject getActiveObject() {
            return activeObject;
        }

        public ComponentParameters getParameters() {
            return parameters;
        }
    }
}
