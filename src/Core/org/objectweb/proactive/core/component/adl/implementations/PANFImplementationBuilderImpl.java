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
package org.objectweb.proactive.core.component.adl.implementations;

import java.util.Map;

import org.objectweb.proactive.core.component.ContentDescription;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.adl.nodes.VirtualNode;


/**
 * This class is used by the NFCompiler factory component that is instantiated by the NFFactory.
 * 
 * When the NFCompiler is used, it replaces the {@link PAImplementationBuilder} by this class,
 * whose only difference is to force the 'isFunctional' parameters in the super class, consequently
 * creating a NF component. 
 * 
 * @author The ProActive Team
 */
public class PANFImplementationBuilderImpl extends PAImplementationBuilderImpl {
    @Override
    public Object createComponent(Object type, String name, String definition,
            ControllerDescription controllerDesc, ContentDescription contentDesc, VirtualNode adlVN,
            boolean isFunctional, Map<Object, Object> context) throws Exception {

        // forces the component to be NF
        return super.createComponent(type, name, definition, controllerDesc, contentDesc, adlVN, false,
                context);

    }
}
