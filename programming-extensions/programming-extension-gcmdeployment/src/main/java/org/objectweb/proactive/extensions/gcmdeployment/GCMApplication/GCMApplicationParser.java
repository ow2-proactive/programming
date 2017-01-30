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
package org.objectweb.proactive.extensions.gcmdeployment.GCMApplication;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilder;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserConstants;
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
    public void setInputOutputSpacesConfigurations(Set<InputOutputSpaceConfiguration> inputOutputSpacesConfigurations);

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
