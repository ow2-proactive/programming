/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2
 * or a different license than the GPL.
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

import org.etsi.uri.gcm.api.type.GCMTypeFactory;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ContextMap;
import org.objectweb.fractal.adl.types.FractalTypeBuilder;
import org.objectweb.fractal.adl.util.ClassLoaderHelper;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactoryImpl;


/**
 * @author The ProActive Team
 */
public class PATypeBuilder extends FractalTypeBuilder {
    @Override
    public Object createInterfaceType(final String name, final String signature, final String role,
            final String contingency, final String cardinality, final Object context) throws Exception {
        // TODO : cache already created types ?
        boolean client = "client".equals(role);
        boolean optional = "optional".equals(contingency);

        String checkedCardinality = (cardinality == null) ? GCMTypeFactory.SINGLETON_CARDINALITY
                : cardinality;

        // TODO_M should use bootstrap type factory with extended createFcItfType method
        return PAGCMTypeFactoryImpl.instance().createGCMItfType(name, signature, client, optional,
                checkedCardinality);
    }

    @Override
    public Object createComponentType(final String name, final Object[] interfaceTypes, final Object context)
            throws Exception {
        ClassLoader loader = ClassLoaderHelper.getClassLoader(this, context);

        Component bootstrap = null;
        if (context != null) {
            bootstrap = (Component) ((Map) context).get("bootstrap");
        }
        if (bootstrap == null) {
            Map ctxt = ContextMap.instance(); // new HashMap();
            ctxt.put("classloader", loader);
            bootstrap = Utils.getBootstrapComponent(ctxt);
        }
        InterfaceType[] types = new InterfaceType[interfaceTypes.length];
        for (int i = 0; i < types.length; ++i) {
            types[i] = (InterfaceType) interfaceTypes[i];
        }
        return GCM.getGCMTypeFactory(bootstrap).createFcType(types);
    }
}
