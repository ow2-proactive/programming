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
package org.objectweb.proactive.core.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.Constants;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.mop.ConstructionOfProxyObjectFailedException;
import org.objectweb.proactive.core.mop.MOP;
import org.objectweb.proactive.core.mop.MOPException;
import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.RuntimeFactory;
import org.objectweb.proactive.core.runtime.VMInformation;
import org.objectweb.proactive.utils.StackTraceUtil;


/**
 * <p>
 * A <code>Node</code> offers a set of services needed by ProActive to work with
 * remote JVM. Each JVM that is aimed to hold active objects should contains at least
 * one instance of the node class. That instance, when created, will be registered
 * to some registry where it is possible to perform a lookup (such as the RMI registry).
 * </p><p>
 * When ProActive needs to interact with a remote JVM, it will lookup for one node associated
 * with that JVM (using typically the RMI Registry) and use this node to perform the interaction.
 * </p><p>
 * We expect several concrete implementations of the Node to be wrtten such as a RMI node, a HTTP node ...
 * </p>
 *
 * @author The ProActive Team
 * @version 1.1,  2002/08/28
 * @since   ProActive 0.9
 *
 */

public class NodeImpl implements Node, Serializable {

    protected NodeInformation nodeInformation;

    protected ProActiveRuntime proActiveRuntime;

    protected String vnName;

    //
    // ----------Constructors--------------------
    //
    public NodeImpl() {
    }

    public NodeImpl(ProActiveRuntime proActiveRuntime, String nodeURL) {
        this.proActiveRuntime = proActiveRuntime;
        this.nodeInformation = new NodeInformationImpl(nodeURL);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nodeInformation == null) ? 0 : nodeInformation.hashCode());
        result = prime * result + ((vnName == null) ? 0 : vnName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeImpl other = (NodeImpl) obj;
        if (nodeInformation == null) {
            if (other.nodeInformation != null)
                return false;
        } else if (!nodeInformation.equals(other.nodeInformation))
            return false;
        if (vnName == null) {
            if (other.vnName != null)
                return false;
        } else if (!vnName.equals(other.vnName))
            return false;
        return true;
    }

    //
    //--------------------------Implements Node-----------------------------

    /**
     * @see org.objectweb.proactive.core.node.Node#getNodeInformation()
     */
    public NodeInformation getNodeInformation() {
        return nodeInformation;
    }

    /**
     * @see org.objectweb.proactive.core.node.Node#getProActiveRuntime
     */
    public ProActiveRuntime getProActiveRuntime() {
        return proActiveRuntime;
    }

    /**
     * @see org.objectweb.proactive.core.node.Node#getActiveObjects()
     */
    public Object[] getActiveObjects() throws NodeException, ActiveObjectCreationException {
        List<UniversalBody> bodyArray;
        try {
            bodyArray = this.proActiveRuntime.getActiveObjects(this.nodeInformation.getName());
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get Active Objects registered on this node: " +
                                    this.nodeInformation.getURL(), e);
        }
        if (bodyArray.size() == 0) {
            return new Object[0];
        } else {
            Object[] stubOnAO = new Object[bodyArray.size()];
            for (int i = 0; i < bodyArray.size(); i++) {
                UniversalBody body = bodyArray.get(i);
                String className = body.getReifiedClassName();
                try {
                    stubOnAO[i] = createStubObject(className, body);
                } catch (MOPException e) {
                    throw new ActiveObjectCreationException("Exception occured when trying to create stub-proxy", e);
                }
            }
            return stubOnAO;
        }
    }

    /**
     * @see org.objectweb.proactive.core.node.Node#getNumberOfActiveObjects()
     */
    public int getNumberOfActiveObjects() throws NodeException {
        List<UniversalBody> bodyArray;
        try {
            bodyArray = this.proActiveRuntime.getActiveObjects(this.nodeInformation.getName());
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get Active Objects registered on this node: " +
                                    this.nodeInformation.getURL(), e);
        }
        return bodyArray.size();
    }

    /**
     * @see org.objectweb.proactive.core.node.Node#getActiveObjects(String)
     */
    public Object[] getActiveObjects(String className) throws NodeException, ActiveObjectCreationException {
        List<UniversalBody> bodyArray;
        try {
            bodyArray = this.proActiveRuntime.getActiveObjects(this.nodeInformation.getName(), className);
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get Active Objects of type " + className + " registered on this node: " +
                                    this.nodeInformation.getURL(), e);
        }
        if (bodyArray.size() == 0) {
            throw new NodeException("no ActiveObjects of type " + className + " are registered for this node: " +
                                    this.nodeInformation.getURL());
        } else {
            Object[] stubOnAO = new Object[bodyArray.size()];
            for (int i = 0; i < bodyArray.size(); i++) {
                UniversalBody body = bodyArray.get(i);
                try {
                    stubOnAO[i] = createStubObject(className, body);
                } catch (MOPException e) {
                    throw new ActiveObjectCreationException("Exception occured when trying to create stub-proxy", e);
                }
            }
            return stubOnAO;
        }
    }

    private void readObject(ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException, ProActiveException {
        in.defaultReadObject();
        if (NodeFactory.isNodeLocal(this)) {
            this.proActiveRuntime = RuntimeFactory.getRuntime(nodeInformation.getURL());
        }
    }

    // -------------------------------------------------------------------------------------------
    //
    // STUB CREATION
    //
    // -------------------------------------------------------------------------------------------
    private static Object createStubObject(String className, UniversalBody body) throws MOPException {
        return createStubObject(className, null, new Object[] { body });
    }

    private static Object createStubObject(String className, Object[] constructorParameters, Object[] proxyParameters)
            throws MOPException {
        try {
            return MOP.newInstance(className,
                                   (Class[]) null,
                                   constructorParameters,
                                   Constants.DEFAULT_BODY_PROXY_CLASS_NAME,
                                   proxyParameters);
        } catch (ClassNotFoundException e) {
            throw new ConstructionOfProxyObjectFailedException("Class can't be found e=" + e);
        }
    }

    //
    //------------------------INNER CLASS---------------------------------------
    //
    protected class NodeInformationImpl implements NodeInformation {

        final private String nodeName;

        final private String nodeURL;

        final private VMInformation vmInformation;

        public NodeInformationImpl(String url) {
            this.nodeURL = url;
            this.nodeName = extractNameFromUrl(url);

            this.vmInformation = proActiveRuntime.getVMInformation();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
            result = prime * result + ((vmInformation == null) ? 0 : vmInformation.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof NodeInformationImpl))
                return false;
            NodeInformationImpl other = (NodeInformationImpl) obj;
            if (nodeName == null) {
                if (other.nodeName != null)
                    return false;
            } else if (!nodeName.equals(other.nodeName))
                return false;
            if (vmInformation == null) {
                if (other.vmInformation != null)
                    return false;
            } else if (!vmInformation.equals(other.vmInformation))
                return false;
            return true;
        }

        /**
         * @see org.objectweb.proactive.core.node.NodeInformation#getName()
         */
        public String getName() {
            return nodeName;
        }

        /**
         * @see org.objectweb.proactive.core.node.NodeInformation#getURL()
         */
        public String getURL() {
            return nodeURL;
        }

        /**
         * Returns the name specified in the url
         * @param url The url of the node
         * @return String. The name of the node
         */
        private String extractNameFromUrl(String url) {
            int n = url.lastIndexOf("/");
            String name = url.substring(n + 1);
            return name;
        }

        public VMInformation getVMInformation() {
            return vmInformation;
        }
    }

    // SECURITY

    /**
     *
     * @throws IOException
     * @see org.objectweb.proactive.core.node.Node#killAllActiveObjects()
     */
    public void killAllActiveObjects() throws NodeException, IOException {
        List<UniversalBody> bodyArray;
        try {
            bodyArray = this.proActiveRuntime.getActiveObjects(this.nodeInformation.getName());
        } catch (ProActiveException e) {
            throw new NodeException("Cannot get Active Objects registered on this node: " +
                                    this.nodeInformation.getURL(), e);
        }

        for (UniversalBody body : bodyArray) {
            try {
                // reify for remote terminate
                PAActiveObject.terminateActiveObject(MOP.createStubObject(Object.class.getName(), body), true);
            } catch (MOPException e) {
                // Bad error handling but terminateActiveObject eat remote exceptions
                throw new IOException("Cannot contact Active Objects on this node: " + this.nodeInformation.getURL() +
                                      " caused by " + e.getMessage());
            }
        }
    }

    /**
     * @see org.objectweb.proactive.core.node.Node#setProperty(java.lang.String, java.lang.String)
     */
    public Object setProperty(String key, String value) throws ProActiveException {
        return this.proActiveRuntime.setLocalNodeProperty(this.nodeInformation.getName(), key, value);
    }

    /**
     * @see org.objectweb.proactive.core.node.Node#getProperty(java.lang.String)
     */
    public String getProperty(String key) throws ProActiveException {
        return this.proActiveRuntime.getLocalNodeProperty(this.nodeInformation.getName(), key);
    }

    @Override
    public String getThreadDump() throws ProActiveException {
        return StackTraceUtil.getAllStackTraces();
    }

    public VMInformation getVMInformation() {
        return proActiveRuntime.getVMInformation();
    }
}
