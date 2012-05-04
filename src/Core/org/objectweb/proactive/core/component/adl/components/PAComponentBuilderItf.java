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

import java.util.Map;

import org.objectweb.fractal.adl.components.ComponentBuilder;


/**
 * The {@link PAComponentBuilderItf} extends the {@link ComponentBuilder} interface,
 * to indicate if the subcomponent to add is functional or non-functional.<br/>
 * 
 * The implementation may, so, determine if the subcomponent is added on the functional content
 * (ContentController) or in the membrane (MembraneController).<br/>
 * 
 * It also includes a method to explicitly start the membrane, so that the functional lifecycle can be
 * properly started later.<br/>
 * 
 * @author The ProActive Team
 *
 */
public interface PAComponentBuilderItf extends ComponentBuilder {

    void addComponent(Object superComponent, Object subComponent, String name, boolean isFunctional,
            Map<Object, Object> context) throws Exception;

    void startMembrane(Object component, Map<Object, Object> context) throws Exception;

}
