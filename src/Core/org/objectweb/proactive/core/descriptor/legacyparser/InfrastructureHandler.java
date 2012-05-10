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
package org.objectweb.proactive.core.descriptor.legacyparser;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;


/**
 * This class receives deployment events
 *
 * @author The ProActive Team
 * @version      1.0
 */
class InfrastructureHandler extends PassiveCompositeUnmarshaller implements ProActiveDescriptorConstants {
    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //
    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //
    public InfrastructureHandler(ProActiveDescriptorInternal proActiveDescriptor) {
        super(false);
        CollectionUnmarshaller ch = new CollectionUnmarshaller();
        ch.addHandler(PROCESS_DEFINITION_TAG, new ProcessDefinitionHandler(proActiveDescriptor));
        this.addHandler(PROCESSES_TAG, ch);
        ch.addHandler(SERVICE_DEFINITION_TAG, new ServiceDefinitionHandler(proActiveDescriptor));
        this.addHandler(SERVICES_TAG, ch);
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    //
    //  ----- PROTECTED METHODS -----------------------------------------------------------------------------------
    //
    //
    //  ----- INNER CLASSES -----------------------------------------------------------------------------------
    //
}
