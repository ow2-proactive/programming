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
package org.objectweb.proactive.core.component.adl.types;

import java.util.Map;

import org.apache.log4j.Logger;
import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ContextMap;
import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The {@link PATypeBuilder} makes the calls to the GCM API to create the GCM InterfaceType,
 * and the GCM ComponentType.
 * 
 * @author The ProActive Team
 */
public class PATypeBuilder implements PATypeBuilderItf {

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);

    @SuppressWarnings("unchecked")
    public Object createInterfaceType(String name, String signature, String role, String contingency,
            String cardinality, Object context) throws Exception {
        return createInterfaceType(name, signature, role, contingency, cardinality,
                (Map<Object, Object>) context);
    }

    /**
     * Default: isInternal = false
     */
    public InterfaceType createInterfaceType(final String name, final String signature, final String role,
            final String contingency, final String cardinality, final Map<Object, Object> context)
            throws Exception {

        // default: isInternal = false;
        return createInterfaceType(name, signature, role, contingency, cardinality, false, context);
    }

    /**
     * Makes the call to the GCM API to create the GCM InterfaceType, using the PAGCMTypeFactory.
     */
    public InterfaceType createInterfaceType(String name, String signature, String role, String contingency,
            String cardinality, boolean isInternal, Map<Object, Object> context) throws Exception {

        ClassLoader loader = ClassLoaderHelper.getClassLoader(this, context);
        // bootstrap component
        Component bootstrap = null;
        if (context != null) {
            bootstrap = (Component) context.get("bootstrap");
        }
        if (bootstrap == null) {
            Map<Object, Object> ctxt = ContextMap.instance();
            ctxt.put("classloader", loader);
            bootstrap = GCM.getBootstrapComponent(ctxt);
        }
        // type factory
        PAGCMTypeFactory patf = (PAGCMTypeFactory) GCM.getGCMTypeFactory(bootstrap);

        // TODO : cache already created types ?
        boolean client = "client".equals(role) || "internal-client".equals(role);
        boolean optional = "optional".equals(contingency);
        // default cardinality = singleton
        String checkedCardinality = (cardinality == null) ? GCMTypeFactory.SINGLETON_CARDINALITY
                : cardinality;

        return patf.createGCMItfType(name, signature, client, optional, checkedCardinality, isInternal);
    }

    @SuppressWarnings("unchecked")
    public Object createComponentType(String name, Object[] interfaceTypes, Object context) throws Exception {
        return createComponentType(name, interfaceTypes, (Map<Object, Object>) context);
    }

    /**
     * Default: empty NF InterfaceType
     */
    public ComponentType createComponentType(final String name, final Object[] interfaceTypes,
            final Map<Object, Object> context) throws Exception {
        // pass an empty array of NF InterfaceType
        return createComponentType(name, interfaceTypes, new Object[] {}, context);
    }

    /**
     * Makes the call to the GCM API to create the GCM ComponentType, using the PAGCMTypeFactory,
     * obtained from the bootstrap component.
     */
    public ComponentType createComponentType(String name, Object[] fInterfaceTypes,
            Object[] nfInterfaceTypes, Map<Object, Object> context) throws Exception {

        logger.debug("[PATypeBuilder] Building types for component [" + name + "]");

        ClassLoader loader = ClassLoaderHelper.getClassLoader(this, context);
        // bootstrap component
        Component bootstrap = null;
        if (context != null) {
            bootstrap = (Component) context.get("bootstrap");
        }
        if (bootstrap == null) {
            Map<Object, Object> ctxt = ContextMap.instance();
            ctxt.put("classloader", loader);
            bootstrap = GCM.getBootstrapComponent(ctxt);
        }
        // type factory
        PAGCMTypeFactory patf = (PAGCMTypeFactory) GCM.getGCMTypeFactory(bootstrap);

        // type copy
        InterfaceType[] fItfsTypes = new InterfaceType[fInterfaceTypes.length];
        for (int i = 0; i < fInterfaceTypes.length; i++) {
            fItfsTypes[i] = (InterfaceType) fInterfaceTypes[i];
        }
        InterfaceType[] nfItfsTypes = new InterfaceType[nfInterfaceTypes.length];
        for (int i = 0; i < nfInterfaceTypes.length; i++) {
            nfItfsTypes[i] = (InterfaceType) nfInterfaceTypes[i];
        }

        return patf.createFcType(fItfsTypes, nfItfsTypes);
    }

}
