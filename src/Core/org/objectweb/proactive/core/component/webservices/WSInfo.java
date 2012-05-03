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
package org.objectweb.proactive.core.component.webservices;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.proactive.annotation.PublicAPI;


/**
 * Web service informations used to bind a client interface of a component to a web service: contains the URL
 * of the web service and the name of the class which has to be used to call this web service.
 * <br>
 * The class in charge to call the web service must implement the {@link PAWSCaller} interface.
 * <br>
 * By default, the class {@link CXFWSCaller}, using the <a href="http://cxf.apache.org/">CXF</a> API,
 * is used.
 *
 * @author The ProActive Team
 */
@PublicAPI
public class WSInfo implements Serializable {
    /**
     * Name of the interface which must be implemented by the class in charge of calling the web service.
     */
    public static final String PAWSCALLER_ITF_NAME = PAWSCaller.class.getName();

    /**
     * Shortcut ID to specify that CXF must be used to call the web service.
     */
    public static final String CXFWSCALLER_ID = "CXF";

    /**
     * Full name of the class calling web services using CXF.
     */
    public static final String CXFWSCALLER_CLASSNAME = CXFWSCaller.class.getName();

    /**
     * Shortcut ID to specify that CXF configured for JAX-WS must be used to call the web service.
     */
    public static final String JAXWSCXFWSCALLER_ID = "JaxWsCXF";

    /**
     * Full name of the class calling web services using CXF configured for JAX-WS.
     */
    public static final String JAXWSCXFWSCALLER_CLASSNAME = JaxWsCXFWSCaller.class.getName();

    /**
     * Shortcut ID to specify that CXF configured for JAX-WS must be used to call the web service.
     */
    public static final String JAXWSCXFWSCALLER_ID = "JaxWsCXF";

    /**
     * Full name of the class calling web services using CXF configured for JAX-WS.
     */
    public static final String JAXWSCXFWSCALLER_CLASSNAME = JaxWsCXFWSCaller.class.getName();

    /**
     * Shortcut ID to specify that CXF configured for an Aegis data binding must be used to call the web service.
     */
    public static final String CXFAEGISWSCALLER_ID = "CXFAegis";

    /**
     * Full name of the class calling web services using CXF configured for an Aegis data binding.
     */
    public static final String CXFAEGISWSCALLER_CLASSNAME = CXFAegisWSCaller.class.getName();

    /**
     * Shortcut ID to specify that CXF with a dynamic client must be used to call the web service.
     */
    public static final String DYNAMICCXFWSCALLER_ID = "DynamicCXF";

    /**
     * Full name of the class calling web services using CXF with a dynamic client.
     */
    public static final String DYNAMICCXFWSCALLER_CLASSNAME = DynamicCXFWSCaller.class.getName();

    /**
     * Shortcut ID to specify that CXF must be used to call the RESTful service.
     */
    public static final String CXFRESTFULSERVICECALLER_ID = "CXFRESTful";

    /**
     * Full name of the class calling RESTful services using CXF.
     */
    public static final String CXFRESTFULSERVICECALLER_CLASSNAME = CXFRESTfulServiceCaller.class.getName();

    /**
     * URL of the web service.
     */
    private String wsUrl;

    /**
     * Full name of the class to use to call the web service.
     */
    private String wsCallerClassName;

    /**
     * Main constructor.
     * <br>
     * The String passed as argument is the URL of the web service (not the WSDL address).
     * By default the CXF API is used to call the web service, but it is also possible to
     * specify another library. If so, the URL must be followed, in parenthesis, by the ID
     * or the full name of the class to use to call the web service. The ID (not case
     * sensitive) may be CXF, JaxWsCXF, CXFAegis, DynamicCXF or CXFRESTful to use one of
     * these client configuration to call the web service. Otherwise, the full class name
     * given must be the one of a class implementing the {@link PAWSCaller} interface.
     * <br>
     * For instance:
     * <br>
     * "http://localhost:8080/proactive/services/Server_HelloWorld(org.objectweb.proactive.core.component.webservices.CXFWSCaller)"
     * <br>
     * which is equivalent to:
     * <br>
     * "http://localhost:8080/proactive/services/Server_HelloWorld(CXF)"
     * <br>
     * and which, as CXF is used by default, is also equivalent to:
     * <br>
     * "http://localhost:8080/proactive/services/Server_HelloWorld"
     *
     * @param infos URL of the web service, possibly followed in parenthesis by the ID or
     * the full name of the class to use to call the web service.
     * @throws IllegalBindingException If the URL is malformed or if the ID or class name is incorrect.
     */
    public WSInfo(String infos) throws IllegalBindingException {
        String[] wsInfo = infos.split("\\(|\\)");
        this.wsUrl = checkWSURL(wsInfo[0]);
        this.wsCallerClassName = selectWSCallerClassName(wsInfo);
    }

    /**
     * Secondary constructor.
     *
     * @param wsUrl The URL of the web service.
     * @param wsCallerClassName Name of the class, implementing the {@link PAWSCaller}
     * interface, to use to call the web service.
     * @throws IllegalBindingException If the URL is malformed or if the ID or class name is incorrect.
     */
    public WSInfo(String wsUrl, String wsCallerClassName) throws IllegalBindingException {
        this.wsUrl = checkWSURL(wsUrl);
        this.wsCallerClassName = checkClassName(wsCallerClassName);
    }

    /*
     * Checks if the given URL is valid.
     */
    private String checkWSURL(String wsUrl) throws IllegalBindingException {
        try {
            new URL(wsUrl);
            return wsUrl;
        } catch (MalformedURLException e) {
            IllegalBindingException ibe = new IllegalBindingException(
                "The URL of the web service is malformed: " + wsUrl);
            ibe.initCause(e);
            throw ibe;
        }
    }

    /*
     * Checks if the given class exists and implements the PROACTIVEWSCALLER_ITF_NAME interface.
     */
    private String checkClassName(String className) throws IllegalBindingException {
        try {
            Class<?> c = Class.forName(className);
            if (!Class.forName(PAWSCALLER_ITF_NAME).isAssignableFrom(c)) {
                throw new IllegalBindingException("The web service caller: " + className +
                    " must implement the " + PAWSCALLER_ITF_NAME + " interface");
            }
            return className;
        } catch (ClassNotFoundException e) {
            IllegalBindingException ibe = new IllegalBindingException("Web service caller: " + className +
                " cannot be found");
            ibe.initCause(e);
            throw ibe;
        }
    }

    /*
     * Selects the appropriate name of the class to use to call the web service.
     */
    private String selectWSCallerClassName(String[] wsInfo) throws IllegalBindingException {
        if (wsInfo.length == 2) {
            if (wsInfo[1].equalsIgnoreCase(CXFWSCALLER_ID)) {
                return CXFWSCALLER_CLASSNAME;
            } else if (wsInfo[1].equalsIgnoreCase(JAXWSCXFWSCALLER_ID)) {
                return JAXWSCXFWSCALLER_CLASSNAME;
            } else if (wsInfo[1].equalsIgnoreCase(CXFAEGISWSCALLER_ID)) {
                return CXFAEGISWSCALLER_CLASSNAME;
            } else if (wsInfo[1].equalsIgnoreCase(DYNAMICCXFWSCALLER_ID)) {
                return DYNAMICCXFWSCALLER_CLASSNAME;
            } else if (wsInfo[1].equalsIgnoreCase(CXFRESTFULSERVICECALLER_ID)) {
                return CXFRESTFULSERVICECALLER_CLASSNAME;
            } else {
                return checkClassName(wsInfo[1]);
            }
        } else {
            return CXFWSCALLER_CLASSNAME;
        }
    }

    /**
     * Getter for the URL of the web service (not the WSDL address).
     *
     * @return URL of the web service.
     */
    public String getWSUrl() {
        return wsUrl;
    }

    /**
     * Setter for the URL of the web service (not the WSDL address).
     *
     * @param wsUrl URL of the web service.
     * @throws IllegalBindingException If the URL is malformed.
     */
    public void setWSUrl(String wsUrl) throws IllegalBindingException {
        this.wsUrl = checkWSURL(wsUrl);
    }

    /**
     * Getter for the full name of the class to use to call the web service.
     *
     * @return The full name of the class to use to call the web service.
     */
    public String getWSCallerClassName() {
        return wsCallerClassName;
    }
}
