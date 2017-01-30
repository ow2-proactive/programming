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
import org.objectweb.proactive.core.descriptor.data.VirtualNodeInternal;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;


/**
 * This class receives main_definition events
 *
 * @author The ProActive Team
 * @version 1.0,  2005/07/07
 * @since   ProActive
 */
class MainDefinitionHandler extends PassiveCompositeUnmarshaller implements ProActiveDescriptorConstants {
    private ProActiveDescriptorInternal proActiveDescriptor;

    //
    //  ----- PRIVATE MEMBERS -----------------------------------------------------------------------------------
    //
    //
    //  ----- CONSTRUCTORS -----------------------------------------------------------------------------------
    //
    public MainDefinitionHandler() {
    }

    public MainDefinitionHandler(ProActiveDescriptorInternal proActiveDescriptor) {
        super();
        this.proActiveDescriptor = proActiveDescriptor;
        this.addHandler(ARG_TAG, new ArgHandler(proActiveDescriptor));
        this.addHandler(MAP_TO_VIRTUAL_NODE_TAG, new MapToVirtualNodeHandler(proActiveDescriptor));
    }

    @Override
    public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
        String id = attributes.getValue("id");
        String className = attributes.getValue("class");

        if (!checkNonEmpty(className)) {
            throw new org.xml.sax.SAXException("class Tag without any mainDefinition defined");
        }

        proActiveDescriptor.createMainDefinition(id);

        proActiveDescriptor.setMainDefined(true);
        proActiveDescriptor.mainDefinitionSetMainClass(className);
    }

    @Override
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
            throws org.xml.sax.SAXException {
        if (name.equals(ARG_TAG)) {
            //System.out.println("end of a arg tag") ;
        }

        if (name.equals(MAP_TO_VIRTUAL_NODE_TAG)) {
            //System.out.println("end of a mapToVirtualNode tag") ;
        }
    }

    //
    //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
    //
    //
    // -- implements UnmarshallerHandler ------------------------------------------------------
    //
    //
    //  ----- PRIVATE METHODS -----------------------------------------------------------------------------------
    //
    //
    //  ----- INNER CLASSES -----------------------------------------------------------------------------------
    //	
    private class ArgHandler extends BasicUnmarshaller {
        ProActiveDescriptorInternal proActiveDescriptor;

        private ArgHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            this.proActiveDescriptor = proActiveDescriptor;
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            String arg = attributes.getValue("value");

            //System.out.println("enter in a arg node : " + arg);
            if (!checkNonEmpty(arg)) {
                throw new org.xml.sax.SAXException("value Tag without any arg defined");
            }

            proActiveDescriptor.mainDefinitionAddParameter(arg);
        }
    }

    private class MapToVirtualNodeHandler extends BasicUnmarshaller {
        ProActiveDescriptorInternal proActiveDescriptor;

        private MapToVirtualNodeHandler(ProActiveDescriptorInternal proActiveDescriptor) {
            this.proActiveDescriptor = proActiveDescriptor;
        }

        @Override
        public void startContextElement(String name, Attributes attributes) throws org.xml.sax.SAXException {
            String virtualNode = attributes.getValue("value");

            if (!checkNonEmpty(virtualNode)) {
                throw new org.xml.sax.SAXException("value Tag without any mapToVirtualNode defined");
            }

            VirtualNodeInternal vn = proActiveDescriptor.createVirtualNode(virtualNode, false, true);

            proActiveDescriptor.mainDefinitionAddVirtualNode(vn);
        }
    }
}
