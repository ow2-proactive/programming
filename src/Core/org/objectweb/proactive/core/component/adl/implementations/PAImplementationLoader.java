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

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.attributes.Attributes;
import org.objectweb.fractal.adl.attributes.AttributesContainer;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.error.ChainedErrorLocator;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.implementations.Implementation;
import org.objectweb.fractal.adl.implementations.ImplementationContainer;
import org.objectweb.fractal.adl.implementations.ImplementationErrors;
import org.objectweb.fractal.adl.implementations.ImplementationLoader;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The Fractal {@link ImplementationLoader} checks that the Java classes specified in
 * the &lt;implementation&gt; nodes exist.
 * 
 * The {@link PAImplementationLoader} extends the Fractal {@link ImplementationLoader} to consider the components
 * that are defined inside the &lt;controller&gt; nodes (i.e., NF components). 
 *  
 * @author The ProActive Team
 *
 */

public class PAImplementationLoader extends ImplementationLoader {

    public static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    /**
     * Checks the &lt;definition&gt; node and marks the components as F or NF
     */
    public Definition load(final String name, final Map<Object, Object> context) throws ADLException {
        final Definition d = clientLoader.load(name, context);
        checkNode(d, context, true);
        return d;
    }

    /** This version of checkNode considers the possibility of having components defined inside
     *  the &lt;controller&gt; tag. The &lt;controller&gt; must check its own tree, because it may contain
     *  (NF) &lt;component&gt;s.
     */
    protected void checkNode(final Object node, final Map<Object, Object> context, boolean functional)
            throws ADLException {

        // ImplementationContainers are directly checked
        if (node instanceof ImplementationContainer) {
            // NF components are marked as such
            if (!functional) {
                ((ImplementationContainer) node).astSetDecoration("NF", true);
            }
            checkImplementationContainer((ImplementationContainer) node, context);
        }

        // The <controller> node may contain NF components
        if (node instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) node).getController();
            // check that the descriptor file is define, or well, that components or interface are defined
            checkControllerContainer((ControllerContainer) node);
            if (ctrl != null) {
                if (ctrl instanceof ComponentContainer) {
                    // check the components described inside the Controller node ... they are NF Components
                    for (final Component comp : ((ComponentContainer) ctrl).getComponents()) {
                        //logger.debug("[PAImplementationLoader]   Check NF component "+ comp.getName() );
                        checkNode(comp, context, false);
                    }
                }
            }
        }
        if (node instanceof ComponentContainer) {
            //logger.debug("[PAImplementationLoader]   Checking component container ["+ (((ComponentContainer) node).astGetSource()) + "]" );
            for (final Component comp : ((ComponentContainer) node).getComponents()) {
                //logger.debug("[PAImplementationLoader]     Check component "+ comp.getName() );
                checkNode(comp, context, functional);
            }
        }
    }

    /** This version of the checkControllerContainer method allows to have a &lt;controller&gt; node
     *  without a controller descriptor file. However, in that case at least one (NF) component or (NF) interface
     *  must be specified.
     *  
     *  If there is no file descriptor, the contents of the membrane can be described inside them, either:
     *  (1) inline (ongoing implementation), or
     *  (2) referencing another definition file (next step, not yet implemented)
     */
    @Override
    protected void checkControllerContainer(final ControllerContainer container) throws ADLException {
        final Controller ctrl = container.getController();
        if (ctrl != null) {
            logger.debug("[PAImplementationLoader] Checking controller " + ctrl.toString());
            if (ctrl.getDescriptor() == null) {
                // if there's no descriptor, then the <controller> must contain at least one <component> or <interface> (or a <binding>?)
                int nComponents = ((ComponentContainer) ctrl).getComponents().length;
                int nInterfaces = ((InterfaceContainer) ctrl).getInterfaces().length;
                if (nComponents + nInterfaces == 0) {
                    throw new ADLException(PAImplementationErrors.EMPTY_CONTROLLER_NODE, ctrl);
                }
            }
            // if there is a controller descriptor, it's ok.
            // if there are also <interface>s, then compatibility should be verified when creating the component
        }
    }

    /** Extended version of the checkImplementationContainer method.
     *  This method checks that the implementation class of primitive components implements
     *  the server interfaces and the "attribute-controller" interface, if any.
     *  
     *  This version considers that a composite may have an implementation class, in the case of
     *  composites with attributes. In that case, the implementation class does not need to implement
     *  the server interfaces (they are delegated to subcomponents)
     */
    @Override
    protected void checkImplementationContainer(final ImplementationContainer container,
            final Map<Object, Object> context) throws ADLException {
        final Implementation impl = container.getImplementation();
        logger.debug("[PAImplementationLoader] Checking component " + container.toString() + " " +
            (container.astGetDecoration("NF") != null ? "NF" : "F"));
        if (impl == null)
            return;

        final String className = impl.getClassName();
        if (className == null) {
            throw new ADLException(ImplementationErrors.IMPLEMENTATION_MISSING, impl);
        }
        Object c;
        try {
            c = implementationCodeLoaderItf.loadImplementation(className, null, context);
        } catch (final ADLException e) {
            ChainedErrorLocator.chainLocator(e, impl);
            throw e;
        }
        impl.astSetDecoration("code", c);

        if (!(c instanceof Class))
            return;

        final Class<?> implemClass = (Class<?>) c;

        // if it is a composite without attributes, we don't arrive here (impl == null)
        // if it is a composite with attributes, we don't need to check the server interfaces, because
        //   they are delegated to a subcomponent
        boolean hasSubcomponents = false;
        if (container instanceof ComponentContainer) {
            if (((ComponentContainer) container).getComponents().length > 0) {
                hasSubcomponents = true;
            }
        }
        // if there are subcomponents, don't check the interfaces
        if (container instanceof InterfaceContainer && !hasSubcomponents) {
            for (final Interface itf : ((InterfaceContainer) container).getInterfaces()) {
                if (!(itf instanceof TypeInterface))
                    continue;

                final TypeInterface tItf = (TypeInterface) itf;
                if (tItf.getRole().equals(TypeInterface.SERVER_ROLE)) {
                    final Object d = interfaceLoaderItf.loadInterface(tItf.getSignature(), context);
                    if (d instanceof Class) {
                        if (!((Class<?>) d).isAssignableFrom(implemClass)) {
                            throw new ADLException(ImplementationErrors.CLASS_DOES_NOT_IMPLEMENT_INTERFACE,
                                impl, implemClass.getName(), ((Class<?>) d).getName());
                        }
                    }
                }
            }
        }

        // if there is an 'attribute-controller' interface, it must be implemented
        // by the implementing class
        if (container instanceof AttributesContainer) {
            final Attributes attrs = ((AttributesContainer) container).getAttributes();
            if (attrs != null) {
                final Object d = interfaceLoaderItf.loadInterface(attrs.getSignature(), context);
                if (d instanceof Class) {
                    if (!((Class<?>) d).isAssignableFrom(implemClass)) {
                        throw new ADLException(ImplementationErrors.CLASS_DOES_NOT_IMPLEMENT_ATTR_CTRL, impl,
                            implemClass.getName(), ((Class<?>) d).getName());
                    }
                }
            }
        }
    }

}
