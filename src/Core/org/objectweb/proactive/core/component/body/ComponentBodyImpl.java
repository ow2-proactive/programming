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
package org.objectweb.proactive.core.component.body;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.LifeCycleController;
import org.objectweb.proactive.Active;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.body.MetaObjectFactory;
import org.objectweb.proactive.core.body.ProActiveMetaObjectFactory;
import org.objectweb.proactive.core.body.migration.MigratableBody;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.identity.PAComponentImpl;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * This class has been inserted into the bodies hierarchy in order to instantiate the
 * component metaobject {@link PAComponent}.
 */
public class ComponentBodyImpl extends MigratableBody implements ComponentBody {

    private static final long serialVersionUID = 52;
    private PAComponent componentIdentity = null;
    private Map<String, Shortcut> shortcutsOnThis = null; // key = functionalItfName, value = shortcut
    private static Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    private boolean insideFunctionalActivity = false;

    public ComponentBodyImpl() {
        super();
    }

    /**
     * Constructor for ComponentBodyImpl.
     *
     * It creates the component metaobject only if the MetaObjectFactory is parameterized
     * with ComponentParameters (thus implicitly constructing components).
     *
     * It may also modify the activity to be compatible with the life cycle of the component and
     * the management of non functional invocations.
     * @param reifiedObject a reference on the reified object
     * @param nodeURL node url
     * @param factory factory for the corresponding metaobjects
     */
    public ComponentBodyImpl(Object reifiedObject, String nodeURL, Active activity, MetaObjectFactory factory)
            throws ActiveObjectCreationException {
        super(reifiedObject, nodeURL, factory);
        //        filterOnNFRequests = new RequestFilterOnPrioritizedNFRequests();
        // create the component metaobject if necessary
        // --> check the value of the "parameters" field
        Map<String, Object> factory_parameters = factory.getParameters();
        if ((null != factory_parameters)) {
            if (null != factory_parameters.get(ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY)) {
                if (factory_parameters.get(ProActiveMetaObjectFactory.COMPONENT_PARAMETERS_KEY) instanceof ComponentParameters) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("creating metaobject component identity");
                    }
                    this.componentIdentity = factory.newComponentFactory().newPAComponent(this);

                    // change activity into a component activity
                    // activity = new ComponentActivity(activity, reifiedObject);
                } else {
                    logger
                            .error("component parameters for the components factory are not of type ComponentParameters");
                }
            }
        }
    }

    /**
     * Returns the a reference on the Component meta object
     * @return the PAComponent meta-object
     */
    public PAComponentImpl getPAComponentImpl() {
        return (PAComponentImpl) this.componentIdentity;
    }

    /**
     * overrides the @link{Body#isActive()} method :
     * when the process flow is inside a functional activity of a component,
     * isActive corresponds to the started state in the lifecycle of the component, while
     * !isActive corresponds to the stopped state.
     * If the process flow is outside of the functional activity of a component, then return the
     * default result for isActive() (unoverriden)
     *
     */
    @Override
    public boolean isActive() {
        if (this.insideFunctionalActivity) {
            try {
                return LifeCycleController.STARTED.equals(GCM.getGCMLifeCycleController(getPAComponentImpl())
                        .getFcState());
            } catch (NoSuchInterfaceException e) {
                logger.error("could not find the life cycle controller of this component");
                return false;
            }
        } else {
            return super.isActive();
        }
    }

    /*
     * @see org.objectweb.proactive.core.component.body.ComponentBody#isComponent()
     */
    public boolean isComponent() {
        return (getPAComponentImpl() != null);
    }

    /*
     * @see org.objectweb.proactive.core.component.body.ComponentBody#finishedFunctionalActivity()
     */
    public void finishedFunctionalActivity() {
        this.insideFunctionalActivity = false;
    }

    /*
     * @see org.objectweb.proactive.core.component.body.ComponentBody#startingFunctionalActivity()
     */
    public void startingFunctionalActivity() {
        this.insideFunctionalActivity = true;
    }

    public void keepShortcut(Shortcut shortcut) {
        if (this.shortcutsOnThis == null) {
            this.shortcutsOnThis = new HashMap<String, Shortcut>();
        }
        this.shortcutsOnThis.put(shortcut.getFcFunctionalInterfaceName(), shortcut);
    }
}
