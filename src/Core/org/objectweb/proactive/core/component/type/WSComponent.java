/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.component.type;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.factory.InstantiationException;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.fractal.api.type.TypeFactory;
import org.objectweb.fractal.util.Fractal;
import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.webservices.WSInfo;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Fictive component used as owner for the proxy generated to bind the client interface
 * of a component to a web service.
 * <br>
 * This component implements the {@link Serializable}, the {@link Component}, the
 * {@link NameController} and the {@link LifeCycleController} interfaces.
 * <br>
 * This component is always in the started state and cannot be stopped.
 *
 * @author The ProActive Team
 * @see Serializable
 * @see Component
 * @see NameController
 * @see LifeCycleController
 */
@PublicAPI
public class WSComponent implements Serializable, Component, NameController, LifeCycleController {
    protected static final transient Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);

    /**
     * Functional interface type.
     */
    private ProActiveInterfaceType fcInterfaceType;

    /**
     * Functional interface implementation.
     */
    private Object fcInterfaceImpl;

    /**
     * Web service informations.
     */
    private WSInfo wsInfo;

    /**
     * Default constructor.
     */
    public WSComponent() {
    }

    /**
     * Constructor specifying the {@link WSInfo} corresponding to the web service to bind to.
     *
     * @param wsInfo {@link WSInfo} corresponding to the web service to bind to.
     */
    public WSComponent(WSInfo wsInfo) {
        this.wsInfo = wsInfo;
    }

    /**
     * Getter for the type of the functional interface corresponding to the generated proxy.
     *
     * @return Type of the functional interface corresponding to the generated proxy.
     */
    public ProActiveInterfaceType getFcInterfaceType() {
        return fcInterfaceType;
    }

    /**
     * Setter for the type of the functional interface corresponding to the generated proxy.
     *
     * @param fcInterfaceType Type of the functional interface corresponding to the generated proxy.
     */
    public void setFcInterfaceType(ProActiveInterfaceType fcInterfaceType) {
        this.fcInterfaceType = fcInterfaceType;
    }

    /**
     * Getter for the functional interface implementation corresponding to the generated proxy.
     *
     * @return Functional interface implementation corresponding to the generated proxy.
     */
    public Object getFcInterfaceImpl() {
        return fcInterfaceImpl;
    }

    /**
     * Setter for the functional interface implementation corresponding to the generated proxy.
     *
     * @param fcInterfaceImpl Functional interface implementation corresponding to the generated proxy.
     */
    public void setFcInterfaceImpl(Object fcInterfaceImpl) {
        this.fcInterfaceImpl = fcInterfaceImpl;
    }

    /**
     * Getter for the {@link WSInfo} corresponding to the web service to bind to.
     *
     * @return {@link WSInfo} corresponding to the web service to bind to.
     */
    public WSInfo getWSInfo() {
        return wsInfo;
    }

    /**
     * Setter for the {@link WSInfo} corresponding to the web service to bind to.
     *
     * @param wsInfo {@link WSInfo} corresponding to the web service to bind to.
     */
    public void setWSInfo(WSInfo wsInfo) {
        this.wsInfo = wsInfo;
    }

    public Object getFcInterface(String interfaceName) throws NoSuchInterfaceException {
        if (interfaceName.equals(Constants.COMPONENT)) {
            return this;
        }
        if (interfaceName.equals(Constants.NAME_CONTROLLER)) {
            return this;
        }
        if (interfaceName.equals(Constants.LIFECYCLE_CONTROLLER)) {
            return this;
        }
        if (interfaceName.equals(fcInterfaceType.getFcItfName())) {
            return fcInterfaceImpl;
        }
        throw new NoSuchInterfaceException(interfaceName);
    }

    public Object[] getFcInterfaces() {
        return new Object[] { this, this, fcInterfaceImpl };
    }

    public Type getFcType() {
        try {
            Component boot = Fractal.getBootstrapComponent();
            TypeFactory tf = Fractal.getTypeFactory(boot);
            return tf.createFcType(new InterfaceType[] { fcInterfaceType });
        } catch (InstantiationException e) {
            // should never append
            logger.error("Could not generate type for web service component", e);
            return null;
        } catch (NoSuchInterfaceException e) {
            // should never append
            logger.error("Could not generate type for web service component", e);
            return null;
        }
    }

    public String getFcName() {
        return "WSComponent-" + wsInfo.getWSUrl();
    }

    public void setFcName(String name) {
    }

    public String getFcState() {
        return LifeCycleController.STARTED;
    }

    public void startFc() throws IllegalLifeCycleException {
    }

    public void stopFc() throws IllegalLifeCycleException {
    }
}
