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
package org.objectweb.proactive.core.component;

import java.io.Serializable;
import java.util.Map;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.component.identity.PAComponentImpl;


/**
 * Fractal implementation-specific description of the controllers of components.
 * It is currently used to specify the hierarchical type and the name of the
 * components. <br>
 * <p>
 * It is also a place to specify custom controllers for a given component ; the
 * configuration of the controllers is described in a properties file whose
 * location can be given as a parameter. <br>
 * The controllers configuration file is simple : it associates the signature of
 * a controller interface with the implementation that has to be used. <br>
 * During the construction of the component, the membrane is automatically
 * constructed with these controllers. The controllers are linked together, and
 * requests targeting a control interface visit the different controllers until
 * they find the suitable controller, and then the request is executed on this
 * controller.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class ControllerDescription implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 51L;
    protected String hierarchicalType;
    protected String name;
    protected boolean synchronous = false;
    public static final String DEFAULT_COMPONENT_CONFIG_FILE_LOCATION = "/org/objectweb/proactive/core/component/config/default-component-config.xml";
    protected String controllersConfigFileLocation;
    protected Map<String, String> controllersSignatures;
    protected boolean conFigFileIsDefined;

    /**
     * a no-arg constructor (used in the ProActive parser)
     *
     */
    public ControllerDescription() {
        this((String) null, (String) null, (String) null, false);
    }

    /**
     * constructor
     * @param name the name of the component
     * @param hierarchicalType the hierachical type of the component. See {@link Constants}
     */
    public ControllerDescription(String name, String hierarchicalType) {
        this(name, hierarchicalType, null, false);
    }

    /**
     * Constructor for ControllerDescription.
     * @param name String
     * @param hierarchicalType String
     * @param synchronous boolean
     */
    public ControllerDescription(String name, String hierarchicalType, boolean synchronous) {
        this(name, hierarchicalType, null, synchronous);
    }

    /**
     * Constructor for ControllerDescription.
     * @param name String
     * @param hierarchicalType String
     * @param controllersConfigFileLocation String
     */
    public ControllerDescription(String name, String hierarchicalType, String controllersConfigFileLocation) {
        this(name, hierarchicalType, controllersConfigFileLocation, false);
    }

    /**
     * Constructor for ControllerDescription.
     * @param name String
     * @param hierarchicalType String
     * @param controllersConfigFileLocation String
     * @param synchronous boolean
     */
    public ControllerDescription(String name, String hierarchicalType, String controllersConfigFileLocation,
            boolean synchronous) {
        //FIXME use an enum to avoid unknow hierarchicalType
        conFigFileIsDefined = true;
        this.hierarchicalType = hierarchicalType;
        this.name = name;
        if (!Constants.PRIMITIVE.equals(hierarchicalType)) {
            this.synchronous = synchronous;
        }
        if (controllersConfigFileLocation == null) {
            this.controllersConfigFileLocation = DEFAULT_COMPONENT_CONFIG_FILE_LOCATION;
        } else {
            this.controllersConfigFileLocation = controllersConfigFileLocation;
        }
        controllersSignatures = PAComponentImpl.loadControllerConfiguration(
                this.controllersConfigFileLocation).getControllers();
    }

    /**
     * copy constructor (clones the object)
     * @param controllerDesc the ControllerDescription to copy.
     */
    public ControllerDescription(ControllerDescription controllerDesc) {
        this(controllerDesc.name, controllerDesc.hierarchicalType,
                controllerDesc.controllersConfigFileLocation, controllerDesc.synchronous);
    }

    public ControllerDescription(String name, String hierarchicalType, boolean synchronous,
            boolean withConfigFile) {
        conFigFileIsDefined = withConfigFile;
        this.hierarchicalType = hierarchicalType;
        this.name = name;
        if (!Constants.PRIMITIVE.equals(hierarchicalType)) {
            this.synchronous = synchronous;
        }
        if (!withConfigFile) { //Without specifying a specific configuration described in a file
            this.controllersConfigFileLocation = null;
        } else {
            this.controllersConfigFileLocation = DEFAULT_COMPONENT_CONFIG_FILE_LOCATION;
            controllersSignatures = PAComponentImpl.loadControllerConfiguration(
                    this.controllersConfigFileLocation).getControllers();
        }
    }

    /**
     * Returns the hierarchicalType.
     * @return String
     */
    public String getHierarchicalType() {
        return hierarchicalType;
    }

    /**
     * setter for hierarchical type
     * @param string hierarchical type. See {@link Constants}
     */
    public void setHierarchicalType(String string) {
        hierarchicalType = string;
    }

    /**
     * getter for the name
     * @return the name of the component
     */
    public String getName() {
        return name;
    }

    /**
     * setter for the name
     * @param name name of the component
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method isSynchronous.
     * @return boolean
     */
    public boolean isSynchronous() {
        return synchronous;
    }

    /**
     * Method getControllersConfigFileLocation.
     * @return String
     */
    public String getControllersConfigFileLocation() {
        return controllersConfigFileLocation;
    }

    public Map<String, String> getControllersSignatures() {
        return controllersSignatures;
    }

    public boolean configFileIsSpecified() {
        return conFigFileIsDefined;
    }
}
