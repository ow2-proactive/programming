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

import org.objectweb.fractal.adl.types.TypeBuilder;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;


/**
 * The {@link PATypeBuilderItf} extends the {@link TypeBuilder} interface to provide
 * a method that creates a ComponentType (technically, a PAComponentType) using an array
 * of F InterfaceTypes, and a set of NF InterfaceTypes. 
 * 
 * It also adds a method that considers the internal/external attribute for the {@link InterfaceType}.
 * 
 * @author The ProActive Team
 *
 */
public interface PATypeBuilderItf extends TypeBuilder {

    InterfaceType createInterfaceType(String name, String signature, String role, String contingency,
            String cardinality, Map<Object, Object> context) throws Exception;

    InterfaceType createInterfaceType(String name, String signature, String role, String contingency,
            String cardinality, boolean isInternal, Map<Object, Object> context) throws Exception;

    ComponentType createComponentType(String name, Object[] interfaceTypes, Map<Object, Object> context)
            throws Exception;

    ComponentType createComponentType(String name, Object[] fInterfaceTypes, Object[] nfInterfaceTypes,
            Map<Object, Object> context) throws Exception;
}
