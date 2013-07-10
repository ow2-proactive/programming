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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.etsi.uri.gcm.api.control.GathercastController;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.body.request.ServeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.collectiveitfs.GatherBindingChecker;
import org.objectweb.proactive.core.component.collectiveitfs.GatherRequestsQueues;
import org.objectweb.proactive.core.component.exceptions.ParameterDispatchException;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.representative.ItfID;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;
import org.objectweb.proactive.core.node.Node;


/**
 * Implementation of the {@link GathercastController gathercast controller}.
 *
 * @author The ProActive Team
 * @see GathercastController
 */
public class PAGathercastControllerImpl extends AbstractCollectiveInterfaceController implements
        GathercastController, ControllerStateDuplication {
    private Map<String, List<Object>> bindingsOnServerItfs = new HashMap<String, List<Object>>();
    private Map<String, PAInterface> gatherItfs = new HashMap<String, PAInterface>();
    private GatherRequestsQueues gatherRequestsHandler;

    /**
     * Creates a {@link PAGathercastControllerImpl}.
     * 
     * @param owner Component owning the controller.
     */
    public PAGathercastControllerImpl(Component owner) {
        super(owner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initController() {
        gatherRequestsHandler = new GatherRequestsQueues(owner);
        List<Object> interfaces = Arrays.asList(owner.getFcInterfaces());
        Iterator<Object> it = interfaces.iterator();

        while (it.hasNext()) {
            Interface itf = (Interface) it.next();

            // gather mechanism currently only offered for functional interfaces
            if (!Utils.isControllerItfName(itf.getFcItfName())) {
                addManagedInterface((PAInterface) itf);
            }
        }
    }

    private boolean addManagedInterface(PAInterface itf) {
        if (gatherItfs.containsKey(itf.getFcItfName())) {
            return false;
        }

        PAGCMInterfaceType itfType = (PAGCMInterfaceType) itf.getFcItfType();

        if (itfType.isGCMGathercastItf()) {
            gatherItfs.put(itf.getFcItfName(), itf);
        } else {
            return false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Method searchMatchingMethod(Method clientSideMethod, Method[] serverSideMethods,
            boolean clientItfIsMulticast, boolean serverItfIsGathercast, PAInterface serverSideItf) {
        return searchMatchingMethod(clientSideMethod, serverSideMethods, clientItfIsMulticast);
    }

    /*
     * @seeorg.objectweb.proactive.core.component.control.AbstractCollectiveInterfaceController#
     * searchMatchingMethod(java.lang.reflect.Method, java.lang.reflect.Method[])
     */
    protected Method searchMatchingMethod(Method clientSideMethod, Method[] serverSideMethods,
            boolean clientItfIsMulticast) {
        try {
            return GatherBindingChecker.searchMatchingMethod(clientSideMethod, serverSideMethods,
                    clientItfIsMulticast);
        } catch (ParameterDispatchException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(Constants.GATHERCAST_CONTROLLER,
                    GathercastController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller type for controller " +
                this.getClass().getName());
        }
    }

    /**
     * Delegates a request to be processed by the gathercast controller
     * @param r a request on a gathercast interface
     * @return the result of the gathercast invocation
     * @throws ServeException if the request handling failed
     */
    public Object handleRequestOnGatherItf(ComponentRequest r) throws ServeException {
        return gatherRequestsHandler.addRequest(r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    /*
     * TODO : throw exception when binding already exists? (this would make the method synchronous)
     */
    public void notifyAddedGCMBinding(String gathercastItfName, Component owner, String clientItfName) {
        ItfID itfID = new ItfID(clientItfName, ((PAComponent) owner).getID());
        if (bindingsOnServerItfs.containsKey(gathercastItfName)) {
            if (bindingsOnServerItfs.get(gathercastItfName).contains(itfID)) {
                throw new ProActiveRuntimeException("trying to add twice the binding of client interface " +
                    clientItfName + " on server interface " + gathercastItfName);
            }
            bindingsOnServerItfs.get(gathercastItfName).add(itfID);
        } else {
            List<Object> connectedClientItfs = new ArrayList<Object>();
            connectedClientItfs.add(itfID);
            bindingsOnServerItfs.put(gathercastItfName, connectedClientItfs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyRemovedGCMBinding(String gathercastItfName, Component owner, String clientItfName) {
        ItfID itfID = new ItfID(clientItfName, ((PAComponent) owner).getID());
        if (bindingsOnServerItfs.containsKey(gathercastItfName)) {
            List<Object> connectedClientItfs = bindingsOnServerItfs.get(gathercastItfName);
            if (connectedClientItfs.contains(itfID)) {
                connectedClientItfs.remove(itfID);
            } else {
                controllerLogger.error("could not remove binding on server interface " + gathercastItfName +
                    " because owner component is not listed as connected components");
            }
        } else {
            controllerLogger.error("could not remove binding on server interface " + gathercastItfName +
                " because there is no component listed as connected on this server interface");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Object> getGCMConnectedClients(String gathercastItfName) {
        return bindingsOnServerItfs.get(gathercastItfName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void migrateDependentActiveObjectsTo(Node node) throws MigrationException {
        if (gatherRequestsHandler != null) {
            gatherRequestsHandler.migrateFuturesHandlersTo(node);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void ensureGCMCompatibility(InterfaceType itfType, Interface itf) {
        // nothing to do in this version
    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void duplicateController(Object c) {
        if (c instanceof GatherCastItfState) {

            GatherCastItfState state = (GatherCastItfState) c;

            bindingsOnServerItfs = state.getBindingsOnServerItfs();
            gatherItfs = state.getGatherItfs();
            gatherRequestsHandler = state.getGatherRequestsHandler();
        } else {
            throw new ProActiveRuntimeException(
                "GathercastControllerImpl : Impossible to duplicate the controller " + this +
                    " from the controller" + c);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ControllerState getState() {

        return new ControllerState(new GatherCastItfState(
            (HashMap<String, List<Object>>) bindingsOnServerItfs, (HashMap<String, PAInterface>) gatherItfs,
            gatherRequestsHandler));
    }

    class GatherCastItfState implements Serializable {
        private HashMap<String, List<Object>> bindingsOnServerItfs;
        private HashMap<String, PAInterface> gatherItfs;
        private GatherRequestsQueues gatherRequestsHandler;

        public GatherCastItfState(HashMap<String, List<Object>> bindingsOnServerItfs,
                HashMap<String, PAInterface> gatherItfs, GatherRequestsQueues gatherRequestsHandler) {

            this.bindingsOnServerItfs = bindingsOnServerItfs;
            this.gatherItfs = gatherItfs;
            this.gatherRequestsHandler = gatherRequestsHandler;
        }

        public HashMap<String, List<Object>> getBindingsOnServerItfs() {
            return bindingsOnServerItfs;
        }

        public void setBindingsOnServerItfs(HashMap<String, List<Object>> bindingsOnServerItfs) {
            this.bindingsOnServerItfs = bindingsOnServerItfs;
        }

        public HashMap<String, PAInterface> getGatherItfs() {
            return gatherItfs;
        }

        public void setGatherItfs(HashMap<String, PAInterface> gatherItfs) {
            this.gatherItfs = gatherItfs;
        }

        public GatherRequestsQueues getGatherRequestsHandler() {
            return gatherRequestsHandler;
        }

        public void setGatherRequestsHandler(GatherRequestsQueues gatherRequestsHandler) {
            this.gatherRequestsHandler = gatherRequestsHandler;
        }
    }
}
