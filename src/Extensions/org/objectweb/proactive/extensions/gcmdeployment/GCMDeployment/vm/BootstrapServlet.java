/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.mortbay.jetty.servlet.ServletHolder;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.httpserver.HTTPServer;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.core.util.URIBuilder;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


public final class BootstrapServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 430L;
    final static public String NS = "/bootstrap";
    final static public String MAPPING = NS + "/*";
    final static public String VM_ID = "vmid";
    final static public String DEPLOYMENT_ID = "deploymentId";
    final static public String TOPOLOGY_ID = "topologyId";
    final static public String PARENT_URL = "parentURL";
    final static public String ROUTER_ADDRESS = "routerAddress";
    final static public String ROUTER_PORT = "routerPort";
    final static public String COMMUNICATION_PROTOCOL = "communicationProtocol";
    final static public String SERVER_CODEBASE = "serverCodebase";
    final static public String PA_RT_COMMAND = "runtimeCommand";
    private Map<String, HashMap<String, String>> applis = new HashMap<String, HashMap<String, String>>();

    final static private Logger logger = ProActiveLogger.getLogger(Loggers.VIRTUALIZATION_BOOTSTRAP);

    static BootstrapServlet servlet = null;

    private static final ReentrantLock serviceLock = new ReentrantLock();

    static public BootstrapServlet get() {
        try {
            serviceLock.lock();
            if (servlet == null) {
                HTTPServer server = HTTPServer.get();
                servlet = new BootstrapServlet();
                server.registerServlet(new ServletHolder(servlet), BootstrapServlet.MAPPING);
                logger.debug("Bootstrap Servlet initialized, mapped: " + BootstrapServlet.MAPPING);
            }

            return servlet;
        } finally {
            serviceLock.unlock();
        }
    }

    private BootstrapServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        if (logger.isDebugEnabled()) {
            String pathInfo = req.getPathInfo();
            String from = req.getLocalAddr() + ":" + req.getLocalPort();
            logger.debug("Serving request " + pathInfo + " from " + from);
        }
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        String vmid = req.getParameter(VM_ID);
        HashMap<String, String> values = applis.get(vmid.trim());
        if (values != null) {
            Set<String> keys = values.keySet();
            for (String key : keys) {
                resp.getOutputStream().println(key + " = " + values.get(key));
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    public boolean isRegistered(String deploymentId) {
        try {
            serviceLock.lock();
            return applis.get(deploymentId) != null;
        } finally {
            serviceLock.unlock();
        }
    }

    public String getBaseURI() {
        final URI uri = URIBuilder
                .buildURI(URIBuilder.getHostNameorIP(ProActiveInet.getInstance().getInetAddress()),
                        HTTPServer.SERVER_CONTEXT + NS + "/", Constants.XMLHTTP_PROTOCOL_IDENTIFIER,
                        CentralPAPropertyRepository.PA_XMLHTTP_PORT.getValue());

        return uri.toString();
    }

    /**
     * This method is used to register a new remote Virtual Machine runtime dedicated
     * web page. The virtual machine will be able to connect to the given url to
     * gather required pieces information to successfully bootstrap
     * child ProActive Runtime.
     * @param id A string used to register the virtual machine web page. You'll have to
     * use it later to retrieve the associated URL.
     * @param values A hashmap containing every information to display on the web page.
     * @return the address where you'll be able to display required information.
     */
    public String registerAppli(String id, HashMap<String, String> values) {
        try {
            serviceLock.lock();
            applis.put(id, values);
            String res = getBaseURI() + "?" + BootstrapServlet.VM_ID + "=" + id;
            logger.debug("Bootstrap servlet registered an app on:  " + res);
            return res;
        } finally {
            serviceLock.unlock();
        }
    }
}
