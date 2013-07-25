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
package org.objectweb.proactive.core.component.adl.types;

import java.util.Map;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMInterfaceType;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.Loader;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.types.TypeErrors;
import org.objectweb.fractal.adl.types.TypeInterface;
import org.objectweb.fractal.adl.types.TypeLoader;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * A {@link Loader} to check {@link GCMInterfaceType} nodes in definitions. This
 * loader checks that the Java interfaces specified in these nodes exist.
 * <br/><br/>
 * The {@link PATypeLoader} checks all the &lt;interface&gt; nodes and check
 * that the Java interfaces specified exist, including interfaces defined inside
 * &lt;controller&gt; nodes. 
 * 
 * @author The ProActive Team
 * 
 */
public class PATypeLoader extends TypeLoader {

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    /** 
     * Overriden to use our own version of 'checkNode', which is private in {@link TypeLoader}.
     */
    @Override
    public Definition load(final String name, final Map<Object, Object> context) throws ADLException {
        final Definition d = clientLoader.load(name, context);
        checkNode(d, context);
        return d;
    }

    /**
     * Checks a node that contains &lt;interface&gt; nodes.<br/>
     * Uses the interfaceCodeLoader interface to verify the existence of the class specified by the 'signature' attribute.
     * Then, checks the values of the 'role', 'contingency' and 'cardinality' attributes according to the possible values
     * specified by GCM.  
     */
    @Override
    protected void checkInterfaceContainer(final InterfaceContainer container,
            final Map<Object, Object> context) throws ADLException {
        Interface[] itfs = container.getInterfaces();
        for (int i = 0; i < itfs.length; i++) {
            Interface itf = itfs[i];
            if (itf instanceof TypeInterface) {
                logger.debug("[PATypeLoader] Checking interface:" + itf.toString() + " (" +
                    (itf.astGetDecoration("NF") != null ? "NF" : "F") + ")");
                String signature = ((TypeInterface) itf).getSignature();
                if (signature == null) {
                    throw new ADLException(TypeErrors.SIGNATURE_MISSING, itf);
                } else {
                    try {
                        interfaceCodeLoaderItf.loadInterface(signature, context);
                    } catch (final ADLException e) {
                        throw e;
                    }
                }
                String role = ((TypeInterface) itf).getRole();
                if (role == null) {
                    throw new ADLException(TypeErrors.ROLE_MISSING, itf);
                } else {
                    if (!role.equals(TypeInterface.CLIENT_ROLE) && !role.equals(TypeInterface.SERVER_ROLE) &&
                        !role.equals(PATypeInterface.INTERNAL_CLIENT_ROLE) &&
                        !role.equals(PATypeInterface.INTERNAL_SERVER_ROLE)) {
                        throw new ADLException(PATypeErrors.INVALID_ROLE, itf, role);
                    }
                }
                String contingency = ((TypeInterface) itf).getContingency();
                if (contingency != null) {
                    if (!contingency.equals(TypeInterface.MANDATORY_CONTINGENCY) &&
                        !contingency.equals(TypeInterface.OPTIONAL_CONTINGENCY)) {
                        throw new ADLException(TypeErrors.INVALID_CONTINGENCY, itf, contingency);
                    }
                }

                String cardinality = ((TypeInterface) itf).getCardinality();
                if (cardinality != null) {
                    if (!GCMTypeFactory.SINGLETON_CARDINALITY.equals(cardinality) &&
                        !GCMTypeFactory.COLLECTION_CARDINALITY.equals(cardinality) &&
                        !GCMTypeFactory.MULTICAST_CARDINALITY.equals(cardinality) &&
                        !GCMTypeFactory.GATHERCAST_CARDINALITY.equals(cardinality)) {
                        throw new ADLException(TypeErrors.INVALID_CARDINALITY, itf, cardinality);
                    }
                }
            }
        }
    }

    /**
     * Looks for containers of &lt;interface&gt; nodes.
     * 
     * @param node
     * @param context
     * @throws ADLException
     */
    private void checkNode(final Object node, final Map<Object, Object> context) throws ADLException {

        // The node contains <interface> nodes. Check it.
        if (node instanceof InterfaceContainer) {
            checkInterfaceContainer((InterfaceContainer) node, context);
        }
        // The node contains <component> nodes. Check each <component>
        if (node instanceof ComponentContainer) {
            for (final Component comp : ((ComponentContainer) node).getComponents()) {
                checkNode(comp, context);
            }
        }
        // The node contains <controller> nodes. The <controller> may contain NF <interface> nodes.
        if (node instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) node).getController();
            if (ctrl != null) {
                checkNode(ctrl, context);
            }

        }
    }

}
