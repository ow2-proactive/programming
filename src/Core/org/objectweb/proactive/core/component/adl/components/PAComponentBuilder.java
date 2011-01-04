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
package org.objectweb.proactive.core.component.adl.components;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.components.ComponentBuilder;
import org.objectweb.fractal.api.Component;


/**
 * A ProActive based implementation of the {@link ComponentBuilder} interface.
 * This implementation uses the Fractal API to add and start components.
 * It slightly differs from the FractalComponentBuilder class : the name of the component
 * is not specified in this addition operation, but when the component is instantiated.
 *
 */
public class PAComponentBuilder implements ComponentBuilder {
    // --------------------------------------------------------------------------
    // Implementation of the ComponentBuilder interface
    // --------------------------------------------------------------------------
    public void addComponent(final Object superComponent, final Object subComponent, final String name,
            final Object context) throws Exception {
        GCM.getContentController((Component) superComponent).addFcSubComponent((Component) subComponent);
        // as opposed  to the standard fractal implementation, we do not set
        // the name of the component here because :
        // 1. it is already name at instantiation time
        // 2. it could be a group of components, and we do not want to give the
        // same name to all the elements of the group
        //    try {
        //      GCM.getNameController((Component)subComponent).setFcName(name);
        //    } catch (NoSuchInterfaceException ignored) {
        //    }
    }

    public void startComponent(final Object component, final Object context) throws Exception {
    }
}
