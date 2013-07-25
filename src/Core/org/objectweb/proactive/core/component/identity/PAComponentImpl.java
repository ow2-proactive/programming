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
package org.objectweb.proactive.core.component.identity;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.NameController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.migration.MigrationException;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.PAInterfaceImpl;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.config.ComponentConfigurationHandler;
import org.objectweb.proactive.core.component.control.AbstractPAController;
import org.objectweb.proactive.core.component.control.ControllerState;
import org.objectweb.proactive.core.component.control.ControllerStateDuplication;
import org.objectweb.proactive.core.component.control.PABindingControllerImpl;
import org.objectweb.proactive.core.component.control.PAContentControllerImpl;
import org.objectweb.proactive.core.component.control.PAController;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleControllerImpl;
import org.objectweb.proactive.core.component.control.PAInterceptorControllerImpl;
import org.objectweb.proactive.core.component.control.PAMembraneControllerImpl;
import org.objectweb.proactive.core.component.control.PANameControllerImpl;
import org.objectweb.proactive.core.component.exceptions.InterfaceGenerationFailedException;
import org.objectweb.proactive.core.component.gen.MetaObjectInterfaceClassGenerator;
import org.objectweb.proactive.core.component.group.PAComponentGroup;
import org.objectweb.proactive.core.component.representative.PAComponentRepresentativeFactory;
import org.objectweb.proactive.core.component.type.PAComponentType;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * The base class for managing components. It builds the "membrane" in the
 * Fractal terminology : the controllers of the components.
 *
 * @author The ProActive Team
 */
public class PAComponentImpl implements PAComponent, Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    protected static final Logger loggerADL = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);
    private transient PAComponent representativeOnMyself = null;
    private ComponentParameters componentParameters;
    private Map<String, Interface> serverItfs = new HashMap<String, Interface>();
    private Map<String, Interface> clientItfs = new HashMap<String, Interface>();
    private Map<String, Interface> nfServerItfs = new HashMap<String, Interface>();
    private Map<String, Interface> nfClientItfs = new HashMap<String, Interface>();
    private Map<String, Interface> collectionItfsMembers = new HashMap<String, Interface>();
    private Map<String, Interface> collectionNfItfsMembers = new HashMap<String, Interface>();
    private Body body;

    public PAComponentImpl() {
    }

    /**
     * Constructor for PAComponent.
     *
     * @param componentParameters
     * @param myBody
     *            a reference on the body (required notably to get a reference
     *            on the request queue, used to control the life cycle of the
     *            component)
     */
    public PAComponentImpl(ComponentParameters componentParameters, Body myBody) {
        this.body = myBody;
        this.componentParameters = componentParameters;

        loggerADL.debug("[PAComponentImpl] Construction of " + this.componentParameters.getName());

        // 1. control interfaces
        Type componentType = componentParameters.getComponentType();
        loggerADL.debug("[PAComponentImpl] Type instance of PAComponentType? " +
            (componentType instanceof PAComponentType));
        loggerADL.debug("[PAComponentImpl] Hierarchy: " + this.componentParameters.getHierarchicalType());
        loggerADL.debug("[PAComponentImpl]  FType: " +
            ((PAComponentType) componentType).getFcInterfaceTypes().length);
        loggerADL.debug("[PAComponentImpl] NFType: " +
            ((PAComponentType) componentType).getNfFcInterfaceTypes().length);
        loggerADL
                .debug("[PAComponentImpl] Config File: " +
                    (this.componentParameters.getControllerDescription().configFileIsSpecified() ? this.componentParameters
                            .getControllerDescription().getControllersConfigFileLocation()
                            : "---"));

        // 1. Add NF interfaces
        if ((componentType instanceof PAComponentType)) {
            loggerADL.debug("[PAComponentImpl] GENERAL CREATION of controller interfaces");
            addControllerInterfaces();
        }

        // 2. external functional interfaces
        addFunctionalInterfaces();

        // 3. Call initController for each controller interface
        for (Interface itf : this.nfServerItfs.values()) {
            // Warning: some interfaces maybe still null. If some object controller is added later (via the membrane setObjectController method), the corresponding initController
            // method must be called.
            // A multicast client interface has an implementation (so, it's != null), but it is not a PAController
            if (((PAInterfaceImpl) itf).getFcItfImpl() instanceof PAController) {
                PAController itfImpl = (PAController) ((PAInterfaceImpl) itf).getFcItfImpl();
                if (itfImpl != null) { // due to non-functional interface implemented using component
                    itfImpl.initController();
                }
            }
        }

        logger.debug("created component : " + this.componentParameters.getControllerDescription().getName());
        loggerADL.debug("[PAComponentImpl]  FType: (" + (serverItfs.size() + clientItfs.size()) + ")");
        loggerADL.debug("[PAComponentImpl] NFType: (" + (nfServerItfs.size() + nfClientItfs.size()) + ")");
    }

    /**
     * Create and add the NF Interfaces using both a component configuration file, and an NF Type.<br/>
     * <ol>
     * <li>Creates NF Interfaces from the declared NF Type. If there is non NF Type, this step is ignored.</li>
     * <li>Creates Object Controllers from the Controllers Configuration File. If some of them duplicates interfaces declared in the NF Type, they are ignored.
     *     In other words, the declared NF Type has priority over the Controllers Configuration File.</li>
     * <li>Checks that the mandatory interfaces are declared. In particular, if the membrane controller has been previously declared, it is created here.</li>
     * <li>Updates the NF Type of the Component.
     * </ol>
     * 
     * NOTE: When an NF interface is described in the ADL file, the Factory adds the 'membrane-controller' default implementation automatically, so that it is not necessary
     * to specify the 'membrane-controller' explicitly.
     */
    private void addControllerInterfaces() {

        // Vector to collect the real NF type
        Vector<InterfaceType> nfType = new Vector<InterfaceType>();

        boolean isPrimitive = Constants.PRIMITIVE.equals(this.componentParameters.getHierarchicalType());

        //------------------------------------------------------------
        // 1. Create interfaces from the declared NF type
        // Read the NF Type
        PAComponentType componentType = (PAComponentType) this.componentParameters.getComponentType();
        InterfaceType[] nfItfTypes = componentType.getNfFcInterfaceTypes();
        PAGCMInterfaceType[] pagcmNfItfTypes = new PAGCMInterfaceType[nfItfTypes.length];
        System.arraycopy(nfItfTypes, 0, pagcmNfItfTypes, 0, nfItfTypes.length);

        for (PAGCMInterfaceType pagcmNfItfType : pagcmNfItfTypes) {

            String itfName = pagcmNfItfType.getFcItfName();
            PAInterface itfRef = null;
            try {
                // some controllers interfaces are ignored
                if (specialCasesForNfType(itfName, pagcmNfItfType, isPrimitive))
                    continue;

                // Multicast AND client
                if (pagcmNfItfType.isGCMMulticastItf() && pagcmNfItfType.isFcClientItf()) {
                    //TODO : This piece of code has to be tested
                    itfRef = createInterfaceOnGroupOfDelegatees(pagcmNfItfType);
                }
                // standard case, regular singleton interface
                else {
                    // generate interface for the current controller, owned by this component.
                    itfRef = MetaObjectInterfaceClassGenerator.instance().generateInterface(
                            pagcmNfItfType.getFcItfName(), this, pagcmNfItfType, pagcmNfItfType.isInternal(),
                            false);
                }

                // add generated interface to the nfItfs map, and the nfType vector
                if (existsNfInterface(itfName)) {
                    throw new Exception("Duplicated NF interface '" + itfName + "'");
                }
                if (pagcmNfItfType.isFcClientItf())
                    this.nfClientItfs.put(itfName, itfRef);
                else
                    this.nfServerItfs.put(itfName, itfRef);
                nfType.add((InterfaceType) itfRef.getFcItfType());

            } catch (Exception e) {
                throw new ProActiveRuntimeException("Could not create NF interface '" + itfName +
                    "' while instantiating component '" + this.componentParameters.getName() +
                    "'. Check the declared NF type.\n" + e.getMessage(), e);
            }
        }

        //------------------------------------------------------------        
        // 2. Create interfaces from the Controller Configuration file

        // read the Controller Configuration File
        Map<String, String> controllerEntries = null;
        if (this.componentParameters.getControllerDescription().configFileIsSpecified()) {
            // Parse controller config file
            String controllersConfigFileLocation = this.componentParameters.getControllerDescription()
                    .getControllersConfigFileLocation();
            loggerADL.debug("[PAComponentImpl] Parsing Controller Configuration File: " +
                controllersConfigFileLocation);
            ComponentConfigurationHandler componentConfiguration = PAComponentImpl
                    .loadControllerConfiguration(controllersConfigFileLocation);
            controllerEntries = componentConfiguration.getControllers();

            // Create controller objects from the Controller Configuration File
            for (Map.Entry<String, String> controllerEntry : controllerEntries.entrySet()) {

                String controllerName = null;
                String controllerItfName = controllerEntry.getKey();
                String controllerClassName = controllerEntry.getValue();
                Class<?> controllerItf = null;
                Class<?> controllerClass = null;
                AbstractPAController controller = null;
                PAInterface itfRef = null;
                PAGCMInterfaceType controllerItfType = null;

                try {
                    // fetch the classes
                    controllerItf = Class.forName(controllerItfName);
                    controllerClass = Class.forName(controllerClassName);
                    // Instantiates the controller object, using 'this' component as owner.
                    Constructor<?> controllerClassConstructor = controllerClass
                            .getConstructor(new Class[] { Component.class });
                    controller = (AbstractPAController) controllerClassConstructor
                            .newInstance(new Object[] { this });

                    // Obtains the controller interfaceType as declared by the object (in the method setControllerItfType)
                    controllerItfType = (PAGCMInterfaceType) controller.getFcItfType();
                    // now we can know the name of the controller, and discriminate special cases
                    controllerName = controllerItfType.getFcItfName();
                    if (specialCasesForController(controllerName, controllerItfType, isPrimitive,
                            controllersConfigFileLocation)) {
                        continue;
                    }

                    if (!controllerItf.isAssignableFrom(controllerClass)) {
                        logger.error("Could not create controller. Class '" + controllerClassName +
                            " does not implement interface '" + controllerItfName +
                            ". Check controller configuration file.");
                        continue;
                    }

                    // use the interfaceType to generate the PAInterface on the controller object, and set the controller object as its implementation
                    itfRef = MetaObjectInterfaceClassGenerator.instance().generateInterface(controllerName,
                            this, controllerItfType, controllerItfType.isInternal(), false);
                    itfRef.setFcItfImpl(controller);
                } catch (Exception e) {
                    throw new ProActiveRuntimeException("Could not create controller '" +
                        controllerClassName +
                        "' while instantiating component'" +
                        this.componentParameters.getName() +
                        "'. Check your configuration file " +
                        this.componentParameters.getControllerDescription()
                                .getControllersConfigFileLocation() + " : " + e.getMessage(), e);
                }

                // add the controller to the controllers interfaces map, and add the controller type to the NF type
                nfServerItfs.put(controllerName, itfRef);
                nfType.add((InterfaceType) itfRef.getFcItfType());
            }
        }

        //------------------------------------------------------------        
        // 3. Check that the mandatory controllers have been created and set the name
        checkMandatoryControllers(nfType);
        try {
            ((NameController) getFcInterface(Constants.NAME_CONTROLLER)).setFcName(this.componentParameters
                    .getName());
        } catch (NoSuchInterfaceException e) {
            throw new ProActiveRuntimeException("Interface '" + Constants.NAME_CONTROLLER + "' not found. " +
                e.getMessage(), e);
        }

        //------------------------------------------------------------        
        // 4. Set the real NF type, after having created all the NF interfaces
        try {
            Component boot = Utils.getBootstrapComponent();
            PAGCMTypeFactory tf = Utils.getPAGCMTypeFactory(boot);
            InterfaceType[] f = this.componentParameters.getComponentType().getFcInterfaceTypes();
            InterfaceType[] nf = nfType.toArray(new InterfaceType[] {});
            for (InterfaceType i : nf) {
                if (!i.getFcItfName().endsWith("-controller")) {
                    throw new RuntimeException("Could not create NF interface '" + i.getFcItfName() +
                        "' because the interface name does not end by \"-controller\"");
                }
                loggerADL.debug("[PAComponentImpl] Interface: " + i.getFcItfName());
            }
            // Re-Set the real ComponentType
            this.componentParameters.setComponentType(tf.createFcType(f, nf));
        } catch (Exception e) {
            logger.error("NF type could not be set");
            throw new ProActiveRuntimeException("Could not create NF type while instantiating component '" +
                this.componentParameters.getName() + " : " + e.getMessage(), e);
        }

    }

    /**
     * Checks that the mandatory controllers are defined and, if not, creates them.
     * Mandatory controllers: NAME, LIFECYCLE, MEMBRANE.
     * Also: CONTENT for composite, BINDING for composites and primitive with F client itfs.<br/>
     * <br/>
     * After this method, the component has an implementation of NAME, LIFECYCLE, MEMBRANE and,
     * if conditions are enough, for CONTENT and BINDING. 
     */
    private void checkMandatoryControllers(Vector<InterfaceType> nfType) {

        PAInterface itfRef = null;
        Class<?> controllerClass = null;
        boolean isPrimitive = Constants.PRIMITIVE.equals(this.componentParameters.getHierarchicalType());
        boolean hasFClientInterfaces = this.componentParameters.getClientInterfaceTypes().length > 0;

        try {
            // LIFECYCLE Controller
            if (!hasNfImplementation(Constants.LIFECYCLE_CONTROLLER)) {
                // default implementation of PAGCMLifeCycleController
                controllerClass = PAGCMLifeCycleControllerImpl.class;
                itfRef = createObjectController(controllerClass);
                this.nfServerItfs.put(itfRef.getFcItfName(), itfRef);
                // don't re-add the type to the vector if it already existed
                if (!nfType.contains(itfRef.getFcItfType())) {
                    nfType.add((InterfaceType) itfRef.getFcItfType());
                }
                // ASSERTIONS: controller implements PAGCMLifeCycleController, and controllerName is "lifecycle-controller"
            }

            // NAME Controller
            if (!hasNfImplementation(Constants.NAME_CONTROLLER)) {
                loggerADL.debug("[PAComponentImpl] Creating Name Controller");
                // default implementation of NameController 
                controllerClass = PANameControllerImpl.class;
                itfRef = createObjectController(controllerClass);
                this.nfServerItfs.put(itfRef.getFcItfName(), itfRef);
                // don't re-add the type to the vector if it already existed
                if (!nfType.contains(itfRef.getFcItfType())) {
                    nfType.add((InterfaceType) itfRef.getFcItfType());
                }
                // ASSERTIONS: controller implements NameController, and controllerName is "name-controller"
            }

            // CONTENT Controller if composite
            if (!hasNfImplementation(Constants.CONTENT_CONTROLLER) && !isPrimitive) {
                // default implementation of PAContentController
                controllerClass = PAContentControllerImpl.class;
                itfRef = createObjectController(controllerClass);
                this.nfServerItfs.put(itfRef.getFcItfName(), itfRef);
                // don't re-add the type to the vector if it already existed
                if (!nfType.contains(itfRef.getFcItfType())) {
                    nfType.add((InterfaceType) itfRef.getFcItfType());
                }
                // ASSERTIONS: controller implements PAContentController, and controllerName is "content-controller"
            }

            //BINDING Controller if composite, or primitive with F client interfaces or NF internal server interfaces
            if (!hasNfImplementation(Constants.BINDING_CONTROLLER) &&
                !(isPrimitive && !hasFClientInterfaces && !hasNfInternalServerInterfaces())) {
                // default implementation of PABindingController
                controllerClass = PABindingControllerImpl.class;
                itfRef = createObjectController(controllerClass);
                this.nfServerItfs.put(itfRef.getFcItfName(), itfRef);
                // don't re-add the type to the vector if it already existed
                if (!nfType.contains(itfRef.getFcItfType())) {
                    nfType.add((InterfaceType) itfRef.getFcItfType());
                }
                // ASSERTIONS: controller implements PABindingController, and controllerName is "binding-controller"
            }

            // Membrane Controller
            // Must be created if the interface has been defined (generated) and has not been assigned an implementation yet
            // (i.e. no implementation has been defined in the configuration file)
            if (existsNfInterface(Constants.MEMBRANE_CONTROLLER)) {
                PAInterface membraneItfRef = (PAInterface) this.nfServerItfs
                        .get(Constants.MEMBRANE_CONTROLLER);
                if (membraneItfRef.getFcItfImpl() == null) {
                    // default implementation of PAMembraneController 
                    controllerClass = PAMembraneControllerImpl.class;
                    itfRef = createObjectController(controllerClass);
                    // replace the current entry for "membrane-controller" (null implementation) by the recently created one
                    this.nfServerItfs.put(itfRef.getFcItfName(), itfRef);
                    // don't re-add it to the InterfaceType vector again, because it already exists

                }
                // ASSERTIONS: controller implements PAMembraneController, and controllerName is "membrane-controller"
            }

            // Interceptor Controller
            // Must be created if the interface has been defined (generated) and has not been assigned an implementation yet
            // (i.e. no implementation has been defined in the configuration file)
            if (existsNfInterface(Constants.INTERCEPTOR_CONTROLLER)) {
                PAInterface interceptorItfRef = (PAInterface) this.nfServerItfs
                        .get(Constants.INTERCEPTOR_CONTROLLER);
                if (interceptorItfRef.getFcItfImpl() == null) {
                    // default implementation of PAInterceptorController 
                    controllerClass = PAInterceptorControllerImpl.class;
                    itfRef = createObjectController(controllerClass);
                    // replace the current entry for "interceptor-controller" (null implementation) by the recently created one
                    this.nfServerItfs.put(itfRef.getFcItfName(), itfRef);
                    // don't re-add it to the InterfaceType vector again, because it already exists

                }
                // ASSERTIONS: controller implements PAInterceptorController, and controllerName is "interceptor-controller"
            }

        } catch (Exception e) {
            throw new ProActiveRuntimeException("Could not create mandatory controller '" +
                controllerClass.getName() + "' while instantiating component'" +
                this.componentParameters.getName() + "': " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a server NF interface has an implementation.
     * 
     * @param itfName
     * @return true if the interface exists AND has implementation; false otherwise
     */
    private boolean hasNfImplementation(String itfName) {
        if (this.nfServerItfs.containsKey(itfName)) {
            PAInterface itf = (PAInterface) nfServerItfs.get(itfName);
            if (itf.getFcItfImpl() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the NF interface 'itfName' has already been defined.
     * @param itfName
     * @return
     */
    private boolean existsNfInterface(String itfName) {
        if (this.nfServerItfs.containsKey(itfName)) {
            return true;
        }
        if (this.nfClientItfs.containsKey(itfName)) {
            return true;
        }
        if (this.collectionNfItfsMembers.containsKey(itfName)) {
            return true;
        }
        return false;
    }

    /**
     * Discriminate special controller interfaces
     * <ul>
     *    <li>COLLECTION: ignored</li>
     *    <li>CONTENT: if primitive, ignore it</li>
     *    <li>BINDING: if primitive and DOESN'T HAVE F client interfaces, ignore it</li>
     *    <li>Avoid duplicates</li>
     * </ul>
     * @param controllerName
     * @param itfType
     * @param isPrimitive
     * @return
     */
    private boolean specialCasesForController(String controllerName, PAGCMInterfaceType itfType,
            boolean isPrimitive, String controllersConfigFileLocation) {

        // COLLECTION interfaces are ignored, because they are generated dynamically (and an object controller shouldn't be a collection, right?)
        if (itfType.isFcCollectionItf()) {
            return true;
        }

        // CONTENT controller is not created for primitives
        if (Constants.CONTENT_CONTROLLER.equals(controllerName) && !itfType.isFcClientItf() &&
            !itfType.isInternal() && isPrimitive) {
            logger.debug("[PAComponentImpl] Ignored controller '" + Constants.CONTENT_CONTROLLER +
                "' declared for component '" + this.componentParameters.getName() + "' in file: " +
                controllersConfigFileLocation);
            return true;
        }

        // BINDING controller is not created for primitives without client interfaces except if it has an internal server interface
        if (Constants.BINDING_CONTROLLER.equals(controllerName) && !itfType.isFcClientItf() &&
            !itfType.isInternal() && isPrimitive) {
            if ((Utils.getClientItfTypes(this.componentParameters.getComponentType()).length == 0) &&
                !hasNfInternalServerInterfaces()) {
                logger.debug("[PAComponentImpl] Ignored controller '" + Constants.BINDING_CONTROLLER +
                    "' declared for component '" + this.componentParameters.getName() + "' in file: " +
                    controllersConfigFileLocation);
                return true;
            }
        }

        // Controller interface had already been declared (f.e., in the NF Type). Do not create this controller.
        if (existsNfInterface(controllerName)) {
            logger.debug("[PAComponentImpl] Controller interface '" + controllerName +
                "' already created. Ignoring this controller.");
            return true;
        }

        return false;
    }

    /**
     * Discriminate special NF interfaces
     * <ul>
     *    <li>COLLECTION: ignored, they are dynamically generated</li>
     *    <li>CONTENT: if primitive, ignore it</li>
     *    <li>BINDING: if primitive and DOESN'T HAVE F client interfaces, ignore it</li>
     * </ul>
     * @return true if 'special case', false otherwise
     */
    private boolean specialCasesForNfType(String itfName, PAGCMInterfaceType itfType, boolean isPrimitive) {

        // COLLECTION interfaces are ignored, because they are generated dynamically
        if (itfType.isFcCollectionItf()) {
            return true;
        }

        // MEMBRANE controller must be created as an object controller
        //    	if(Constants.MEMBRANE_CONTROLLER.equals(itfName) && !itfType.isFcClientItf() && !itfType.isInternal()) {
        //    		logger.debug("[PAComponentImpl] Ignored NF Interface '"+ Constants.MEMBRANE_CONTROLLER +"' declared for component '"+ this.componentParameters.getName() + "'");
        //    		return true;
        //    	}

        // CONTENT controller is not created for primitives
        if (Constants.CONTENT_CONTROLLER.equals(itfName) && !itfType.isFcClientItf() &&
            !itfType.isInternal() && isPrimitive) {
            logger.debug("[PAComponentImpl] Ignored NF Interface '" + Constants.CONTENT_CONTROLLER +
                "' declared for component '" + this.componentParameters.getName() + "'");
            return true;
        }

        // BINDING controller is not created for primitives without client interfaces except if it has an internal server interface
        if (Constants.BINDING_CONTROLLER.equals(itfName) && !itfType.isFcClientItf() &&
            !itfType.isInternal() && isPrimitive) {
            if ((Utils.getClientItfTypes(this.componentParameters.getComponentType()).length == 0) &&
                !hasNfInternalServerInterfaces()) {
                logger.debug("[PAComponentImpl] Ignored NF Interface '" + Constants.BINDING_CONTROLLER +
                    "' declared for component '" + this.componentParameters.getName() + "'");
                return true;
            }
        }

        return false;
    }

    private boolean hasNfInternalServerInterfaces() {
        InterfaceType[] nfItfTypes = ((PAComponentType) this.componentParameters.getComponentType())
                .getNfFcInterfaceTypes();

        for (InterfaceType nfItfType : nfItfTypes) {
            if (!nfItfType.isFcClientItf() && ((PAGCMInterfaceType) nfItfType).isInternal()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Instantiate a controller object using an implementing class (which must implement {@link AbstractPAController}) 
     * {@link PAGCMInterfaceType} 
     * , and returns a generated {@link PAInterface}.
     * 
     * @param itfType
     * @param controllerClass
     * @return
     * @throws Exception
     */
    private PAInterface createController(PAGCMInterfaceType itfType, Class<?> controllerClass)
            throws Exception {
        PAInterface itfRef = null;
        AbstractPAController controller = null;
        Constructor<?> controllerClassConstructor = controllerClass
                .getConstructor(new Class[] { Component.class });
        controller = (AbstractPAController) controllerClassConstructor.newInstance(new Object[] { this });
        itfRef = MetaObjectInterfaceClassGenerator.instance().generateInterface(itfType.getFcItfName(), this,
                itfType, itfType.isInternal(), false);
        itfRef.setFcItfImpl(controller);
        return itfRef;
    }

    /**
     * Instantiates an object controller and generates its interface using only the Controller class
     * (which must implement {@link AbstractPAController}.
     * 
     * The {@link PAGCMInterfaceType} is obtained from the object after instantiating it. 
     * 
     * @param controllerClass
     * @return interface generated for the controller
     */
    private PAInterface createObjectController(Class<?> controllerClass) throws Exception {

        // Instantiate the controller object, setting THIS component as its owner.
        Constructor<?> controllerClassConstructor = controllerClass
                .getConstructor(new Class[] { Component.class });
        AbstractPAController controller = (AbstractPAController) controllerClassConstructor
                .newInstance(new Object[] { this });
        // Obtains the interface type after having instantiated the object
        PAGCMInterfaceType controllerItfType = (PAGCMInterfaceType) controller.getFcItfType();
        String controllerName = controller.getFcItfName();
        // Generates the PAInterface and sets the instantiated object as its implementation
        PAInterface itfRef = MetaObjectInterfaceClassGenerator.instance().generateInterface(controllerName,
                this, controllerItfType, controllerItfType.isInternal(), false);
        itfRef.setFcItfImpl(controller);
        return itfRef;
    }

    /**
     * @param controllerConfigFileLocation
     *            the location of the configuration file
     * @return a xml parsing handler
     */
    public static ComponentConfigurationHandler loadControllerConfiguration(
            String controllerConfigFileLocation) {
        try {
            return ComponentConfigurationHandler
                    .createComponentConfigurationHandler(controllerConfigFileLocation);
        } catch (Exception e) {
            logger.error("could not load controller config file : " + controllerConfigFileLocation +
                ". Reverting to default controllers configuration.");

            try {
                return ComponentConfigurationHandler
                        .createComponentConfigurationHandler(ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION);
            } catch (Exception e1) {
                logger
                        .error("could not load default controller config file either. Check that the default controller config file is available in your classpath at : " +
                            ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION);
                throw new ProActiveRuntimeException(
                    "could not load default controller config file either. Check that the default controller config file is available on your system at : " +
                        ControllerDescription.DEFAULT_COMPONENT_CONFIG_FILE_LOCATION, e1);
            }
        }
    }

    /**
     * Generates the Functional Interfaces
     * 
     * @param componentParameters
     * @param isPrimitive
     */
    private void addFunctionalInterfaces() {
        InterfaceType[] tmp = this.componentParameters.getComponentType().getFcInterfaceTypes();
        PAGCMInterfaceType[] interfaceTypes = new PAGCMInterfaceType[tmp.length];
        System.arraycopy(tmp, 0, interfaceTypes, 0, tmp.length);
        boolean isPrimitive = this.componentParameters.getHierarchicalType().equals(Constants.PRIMITIVE);

        try {
            for (PAGCMInterfaceType interfaceType : interfaceTypes) {
                //for (int i = 0; i < interfaceTypes.length; i++) {
                PAInterface itfRef = null;

                // collection interfaces are ignored
                if (interfaceType.isFcCollectionItf()) {
                    // Members of collection itfs are created dynamically
                    continue;
                }
                // multicast interfaces
                if (interfaceType.isGCMMulticastItf()) {
                    itfRef = createInterfaceOnGroupOfDelegatees(interfaceType);
                } else {
                    // No interface generated for client itfs of primitive
                    if (!(interfaceType.isFcClientItf() && isPrimitive)) {
                        // Generate the interface as external
                        itfRef = MetaObjectInterfaceClassGenerator.instance().generateFunctionalInterface(
                                interfaceType.getFcItfName(), this, interfaceType);
                        // Set delegation link
                        if (isPrimitive) {
                            if (!interfaceType.isFcCollectionItf()) {
                                if (!interfaceType.isFcClientItf()) {
                                    // primitive + singleton + server
                                    itfRef.setFcItfImpl(getReferenceOnBaseObject());
                                } else {
                                    // primitive + singleton + client
                                    itfRef.setFcItfImpl(null);
                                }
                            }
                        }
                    }
                    // Non multicast client itf of primitive comp : do nothing ... (why?)
                }
                loggerADL.debug("[PAComponentImpl] Created F interface " + interfaceType.getFcItfName());
                // add the interface to the corresponding map
                if (!interfaceType.isFcClientItf()) {
                    this.serverItfs.put(interfaceType.getFcItfName(), itfRef);
                } else if (itfRef != null) {
                    this.clientItfs.put(interfaceType.getFcItfName(), itfRef);
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot create interface references : " + e.getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("cannot create interface references : " + e.getMessage(), e);
        }
    }

    // returns a generated interface reference, whose impl field is a group
    // It is able to handle multiple bindings
    private PAInterface createInterfaceOnGroupOfDelegatees(PAGCMInterfaceType itfType) throws Exception {
        PAInterface itf_ref = MetaObjectInterfaceClassGenerator.instance().generateFunctionalInterface(
                itfType.getFcItfName(), this, itfType);

        // create a group of impl target objects
        PAInterface itf_ref_group = PAComponentGroup.newComponentInterfaceGroup(itfType, this);
        itf_ref.setFcItfImpl(itf_ref_group);
        return itf_ref;
    }

    /**
     * Retrieves an interface from its name.
     * NF Interfaces (both client/server) must have a name ending in "-controller".
     * 
     * see {@link org.objectweb.fractal.api.Component#getFcInterface(String)}
     */
    public Object getFcInterface(String interfaceName) throws NoSuchInterfaceException {
        if (!(Constants.ATTRIBUTE_CONTROLLER.equals(interfaceName)) &&
            (interfaceName.endsWith("-controller"))) {
            if (this.nfServerItfs.containsKey(interfaceName)) {
                return this.nfServerItfs.get(interfaceName);
            }
            if (this.nfClientItfs.containsKey(interfaceName)) {
                return this.nfClientItfs.get(interfaceName);
            }
            // TODO: Check how are the collective NF interfaces handled here ... should they also finish by "-controller" ?
            throw new NoSuchInterfaceException(interfaceName);
        }
        if (interfaceName.equals(Constants.COMPONENT)) {
            return this;
        }
        if (this.serverItfs.containsKey(interfaceName)) {
            return this.serverItfs.get(interfaceName);
        }
        if (this.clientItfs.containsKey(interfaceName)) {
            return this.clientItfs.get(interfaceName);
        }

        // a member of a collection itf?
        InterfaceType[] itfTypes = ((ComponentType) getFcType()).getFcInterfaceTypes();
        for (int i = 0; i < itfTypes.length; i++) {
            InterfaceType type = itfTypes[i];
            if (type.isFcCollectionItf()) {
                if ((interfaceName.startsWith(type.getFcItfName()) && !type.getFcItfName().equals(
                        interfaceName))) {
                    if (this.collectionItfsMembers.containsKey(interfaceName)) {
                        return this.collectionItfsMembers.get(interfaceName);
                    } else {
                        // generate a new interface and add it to the list of members of collection its
                        try {
                            Interface clientItf = MetaObjectInterfaceClassGenerator.instance()
                                    .generateFunctionalInterface(interfaceName, this,
                                            (PAGCMInterfaceType) itfTypes[i]);
                            this.collectionItfsMembers.put(interfaceName, clientItf);
                            return clientItf;
                        } catch (InterfaceGenerationFailedException e1) {
                            logger.info("Generation of the interface '" + interfaceName + "' failed.", e1);
                        }
                    }
                }
            }
        }

        throw new NoSuchInterfaceException(interfaceName);
    }

    /*
     * see {@link org.objectweb.fractal.api.Component#getFcInterfaces()}
     */
    public Object[] getFcInterfaces() {
        List<Object> itfs = new ArrayList<Object>(15); //we have at least 10 control itfs

        // add interface component
        itfs.add(this);

        // add controller server interfaces
        for (Object object : this.nfServerItfs.values()) {
            itfs.add(object);
        }

        // add controller client interfaces
        for (Object object : this.nfClientItfs.values()) {
            itfs.add(object);
        }

        //add server interface
        for (Object object : this.serverItfs.values()) {
            itfs.add(object);
        }

        //add client interfaces
        for (Object object : this.clientItfs.values()) {
            itfs.add(object);
        }

        return itfs.toArray(new Interface[itfs.size()]);
    }

    /*
     * see {@link org.objectweb.fractal.api.Component#getFcType()}
     */
    public Type getFcType() {
        return this.componentParameters.getComponentType();
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#getFcItfName()}
     */
    public String getFcItfName() {
        return Constants.COMPONENT;
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#getFcItfOwner()}
     */
    public Component getFcItfOwner() {
        return this;
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#getFcItfType()}
     */
    public Type getFcItfType() {
        return getFcType();
    }

    /**
     * see {@link org.objectweb.fractal.api.Interface#isFcInternalItf()}
     */
    public boolean isFcInternalItf() {
        return true;
    }

    /**
     * Returns the base object. If the component is a composite, a basic
     * do-nothing instance of class Composite is returned.
     *
     * @return the base object underneath
     */
    public Object getReferenceOnBaseObject() {
        return getBody().getReifiedObject();
    }

    /**
     * @return a ComponentParameters instance, corresponding to the
     *         configuration of the current component
     */
    public ComponentParameters getComponentParameters() {
        return this.componentParameters;
    }

    /**
     * @return the body of the current active object
     */
    public Body getBody() {
        return this.body;
    }

    /**
     * see
     * {@link org.objectweb.proactive.core.component.identity.PAComponent#getID()}
     */
    public UniqueID getID() {
        return getBody().getID();
    }

    public void setControllerObject(String itfName, Object classToCreate) throws Exception {
        PAInterface itf_ref = (PAInterface) this.nfServerItfs.get(itfName);
        if (itf_ref == null) {
            throw new NoSuchInterfaceException("The requested interface :" + itfName + " doesn't exist");
        } else {
            Class<?> controllerClass = Class.forName((String) classToCreate);

            PAGCMInterfaceType itf_type = (PAGCMInterfaceType) itf_ref.getFcItfType();
            Class<?> interfaceClass = Class.forName(itf_type.getFcItfSignature());
            if (interfaceClass.isAssignableFrom(controllerClass)) { // Check that the class implements the specified interface
                PAInterface controller = createController(itf_type, controllerClass);

                if (itf_ref.getFcItfImpl() != null) { // Dealing with first initialization
                    if (itf_ref.getFcItfImpl() instanceof AbstractPAController) { // In this case, itf_ref is implemented by a controller object
                        if (controller.getFcItfImpl() instanceof ControllerStateDuplication) { // Duplicate the state of the existing controller
                            Object ob = itf_ref.getFcItfImpl();
                            if (ob instanceof ControllerStateDuplication) {
                                ((ControllerStateDuplication) controller.getFcItfImpl())
                                        .duplicateController(((ControllerStateDuplication) ob).getState()
                                                .getStateObject());
                            }
                        }
                    } else { // In this case, the object controller will replace an interface of a non-functional component
                        if (controller.getFcItfImpl() instanceof ControllerStateDuplication) {
                            try {
                                Component theOwner = ((PAInterface) itf_ref.getFcItfImpl()).getFcItfOwner();
                                ControllerStateDuplication dup = (ControllerStateDuplication) theOwner
                                        .getFcInterface(Constants.CONTROLLER_STATE_DUPLICATION);
                                ControllerState cs = dup.getState();

                                ((ControllerStateDuplication) controller.getFcItfImpl())
                                        .duplicateController(cs.getStateObject());
                            } catch (NoSuchInterfaceException e) {
                                // Here nothing to do, if the component controller doesnt have a ControllerDuplication, then no duplication can be done
                            }
                        }
                    }
                } else {
                    // Here, the controller has not been initialized (f.e., the Multicast and Gathercast controllers, when added manually)
                    // The Multicast Controller (just like the Gathercast Controller), needs to execute "initController".
                    // In PAComponentImpl constructor, when the NFType has been specified, the controllers that have not been assigned 
                    //    (f.e. the controllers created in this method) are not initialized, so it must be done here.
                    ((PAController) (controller.getFcItfImpl())).initController();
                }

                this.nfServerItfs.put(controller.getFcItfName(), controller);
            } else { /* The controller class does not implement the specified interface */
                throw new IllegalBindingException("The class " + classToCreate + " does not implement the " +
                    itf_type.getFcItfSignature() + " interface");
            }
        }
    }

    /**
     * see
     * {@link org.objectweb.proactive.core.component.identity.PAComponent#getRepresentativeOnThis()}
     * There should be no generic return type in the methods of the representative class
     */
    public PAComponent getRepresentativeOnThis() {
        // optimization : cache self reference
        if (this.representativeOnMyself != null) {
            return this.representativeOnMyself;
        }

        try {
            return this.representativeOnMyself = PAComponentRepresentativeFactory.instance()
                    .createComponentRepresentative(
                            getComponentParameters(),
                            ((StubObject) MOP.turnReified(this.body.getReifiedObject().getClass().getName(),
                                    org.objectweb.proactive.core.Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
                                    new Object[] { this.body }, this.body.getReifiedObject(), null))
                                    .getProxy());
        } catch (Exception e) {
            throw new ProActiveRuntimeException("This component could not generate a reference on itself", e);
        }
    }

    @Override
    public String toString() {
        String string = "name : " + getFcItfName() + "\n" + getFcItfType() + "\n" + "isInternal : " +
            isFcInternalItf() + "\n";
        return string;
    }

    public void migrateControllersDependentActiveObjectsTo(Node node) throws MigrationException {
        /*
         * for (PAController controller : controlItfs.values()) {
         * controller.migrateDependentActiveObjectsTo(node); }
         */
        for (Interface controller : this.nfServerItfs.values()) {
            Object c = ((PAInterface) controller).getFcItfImpl();
            if (c instanceof PAController) { // Object Controller
                ((PAController) c).migrateDependentActiveObjectsTo(node);
            } else {
                //TODO : Case of migration of component controllers
            }
        }

    }

    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    public void setImmediateServices() {
        PAActiveObject.setImmediateService("getComponentParameters");
    }
}
