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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.api.PAGroup;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.exceptions.ContentControllerExceptionListException;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;
import org.objectweb.proactive.core.group.Group;
import org.objectweb.proactive.utils.NamedThreadFactory;


/**
 * Implementation of {@link ContentController content controller}.
 *
 * @author The ProActive Team
 */
public class PAContentControllerImpl extends AbstractPAController implements PAContentController,
        Serializable, ControllerStateDuplication {
    protected List<Component> fcSubComponents;

    /**
     * Creates a {@link PAContentControllerImpl}.
     * 
     * @param owner Component owning the controller.
     */
    public PAContentControllerImpl(Component owner) {
        super(owner);
        fcSubComponents = new ArrayList<Component>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(Constants.CONTENT_CONTROLLER,
                    PAContentController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*
     * In this implementation, the external interfaces are also internal interfaces
     */
    public Object[] getFcInternalInterfaces() {
        Object[] itfs = ((PAComponent) getFcItfOwner()).getRepresentativeOnThis().getFcInterfaces();
        List<Object> internalItfs = new ArrayList<Object>();

        for (int i = 0; i < itfs.length; i++) {
            InterfaceType itfType = (InterfaceType) ((Interface) itfs[i]).getFcItfType();
            if (!itfType.isFcClientItf() && !Utils.isControllerItfName(itfType.getFcItfName())) {
                internalItfs.add(itfs[i]);
            }
        }

        return internalItfs.toArray(new Interface[internalItfs.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*
     * In this implementation, the external interfaces are also internal interfaces
     */
    public Object getFcInternalInterface(String interfaceName) throws NoSuchInterfaceException {
        return ((PAComponent) getFcItfOwner()).getRepresentativeOnThis().getFcInterface(interfaceName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component[] getFcSubComponents() {
        return fcSubComponents.toArray(new Component[fcSubComponents.size()]);
    }

    public boolean isSubComponent(Component component) {
        if (PAGroup.isGroup(component)) {
            Group<Component> group = PAGroup.getGroup(component);
            for (Iterator<Component> it = group.iterator(); it.hasNext();) {
                if (!fcSubComponents.contains(it.next())) {
                    return false;
                }
            }
        } else if (!fcSubComponents.contains(component)) {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*
     * If subComponent is a group, each element of the group is added as a subcomponent
     */
    public void addFcSubComponent(Component subComponent) throws IllegalLifeCycleException,
            IllegalContentException {
        checkLifeCycleIsStopped();
        // no sharing in the current implementation of Fractal
        // => only one parent for a given component
        // FIXME control requests are enqueued
        // pb is that the subComponent might not be stopped
        try {
            // could not do this invocation on a group (non reifiable return type)
            if (PAGroup.isGroup(subComponent)) {
                try {
                    addFcSubComponent(PAGroup.getGroup(subComponent));
                } catch (ContentControllerExceptionListException e) {
                    IllegalContentException ice = new IllegalContentException(
                        "problem adding a list of component to a composite : " + e.getMessage());
                    ice.initCause(e);
                    throw ice;
                }
                return;
            }
            if (GCM.getSuperController(subComponent).getFcSuperComponents().length != 0) {
                throw new IllegalContentException(
                    "This implementation of the Fractal model does not currently allow sharing : " +
                        GCM.getNameController(subComponent).getFcName() + " has no super controller");
            }
        } catch (NoSuchInterfaceException e) {
            controllerLogger
                    .error("could not check that the subcomponent is not shared, continuing ignoring this verification ... " +
                        e);
        }

        PAComponent this_component = ((PAComponent) getFcItfOwner());
        Component ref_on_this_component = this_component.getRepresentativeOnThis();

        // check whether the subComponent is the component itself
        if (ref_on_this_component.equals(subComponent)) {
            try {
                throw new IllegalArgumentException("cannot add " +
                    GCM.getNameController(getFcItfOwner()).getFcName() + " component into itself ");
            } catch (NoSuchInterfaceException e) {
                controllerLogger.error(e.getMessage());
            }
        }

        // check whether already a sub component
        if (getAllSubComponents(this_component).contains(ref_on_this_component)) {
            String name;
            try {
                name = GCM.getNameController(subComponent).getFcName();
            } catch (NoSuchInterfaceException nsie) {
                throw new ProActiveRuntimeException("cannot access the component parameters controller", nsie);
            }
            throw new IllegalArgumentException("already a sub component : " + name);
        }

        fcSubComponents.add(subComponent);
        // add a ref on the current component
        try {
            PASuperController itf = Utils.getPASuperController(subComponent);
            itf.addParent(ref_on_this_component);
        } catch (NoSuchInterfaceException e) {
            IllegalContentException ice = new IllegalContentException(
                "Cannot add component : cannot find super-controller interface.");
            ice.initCause(e);
            throw ice;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFcSubComponent(Component subComponent) throws IllegalLifeCycleException,
            IllegalContentException {
        checkLifeCycleIsStopped();
        try {
            if (Utils.getPABindingController(getFcItfOwner()).isBoundTo(subComponent)) {
                throw new IllegalContentException(
                    "cannot remove a sub component that holds bindings on its external server interfaces");
            }
            Component[] subComponents = getFcSubComponents();
            for (int i = 0; i < subComponents.length; i++) {
                if (Utils.getPABindingController(subComponents[i]).isBoundTo(subComponent)) {
                    throw new IllegalContentException(
                        "cannot remove a sub component that holds bindings on its external server interfaces");
                }
            }
            if (Utils.getPABindingController(subComponent).isBound()) {
                throw new IllegalContentException(
                    "cannot remove a sub component that holds bindings on its external client interfaces");
            }
        } catch (NoSuchInterfaceException ignored) {
            // no binding controller
        }
        if (!fcSubComponents.remove(subComponent)) {
            throw new IllegalContentException("not a sub component : " + subComponent);
        }
        try {
            Utils.getPASuperController(subComponent).removeParent(subComponent);
        } catch (NoSuchInterfaceException e) {
            fcSubComponents.add(subComponent);
            IllegalContentException ice = new IllegalContentException(
                "cannot remove component : cannot find super-controller interface");
            ice.initCause(e);
            throw ice;
        }
    }

    /*
     * Returns all the direct and indirect sub components of the given component.
     * 
     * @param component a component.
     * 
     * @return all the direct and indirect sub components of the given component.
     */
    private List<Component> getAllSubComponents(final Component component) {
        List<Component> allSubComponents = new ArrayList<Component>();
        List<Component> stack = new ArrayList<Component>();

        // first layer of sub components retrieved directly (do not go through the representative)
        Component[] subComponents = getFcSubComponents();

        for (int i = subComponents.length - 1; i >= 0; --i) {
            stack.add(subComponents[i]);
        }
        while (stack.size() > 0) {
            int index = stack.size() - 1;
            Component c = stack.get(index);
            stack.remove(index);

            if (!allSubComponents.contains(c)) {
                try {
                    ContentController cc = GCM.getContentController(c);
                    subComponents = cc.getFcSubComponents();
                    for (int i = subComponents.length - 1; i >= 0; --i) {
                        stack.add(subComponents[i]);
                    }
                } catch (NoSuchInterfaceException ignored) {
                    // c is not a composite component: nothing to do
                }
                allSubComponents.add(c);
            }
        }
        return allSubComponents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addFcSubComponent(List<Component> subComponents)
            throws ContentControllerExceptionListException {
        ThreadFactory tf = new NamedThreadFactory("ProActive/GCM add subcomponents");
        ExecutorService threadPool = Executors.newCachedThreadPool(tf);
        ContentControllerExceptionListException e = new ContentControllerExceptionListException();
        for (Iterator<Component> iter = subComponents.iterator(); iter.hasNext();) {
            Component element = iter.next();
            AddSubComponentTask task = new AddSubComponentTask(e, this, element);
            threadPool.execute(task);
        }
        threadPool.shutdown();
        try {
            boolean terminated = threadPool.awaitTermination(120, TimeUnit.SECONDS);
            if (!terminated) {
                controllerLogger
                        .error("Threadpool cannot be properly terminated: termination timeout reached");
            }
        } catch (InterruptedException ie) {
            controllerLogger.error("Threadpool cannot be properly terminated: " + ie.getMessage(), ie);
        }
        if (!e.isEmpty()) {
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeFcSubComponent(List<Component> subComponents)
            throws ContentControllerExceptionListException {
        ThreadFactory tf = new NamedThreadFactory("ProActive/GCM remove subcomponents");
        ExecutorService threadPool = Executors.newCachedThreadPool(tf);
        ContentControllerExceptionListException e = new ContentControllerExceptionListException();
        for (Iterator<Component> iter = subComponents.iterator(); iter.hasNext();) {
            Component element = iter.next();
            RemoveSubComponentTask task = new RemoveSubComponentTask(e, this, element);
            threadPool.execute(task);
        }
        threadPool.shutdown();
        try {
            boolean terminated = threadPool.awaitTermination(120, TimeUnit.SECONDS);
            if (!terminated) {
                controllerLogger
                        .error("Threadpool cannot be properly terminated: termination timeout reached");
            }
        } catch (InterruptedException ie) {
            controllerLogger.error("Threadpool cannot be properly terminated: " + ie.getMessage(), ie);
        }
        if (!e.isEmpty()) {
            throw e;
        }
    }

    private static class AddSubComponentTask implements Runnable {
        ContentControllerExceptionListException exceptions;
        PAContentControllerImpl controller;
        Component component;

        public AddSubComponentTask(ContentControllerExceptionListException exceptions,
                PAContentControllerImpl controller, Component component) {
            this.exceptions = exceptions;
            this.controller = controller;
            this.component = component;
        }

        public void run() {
            try {
                controller.addFcSubComponent(component);
            } catch (IllegalContentException e) {
                e.printStackTrace();
                exceptions.addIllegalContentException(component, e);
            } catch (IllegalLifeCycleException e) {
                e.printStackTrace();
                exceptions.addIllegalLifeCycleException(component, e);
            }
        }
    }

    private static class RemoveSubComponentTask implements Runnable {
        ContentControllerExceptionListException exceptions;
        PAContentControllerImpl controller;
        Component component;

        public RemoveSubComponentTask(ContentControllerExceptionListException exceptions,
                PAContentControllerImpl controller, Component component) {
            this.exceptions = exceptions;
            this.controller = controller;
            this.component = component;
        }

        public void run() {
            try {
                controller.removeFcSubComponent(component);
            } catch (IllegalContentException e) {
                e.printStackTrace();
                exceptions.addIllegalContentException(component, e);
            } catch (IllegalLifeCycleException e) {
                e.printStackTrace();
                exceptions.addIllegalLifeCycleException(component, e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void duplicateController(Object c) {
        if (c instanceof ContentControllerState) {
            ContentControllerState state = (ContentControllerState) c;

            fcSubComponents = state.getFcSubComponents();

        } else {
            throw new ProActiveRuntimeException(
                "PAContentControllerImpl: Impossible to duplicate the controller " + this +
                    " from the controller" + c);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ControllerState getState() {
        return new ControllerState(new ContentControllerState((ArrayList<Component>) fcSubComponents));
    }

    class ContentControllerState implements Serializable {
        private ArrayList<Component> fcSubComponents;

        public ContentControllerState(ArrayList<Component> fcSubComponents) {
            this.fcSubComponents = fcSubComponents;

        }

        public ArrayList<Component> getFcSubComponents() {
            return fcSubComponents;
        }

        public void setFcSubComponents(ArrayList<Component> fcSubComponents) {
            this.fcSubComponents = fcSubComponents;
        }

    }
}
