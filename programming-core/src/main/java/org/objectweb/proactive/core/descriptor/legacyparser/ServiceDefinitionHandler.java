/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.descriptor.legacyparser;

import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptorInternal;
import org.objectweb.proactive.core.descriptor.services.RMIRegistryLookupService;
import org.objectweb.proactive.core.descriptor.services.UniversalService;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.SAXException;


public class ServiceDefinitionHandler extends PassiveCompositeUnmarshaller implements ProActiveDescriptorConstants {
    ProActiveDescriptorInternal pad;

    protected String serviceId;

    public ServiceDefinitionHandler(ProActiveDescriptorInternal pad) {
        super(false);
        this.pad = pad;
        this.addHandler(RMI_LOOKUP_TAG, new RMILookupHandler());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler
     * (java.lang.String, org.objectweb.proactive.core.xml.handler.UnmarshallerHandler)
     */
    @Override
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler) throws SAXException {
        UniversalService service = (UniversalService) activeHandler.getResultObject();
        pad.addService(serviceId, service);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(java.lang.
     * String, org.objectweb.proactive.core.xml.io.Attributes)
     */
    @Override
    public void startContextElement(String name, Attributes attributes) throws SAXException {
        this.serviceId = attributes.getValue("id");
    }

    protected class RMILookupHandler extends BasicUnmarshaller {
        public RMILookupHandler() {
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            String lookupUrl = attributes.getValue("url");
            RMIRegistryLookupService rmiService = new RMIRegistryLookupService(lookupUrl);
            setResultObject(rmiService);
        }
    } // end of inner class RMILookupHandler
}
