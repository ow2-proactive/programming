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
package org.objectweb.proactive.extensions.gcmdeployment;

import org.objectweb.proactive.annotation.PublicAPI;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;
import org.objectweb.proactive.core.xml.VariableContractImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationImpl;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationRemoteObjectAdapter;
import org.objectweb.proactive.gcmdeployment.GCMApplication;

import java.io.File;
import java.net.URL;


/**
 * 
 * This class provides entry points to deploy application by using the GCM Deployment framework.
 * 
 * It allows to create a GCMApplication from an GCM Application descriptor XML file.
 * 
 * @author The ProActive Team
 * @since ProActive 4.0
 * 
 */
@PublicAPI
public class PAGCMDeployment {

    /**
     * Returns a {@link GCMApplication} described by the given GCM Application XML Descriptor file
     * 
     * @param url
     *            URL to the GCM Application Descriptor file
     * @return The GCM Application described by the XML Descriptor
     * @throws ProActiveException
     *             If the GCM Application Descriptor cannot be loaded
     */
    public static GCMApplication loadApplicationDescriptor(URL url) throws ProActiveException {
        return loadApplicationDescriptor(url, null);
    }

    /**
     * Returns a {@link GCMApplication} to manage the GCM Application described by the GCM
     * Application Descriptor XML file
     * 
     * @param file
     *            abstract file to the GCM Application Descriptor file
     * @return A GCM Application
     * @throws ProActiveException
     *             If the GCM Application Descriptor cannot be loaded
     */
    public static GCMApplication loadApplicationDescriptor(File file) throws ProActiveException {
        return loadApplicationDescriptor(Helpers.fileToURL(file), null);
    }

    /**
     * Returns a {@link GCMApplication} described by the given GCM Application XML Descriptor file
     * 
     * 
     * @param url
     *            URL to The GCM Application Descriptor file
     * @param vContract
     *            A Variable Contract between GCM Application and Deployment XML descriptors and the
     *            program.
     * @return The GCM Application described by the XML Descriptor
     * @throws ProActiveException
     *             If the GCM Application Descriptor cannot be loaded
     */
    public static GCMApplication loadApplicationDescriptor(URL url, VariableContractImpl vContract)
            throws ProActiveException {
        GCMApplication gcma = new GCMApplicationImpl(url, vContract);
        return getRemoteObjectAdapter(gcma);
    }

    /**
     * Returns a {@link GCMApplication} to manage the GCM Application described by the GCM
     * Application Descriptor XML file
     * 
     * @param file
     *            abstract file to the GCM Application Descriptor file
     * @param vContract
     *            A Variable Contract between the descriptors and the application program
     * @return A GCM Application
     * @throws ProActiveException
     *             If the GCM Application Descriptor cannot be loaded
     */
    public static GCMApplication loadApplicationDescriptor(File file, VariableContractImpl vContract)
            throws ProActiveException {
        GCMApplication gcma = new GCMApplicationImpl(Helpers.fileToURL(file), vContract);
        return getRemoteObjectAdapter(gcma);
    }

    private static GCMApplication getRemoteObjectAdapter(GCMApplication gcma) throws ProActiveException {
        // Export this GCMApplication as a remote object
        String name = gcma.getDeploymentId() + "/GCMApplication";
        RemoteObjectExposer<GCMApplication> roe = new RemoteObjectExposer<GCMApplication>(name,
            GCMApplication.class.getName(), gcma, GCMApplicationRemoteObjectAdapter.class);
        RemoteRemoteObject rro = roe.createRemoteObject(name, false);
        return (GCMApplication) RemoteObjectHelper.generatedObjectStub(new RemoteObjectAdapter(rro));
    }
}
