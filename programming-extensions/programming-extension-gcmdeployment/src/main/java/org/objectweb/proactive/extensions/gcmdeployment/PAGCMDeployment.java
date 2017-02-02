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
package org.objectweb.proactive.extensions.gcmdeployment;

import java.io.File;
import java.net.URL;

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
                                                                                          GCMApplication.class.getName(),
                                                                                          gcma,
                                                                                          GCMApplicationRemoteObjectAdapter.class);
        RemoteRemoteObject rro = roe.createRemoteObject(name, false);

        @SuppressWarnings("unchecked")
        GCMApplication gcmApplication = (GCMApplication) RemoteObjectHelper.generatedObjectStub(new RemoteObjectAdapter(rro));

        return gcmApplication;
    }
}
