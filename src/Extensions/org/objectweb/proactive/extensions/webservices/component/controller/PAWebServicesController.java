package org.objectweb.proactive.extensions.webservices.component.controller;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.webservices.exceptions.UnknownFrameWorkException;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;


public interface PAWebServicesController {

    public final static String WEBSERVICES_CONTROLLER = "webservices-controller";

    /**
     * Sets the url where the component will be deployed.
     *
     * @param The url where the component will be deployed.
     */
    public void setUrl(String url);

    /**
     * Gets the url where the component will be deployed.
     *
     * @return The url where the component will be deployed.
     */
    public String getUrl();

    /**
     * Initializes the web service servlet on a remote node
     *
     * @param node Node where to initialize the servlet
     * @throws ActiveObjectCreationException
     * @throws NodeException
     * @throws UnknownFrameWorkException
     */
    public void initServlet(Node... nodes) throws ActiveObjectCreationException, NodeException,
            UnknownFrameWorkException;

    /**
     * Gets the default local url where the component will be deployed.
     *
     * @return The default local url where the component will be deployed
     */
    public String getLocalUrl();

    //@snippet-start PAWebServicesController_expose_methods
    /**
     * Expose a component as a web service. Each server interface of the component
     * will be accessible by  the urn [componentName]_[interfaceName].
     * Only the interfaces public methods of the specified interfaces in
     * <code>interfaceNames</code> will be exposed.
     *
     * @param componentName Name of the component
     * @param interfaceNames Names of the interfaces we want to deploy.
      *                           If null, then all the interfaces will be deployed
     * @throws WebServicesException
     */
    public void exposeComponentAsWebService(String componentName, String[] interfaceNames)
            throws WebServicesException;

    /**
     * Expose a component as web service. Each server interface of the component
     * will be accessible by  the urn [componentName]_[interfaceName].
     * All the interfaces public methods of all interfaces will be exposed.
     *
     * @param componentName Name of the component
     * @throws WebServicesException
     */
    public void exposeComponentAsWebService(String componentName) throws WebServicesException;

    /**
     * Undeploy all the client interfaces of a component deployed on a web server.
     *
     * @param componentName The name of the component
     * @throws WebServicesException
     */
    public void unExposeComponentAsWebService(String componentName) throws WebServicesException;

    /**
     * Undeploy specified interfaces of a component deployed on a web server
     *
     * @param componentName The name of the component
     * @param interfaceNames Interfaces to be undeployed
     * @throws WebServicesException
     */
    public void unExposeComponentAsWebService(String componentName, String[] interfaceNames)
            throws WebServicesException;
    //@snippet-end PAWebServicesController_expose_methods
}
