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

import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.core.xml.VariableContractType;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.xml.sax.InputSource;


/**
 * This class handles all the parsing of Variable Contract XML tags.
 * @author The ProActive Team
 *
 */
public class VariablesHandler extends PassiveCompositeUnmarshaller implements ProActiveDescriptorConstants {
    protected VariableContractImpl variableContract;

    public VariablesHandler(VariableContractImpl variableContract) {
        super(false);
        this.variableContract = variableContract;

        this.addHandler(VARIABLES_DESCRIPTOR_TAG, new VariableHandler(VARIABLES_DESCRIPTOR_TAG));
        this.addHandler(VARIABLES_PROGRAM_TAG, new VariableHandler(VARIABLES_PROGRAM_TAG));
        this.addHandler(VARIABLES_JAVAPROPERTY_TAG, new VariableHandler(VARIABLES_JAVAPROPERTY_TAG));
        this.addHandler(VARIABLES_PROGRAM_DEFAULT_TAG, new VariableHandler(VARIABLES_PROGRAM_DEFAULT_TAG));
        this.addHandler(VARIABLES_DESCRIPTOR_DEFAULT_TAG, new VariableHandler(VARIABLES_DESCRIPTOR_DEFAULT_TAG));
        this.addHandler(VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG,
                        new VariableHandler(VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG));
        this.addHandler(VARIABLES_JAVAPROPERTY_PROGRAM_TAG, new VariableHandler(VARIABLES_JAVAPROPERTY_PROGRAM_TAG));

        this.addHandler(VARIABLES_INCLUDE_XML_FILE_TAG, new IncludeXMLFileHandler());
        this.addHandler(VARIABLES_INCLUDE_PROPERTY_FILE_TAG, new IncludePropertiesFileHandler());
    }

    @Override
    protected void notifyEndActiveHandler(String name, UnmarshallerHandler activeHandler)
            throws org.xml.sax.SAXException {
        //Once the variables have been defined, we load pending values from the javaproperties
        variableContract.setJavaPropertiesValues();
    }

    /**
     * Creates a SAX parser on the specified file for variables. This is used
     * when including a variable contract defined on a different file.
     * @param filename the full path to the file
     */
    public static void createVariablesHandler(String filename, VariableContractImpl variableContract) {
        VariablesFileHandler vfh = new VariablesFileHandler(variableContract);

        org.objectweb.proactive.core.xml.io.StreamReader sr;

        //String file = VariablesHandler.class.getResource(filename).getPath();
        InputSource source = new org.xml.sax.InputSource(filename);
        try {
            sr = new org.objectweb.proactive.core.xml.io.StreamReader(source, vfh);
            sr.read();
            //return (cast) vh.getResultObject();
        } catch (Exception e) {
            logger.error("Unable to load Variable Contract from:" + filename);
            e.printStackTrace();
        }
    }

    public static class VariablesFileHandler extends PassiveCompositeUnmarshaller {
        VariablesFileHandler(VariableContractImpl variableContract) {
            super(false);
            this.addHandler(VARIABLES_TAG, new VariablesHandler(variableContract));
        }
    }

    private class VariableHandler extends BasicUnmarshaller {
        VariableContractType varType;

        String varStringType;

        VariableHandler(String varStringType) {
            this.varType = VariableContractType.getType(varStringType);
            this.varStringType = varStringType;
        }

        @Override
        public void startContextElement(String tag, Attributes attributes) throws org.xml.sax.SAXException {
            if (this.varType == null) {
                throw new org.xml.sax.SAXException("Ilegal Descriptor Variable Type: " + varStringType);
            }

            // Variable Name
            String name = attributes.getValue("name");
            if (!checkNonEmpty(name)) {
                throw new org.xml.sax.SAXException("Variable has no name");
            }

            String value = attributes.getValue("value");
            if (value == null) {
                value = "";
            }
            // Define and set variables into the contract
            variableContract.setDescriptorVariable(name, value, varType);
        }
    }

    private class IncludeXMLFileHandler extends BasicUnmarshaller {
        IncludeXMLFileHandler() {
        }

        @Override
        public void startContextElement(String tag, Attributes attributes) throws org.xml.sax.SAXException {
            String file = attributes.getValue("location");
            if (checkNonEmpty(file)) {
                // Specific processing for loading an xml file
                variableContract.loadXML(file);
                return;
            }
        }
    }

    private class IncludePropertiesFileHandler extends BasicUnmarshaller {
        IncludePropertiesFileHandler() {
        }

        @Override
        public void startContextElement(String tag, Attributes attributes) throws org.xml.sax.SAXException {
            String file = attributes.getValue("location");
            if (checkNonEmpty(file)) {
                // Specific processing for loading a file
                variableContract.load(file);
                return;
            }
        }
    }
}
