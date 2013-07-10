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

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Base class for all component controllers.
 *
 * @author The ProActive Team
 */
public abstract class AbstractPAController implements PAController, Serializable {
    private boolean isInternal = true;
    private InterfaceType interfaceType;
    final protected static Logger controllerLogger = ProActiveLogger
            .getLogger(Loggers.COMPONENTS_CONTROLLERS);

    protected PAComponent owner;

    /**
     * Creates a {@link AbstractPAController}.
     * 
     * @param owner Component owning the controller.
     */
    public AbstractPAController(Component owner) {
        this.owner = (PAComponent) owner;
        setControllerItfType();
    }

    /**
     * Default void implementation of the initController method. A controller requiring
     * initialization *after* all interfaces are instantiated have to override this method.
     *
     * @see org.objectweb.proactive.core.component.control.PAController#initController()
     */
    public void initController() {
    }

    /*
     * see {@link org.objectweb.fractal.api.Interface#isFcInternalItf()}
     */
    public boolean isFcInternalItf() {
        return isInternal;
    }

    /*
     * see {@link org.objectweb.fractal.api.Interface#getFcItfName()}
     */
    public String getFcItfName() {
        return interfaceType.getFcItfName();
    }

    /*
     * see {@link org.objectweb.fractal.api.Interface#getFcItfType()}
     */
    public Type getFcItfType() {
        return interfaceType;
    }

    public Component getFcItfOwner() {
        return owner;
    }

    /*
     * some control operations are to be performed while the component is stopped
     */
    protected void checkLifeCycleIsStopped() throws IllegalLifeCycleException {
        try {
            if (!(GCM.getGCMLifeCycleController(getFcItfOwner())).getFcState().equals(
                    LifeCycleController.STOPPED)) {
                throw new IllegalLifeCycleException(
                    "this control operation should be performed while the component is stopped");
            }
        } catch (NoSuchInterfaceException nsie) {
            throw new ProActiveRuntimeException("life cycle controller interface not found");
        }
    }

    protected void setItfType(InterfaceType itfType) {
        this.interfaceType = itfType;
    }

    protected abstract void setControllerItfType();

    protected String getHierarchicalType() {
        return owner.getComponentParameters().getHierarchicalType();
    }

    protected boolean isPrimitive() {
        return Constants.PRIMITIVE.equals(getHierarchicalType());
    }

    protected boolean isComposite() {
        return Constants.COMPOSITE.equals(getHierarchicalType());
    }

    /**
     * If a controller holds references to active objects which are dependent on it, it needs to
     * trigger the migration of these active objects. This is done by overriding this method.
     * @param node
     * @throws MigrationException
     */
    public void migrateDependentActiveObjectsTo(Node node) throws MigrationException {
        // nothing by default
    }
}
