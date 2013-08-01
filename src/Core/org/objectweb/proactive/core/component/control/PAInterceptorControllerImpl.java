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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.exceptions.IllegalInterceptorException;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;
import org.objectweb.proactive.core.component.interception.Interceptor;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;


/**
 * Implementation of the {@link PAInterceptorController interceptor controller}.
 *
 * @author The ProActive Team
 */
public class PAInterceptorControllerImpl extends AbstractPAController implements PAInterceptorController {
    private PAMembraneController membraneController;

    private Map<String, List<Interceptor>> interceptors;

    /**
     * Creates a {@link PAInterceptorControllerImpl}.
     * 
     * @param owner Component owning the controller.
     */
    public PAInterceptorControllerImpl(Component owner) {
        super(owner);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setControllerItfType() {
        try {
            setItfType(PAGCMTypeFactoryImpl.instance().createFcItfType(Constants.INTERCEPTOR_CONTROLLER,
                    PAInterceptorController.class.getName(), TypeFactory.SERVER, TypeFactory.MANDATORY,
                    TypeFactory.SINGLE));
        } catch (InstantiationException e) {
            throw new ProActiveRuntimeException("cannot create controller " + this.getClass().getName(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initController() {
        try {
            this.membraneController = Utils.getPAMembraneController(this.owner);
        } catch (NoSuchInterfaceException nsie) {
            // No membrane controller
            this.membraneController = null;
        }

        this.interceptors = new HashMap<String, List<Interceptor>>();
        InterfaceType[] interfaceTypes = ((ComponentType) this.owner.getFcType()).getFcInterfaceTypes();

        for (InterfaceType interfaceType : interfaceTypes) {
            this.interceptors.put(interfaceType.getFcItfName(), new ArrayList<Interceptor>());
        }
    }

    /**
     * Returns the {@link Interceptor interceptors} attached to the interface with the specified name.
     * 
     * @param interfaceName Name of the interface on which to get its {@link Interceptor interceptors}.
     * @return The {@link Interceptor interceptors} attached to the interface with the specified name.
     * @throws NoSuchInterfaceException If there is no such interface.
     */
    public List<Interceptor> getInterceptors(String interfaceName) throws NoSuchInterfaceException {
        if (this.interceptors.containsKey(interfaceName)) {
            return this.interceptors.get(interfaceName);
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getInterceptorIDsFromInterface(String interfaceName) throws NoSuchInterfaceException {
        if (this.interceptors.containsKey(interfaceName)) {
            List<String> interceptorIDs = new ArrayList<String>();

            for (Interceptor interceptor : this.interceptors.get(interfaceName)) {
                Interface itf = (Interface) interceptor;
                Component componentOwner = itf.getFcItfOwner();

                if (componentOwner.equals(this.owner)) {
                    interceptorIDs.add(itf.getFcItfName());
                } else { // Functional interface of a NF component
                    try {
                        interceptorIDs.add(GCM.getNameController(componentOwner).getFcName() + "." +
                            itf.getFcItfName());
                    } catch (NoSuchInterfaceException nsie) {
                        // Should never happen since a NF component must have a name
                        controllerLogger.error(nsie);
                    }
                }
            }

            return interceptorIDs;
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    private Interceptor getInterceptorFromID(String interceptorID) throws NoSuchInterfaceException,
            NoSuchComponentException, IllegalInterceptorException {
        try {
            Object controllerInterface = this.owner.getFcInterface(interceptorID);

            if (controllerInterface instanceof Interceptor) {
                return (Interceptor) controllerInterface;
            } else {
                throw new IllegalInterceptorException("The controller interface " + interceptorID +
                    " does not implement the Interceptor interface");
            }
        } catch (NoSuchInterfaceException nsie) { // No controller interface with such a name, maybe a NF component?
            String[] interceptorIDElements = interceptorID.split("\\.");

            if (interceptorIDElements.length == 2) { // Interceptor is an interface of a NF component
                if (this.membraneController != null) {
                    String nfComponentName = interceptorIDElements[0];
                    String interfaceName = interceptorIDElements[1];

                    Object itf = this.membraneController.nfGetFcSubComponent(nfComponentName).getFcInterface(
                            interfaceName);

                    if (itf instanceof Interceptor) {
                        return (Interceptor) itf;
                    } else {
                        throw new IllegalInterceptorException("The interface " + interfaceName +
                            " of the NF component " + nfComponentName +
                            " does not implement the Interceptor interface");
                    }
                } else {
                    throw new IllegalInterceptorException("Invalid interceptor ID " + interceptorID +
                        " since there is no membrane controller");
                }
            } else { // The interceptor ID does not a match the format for a NF component
                throw nsie;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInterceptorOnInterface(String interfaceName, String interceptorID, int index)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException {
        checkLifeCycleIsStopped();

        if (this.interceptors.containsKey(interfaceName)) {
            if ((index >= 0) && (index <= this.interceptors.get(interfaceName).size())) {
                this.interceptors.get(interfaceName).add(index, this.getInterceptorFromID(interceptorID));
            } else {
                throw new IllegalInterceptorException(
                    "The specified index for the new interceptor is invalid, current size of the interceptors = " +
                        this.interceptors.get(interfaceName).size() + ", specified index = " + index);
            }
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    private void internalAddInterceptorOnInterface(String interfaceName, Interceptor interceptor)
            throws NoSuchInterfaceException {
        if (this.interceptors.containsKey(interfaceName)) {
            this.interceptors.get(interfaceName).add(interceptor);
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInterceptorOnInterface(String interfaceName, String interceptorID)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException {
        checkLifeCycleIsStopped();

        internalAddInterceptorOnInterface(interfaceName, this.getInterceptorFromID(interceptorID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInterceptorsOnInterface(String interfaceName, List<String> interceptorIDs)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException {
        checkLifeCycleIsStopped();

        for (String interceptorID : interceptorIDs) {
            internalAddInterceptorOnInterface(interfaceName, this.getInterceptorFromID(interceptorID));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInterceptorOnAllServerInterfaces(String interceptorID) throws IllegalLifeCycleException,
            NoSuchInterfaceException, NoSuchComponentException, IllegalInterceptorException {
        checkLifeCycleIsStopped();

        Interceptor interceptor = this.getInterceptorFromID(interceptorID);

        for (InterfaceType interfaceType : ((ComponentType) this.owner.getFcType()).getFcInterfaceTypes()) {
            if (!interfaceType.isFcClientItf()) {
                try {
                    this.internalAddInterceptorOnInterface(interfaceType.getFcItfName(), interceptor);
                } catch (NoSuchInterfaceException nsie) {
                    // Should never happen
                    controllerLogger.error(nsie);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInterceptorOnAllClientInterfaces(String interceptorID) throws IllegalLifeCycleException,
            NoSuchInterfaceException, NoSuchComponentException, IllegalInterceptorException {
        checkLifeCycleIsStopped();

        Interceptor interceptor = this.getInterceptorFromID(interceptorID);

        for (InterfaceType interfaceType : ((ComponentType) this.owner.getFcType()).getFcInterfaceTypes()) {
            if (interfaceType.isFcClientItf()) {
                try {
                    this.internalAddInterceptorOnInterface(interfaceType.getFcItfName(), interceptor);
                } catch (NoSuchInterfaceException nsie) {
                    // Should never happen
                    controllerLogger.error(nsie);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInterceptorOnAllInterfaces(String interceptorID) throws IllegalLifeCycleException,
            NoSuchInterfaceException, NoSuchComponentException, IllegalInterceptorException {
        checkLifeCycleIsStopped();

        Interceptor interceptor = this.getInterceptorFromID(interceptorID);

        for (String interfaceName : this.interceptors.keySet()) {
            try {
                this.internalAddInterceptorOnInterface(interfaceName, interceptor);
            } catch (NoSuchInterfaceException nsie) {
                // Should never happen
                controllerLogger.error(nsie);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInterceptorFromInterface(String interfaceName, int index)
            throws IllegalLifeCycleException, NoSuchInterfaceException, IllegalInterceptorException {
        checkLifeCycleIsStopped();

        if (this.interceptors.containsKey(interfaceName)) {
            if ((index > -1) && (index < this.interceptors.get(interfaceName).size())) {
                this.interceptors.get(interfaceName).remove(index);
            } else {
                throw new IllegalInterceptorException(
                    "The specified index for the interceptor to remove is invalid, current size of the interceptors = " +
                        this.interceptors.get(interfaceName).size() + ", specified index = " + index);
            }
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    private void internalRemoveInterceptorFromInterface(String interfaceName, Interceptor interceptor)
            throws NoSuchInterfaceException {
        if (this.interceptors.containsKey(interfaceName)) {
            this.interceptors.get(interfaceName).remove(interceptor);
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInterceptorFromInterface(String interfaceName, String interceptorID)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException {
        checkLifeCycleIsStopped();

        internalRemoveInterceptorFromInterface(interfaceName, this.getInterceptorFromID(interceptorID));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeAllInterceptorsFromInterface(String interfaceName) throws IllegalLifeCycleException,
            NoSuchInterfaceException {
        checkLifeCycleIsStopped();

        if (this.interceptors.containsKey(interfaceName)) {
            this.interceptors.get(interfaceName).clear();
        } else {
            throw new NoSuchInterfaceException(interfaceName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInterceptorFromAllServerInterfaces(String interceptorID)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException {
        checkLifeCycleIsStopped();

        Interceptor interceptor = this.getInterceptorFromID(interceptorID);

        for (InterfaceType interfaceType : ((ComponentType) this.owner.getFcType()).getFcInterfaceTypes()) {
            if (!interfaceType.isFcClientItf()) {
                try {
                    this.internalRemoveInterceptorFromInterface(interfaceType.getFcItfName(), interceptor);
                } catch (NoSuchInterfaceException nsie) {
                    // Should never happen
                    controllerLogger.error(nsie);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInterceptorFromAllClientInterfaces(String interceptorID)
            throws IllegalLifeCycleException, NoSuchInterfaceException, NoSuchComponentException,
            IllegalInterceptorException {
        checkLifeCycleIsStopped();

        Interceptor interceptor = this.getInterceptorFromID(interceptorID);

        for (InterfaceType interfaceType : ((ComponentType) this.owner.getFcType()).getFcInterfaceTypes()) {
            if (interfaceType.isFcClientItf()) {
                try {
                    this.internalRemoveInterceptorFromInterface(interfaceType.getFcItfName(), interceptor);
                } catch (NoSuchInterfaceException nsie) {
                    // Should never happen
                    controllerLogger.error(nsie);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInterceptorFromAllInterfaces(String interceptorID) throws IllegalLifeCycleException,
            NoSuchInterfaceException, NoSuchComponentException, IllegalInterceptorException {
        checkLifeCycleIsStopped();

        Interceptor interceptor = this.getInterceptorFromID(interceptorID);

        for (String interfaceName : this.interceptors.keySet()) {
            try {
                this.internalRemoveInterceptorFromInterface(interfaceName, interceptor);
            } catch (NoSuchInterfaceException nsie) {
                // Should never happen
                controllerLogger.error(nsie);
            }
        }
    }
}
