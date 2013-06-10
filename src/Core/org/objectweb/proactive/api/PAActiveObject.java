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
 *  Contributor(s): ActiveEon Team - http://www.activeeon.com
 *
 * ################################################################
 * $$ACTIVEEON_CONTRIBUTOR$$
 */
package org.objectweb.proactive.api;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.benchmarks.timit.util.basic.TimItBasicManager;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.AbstractBody;
import org.objectweb.proactive.core.body.Context;
import org.objectweb.proactive.core.body.HalfBody;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.UniversalBodyRemoteObjectAdapter;
import org.objectweb.proactive.core.body.exceptions.BodyTerminatedException;
import org.objectweb.proactive.core.body.ft.internalmsg.Heartbeat;
import org.objectweb.proactive.core.body.proxy.BodyProxy;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.exceptions.IOException6;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteObjectSet.NotYetExposedException;
import org.objectweb.proactive.core.remoteobject.SynchronousProxy;
import org.objectweb.proactive.core.remoteobject.exception.UnknownProtocolException;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityConstants.EntityType;
import org.objectweb.proactive.core.util.NonFunctionalServices;
import org.objectweb.proactive.core.util.ProcessForAoCreation;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.profiling.Profiling;
import org.objectweb.proactive.ext.hpc.exchange.ExchangeManager;
import org.objectweb.proactive.ext.hpc.exchange.ExchangeableDouble;
import org.objectweb.proactive.utils.NamedThreadFactory;


/**
 * This class provides the main operations on active objects. It allows to create and terminate an
 * active object, and to get a reference on it. It also provides methods to register and lookup an
 * active object through the network. Finally, it allows to control the synchronicity related
 * behavior of an active object such as automatic continuation or immediate services.
 * 
 * @author The ProActive Team
 * @since ProActive 3.9 (December 2007)
 */
@PublicAPI
public class PAActiveObject {
    protected final static Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
    private final static Heartbeat hb = new Heartbeat();

    static {
        ProActiveConfiguration.load();
        @SuppressWarnings("unused")
        // Execute RuntimeFactory's static blocks
        Class<?> c = org.objectweb.proactive.core.runtime.RuntimeFactory.class;
    }

    //
    // -- CONSTRUCTORS -----------------------------------------------
    //
    private PAActiveObject() {
    }

    /**
     * Creates a new ActiveObject based on classname attached to a default node in the local JVM.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param constructorParameters
     *            the parameters of the constructor.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Object[] constructorParameters)
            throws ActiveObjectCreationException, NodeException {
        return newActive(classname, null, constructorParameters, (Node) null, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to a default node in the local JVM.
     * 
     * @param classn
     *            the class to instantiate as active
     * @param constructorParameters
     *            the parameters of the constructor.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the DefaultNode cannot be created
     */
    public static <T> T newActive(Class<T> clazz, Object[] constructorParameters)
            throws ActiveObjectCreationException, NodeException {
        return (T) newActive(clazz.getName(), null, constructorParameters, (Node) null, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to the node of the given URL.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param nodeURL
     *            the URL of the node where to create the active object. If null, the active object
     *            is created locally on a default node
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node URL cannot be resolved as an existing Node
     */
    public static Object newActive(String classname, Object[] constructorParameters, String nodeURL)
            throws ActiveObjectCreationException, NodeException {
        if (nodeURL == null) {
            return newActive(classname, null, constructorParameters, (Node) null, null, null);
        } else {
            return newActive(classname, null, constructorParameters, NodeFactory.getNode(nodeURL), null, null);
        }
    }

    /**
     * Creates a new ActiveObject based on classname attached to the node of the given URL.
     * 
     * @param classname
     *            the class to instantiate as active
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param nodeURL
     *            the URL of the node where to create the active object. If null, the active object
     *            is created locally on a default node
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node URL cannot be resolved as an existing Node
     */
    public static <T> T newActive(Class<T> clazz, Object[] constructorParameters, String nodeURL)
            throws ActiveObjectCreationException, NodeException {
        if (nodeURL == null) {
            return (T) newActive(clazz, null, constructorParameters, (Node) null, null, null);
        } else {
            return (T) newActive(clazz, null, constructorParameters, NodeFactory.getNode(nodeURL), null, null);
        }
    }

    /**
     * Creates a new ActiveObject based on classname attached to the given node or on a default node
     * in the local JVM if the given node is null.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param node
     *            the possibly null node where to create the active object.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Object[] constructorParameters, Node node)
            throws ActiveObjectCreationException, NodeException {
        return newActive(classname, null, constructorParameters, node, null, null);
    }

    /**
     * Creates a new ActiveObject based on class attached to the given node or on a default node
     * in the local JVM if the given node is null.
     * 
     * @param classname
     *            the class to instantiate as active
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param node
     *            the possibly null node where to create the active object.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T newActive(Class<T> clazz, Object[] constructorParameters, Node node)
            throws ActiveObjectCreationException, NodeException {
        return (T) newActive(clazz.getName(), null, constructorParameters, node, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to a default node in the local JVM.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param genericParameters
     *            parameterizing types (of class
     * @param constructorParameters
     *            the parameters of the constructor.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Class<?>[] genericParameters,
            Object[] constructorParameters) throws ActiveObjectCreationException, NodeException {
        // avoid ambiguity for method parameters types
        Node nullNode = null;
        return newActive(classname, genericParameters, constructorParameters, nullNode, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to a default node in the local JVM.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param genericParameters
     *            parameterizing types (of class
     * @param constructorParameters
     *            the parameters of the constructor.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the DefaultNode cannot be created
     */
    public static <T> T newActive(Class<T> clazz, Class<?>[] genericParameters, Object[] constructorParameters)
            throws ActiveObjectCreationException, NodeException {
        // avoid ambiguity for method parameters types
        Node nullNode = null;
        return newActive(clazz, genericParameters, constructorParameters, nullNode, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to the node of the given URL.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param genericParameters
     *            parameterizing types (of class
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param nodeURL
     *            the URL of the node where to create the active object. If null, the active object
     *            is created localy on a default node
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node URL cannot be resolved as an existing Node
     */
    public static Object newActive(String classname, Class<?>[] genericParameters,
            Object[] constructorParameters, String nodeURL) throws ActiveObjectCreationException,
            NodeException {
        if (nodeURL == null) {
            // avoid ambiguity for method parameters types
            Node nullNode = null;
            return newActive(classname, genericParameters, constructorParameters, nullNode, null, null);
        } else {
            return newActive(classname, genericParameters, constructorParameters, NodeFactory
                    .getNode(nodeURL), null, null);
        }
    }

    /**
     * Creates a new ActiveObject based on classname attached to the node of the given URL.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param genericParameters
     *            parameterizing types (of class
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param nodeURL
     *            the URL of the node where to create the active object. If null, the active object
     *            is created localy on a default node
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node URL cannot be resolved as an existing Node
     */
    public static <T> T newActive(Class<T> clazz, Class<?>[] genericParameters,
            Object[] constructorParameters, String nodeURL) throws ActiveObjectCreationException,
            NodeException {
        return (T) newActive(clazz.getName(), genericParameters, constructorParameters, nodeURL);

    }

    /**
     * Creates a new ActiveObject based on classname attached to the given node or on a default node
     * in the local JVM if the given node is null.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param genericParameters
     *            parameterizing types (of class
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param node
     *            the possibly null node where to create the active object.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Class<?>[] genericParameters,
            Object[] constructorParameters, Node node) throws ActiveObjectCreationException, NodeException {
        return newActive(classname, genericParameters, constructorParameters, node, null, null);
    }

    /**
     * Creates a new ActiveObject based on the class attached to the given node or on a default node
     * in the local JVM if the given node is null.
     * 
     * @param classname
     *            the class to instantiate as active
     * @param genericParameters
     *            parameterizing types (of class
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param node
     *            the possibly null node where to create the active object.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T newActive(Class<T> clazz, Class<?>[] genericParameters,
            Object[] constructorParameters, Node node) throws ActiveObjectCreationException, NodeException {
        return newActive(clazz, genericParameters, constructorParameters, node, null, null);
    }

    /**
     * Creates a new ActiveObject based on classname attached to the given node or on a default node
     * in the local JVM if the given node is null. The object returned is a stub class that extends
     * the target class and that is automatically generated on the fly. The Stub class reference a
     * the proxy object that reference the body of the active object. The body referenced by the
     * proxy can either be local of remote, depending or the respective location of the object
     * calling the newActive and the active object itself.
     * 
     * @param classname
     *            the name of the class to instantiate as active
     * @param genericParameters
     *            parameterizing types (of class
     * @param constructorParameters
     *            the parameters of the constructor of the object to instantiate as active. If some
     *            parameters are primitive types, the wrapper class types should be given here. null
     *            can be used to specify that no parameter are passed to the constructor.
     * @param node
     *            the possibly null node where to create the active object. If null, the active
     *            object is created localy on a default node
     * @param activity
     *            the possibly null activity object defining the different step in the activity of
     *            the object. see the definition of the activity in the javadoc of this classe for
     *            more information.
     * @param factory
     *            the possibly null meta factory giving all factories for creating the meta-objects
     *            part of the body associated to the reified object. If null the default ProActive
     *            MetaObject factory is used.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static Object newActive(String classname, Class<?>[] genericParameters,
            Object[] constructorParameters, Node node, Active activity, MetaObjectFactory factory)
            throws ActiveObjectCreationException, NodeException {

        if (factory == null) {
            factory = ProActiveMetaObjectFactory.newInstance();
            if (factory.getProActiveSecurityManager() == null) {
                factory.setProActiveSecurityManager(((AbstractBody) PAActiveObject.getBodyOnThis())
                        .getProActiveSecurityManager());
            }
        }

        MetaObjectFactory clonedFactory = factory;

        // TIMING
        // First we must create the timit manager then provide the timit
        // reductor to the MetaObjectFactory, this reductor will be used
        // in BodyImpl for the timing of a body.
        if (Profiling.TIMERS_COMPILED) {
            try {
                if (TimItBasicManager.checkNodeProperties(node)) {
                    // Because we don't want to time the TimItReductor
                    // active object and avoid StackOverflow
                    // we need to check the current activated object
                    // classname

                    // // The timit reductor will be passed to the factory
                    // // and used when a body is created
                    clonedFactory.setTimItReductor(TimItBasicManager.getInstance().createReductor());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ProActiveSecurityManager factorySM = factory.getProActiveSecurityManager();
        if (factorySM != null) {
            try {
                clonedFactory = (MetaObjectFactory) factory.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            ProActiveSecurityManager psm = clonedFactory.getProActiveSecurityManager();
            psm = psm.generateSiblingCertificate(EntityType.OBJECT, classname);
            clonedFactory.setProActiveSecurityManager(psm);
        }

        // using default proactive node
        if (node == null) {
            node = NodeFactory.getDefaultNode();
        }

        try {
            // PROACTIVE-277
            Class<?> activatedClass = Class.forName(classname);
            if (activatedClass.isMemberClass() && !Modifier.isStatic(activatedClass.getModifiers())) {
                // the activated class is an internal member class (not static, i.e. not nested top level).
                throw new ActiveObjectCreationException(
                    "Cannot create an active object from a non static member class.");
            } else {
                // create stub object
                Object stub = MOP.createStubObject(classname, genericParameters, constructorParameters, node,
                        activity, clonedFactory);
                return stub;
            }
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException(t);
        } catch (ClassNotFoundException e) {
            // class cannot be loaded
            throw new ActiveObjectCreationException(e);
        }
    }

    /**
     * Creates a new ActiveObject based on the class attached to the given node or on a default node
     * in the local JVM if the given node is null. The object returned is a stub class that extends
     * the target class and that is automatically generated on the fly. The Stub class reference a
     * the proxy object that reference the body of the active object. The body referenced by the
     * proxy can either be local of remote, depending or the respective location of the object
     * calling the newActive and the active object itself.
     * 
     * @param classname
     *            the class to instantiate as active
     * @param genericParameters
     *            parameterizing types (of class
     * @param constructorParameters
     *            the parameters of the constructor of the object to instantiate as active. If some
     *            parameters are primitive types, the wrapper class types should be given here. null
     *            can be used to specify that no parameter are passed to the constructor.
     * @param node
     *            the possibly null node where to create the active object. If null, the active
     *            object is created localy on a default node
     * @param activity
     *            the possibly null activity object defining the different step in the activity of
     *            the object. see the definition of the activity in the javadoc of this classe for
     *            more information.
     * @param factory
     *            the possibly null meta factory giving all factories for creating the meta-objects
     *            part of the body associated to the reified object. If null the default ProActive
     *            MetaObject factory is used.
     * @return a reference (possibly remote) on a Stub of the newly created active object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T newActive(Class<T> clazz, Class<?>[] genericParameters,
            Object[] constructorParameters, Node node, Active activity, MetaObjectFactory factory)
            throws ActiveObjectCreationException, NodeException {
        return (T) newActive(clazz.getName(), genericParameters, constructorParameters, node, activity,
                factory);
    }

    /**
     * <p>
     * Create a set of active objects with given constructor parameters. The object activation is
     * optimized by a thread pool.
     * </p>
     * <p>
     * The total of active objects created is equal to the number of nodes and to the total of
     * constructor parameters also.
     * </p>
     * <p>
     * The condition to use this method is that: <b>constructorParameters.length == nodes.length</b>
     * </p>
     * 
     * @param className
     *            the name of the class to instantiate as active.
     * @param constructorParameters
     *            the array that contains the parameters used to build the active objects. All
     *            active objects have the same constructor parameters.
     * @param nodes
     *            the array of nodes where the active objects are created.
     * @return an array of references (possibly remote) on Stubs of the newly created active
     *         objects.
     * @throws ClassNotFoundException
     *             in the case of className is not a class.
     */
    public static Object[] newActiveInParallel(String className, Object[][] constructorParameters,
            Node[] nodes) throws ClassNotFoundException {
        return newActiveInParallel(className, null, constructorParameters, nodes);
    }

    /**
     * <p>
     * Create a set of active objects with given constructor parameters. The object activation is
     * optimized by a thread pool.
     * </p>
     * <p>
     * The total of active objects created is equal to the number of nodes and to the total of
     * constructor parameters also.
     * </p>
     * <p>
     * The condition to use this method is that: <b>constructorParameters.length == nodes.length</b>
     * </p>
     * 
     * @param className
     *            the name of the class to instantiate as active.
     * @param genericParameters
     *            genericParameters parameterizing types
     * @param constructorParameters
     *            the array that contains the parameters used to build the active objects. All
     *            active objects have the same constructor parameters.
     * @param nodes
     *            the array of nodes where the active objects are created.
     * @return an array of references (possibly remote) on Stubs of the newly created active
     *         objects.
     * @throws ClassNotFoundException
     *             in the case of className is not a class.
     */
    public static Object[] newActiveInParallel(String className, Class<?>[] genericParameters,
            Object[][] constructorParameters, Node[] nodes) throws ClassNotFoundException {
        if (constructorParameters.length != nodes.length) {
            throw new ProActiveRuntimeException("The total of constructors must"
                + " be equal to the total of nodes");
        }

        ThreadFactory tf = new NamedThreadFactory("ProActive newActive in //");
        ExecutorService threadPool = Executors.newCachedThreadPool(tf);

        Vector<Object> result = new Vector<Object>();

        // TODO execute tasks
        // The Virtual Node is already activate
        for (int i = 0; i < constructorParameters.length; i++) {
            threadPool.execute(new ProcessForAoCreation(result, className, genericParameters,
                constructorParameters[i], nodes[i % nodes.length]));
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(CentralPAPropertyRepository.PA_COMPONENT_CREATION_TIMEOUT.getValue(),
                    TimeUnit.SECONDS);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Class<?> classForResult = Class.forName(className);
        return result.toArray((Object[]) Array.newInstance(classForResult, result.size()));
    }

    /**
     * Turns the target object into an ActiveObject attached to a default node in the local JVM. The
     * type of the stub is is the type of the existing object.
     * 
     * @param target
     *            The object to turn active
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, (Class<?>[]) null, (Node) null);
    }

    /**
     * Turns the target object into an Active Object and send it to the Node identified by the given
     * url. The type of the stub is is the type of the existing object.
     * 
     * @param target
     *            The object to turn active
     * @param nodeURL
     *            the URL of the node where to create the active object on. If null, the active
     *            object is created localy on a default node
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, String nodeURL) throws ActiveObjectCreationException,
            NodeException {
        if (nodeURL == null) {
            return turnActive(target, null, target.getClass().getName(), null, null, null);
        } else {
            return turnActive(target, null, target.getClass().getName(), NodeFactory.getNode(nodeURL), null,
                    null);
        }
    }

    /**
     * Turns the target object into an Active Object and send it to the given Node or to a default
     * node in the local JVM if the given node is null. The type of the stub is is the type of the
     * target object.
     * 
     * @param target
     *            The object to turn active
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, Node node) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, null, target.getClass().getName(), node, null, null);
    }

    /**
     * Turns the target object into an Active Object and send it to the given Node or to a default
     * node in the local JVM if the given node is null. The type of the stub is is the type of the
     * target object.
     * 
     * @param target
     *            The object to turn active
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @param activity
     *            the possibly null activity object defining the different step in the activity of
     *            the object. see the definition of the activity in the javadoc of this classe for
     *            more information.
     * @param factory
     *            the possibly null meta factory giving all factories for creating the meta-objects
     *            part of the body associated to the reified object. If null the default ProActive
     *            MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, Node node, Active activity, MetaObjectFactory factory)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, null, target.getClass().getName(), node, activity, factory);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a local node if
     * the given node is null. The type of the stub is given by the parameter
     * <code>nameOfTargetType</code>.
     * 
     * @param target
     *            The object to turn active
     * @param nameOfTargetType
     *            the fully qualified name of the type the stub class should inherit from. That type
     *            can be less specific than the type of the target object.
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, String nameOfTargetType, Node node)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, null, nameOfTargetType, node, null, null);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a local node if
     * the given node is null. The type of the stub is given by the parameter
     * <code>nameOfTargetType</code>. A Stub is dynamically generated for the existing object.
     * The result of the call will be an instance of the Stub class pointing to the proxy object
     * pointing to the body object pointing to the existing object. The body can be remote or local
     * depending if the existing is sent remotely or not.
     * 
     * @param target
     *            The object to turn active
     * @param nameOfTargetType
     *            the fully qualified name of the type the stub class should inherit from. That type
     *            can be less specific than the type of the target object.
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @param activity
     *            the possibly null activity object defining the different step in the activity of
     *            the object. see the definition of the activity in the javadoc of this classe for
     *            more information.
     * @param factory
     *            the possibly null meta factory giving all factories for creating the meta-objects
     *            part of the body associated to the reified object. If null the default ProActive
     *            MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, String nameOfTargetType, Node node, Active activity,
            MetaObjectFactory factory) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, null, nameOfTargetType, node, activity, factory);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a local node if
     * the given node is null. The type of the stub is given by the parameter
     * <code>nameOfTargetType</code>. A Stub is dynamically generated for the existing object.
     * The result of the call will be an instance of the Stub class pointing to the proxy object
     * pointing to the body object pointing to the existing object. The body can be remote or local
     * depending if the existing is sent remotely or not.
     * 
     * @param target
     *            The object to turn active
     * @param genericParameters
     *            parameterizing types (of class
     * @param nameOfTargetType
     *            the fully qualified name of the type the stub class should inherit from. That type
     *            can be less specific than the type of the target object.
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @param activity
     *            the possibly null activity object defining the different step in the activity of
     *            the object. see the definition of the activity in the javadoc of this classe for
     *            more information.
     * @param factory
     *            the possibly null meta factory giving all factories for creating the meta-objects
     *            part of the body associated to the reified object. If null the default ProActive
     *            MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, String nameOfTargetType, Class<?>[] genericParameters,
            Node node, Active activity, MetaObjectFactory factory) throws ActiveObjectCreationException,
            NodeException {
        if (factory == null) {
            factory = ProActiveMetaObjectFactory.newInstance();
            if (factory.getProActiveSecurityManager() == null) {
                factory.setProActiveSecurityManager(((AbstractBody) PAActiveObject.getBodyOnThis())
                        .getProActiveSecurityManager());
            }
        }

        ProActiveSecurityManager factorySM = factory.getProActiveSecurityManager();

        MetaObjectFactory clonedFactory = factory;

        if (factorySM != null) {
            try {
                clonedFactory = (MetaObjectFactory) factory.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            clonedFactory.setProActiveSecurityManager(factory.getProActiveSecurityManager()
                    .generateSiblingCertificate(EntityType.OBJECT, nameOfTargetType));

            ProActiveLogger.getLogger(Loggers.SECURITY).debug("new active object with security manager");
        }

        if (node == null) {
            // using default proactive node
            node = NodeFactory.getDefaultNode();
        }

        try {
            return (T) MOP.createStubObject(target, nameOfTargetType, genericParameters, node, activity,
                    clonedFactory);
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException(t);
        }
    }

    /**
     * Turns the target object into an ActiveObject attached to a default node in the local JVM. The
     * type of the stub is is the type of the existing object.
     * 
     * @param target
     *            The object to turn active
     * @param genericParameters
     *            genericParameters parameterizing types
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, Class<?>[] genericParameters)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, (Node) null, (Active) null, (MetaObjectFactory) null);
    }

    /**
     * Turns the target object into an Active Object and send it to the Node identified by the given
     * url. The type of the stub is is the type of the existing object.
     * 
     * @param target
     *            The object to turn active
     * @param genericParameters
     *            genericParameters parameterizing types
     * @param nodeURL
     *            the URL of the node where to create the active object on. If null, the active
     *            object is created localy on a default node
     * @return a reference (possibly remote) on a Stub of the existing object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, Class<?>[] genericParameters, String nodeURL)
            throws ActiveObjectCreationException, NodeException {
        if (nodeURL == null) {
            return turnActive(target, genericParameters, target.getClass().getName(), null, null, null);
        } else {
            return turnActive(target, genericParameters, target.getClass().getName(), NodeFactory
                    .getNode(nodeURL), null, null);
        }
    }

    /**
     * Turns the target object into an Active Object and send it to the given Node or to a default
     * node in the local JVM if the given node is null. The type of the stub is is the type of the
     * target object.
     * 
     * @param target
     *            The object to turn active
     * @param genericParameters
     *            genericParameters parameterizing types
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, Class<?>[] genericParameters, Node node)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, target.getClass().getName(), node, null, null);
    }

    /**
     * Turns the target object into an Active Object and send it to the given Node or to a default
     * node in the local JVM if the given node is null. The type of the stub is is the type of the
     * target object.
     * 
     * @param target
     *            The object to turn active
     * @param genericParameters
     *            genericParameters parameterizing types
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @param activity
     *            the possibly null activity object defining the different step in the activity of
     *            the object. see the definition of the activity in the javadoc of this classe for
     *            more information.
     * @param factory
     *            the possibly null meta factory giving all factories for creating the meta-objects
     *            part of the body associated to the reified object. If null the default ProActive
     *            MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, Class<?>[] genericParameters, Node node, Active activity,
            MetaObjectFactory factory) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, target.getClass().getName(), node, activity, factory);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a local node if
     * the given node is null. The type of the stub is given by the parameter
     * <code>nameOfTargetType</code>.
     * 
     * @param target
     *            The object to turn active
     * @param genericParameters
     *            genericParameters parameterizing types
     * @param nameOfTargetType
     *            the fully qualified name of the type the stub class should inherit from. That type
     *            can be less specific than the type of the target object.
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, Class<?>[] genericParameters, String nameOfTargetType, Node node)
            throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, nameOfTargetType, node, null, null);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a local node if
     * the given node is null. The type of the stub is given by the parameter
     * <code>nameOfTargetType</code>.
     * 
     * @param target
     *            The object to turn active
     * @param genericParameters
     *            genericParameters parameterizing types
     * @param nameOfTargetType
     *            the class the stub class should inherit from. That type
     *            can be less specific than the type of the target object.
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T, G> T turnActive(T target, Class<?>[] genericParameters, Class<G> nameOfTargetType,
            Node node) throws ActiveObjectCreationException, NodeException {
        return turnActive(target, genericParameters, nameOfTargetType.getName(), node, null, null);
    }

    /**
     * Turns a Java object into an Active Object and send it to a remote Node or to a local node if
     * the given node is null. The type of the stub is given by the parameter
     * <code>nameOfTargetType</code>. A Stub is dynamically generated for the existing object.
     * The result of the call will be an instance of the Stub class pointing to the proxy object
     * pointing to the body object pointing to the existing object. The body can be remote or local
     * depending if the existing is sent remotely or not.
     * 
     * @param target
     *            The object to turn active
     * @param genericParameters
     *            genericParameters parameterizing types
     * @param nameOfTargetType
     *            the fully qualified name of the type the stub class should inherit from. That type
     *            can be less specific than the type of the target object.
     * @param node
     *            The Node the object should be sent to or null to create the active object in the
     *            local JVM
     * @param activity
     *            the possibly null activity object defining the different step in the activity of
     *            the object. see the definition of the activity in the javadoc of this classe for
     *            more information.
     * @param factory
     *            the possibly null meta factory giving all factories for creating the meta-objects
     *            part of the body associated to the reified object. If null the default ProActive
     *            MataObject factory is used.
     * @return a reference (possibly remote) on a Stub of the target object
     * @exception ActiveObjectCreationException
     *                if a problem occur while creating the stub or the body
     * @exception NodeException
     *                if the node was null and that the DefaultNode cannot be created
     */
    public static <T> T turnActive(T target, Class<?>[] genericParameters, String nameOfTargetType,
            Node node, Active activity, MetaObjectFactory factory) throws ActiveObjectCreationException,
            NodeException {
        if (factory == null) {
            factory = ProActiveMetaObjectFactory.newInstance();
        }

        ProActiveSecurityManager factorySM = factory.getProActiveSecurityManager();

        MetaObjectFactory clonedFactory = factory;

        if (factorySM != null) {
            try {
                clonedFactory = (MetaObjectFactory) factory.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }

            clonedFactory.setProActiveSecurityManager(factory.getProActiveSecurityManager()
                    .generateSiblingCertificate(EntityType.OBJECT, nameOfTargetType));
            ProActiveLogger.getLogger(Loggers.SECURITY).debug("new active object with security manager");
        }

        if (node == null) {
            // using default proactive node
            node = NodeFactory.getDefaultNode();
        }

        try {
            return (T) MOP.createStubObject(target, nameOfTargetType, genericParameters, node, activity,
                    clonedFactory);
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException(t);
        }
    }

    public static String registerByName(Object obj, String name) throws ProActiveException {
        return registerByName(obj, name, true);
    }

    public static String registerByName(Object obj, String name, boolean rebind) throws ProActiveException {
        try {
            UniversalBody body = getRemoteBody(obj);

            String url = body.registerByName(name, rebind);
            body.setRegistered(true);
            if (PAActiveObject.logger.isInfoEnabled()) {
                PAActiveObject.logger.info("Success at binding url " + url);
            }

            return url;
        } catch (IOException e) {
            throw new ProActiveException("Failed to register" + obj + " with name " + name, e);
        }
    }

    public static String registerByName(Object obj, String name, String protocol) throws ProActiveException {
        return registerByName(obj, name, true, protocol);
    }

    public static String registerByName(Object obj, String name, boolean rebind, String protocol)
            throws ProActiveException {
        try {
            UniversalBody body = getRemoteBody(obj);

            String url = body.registerByName(name, rebind, protocol);
            body.setRegistered(true);
            if (PAActiveObject.logger.isInfoEnabled()) {
                PAActiveObject.logger.info("Success at binding url " + url);
            }

            return url;
        } catch (IOException e) {
            throw new ProActiveException("Failed to register" + obj + " with name " + name, e);
        }
    }

    /**
     * Looks-up all Active Objects registered on a host, using a registry(RMI or HTTP or IBIS) The
     * registry where to look for is fully determined with the protocol included in the url.
     * 
     * @param url
     *            The url where to perform the lookup. The url takes the following form:
     *            protocol://machine_name:port. Protocol and port can be ommited if respectively RMI
     *            and 1099: //machine_name
     * @return String [] the list of names registered on the host; if no Registry found, returns {}
     * @throws IOException
     *             If the given url does not map to a physical host, or if the connection is
     *             refused.
     */
    public static String[] listActive(String url) throws java.io.IOException {
        String[] activeNames = null;
        try {
            URI[] uris = PARemoteObject.list(URI.create(url));
            activeNames = new String[uris.length];
            for (int i = 0; i < uris.length; i++) {
                activeNames[i] = uris[i].toString();
            }
        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return activeNames;
    }

    /**
     * Kill an Active Object by calling terminate() method on its body.
     * 
     * @param ao
     *            the active object to kill
     * @param immediate
     *            if this boolean is true, this method is served as an immediate service. The
     *            termination is then synchronous. The active object dies immediately. Else, the kill
     *            request is served as a normal request, it is put on the request queue. The
     *            termination is asynchronous.
     */
    public static void terminateActiveObject(Object ao, boolean immediate) {
        if (MOP.isReifiedObject(ao)) {
            // if ao is a future we need to obtain the real stub
            ao = PAFuture.getFutureValue(ao);

            Proxy proxy = ((StubObject) ao).getProxy();
            try {
                if (immediate) {
                    NonFunctionalServices.terminateAOImmediately(proxy);
                } else {
                    NonFunctionalServices.terminateAO(proxy);
                }
            } catch (BodyTerminatedException e) {
                // the terminated body is already terminated
                if (PAActiveObject.logger.isDebugEnabled()) {
                    PAActiveObject.logger.debug("Terminating already terminated body : " + e);
                }
            } catch (Throwable e) {
                PAActiveObject.logger.debug("An exception occurs while sending termination request.", e);
            }
        } else {
            throw new ProActiveRuntimeException("The given object " + ao + " is not a reified object");
        }
    }

    /**
     * Kill the calling active object by calling terminate() method on its body.
     * 
     * @param immediate
     *            if this boolean is true, this method is served as an immediate service. The
     *            termination is then synchronous. The active object dies immediately. Else, the kill
     *            request is served as a normal request, it is put on the request queue. The
     *            termination is asynchronous.
     */
    public static void terminateActiveObject(boolean immediate) {
        terminateActiveObject(PAActiveObject.getStubOnThis(), immediate);
    }

    /**
     * Ping the target active object. Note that this method does not take into account the state of
     * the target object : pinging an inactive but reachable active object actually returns true.
     * 
     * @param target
     *            the pinged active object.
     * @return true if the active object is reachable, false otherwise.
     */
    public static boolean pingActiveObject(Object target) {
        // if target is a future we need to obtain the real stub
        target = PAFuture.getFutureValue(target);

        UniversalBody targetedBody = null;
        try {
            // reified object is checked in getRemoteBody
            targetedBody = getRemoteBody(target);
            targetedBody.receiveFTMessage(PAActiveObject.hb);
            return true;
        } catch (IOException e) {
            if (PAActiveObject.logger.isDebugEnabled()) {
                // id should be cached locally
                PAActiveObject.logger.debug("Active object " + targetedBody.getID() + " is unreachable.", e);
            }
            return false;
        }
    }

    /**
     * Set an immediate execution for the caller active object of the method methodName, ie request
     * of name methodName will be executed right away upon arrival at the caller AO context.
     * 
     * Optionally, an immediate service can be configured with "unique thread mode" : a 
     * dedicated thread is created for each different caller ; all the methods set as immediate 
     * service with unique thread called from the same caller object are executed only by this thread. 
     * This can be useful for methods that use java.util.concurrency.Lock locks, which must be locked 
     * and unlocked by the same thread.
     * 
     * Warning: the execution of an Immediate Service method is achieved in parallel of the current
     * services, so it is the programmer responsibility to ensure that Immediate Services do not
     * interfere with any other methods.
     * 
     * @param methodName
     *            the name of the method
     * @param uniqueThread
     * 			  true if this immediate service should be always executed by the same thread for 
     * 			  a given caller, false if any thread can be used.
     */
    public static void setImmediateService(String methodName, boolean uniqueThread) {
        PAActiveObject.getBodyOnThis().setImmediateService(methodName, uniqueThread);
    }

    /**
     * Set an immediate execution for the caller active object obj of the method methodName with
     * parameters parametersType, ie request of name methodName will be executed right away upon
     * arrival at the caller AO context. 
     * 
     * Optionally, an immediate service can be configured with "unique thread mode" : a 
     * dedicated thread is created for each different caller ; all the methods set as immediate 
     * service with unique thread called from the same caller object are executed only by this thread. 
     * This can be useful for methods that use java.util.concurrency.Lock locks, which must be locked 
     * and unlocked by the same thread.
     * 
     * Warning: the execution of an Immediate Service method is
     * achieved in parallel of the current services, so it is the programmer responsibility to
     * ensure that Immediate Services do not interfere with any other methods.
     * 
     * @param methodName
     *            the name of the method
     * @param parametersTypes
     *            the types of the parameters of the method
     * @param uniqueThread
     * 			  true if this immediate service should be always executed by the same thread for 
     * 			  a given caller, false if any thread can be used.
     */
    public static void setImmediateService(String methodName, Class<?>[] parametersTypes, boolean uniqueThread) {
        PAActiveObject.getBodyOnThis().setImmediateService(methodName, parametersTypes, uniqueThread);
    }

    /**
     * Set an immediate execution for the caller active object of the method methodName, ie request
     * of name methodName will be executed right away upon arrival at the caller AO context.
     * Warning: the execution of an Immediate Service method is achieved in parallel of the current
     * services, so it is the programmer responsibility to ensure that Immediate Services do not
     * interfere with any other methods.
     * 
     * @param methodName
     *            the name of the method
     */
    public static void setImmediateService(String methodName) {
        PAActiveObject.setImmediateService(methodName, false);
    }

    /**
     * Set an immediate execution for the caller active object obj of the method methodName with
     * parameters parametersType, ie request of name methodName will be executed right away upon
     * arrival at the caller AO context. Warning: the execution of an Immediate Service method is
     * achieved in parallel of the current services, so it is the programmer responsibility to
     * ensure that Immediate Services do not interfere with any other methods.
     * 
     * @param methodName
     *            the name of the method
     * @param parametersTypes
     *            the types of the parameters of the method
     */
    public static void setImmediateService(String methodName, Class<?>[] parametersTypes) {
        PAActiveObject.setImmediateService(methodName, parametersTypes, false);
    }

    /**
     * Removes an immmediate execution for the active object obj, i.e. requests corresponding to the
     * name will be executed by the calling thread, and not added in the request queue.
     * 
     * @param methodName
     *            the name of the method
     */
    public static void removeImmediateService(String methodName) {
        PAActiveObject.getBodyOnThis().removeImmediateService(methodName);
    }

    /**
     * Removes an immmediate execution for the active object obj, i.e. requests corresponding to the
     * name and types of parameters will be executed by the calling thread, and not added in the
     * request queue.
     * 
     * @param methodName
     *            the name of the method
     * @param parametersTypes
     *            the types of the parameters of the method
     */
    public static void removeImmediateService(String methodName, Class<?>[] parametersTypes) {
        PAActiveObject.getBodyOnThis().removeImmediateService(methodName, parametersTypes);
    }

    /**
     * Set a ForgetOnSend (FOS) strategy when invoking <i>methodName</i> on <i>activeObject</i>.
     * With this stategy, the <i>ProActive Rendez-Vous</i> is delegated to a parallel thread.</br>
     * </br> *Warning*:<br>
     * When sending a request with the ForgetOnSend strategy, you have to complain with two
     * constraints :</br> First, *you must not* modify the arguments after the call, otherwise
     * unexpected concurrent problems on parameters should occurs.</br> Second, the method must be
     * <i>sterile</i>. A request is known as <i>sterile</i> if it does not have any descendant,
     * i.e. if during its service it does not send new requests, except to itself or to the activity
     * which sent the request it is serving (its parent).
     * 
     * @param activeObject
     *            the remote active object you want to send FOS requests
     * @param methodName
     *            the name of the targeted method
     */
    public static void setForgetOnSend(Object activeObject, String methodName) {
        PAActiveObject.getBodyOnThis().setForgetOnSendRequest(activeObject, methodName);
    }

    /**
     * Remove the ForgetOnSend strategy for the specified method on the specified active object.
     * 
     * @param activeObject
     *            the remote active object you want to send FOS requests
     * @param methodName
     *            the name of the targeted method
     */
    public static void removeForgetOnSend(Object activeObject, String methodName) {
        PAActiveObject.getBodyOnThis().removeForgetOnSendRequest(activeObject, methodName);
    }

    /**
     * Return the URL of the node of the remote <code>activeObject</code>.
     * 
     * @param activeObject
     *            the remote active object.
     * @return the URL of the node of the <code>activeObject</code>.
     */
    public static String getActiveObjectNodeUrl(Object activeObject) {
        UniversalBody body = getRemoteBody(activeObject);
        return body.getNodeURL();
    }

    public static Node getActiveObjectNode(Object activeObject) throws NodeException {
        Node node = NodeFactory.getNode(getActiveObjectNodeUrl(activeObject));
        return node;
    }

    /**
     * Unregisters an active object previously registered into a registry.
     * 
     * @param url
     *            the url under which the active object is registered.
     * @exception java.io.IOException
     *                if the remote object cannot be removed from the registry
     */
    public static void unregister(String url) throws java.io.IOException {

        RemoteObject<?> rmo;
        try {
            rmo = RemoteObjectHelper.lookup(URI.create(url));
            Object o = RemoteObjectHelper.generatedObjectStub(rmo);

            if (o instanceof UniversalBody) {
                UniversalBody ub = (UniversalBody) o;
                ub.setRegistered(false);
            }

            if (PAActiveObject.logger.isDebugEnabled()) {
                PAActiveObject.logger.debug("Success at unbinding url " + url);
            }
        } catch (ProActiveException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Return the current execution context for the calling thread. The execution context contains a
     * reference to the body associated to this thread, and some informations about the currently
     * served request if any.
     * 
     * @return the current execution context associated to the calling thread.
     * @see org.objectweb.proactive.core.body.Context
     */
    public static Context getContext() {
        return LocalBodyStore.getInstance().getContext();
    }

    /**
     * Returns a Stub-Proxy couple pointing to the local body associated to the active object whose
     * active thread is calling this method.
     * 
     * @return a Stub-Proxy couple pointing to the local body, or null if the calling thread is not
     *         an active thread.
     * @see PAActiveObject#getBodyOnThis
     */
    public static StubObject getStubOnThis() {
        Body body = PAActiveObject.getBodyOnThis();
        if (PAActiveObject.logger.isDebugEnabled()) {
            logger.debug("ProActive: getStubOnThis() returns " + body);
        }
        if ((body == null) || (body instanceof HalfBody)) {
            return null;
        } else {
            return PAActiveObject.getStubForBody(body);
        }
    }

    /**
     * @return The node of the current active object.
     * @throws NodeException
     *             problem with the node.
     */
    public static Node getNode() throws NodeException {
        BodyProxy destProxy = (BodyProxy) (getStubOnThis()).getProxy();
        return NodeFactory.getNode(destProxy.getBody().getNodeURL());
    }

    /**
     * Looks-up an active object previously registered in a registry(RMI, IBIS, HTTP). In fact it is
     * the remote version of the body of an active object that can be registered into the Registry
     * under a given URL. If the lookup is successful, the method reconstructs a Stub-Proxy couple
     * and point it to the remote nody found. The registry where to look for is fully determined
     * with the protocol included in the url
     * 
     * @param clazz
     *            the class the generated stub should inherit from.
     * @param url
     *            the url under which the remote body is registered. The url takes the following
     *            form: protocol://machine_name:port/name.
     * @return a remote reference on a Stub of type <code>classname</code> pointing to the remote
     *         body found
     * @exception java.io.IOException
     *                if the remote body cannot be found under the given url or if the object found
     *                is not of type UniversalBody
     * @exception ActiveObjectCreationException
     *                if the stub-proxy couple cannot be created
     */
    public static <T> T lookupActive(Class<T> clazz, String url) throws ActiveObjectCreationException,
            java.io.IOException {
        return (T) lookupActive(clazz.getName(), url);
    }

    /**
     * Looks-up an active object previously registered in a registry(RMI, IBIS, HTTP). In fact it is
     * the remote version of the body of an active object that can be registered into the Registry
     * under a given URL. If the lookup is successful, the method reconstructs a Stub-Proxy couple
     * and point it to the remote body found. The registry where to look for is fully determined
     * with the protocol included in the url
     * 
     *
     * @param classname
     *            the fully qualified name of the class the stub should inherit from.
     * @param url
     *            the url under which the remote body is registered. The url takes the following
     *            form: protocol://machine_name:port/name.
     * @return a remote reference on a Stub of type <code>classname</code> pointing to the remote
     *         body found
     * @exception java.io.IOException
     *                if the remote body cannot be found under the given url or if the object found
     *                is not of type UniversalBody
     * @exception ActiveObjectCreationException
     *                if the stub-proxy couple cannot be created
     */
    public static Object lookupActive(String classname, String url) throws ActiveObjectCreationException,
            java.io.IOException {
        logger.debug("Trying to lookup " + url);
        RemoteObject<?> rmo;
        URI uri = RemoteObjectHelper.expandURI(URI.create(url));

        try {
            rmo = RemoteObjectHelper.lookup(uri);

            Object o = RemoteObjectHelper.generatedObjectStub(rmo);

            if (o instanceof UniversalBody) {
                return MOP.createStubObject(classname, (UniversalBody) o);
            } else {
                throw new IOException("The remote object located at " + url +
                    " is not an Active Object. class=" + o.getClass().getName());
            }
        } catch (ProActiveException e) {
            throw new IOException6("Lookup of " + classname + " at " + url + " failed", e);
        } catch (MOPException e) {
            Throwable t = e;

            if (e.getTargetException() != null) {
                t = e.getTargetException();
            }

            throw new ActiveObjectCreationException("Exception occured when trying to create stub-proxy", t);
        }
    }

    /**
     * Enable the automatic continuation mechanism for this active object.
     */
    public static void enableAC(Object obj) throws java.io.IOException {
        UniversalBody body = getRemoteBody(obj);
        body.enableAC();
    }

    /**
     * Disable the automatic continuation mechanism for this active object.
     */
    public static void disableAC(Object obj) throws java.io.IOException {
        UniversalBody body = getRemoteBody(obj);
        body.disableAC();
    }

    /**
     * Return the URL of a given remote object using the default remote object factory
     * 
     * @param ao An active object
     * @return the URL of the remote object
     * @throws ProActiveRuntimeException if ao is not an active object
     */
    public static String getUrl(Object ao) {
        UniversalBody body = getRemoteBody(ao);
        return body.getUrl();
    }

    /**
     * When an active object is created, it is associated with a Body that takes care of all non
     * fonctionnal properties. Assuming that the active object is only accessed by the different
     * Stub objects, all method calls end-up as Requests sent to this Body. Therefore the only
     * thread calling the method of the active object is the active thread managed by the body.
     * There is an unique mapping between the active thread and the body responsible for it. From
     * any method in the active object the current thread caller of the method is the active thread.
     * When a reified method wants to get a reference to the Body associated to the active object,
     * it can invoke this method. Assuming that the current thread is the active thread, the
     * associated body is returned.
     * 
     * @return the body associated to the active object whose active thread is calling this method.
     * @throws ProActiveException
     */
    public static Body getBodyOnThis() {
        return LocalBodyStore.getInstance().getContext().getBody();
    }

    /**
     * Indicate if the caller is executing in an active object
     * 
     * @return true if in an active object, false otherwise (half body or plain java thread)
     */
    public static boolean isInActiveObject() {
        return LocalBodyStore.getInstance().isInAo();
    }

    /**
     * Performs an exchange on a byte array between two Active Objects.
     * 
     * @param tag
     * @param destAO
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public static void exchange(String tag, Object destAO, byte[] srcArray, int srcOffset, byte[] dstArray,
            int dstOffset, int len) {
        ExchangeManager.getExchangeManager().exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray,
                dstOffset, len);
    }

    /**
     * Performs an exchange on a double array between two Active Objects.
     * 
     * @param tag
     * @param destAO
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public static void exchange(String tag, Object destAO, double[] srcArray, int srcOffset,
            double[] dstArray, int dstOffset, int len) {
        ExchangeManager.getExchangeManager().exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray,
                dstOffset, len);
    }

    /**
     * Performs an exchange on an integer array between two Active Objects.
     * 
     * @param tag
     * @param destAO
     * @param srcArray
     * @param srcOffset
     * @param dstArray
     * @param dstOffset
     * @param len
     */
    public static void exchange(String tag, Object destAO, int[] srcArray, int srcOffset, int[] dstArray,
            int dstOffset, int len) {
        ExchangeManager.getExchangeManager().exchange(tag.hashCode(), destAO, srcArray, srcOffset, dstArray,
                dstOffset, len);
    }

    /**
     * Performs an exchange on a complex structure of doubles between two Active Objects.
     * 
     * @param tag
     * @param destAO
     * @param src
     * @param dst
     */
    public static void exchange(String tag, Object destAO, ExchangeableDouble src, ExchangeableDouble dst) {
        ExchangeManager.getExchangeManager().exchange(tag.hashCode(), destAO, src, dst);
    }

    // -------------------------------------------------------------------------------------------
    //
    // STUB CREATION
    //
    // -------------------------------------------------------------------------------------------
    private static StubObject getStubForBody(Body body) {
        try {
            return MOP.createStubObject(body.getReifiedObject(), new Object[] { body }, body
                    .getReifiedObject().getClass().getName(), null);
        } catch (MOPException e) {
            throw new ProActiveRuntimeException("Cannot create Stub for this Body e=", e);
        }
    }

    private static UniversalBody getRemoteBody(Object obj) {
        // Check if obj is a body and return the remote
        if (obj instanceof UniversalBody) {
            return ((UniversalBody) obj).getRemoteAdapter();
        }
        // Check if obj is really a reified object
        if (!(MOP.isReifiedObject(obj))) {
            throw new ProActiveRuntimeException("The given object " + obj + " is not a reified object");
        }

        // Find the appropriate remoteBody
        org.objectweb.proactive.core.mop.Proxy myProxy = ((StubObject) obj).getProxy();

        if (myProxy == null) {
            throw new ProActiveRuntimeException("Cannot find a Proxy on the stub object: " + obj);
        }

        BodyProxy myBodyProxy = (BodyProxy) myProxy;
        UniversalBody body = myBodyProxy.getBody().getRemoteAdapter();
        return body;
    }

    // -----------------------
    // = Multi-Protocol part =
    // -----------------------

    /**
     * Force usage of a specific protocol to contact an active object.
     *
     * @param obj
     *          Could be remote or local parts of the ActiveObject
     *
     * @param protocol
     *          Can be rmi, http, pamr, rmissh, rmissl
     *
     * @throws UnknownProtocolException
     *
     * @throws NotYetExposedException
     *
     * @throws IllegalArgumentException
     *          If obj isn't an ActiveObject
     */
    public static void forceProtocol(Object obj, String protocol) throws UnknownProtocolException,
            NotYetExposedException {
        if (!(obj instanceof StubObject)) {
            throw new IllegalArgumentException("This method must be call on an ActiveObject");
        }

        UniversalBodyProxy ubp = (UniversalBodyProxy) ((StubObject) obj).getProxy();
        UniversalBody ub = ubp.getBody();
        // Object is a stub which point to a remote Body
        if (ub instanceof UniversalBodyRemoteObjectAdapter) {
            UniversalBodyRemoteObjectAdapter ubroa = (UniversalBodyRemoteObjectAdapter) ubp.getBody();
            SynchronousProxy sp = (SynchronousProxy) ((StubObject) ubroa).getProxy();
            RemoteObject ro = sp.getRemoteObject();
            if (ro instanceof RemoteObjectAdapter) {
                ((RemoteObjectAdapter) ro).forceProtocol(protocol);
            } else {
                throw new IllegalArgumentException(
                    "Method forceProtocol can only be called on stub object (client part of the RemoteObject)");
            }
        } else {
            throw new IllegalArgumentException("The object " + obj + " isn't an ActiveObject");
        }
    }

    public static void forceToDefault(Object obj) throws UnknownProtocolException, NotYetExposedException {
        forceProtocol(obj, CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());
    }

}
