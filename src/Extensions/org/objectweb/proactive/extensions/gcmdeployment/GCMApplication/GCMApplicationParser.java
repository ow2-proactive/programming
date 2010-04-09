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
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserConstants;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.core.GCMVirtualNodeInternal;
import org.xml.sax.SAXException;


/**
 * A parser for the GCM Application descriptor schema.
 *
 * @author The ProActive Team
 *
 */
public interface GCMApplicationParser extends GCMParserConstants {

    /**
     * Returns all the Resources Providers
     * Descriptor
     *
     * @return all the declared Resources Providers as NodeProviderParams
     * @throws IOException
     * @throws SAXException
     */
    public Map<String, NodeProvider> getNodeProviders() throws Exception;

    /**
     * Returns all the Virtual Node
     *
     * @return all the declared Virtual Nodes
     * @throws IOException
     * @throws SAXException
     */
    public Map<String, GCMVirtualNodeInternal> getVirtualNodes() throws Exception;

    /**
     * Returns the Command Builder
     *
     * @return the Command Builder associated to this application
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public CommandBuilder getCommandBuilder() throws Exception;

    /**
     * 
     * @return the technical services for the defined application
     */
    public TechnicalServicesProperties getAppTechnicalServices();

    public ProActiveSecurityManager getProactiveApplicationSecurityManager();

    public void setProactiveApplicationSecurityManager(
            ProActiveSecurityManager proactiveApplicationSecurityManager);

    /**
     * Checks whether Data Spaces should be enabled for an application.
     * 
     * @return <code>true</code> if Data Spaces are requested to be enabled; <code>false</code>
     *         otherwise
     */
    public boolean isDataSpacesEnabled();

    /**
     * Sets Data Spaces to be enabled or not for an application.
     * 
     * @param dataSpacesEnabled
     *            <code>true</code> if Data Spaces are requested to be enabled; <code>false</code>
     *            otherwise (by default)
     */
    public void setDataSpacesEnabled(boolean dataSpacesEnabled);

    /**
     * @return set of input and output data spaces configurations defined for an application; may be
     *         <code>null</code> (by default) if no spaces are defined or
     *         {@link #isDataSpacesEnabled()} returns <code>false</code>.
     */
    public Set<InputOutputSpaceConfiguration> getInputOutputSpacesConfigurations();

    /**
     * Sets input and output data spaces configuration for an application.
     * 
     * @param inputOutputSpacesConfigurations
     *            set of input and output data spaces configurations defined for an application; may
     *            be <code>null</code> if no spaces are defined or {@link #isDataSpacesEnabled()}
     *            returns <code>false</code>.
     */
    public void setInputOutputSpacesConfigurations(
            Set<InputOutputSpaceConfiguration> inputOutputSpacesConfigurations);

    /**
     * @return URL of Data Spaces Naming Service; may be <code>null</code> (by default) if Naming
     *         Service should be created on deployer Node or {@link #isDataSpacesEnabled()} returns
     *         <code>false</code>.
     */
    public String getDataSpacesNamingServiceURL();

    /**
     * Sets URL of Data Spaces Naming Service.
     * 
     * @param dataSpacesNamingServiceURL
     *            URL of Data Spaces Naming Service; may be <code>null</code> if Naming Service
     *            should be created on deployer Node or {@link #isDataSpacesEnabled()} returns
     *            <code>false</code>.
     */
    public void setDataSpacesNamingServiceURL(String dataSpacesNamingServiceURL);
}
