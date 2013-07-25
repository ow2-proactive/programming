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
package org.objectweb.proactive.core.component.representative;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.Type;
import org.objectweb.fractal.api.control.BindingController;
import org.objectweb.fractal.api.control.ContentController;
import org.objectweb.fractal.api.type.ComponentType;
import org.objectweb.fractal.api.type.InterfaceType;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.proxy.UniversalBodyProxy;
import org.objectweb.proactive.core.component.ComponentParameters;
import org.objectweb.proactive.core.component.Constants;
import org.objectweb.proactive.core.component.ControllerDescription;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.config.ComponentConfigurationHandler;
import org.objectweb.proactive.core.component.control.AbstractPAController;
import org.objectweb.proactive.core.component.control.PABindingControllerImpl;
import org.objectweb.proactive.core.component.control.PAContentControllerImpl;
import org.objectweb.proactive.core.component.control.PAGCMLifeCycleControllerImpl;
import org.objectweb.proactive.core.component.control.PAInterceptorControllerImpl;
import org.objectweb.proactive.core.component.control.PAMembraneControllerImpl;
import org.objectweb.proactive.core.component.control.PANameControllerImpl;
import org.objectweb.proactive.core.component.gen.RepresentativeInterfaceClassGenerator;
import org.objectweb.proactive.core.component.identity.PAComponent;
import org.objectweb.proactive.core.component.identity.PAComponentImpl;
import org.objectweb.proactive.core.component.request.ComponentRequest;
import org.objectweb.proactive.core.component.type.PAComponentType;
import org.objectweb.proactive.core.component.type.PAGCMInterfaceType;
import org.objectweb.proactive.core.component.type.PAGCMTypeFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.group.ProxyForGroup;
import org.objectweb.proactive.core.mop.MethodCall;
import org.objectweb.proactive.core.mop.Proxy;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * An object of type <code> Component  </code> which is a remote reference on a
 * component. <br>
 * When creating an active object of type <code> A  </code>, you get a reference
 * on the active object through a dynamically generated stub of type
 * <code> A  </code>. Similarly, when creating a component, you get a reference
 * on an object of type <code> Component  </code>, in other words an instance of
 * this class.
 * <p>
 * During the construction of an instance of this class, references to
 * interfaces of the component are also dynamically generated : references to
 * functional interfaces corresponding to the server interfaces of the
 * component, and references to control interfaces. The idea is to save remote
 * invocations : when requesting a controller or an interface, the generated
 * corresponding interface is directly returned. Then, invocations on this
 * interface are reified and transferred to the actual component. <br>
 *
 * @author The ProActive Team
 */
public class PAComponentRepresentativeImpl implements PAComponentRepresentative, Serializable {
    protected static final Logger logger = ProActiveLogger.getLogger(Loggers.COMPONENTS);
    protected static final Logger loggerADL = ProActiveLogger.getLogger(Loggers.COMPONENTS_ADL);
    private ComponentParameters componentParameters;
    protected Map<String, Interface> fItfs;
    protected Map<String, Interface> nfItfs = new HashMap<String, Interface>();
    protected Proxy proxy;
    protected StubObject stubOnBaseObject = null;
    protected boolean useShortcuts;

    public PAComponentRepresentativeImpl(ComponentType componentType, String hierarchicalType,
            String controllersConfigFileLocation) {
        this(new ComponentParameters(componentType, new ControllerDescription(null, hierarchicalType,
            controllersConfigFileLocation)));
    }

    public PAComponentRepresentativeImpl(ComponentParameters componentParameters) {

        this.componentParameters = componentParameters;
        this.useShortcuts = CentralPAPropertyRepository.PA_COMPONENT_USE_SHORTCUTS.isTrue();

        Type componentType = componentParameters.getComponentType();

        loggerADL.debug("[PAComponentRepresentativeImpl]  FType: " +
            ((PAComponentType) componentType).getFcInterfaceTypes().length);
        loggerADL.debug("[PAComponentRepresentativeImpl] NFType: " +
            ((PAComponentType) componentType).getNfFcInterfaceTypes().length);
        loggerADL
                .debug("[PAComponentRepresentativeImpl] Config File: " +
                    (this.componentParameters.getControllerDescription().configFileIsSpecified() ? this.componentParameters
                            .getControllerDescription().getControllersConfigFileLocation()
                            : "---"));

        if ((componentType instanceof PAComponentType)) {
            loggerADL.debug("[PAComponentRepresentativeImpl] GENERAL CREATION of controller interfaces for " +
                this.componentParameters.getName());
            addControllerInterfaces();
        }

        addFunctionalInterfaces();

        componentType = componentParameters.getComponentType();
        loggerADL.debug("[PAComponentRepresentativeImpl] NFType: " +
            ((PAComponentType) componentType).getNfFcInterfaceTypes().length);
        loggerADL.debug("[PAComponentRepresentativeImpl] NFItfs: " + nfItfs.keySet().size());

    }

    /**
     * Create and add the NF Interfaces using both a component configuration file, and an NF Type.
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
        //Class<?> controllerItf = null;

        for (PAGCMInterfaceType pagcmNfItfType : pagcmNfItfTypes) {

            String itfName = pagcmNfItfType.getFcItfName();
            PAInterface itfRef = null;

            try {
                // some controllers interfaces are ignored
                if (specialCasesForNfType(itfName, pagcmNfItfType, isPrimitive))
                    continue;

                // TODO: check the case MULTICAST && CLIENT, treated in PAComponentImpl.addControllerInterfaces, but not here

                // Generate the representative interface
                itfRef = RepresentativeInterfaceClassGenerator.instance().generateInterface(
                        pagcmNfItfType.getFcItfName(), this, pagcmNfItfType, pagcmNfItfType.isInternal(),
                        false);

                // update the hashmap and the vector of NF types
                this.nfItfs.put(itfName, itfRef);
                nfType.add((InterfaceType) itfRef.getFcItfType());

            } catch (Exception e) {
                throw new ProActiveRuntimeException("Could not create NF interface reference'" + itfName +
                    "' while instantiating component'" + this.componentParameters.getName() + "'. " +
                    e.getMessage(), e);
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
            loggerADL.debug("Parsing Controller Configuration File: " + controllersConfigFileLocation);
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

                    // Some controllers are not created
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

                    // Generate the representative interface
                    itfRef = RepresentativeInterfaceClassGenerator.instance().generateControllerInterface(
                            controllerName, this, controllerItfType);
                    ((StubObject) itfRef).setProxy(this.proxy);

                } catch (Exception e) {
                    throw new ProActiveRuntimeException(
                        "Could not create representative interface for controller '" +
                            controllerClassName +
                            "' while instantiating component'" +
                            this.componentParameters.getName() +
                            "'. Check your configuration file " +
                            this.componentParameters.getControllerDescription()
                                    .getControllersConfigFileLocation() + " : " + e.getMessage(), e);
                }

                // add the controller to the controllers interfaces map, and add the controller type to the NF type
                nfItfs.put(controllerName, itfRef);
                nfType.add((InterfaceType) itfRef.getFcItfType());
            }
        }

        //------------------------------------------------------------        
        // 3. Check that the mandatory controllers have been created
        checkMandatoryControllers(nfType);

        //------------------------------------------------------------        
        // 4. Set the real NF type, after having created all the NF interfaces
        try {
            Component boot = Utils.getBootstrapComponent();
            PAGCMTypeFactory tf = Utils.getPAGCMTypeFactory(boot);
            InterfaceType[] f = this.componentParameters.getComponentType().getFcInterfaceTypes();
            InterfaceType[] nf = nfType.toArray(new InterfaceType[] {});
            // Re-Set the real ComponentType
            this.componentParameters.setComponentType(tf.createFcType(f, nf));
        } catch (Exception e) {
            logger.error("NF type could not be set");
            e.printStackTrace();
        }

    }

    /**
     * Discriminate special NF interfaces
     * <ul>
     *    <li>COLLECTION: ignored, they are dynamically generated</li>
     *    <li>CONTENT: if primitive, ignore it</li>
     *    <li>BINDING: if primitive and DOESN'T HAVE F client interfaces, ignore it</li>
     * </ul>
     * @return true if 'special case', faslse otherwise
     */
    private boolean specialCasesForNfType(String itfName, PAGCMInterfaceType itfType, boolean isPrimitive) {

        // COLLECTION interfaces are ignored, because they are generated dynamically
        if (itfType.isFcCollectionItf()) {
            return true;
        }

        // CONTENT controller is not created for primitives
        if (Constants.CONTENT_CONTROLLER.equals(itfName) && !itfType.isFcClientItf() &&
            !itfType.isInternal() && isPrimitive) {
            //logger.warn("Ignored NF Interface '"+ Constants.CONTENT_CONTROLLER +"' declared for component '"+ this.componentParameters.getName() + "'");
            return true;
        }

        // BINDING controller is not created for primitives without client interfaces except if it has an internal server interface
        if (Constants.BINDING_CONTROLLER.equals(itfName) && !itfType.isFcClientItf() &&
            !itfType.isInternal() && isPrimitive) {
            if ((Utils.getClientItfTypes(this.componentParameters.getComponentType()).length == 0) &&
                !hasNfInternalServerInterfaces()) {
                //logger.warn("Ignored NF Interface '"+ Constants.BINDING_CONTROLLER +"' declared for component '"+ this.componentParameters.getName() + "'");
                return true;
            }
        }

        return false;
    }

    /**
     * Discriminate special controller interfaces
     * <ul>
     *    <li>COLLECTION: ignored??? (TODO:check if this is needed)</li>
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
        //if(itfType.isFcCollectionItf()) {
        //	return true;
        //}

        // CONTENT controller is not created for primitives
        if (Constants.CONTENT_CONTROLLER.equals(controllerName) && !itfType.isFcClientItf() &&
            !itfType.isInternal() && isPrimitive) {
            //logger.warn("Ignored controller '"+ Constants.CONTENT_CONTROLLER +"' declared for component '"+ this.componentParameters.getName() + "' in file: "+ controllersConfigFileLocation);
            return true;
        }

        // BINDING controller is not created for primitives without client interfaces except if it has an internal server interface
        if (Constants.BINDING_CONTROLLER.equals(controllerName) && !itfType.isFcClientItf() &&
            !itfType.isInternal() && isPrimitive) {
            if ((Utils.getClientItfTypes(this.componentParameters.getComponentType()).length == 0) &&
                !hasNfInternalServerInterfaces()) {
                //logger.warn("Ignored controller '"+ Constants.BINDING_CONTROLLER +"' declared for component '"+ this.componentParameters.getName() + "' in file: "+ controllersConfigFileLocation);
                return true;
            }
        }

        // Controller interface had already been declared (f.e., in the NF Type). Do not create this controller.
        if (existsNfInterface(controllerName)) {
            //logger.warn("Controller interface '"+ controllerName +"' already created. Ignoring this controller.");
            return true;
        }

        return false;
    }

    private void addControllers(Map<String, String> controllersConfiguration) {
        // create the interface references tables
        // the size is the addition of :
        // - 1 for the current ItfRef (that is at the same time a binding controller, lifecycle controller,
        // content controller and name controller
        // - the number of client functional interfaces
        // - the number of server functional interfaces
        //ArrayList interface_references_list = new ArrayList(1 +componentType.getFcInterfaceTypes().length+controllersConfiguration.size());
        this.nfItfs = new HashMap<String, Interface>(1 + controllersConfiguration.size());

        // add controllers
        //Enumeration controllersInterfaces = controllersConfiguration.propertyNames();
        Iterator<String> iteratorOnControllers = controllersConfiguration.keySet().iterator();
        Class<?> controllerClass = null;
        AbstractPAController currentController;
        PAInterface currentInterface = null;
        Class<?> controllerItf;
        Vector<InterfaceType> nfType = new Vector<InterfaceType>();
        while (iteratorOnControllers.hasNext()) {
            String controllerItfName = iteratorOnControllers.next();
            try {
                controllerItf = Class.forName(controllerItfName);
                controllerClass = Class.forName(controllersConfiguration.get(controllerItf.getName()));
                Constructor<?> controllerClassConstructor = controllerClass
                        .getConstructor(new Class<?>[] { Component.class });
                currentController = (AbstractPAController) controllerClassConstructor
                        .newInstance(new Object[] { this });
                currentInterface = RepresentativeInterfaceClassGenerator.instance()
                        .generateControllerInterface(currentController.getFcItfName(), this,
                                (PAGCMInterfaceType) currentController.getFcItfType());
                ((StubObject) currentInterface).setProxy(this.proxy);

            } catch (Exception e) {
                logger.error("could not create controller " +
                    controllersConfiguration.get(controllerItfName) + " : " + e.getMessage());
                continue;
            }

            if (BindingController.class.isAssignableFrom(controllerClass)) {
                if ((this.componentParameters.getHierarchicalType().equals(Constants.PRIMITIVE) &&
                    (Utils.getClientItfTypes(this.componentParameters.getComponentType()).length == 0) && !hasNfInternalServerInterfaces())) {
                    if (logger.isDebugEnabled()) {
                        logger
                                .debug("user component class of this component does not have any client interface. It will have no BindingController");
                    }
                    continue;
                }
            }
            if (ContentController.class.isAssignableFrom(controllerClass)) {
                if (Constants.PRIMITIVE.equals(this.componentParameters.getHierarchicalType())) {
                    // no content controller here
                    continue;
                }
            }
            if (currentInterface != null) {
                this.nfItfs.put(currentController.getFcItfName(), currentInterface);
                nfType.add((InterfaceType) currentInterface.getFcItfType());
            }
        }

        try {//Setting the real NF type, as some controllers may not be generated
            Component boot = Utils.getBootstrapComponent();
            PAGCMTypeFactory type_factory = Utils.getPAGCMTypeFactory(boot);
            InterfaceType[] fItfTypes = ((PAComponentType) this.componentParameters.getComponentType())
                    .getFcInterfaceTypes();
            InterfaceType[] nfItfTypes = nfType.toArray(new InterfaceType[] {});
            this.componentParameters.setComponentType(type_factory.createFcType(fItfTypes, nfItfTypes));
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("NF type could not be set");
        }
    }

    /**
     * Checks that the mandatory controllers are defined and, if not, creates them.
     * Mandatory controllers: NAME, LIFECYCLE
     * Also: CONTENT for composite, BINDING for composites and primitive with F client itfs
     * 
     * CHECK: Is it needed to check if the interface exists, but no implementation has been given yet?
     * Compare with the equivalent method of PAComponentImpl.
     * It does not seem to be needed. 
     */
    private void checkMandatoryControllers(Vector<InterfaceType> nfType) {

        PAInterface itfRef = null;
        Class<?> controllerClass = null;
        boolean isPrimitive = Constants.PRIMITIVE.equals(this.componentParameters.getHierarchicalType());
        boolean hasFClientInterfaces = this.componentParameters.getClientInterfaceTypes().length > 0;

        try {
            // LIFECYCLE Controller
            if (!existsNfInterface(Constants.LIFECYCLE_CONTROLLER)) {
                // default implementation of PAGCMLifeCycleController
                controllerClass = PAGCMLifeCycleControllerImpl.class;
                itfRef = createControllerRepresentative(controllerClass);
                this.nfItfs.put(itfRef.getFcItfName(), itfRef);
                nfType.add((InterfaceType) itfRef.getFcItfType());
                // ASSERTIONS: controller implements PAGCMLifeCycleController, and controllerName is "lifecycle-controller"
            }

            // NAME Controller
            if (!existsNfInterface(Constants.NAME_CONTROLLER)) {
                // default implementation of NameController 
                controllerClass = PANameControllerImpl.class;
                itfRef = createControllerRepresentative(controllerClass);
                this.nfItfs.put(itfRef.getFcItfName(), itfRef);
                nfType.add((InterfaceType) itfRef.getFcItfType());
                // ASSERTIONS: controller implements NameController, and controllerName is "name-controller"
            }

            // CONTENT Controller if composite
            if (!existsNfInterface(Constants.CONTENT_CONTROLLER) && !isPrimitive) {
                // default implementation of PAContentController
                controllerClass = PAContentControllerImpl.class;
                itfRef = createControllerRepresentative(controllerClass);
                this.nfItfs.put(itfRef.getFcItfName(), itfRef);
                nfType.add((InterfaceType) itfRef.getFcItfType());
                // ASSERTIONS: controller implements PAContentController, and controllerName is "content-controller"
            }

            //BINDING Controller if composite, or primitive with F client interfaces or NF internal server interfaces
            if (!existsNfInterface(Constants.BINDING_CONTROLLER) &&
                !(isPrimitive && !hasFClientInterfaces && !hasNfInternalServerInterfaces())) {
                // default implementation of PABindingController
                controllerClass = PABindingControllerImpl.class;
                itfRef = createControllerRepresentative(controllerClass);
                this.nfItfs.put(itfRef.getFcItfName(), itfRef);
                nfType.add((InterfaceType) itfRef.getFcItfType());
                // ASSERTIONS: controller implements PABindingController, and controllerName is "binding-controller"
            }

            // Membrane Controller
            // Must be created it was declared and it has no implementation yet
            if (existsNfInterface(Constants.MEMBRANE_CONTROLLER)) {
                PAInterface membraneItfRef = (PAInterface) this.nfItfs.get(Constants.MEMBRANE_CONTROLLER);
                if (((StubObject) membraneItfRef).getProxy() == null) {
                    // default implementation of PAMembraneController 
                    controllerClass = PAMembraneControllerImpl.class;
                    itfRef = createControllerRepresentative(controllerClass);
                    // replace the previous entry for 'membrane-controller'
                    this.nfItfs.put(itfRef.getFcItfName(), itfRef);
                    // but don't re-add the type to the nfType vector, because it already exists
                }
                // ASSERTIONS: controller implements PAMembraneController, and controllerName is "membrane-controller"
            }

            // Interceptor Controller
            // Must be created it was declared and it has no implementation yet
            if (existsNfInterface(Constants.INTERCEPTOR_CONTROLLER)) {
                PAInterface interceptorItfRef = (PAInterface) this.nfItfs
                        .get(Constants.INTERCEPTOR_CONTROLLER);
                if (((StubObject) interceptorItfRef).getProxy() == null) {
                    // default implementation of PAInterceptorController 
                    controllerClass = PAInterceptorControllerImpl.class;
                    itfRef = createControllerRepresentative(controllerClass);
                    // replace the previous entry for 'interceptor-controller'
                    this.nfItfs.put(itfRef.getFcItfName(), itfRef);
                    // but don't re-add the type to the nfType vector, because it already exists
                }
                // ASSERTIONS: controller implements PAInterceptorController, and controllerName is "interceptor-controller"
            }

        } catch (Exception e) {
            throw new ProActiveRuntimeException("Could not create mandatory controller representative '" +
                controllerClass.getName() + "' while instantiating component'" +
                this.componentParameters.getName() + "': " + e.getMessage(), e);
        }
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
     * Instantiates an object controller and generates its representative interface using only the Controller class
     * (which must implement {@link AbstractPAController}.
     * 
     * The {@link PAGCMInterfaceType} is obtained from the object after instantiating it. 
     * 
     * @param controllerClass
     * @return interface generated for the controller
     */
    private PAInterface createControllerRepresentative(Class<?> controllerClass) throws Exception {

        // Instantiate the controller object, setting THIS component as its owner.
        Constructor<?> controllerClassConstructor = controllerClass
                .getConstructor(new Class[] { Component.class });
        AbstractPAController controller = (AbstractPAController) controllerClassConstructor
                .newInstance(new Object[] { this });
        // Obtains the interface type after having instantiated the object
        PAGCMInterfaceType controllerItfType = (PAGCMInterfaceType) controller.getFcItfType();
        String controllerName = controller.getFcItfName();
        // Generates the representative PAInterface 
        PAInterface itfRef = RepresentativeInterfaceClassGenerator.instance().generateInterface(
                controllerName, this, controllerItfType, controllerItfType.isInternal(), false);
        //itfRef.setFcItfImpl(controller);

        // TODO: This was not done before. Is it needed now? Following the scheme of "addControllers" I would say yes
        ((StubObject) itfRef).setProxy(this.proxy);
        return itfRef;
    }

    /**
     * Checks if the NF interface 'itfName' has already been defined.
     * @param itfName
     * @return
     */
    private boolean existsNfInterface(String itfName) {
        if (this.nfItfs.containsKey(itfName)) {
            return true;
        }
        // not sure how to handle collection members (IF they must be handled differently)
        //if(this.collectionNfItfsMembers.containsKey(itfName)) {
        //    		return true;
        //}
        return false;
    }

    /**
     * @param componentType
     */
    private void addFunctionalInterfaces() {
        InterfaceType[] itfTypes = this.componentParameters.getComponentType().getFcInterfaceTypes();
        this.fItfs = new HashMap<String, Interface>(itfTypes.length + (itfTypes.length / 2));

        try {
            for (int j = 0; j < itfTypes.length; j++) {
                if (!itfTypes[j].isFcCollectionItf()) {
                    // itfs members of collection itfs are dynamically generated
                    Interface interface_reference = RepresentativeInterfaceClassGenerator.instance()
                            .generateFunctionalInterface(itfTypes[j].getFcItfName(), this,
                                    (PAGCMInterfaceType) itfTypes[j]);

                    // all calls are to be reified
                    if (interface_reference != null) {
                        this.fItfs.put(interface_reference.getFcItfName(), interface_reference);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("cannot create interface references : " + e.getMessage());
        }
    }

    protected Object reifyCall(String className, String methodName, Class<?>[] parameterTypes,
            Object[] effectiveParameters, short priority) {
        try {
            return this.proxy.reify(MethodCall.getComponentMethodCall(Class.forName(className)
                    .getDeclaredMethod(methodName, parameterTypes), effectiveParameters, null, (String) null,
                    null, priority));

            // functional interface name is null
        } catch (NoSuchMethodException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (Throwable e) {
            throw new ProActiveRuntimeException(e.toString());
        }
    }

    /**
     * Retrieves an interface from its name.
     * NF Interfaces (both client/server) must have a name ending in "-controller".
     * 
     * @see org.objectweb.fractal.api.Component#getFcInterface(String)
     */
    public Object getFcInterface(String interfaceName) throws NoSuchInterfaceException {
        if (interfaceName.endsWith("-controller") && !(Constants.ATTRIBUTE_CONTROLLER.equals(interfaceName))) {
            if (this.nfItfs == null) {
                // Check: is it needed to do this?... the addControllers method, or equivalent, should have been called at construction,
                //        and also maybe we're not using a controller config file
                addControllers(this.componentParameters.getControllerDescription().getControllersSignatures());
            }
            if (this.nfItfs.containsKey(interfaceName)) {
                return this.nfItfs.get(interfaceName);
            }
            // TODO: Check how are the collective NF interfaces handled here ... should they also finish by "-controller" ?
            throw new NoSuchInterfaceException(interfaceName);
        }

        if (this.fItfs.containsKey(interfaceName)) {
            return this.fItfs.get(interfaceName);
        } else {
            if (interfaceName.equals(Constants.COMPONENT)) {
                return this;
            }

            // maybe the member of a collection itf?
            InterfaceType itfType = ((ComponentType) this.getFcType()).getFcInterfaceType(interfaceName);
            if ((itfType != null) && itfType.isFcCollectionItf()) {
                try {
                    // generate the corresponding interface locally
                    Interface interface_reference = RepresentativeInterfaceClassGenerator.instance()
                            .generateFunctionalInterface(interfaceName, this, (PAGCMInterfaceType) itfType);

                    ((StubObject) interface_reference).setProxy(this.proxy);
                    // keep it in the list of functional interfaces
                    this.fItfs.put(interfaceName, interface_reference);
                    return interface_reference;
                } catch (Throwable e) {
                    logger.info("Could not generate " + interfaceName + " collection interface", e);
                }
            }
        }

        throw new NoSuchInterfaceException(interfaceName);
    }

    /*
     * implements org.objectweb.fractal.api.Component#getFcInterfaces()
     */
    public Object[] getFcInterfaces() {
        Interface[] nfInterfaces = this.nfItfs.values().toArray(new Interface[this.nfItfs.size()]);
        Interface[] fcInterfaces = this.fItfs.values().toArray(new Interface[this.fItfs.size()]);
        Interface[] result = new Interface[nfInterfaces.length + fcInterfaces.length + 1];
        System.arraycopy(nfInterfaces, 0, result, 0, nfInterfaces.length);
        System.arraycopy(fcInterfaces, 0, result, nfInterfaces.length, fcInterfaces.length);
        result[result.length - 1] = this;
        return result;
    }

    /*
     * implements org.objectweb.fractal.api.Component#getFcType()
     */
    public Type getFcType() {
        return this.componentParameters.getComponentType();
    }

    /*
     * implements org.objectweb.proactive.core.mop.StubObject#getProxy()
     */
    public Proxy getProxy() {
        return this.proxy;
    }

    /*
     * implements org.objectweb.proactive.core.mop.StubObject#setProxy(Proxy)}
     */
    public void setProxy(Proxy proxy) {
        // sets proxy for non functional interfaces (what does this mean?)
        this.proxy = proxy;

        // sets the same proxy for all interfaces of this component (including NF, because getFcInterfaces returns ALL interfaces)
        Object[] interfaces = getFcInterfaces();
        PAInterface[] interface_references = new PAInterface[interfaces.length - 1];
        for (int i = 0; i < interfaces.length; i++) {
            if (!interfaces[i].equals(this)) {
                interface_references[i] = (PAInterface) interfaces[i];
            }
        }
        for (int i = 0; i < interface_references.length; i++) {
            if (this.useShortcuts) {
                // adds an intermediate FunctionalInterfaceProxy for functional interfaces, to manage shortcutting
                ((StubObject) interface_references[i]).setProxy(new FunctionalInterfaceProxyImpl(proxy,
                    interface_references[i].getFcItfName()));
            } else {
                try {
                    ((StubObject) interface_references[i]).setProxy(proxy);
                } catch (RuntimeException e) {
                    logger.error(e.getMessage());
                    throw new ProActiveRuntimeException(e);
                }
            }
        }
    }

    /**
     *  The comparison of component references is actually a comparison of unique
     * identifiers across jvms.
     */
    @Override
    public boolean equals(Object component) {
        Object result = reifyCall(Object.class.getName(), "equals", new Class<?>[] { Object.class },
                new Object[] { component }, ComponentRequest.STRICT_FIFO_PRIORITY);
        return ((Boolean) result).booleanValue();
    }

    @Override
    public int hashCode() {
        // reified as a standard invocation (not a component one)
        Object result;
        try {
            result = this.proxy.reify(MethodCall.getMethodCall(Class.forName(Object.class.getName())
                    .getDeclaredMethod("hashCode", new Class<?>[] {}), new Object[] {},
                    (Map<TypeVariable<?>, Class<?>>) null));
            return ((Integer) result).intValue();
        } catch (SecurityException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (NoSuchMethodException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (ClassNotFoundException e) {
            throw new ProActiveRuntimeException(e.toString());
        } catch (Throwable e) {
            throw new ProActiveRuntimeException(e.toString());
        }
    }

    /**
     * Only valid for a single element. return null for a group.
     */
    public UniqueID getID() {
        if (!(getProxy() instanceof ProxyForGroup)) {
            return ((UniversalBodyProxy) getProxy()).getBodyID();
        } else {
            return null;
        }
    }

    /*
     * implements
     * org.objectweb.proactive.core.component.identity.PAComponent#getReferenceOnBaseObject()
     */
    public Object getReferenceOnBaseObject() {
        logger.error("getReferenceOnBaseObject() method is not available in component representatives");
        return null;
    }

    /*
     * implements
     * org.objectweb.proactive.core.component.identity.PAComponent#getRepresentativeOnThis()
     */
    public PAComponent getRepresentativeOnThis() {
        return this;
    }

    /*
     * @seeorg.objectweb.proactive.core.component.representative.PAComponentRepresentative#
     * getStubOnReifiedObject()
     */
    public StubObject getStubOnBaseObject() {
        return this.stubOnBaseObject;
    }

    /*
     * @seeorg.objectweb.proactive.core.component.representative.PAComponentRepresentative#
     * setStubOnReifiedObject(org.objectweb.proactive.core.mop.StubObject)
     */
    public void setStubOnBaseObject(StubObject stub) {
        this.stubOnBaseObject = stub;
    }

    public boolean isPrimitive() {
        return Constants.PRIMITIVE.equals(this.componentParameters.getHierarchicalType());
    }

    public void _terminateAO(Proxy proxy) {
    }

    public void _terminateAOImmediatly(Proxy proxy) {
    }

    /**
     * @see org.objectweb.fractal.api.Interface#getFcItfName()
     */
    public String getFcItfName() {
        return Constants.COMPONENT;
    }

    /**
     * @see org.objectweb.fractal.api.Interface#getFcItfOwner()
     */
    public Component getFcItfOwner() {
        return this;
    }

    /**
     * @see org.objectweb.fractal.api.Interface#getFcItfType()
     */
    public Type getFcItfType() {
        return this.componentParameters.getComponentType();
    }

    /**
     * @see org.objectweb.fractal.api.Interface#isFcInternalItf()
     */
    public boolean isFcInternalItf() {
        return false;
    }

    @Override
    public String toString() {
        String string = "name : " + getFcItfName() + "\n" + getFcItfType() + "\n" + "isInternal : " +
            isFcInternalItf() + "\n";
        return string;
    }

    public ComponentParameters getComponentParameters() {
        return this.componentParameters;
    }

    public void setImmediateServices() {
        throw new UnsupportedOperationException("only on the identity component");
    }
}
