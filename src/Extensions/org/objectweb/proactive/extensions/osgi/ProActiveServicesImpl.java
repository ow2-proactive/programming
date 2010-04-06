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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.osgi;

import java.io.IOException;
import java.net.URI;
import java.rmi.AlreadyBoundException;

import javax.servlet.Servlet;

import org.apache.felix.servicebinder.ServiceBinderContext;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.httpserver.ClassServerServlet;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;


/**
 * @see org.objectweb.proactive.osgi.ProActiveService
 * @author The ProActive Team
 *
 */
public class ProActiveServicesImpl implements ProActiveService {
    private Node node;
    private Servlet servlet;
    private BundleContext bc;
    private static final String aliasServlet = "/";
    private static final String OSGI_NODE_NAME = "OSGiNode";
    private HttpService http;
    private int port;

    static {
        ProActiveConfiguration.load();
    }

    public ProActiveServicesImpl() {
    }

    public ProActiveServicesImpl(ServiceBinderContext context) {
        this.bc = context.getBundleContext();
        this.port = Integer.parseInt(this.bc.getProperty("org.osgi.service.http.port"));
    }

    public void setNode(Node node) {
        this.node = node;
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#newActive(java.lang.String, java.lang.Object[])
     */
    public Object newActive(String className, Object[] params) throws ActiveObjectCreationException,
            NodeException {
        return PAActiveObject.newActive(className, params, this.node);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#register(java.lang.Object, java.lang.String)
     */
    @SuppressWarnings("deprecation")
    public void register(Object obj, String url) throws ProActiveException {
        PAActiveObject.register(obj, url);
    }

    /**
     * @see org.objectweb.proactive.osgi.ProActiveService#unregister(URI)
     */
    public void unregister(String url) throws IOException {
        PAActiveObject.unregister(url);
    }

    /**
     *
     * @param ref
     */
    public void bind(HttpService ref) {
        this.http = ref;
        createProActiveService();
    }

    /**
     *
     * @param ref
     */
    public void unbind(HttpService ref) {
        System.out.println("Node is no more accessible by Http, temination of ProActiveService");
        terminate();
    }

    /**
     *
     *
     */
    private void createProActiveService() {
        this.servlet = ClassServerServlet.get();
        boolean b = registerServlet();
        if (!b) {
            System.out.println("Servlet has not been registered");
            return;
        }
        createNode();
    }

    /**
     *
     *
     */
    private void createNode() {
        try {
            this.node = NodeFactory.createLocalNode(OSGI_NODE_NAME, true, null, null, null);
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return
     */
    private boolean registerServlet() {
        try {
            this.http.registerServlet(aliasServlet, this.servlet, null, null);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    /**
     *
     */
    public void terminate() {

        /* kill Nodes */
        try {
            this.node.killAllActiveObjects();
            //            ProActiveRuntimeImpl.getProActiveRuntime().killNode(OSGI_NODE_NAME);
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* unregister Servlet */
        //this.http.unregister(aliasServlet);
    }

    public Object lookupActive(String className, String url) {
        try {
            return PAActiveObject.lookupActive(className, url);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
