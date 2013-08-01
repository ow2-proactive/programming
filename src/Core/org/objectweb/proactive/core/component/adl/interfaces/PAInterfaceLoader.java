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
package org.objectweb.proactive.core.component.adl.interfaces;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Definition;
import org.objectweb.fractal.adl.components.Component;
import org.objectweb.fractal.adl.components.ComponentContainer;
import org.objectweb.fractal.adl.error.NodeErrorLocator;
import org.objectweb.fractal.adl.implementations.Controller;
import org.objectweb.fractal.adl.implementations.ControllerContainer;
import org.objectweb.fractal.adl.interfaces.Interface;
import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
import org.objectweb.fractal.adl.interfaces.InterfaceErrors;
import org.objectweb.fractal.adl.interfaces.InterfaceLoader;
import org.objectweb.proactive.core.component.adl.types.PATypeInterface;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The {@link PAInterfaceLoader} extends the {@link InterfaceLoader} to look for
 * interfaces defined inside the &lt;controller&gt; tag (i.e. NF Interfaces).
 * 
 * Such interfaces are marked as NF by adding a decoration to the node.
 * 
 * @author The ProActive Team
 *
 */

public class PAInterfaceLoader extends InterfaceLoader {

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    /** 
     * Checks the tree considering the initial &lt;definition&gt; as F component
     */
    @Override
    public Definition load(final String name, final Map<Object, Object> context) throws ADLException {
        final Definition d = clientLoader.load(name, context);
        checkNode(d, true);
        return d;
    }

    /**
     * Looks for containers of &lt;interface&gt; nodes.
     * 
     * @param node
     * @throws ADLException
     */
    protected void checkNode(final Object node, boolean functional) throws ADLException {
        //logger.debug("[PAInterfaceLoader] Analyzing node "+ node.toString()); 
        if (node instanceof InterfaceContainer) {
            checkInterfaceContainer((InterfaceContainer) node, functional);
        }
        // interfaces defined inside a <component> node are F, even if the component maybe NF
        if (node instanceof ComponentContainer) {
            for (final Component comp : ((ComponentContainer) node).getComponents()) {
                checkNode(comp, true);
            }
        }
        // interfaces defined inside a <controller> node are NF (i.e. they belong to the membrane)
        if (node instanceof ControllerContainer) {
            Controller ctrl = ((ControllerContainer) node).getController();
            if (ctrl != null) {
                checkNode(ctrl, false);
            }
        }
    }

    /**
     * Checks &lt;interface&gt; nodes, and marks them as F or NF.<br/>
     * This allows duplicate interface names if one of the interfaces is F and the other one is NF
     * 
     * @param container
     * @param functional
     * @throws ADLException
     */
    protected void checkInterfaceContainer(final InterfaceContainer container, boolean functional)
            throws ADLException {

        final Map<String, Interface> names = new HashMap<String, Interface>();
        for (final Interface itf : container.getInterfaces()) {
            if (itf.getName() == null) {
                throw new ADLException(InterfaceErrors.INTERFACE_NAME_MISSING, itf);
            }

            logger.debug("[PAInterfaceLoader] Found interface " + itf.toString() + " " +
                (functional ? "F" : "NF"));

            if (itf.astGetAttributes().get(PATypeInterface.INTERCEPTORS_ATTRIBUTE_NAME) != null) {
                String[] interceptors = itf.astGetAttributes().get(
                        PATypeInterface.INTERCEPTORS_ATTRIBUTE_NAME).split(",");

                for (String interceptor : interceptors) {
                    if (interceptor.startsWith("this.")) {
                        continue;
                    } else {
                        String[] interceptorElements = interceptor.split("\\.");

                        if (interceptorElements.length == 2) {
                            String nfComponentName = interceptorElements[0];
                            String interfaceName = interceptorElements[1];
                            boolean found = false;

                            if (container instanceof ControllerContainer) {
                                Controller controller = ((ControllerContainer) container).getController();

                                if ((controller != null) && (controller instanceof ComponentContainer)) {
                                    for (Component nfComponent : ((ComponentContainer) controller)
                                            .getComponents()) {
                                        if (nfComponentName.equals(nfComponent.getName())) {
                                            if (nfComponent instanceof InterfaceContainer) {
                                                for (Interface itfNfComponent : ((InterfaceContainer) nfComponent)
                                                        .getInterfaces()) {
                                                    if (interfaceName.equals(itfNfComponent.getName())) {
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }

                                            break;
                                        }
                                    }

                                    if (found) {
                                        continue;
                                    }
                                }
                            }
                        }

                        throw new ADLException(PAInterfaceErrors.WRONG_INTERCEPTOR_NAME, itf, interceptor,
                            itf.getName());
                    }
                }
            }

            if (!functional) {
                itf.astSetDecoration("NF", true);
            }

            final Interface previousDefinition = names.put(itf.getName(), itf);
            if (previousDefinition != null) {
                throw new ADLException(InterfaceErrors.DUPLICATED_INTERFACE_NAME, itf, itf.getName(),
                    new NodeErrorLocator(previousDefinition));
            }
        }
    }

}
