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
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.GCMParserHelper;
import org.w3c.dom.Node;


public abstract class AbstractGroupSchedulerParser extends AbstractTupleParser {

    private static final String XPATH_SCRIPTPATH = "dep:scriptPath";

    @Override
    public AbstractGroup parseGroupNode(Node groupNode, XPath xpath) {
        AbstractGroup group = super.parseGroupNode(groupNode, xpath);

        String bookedNodesAccess = GCMParserHelper.getAttributeValue(groupNode, "bookedNodesAccess");
        if (bookedNodesAccess != null) {
            group.setBookedNodesAccess(bookedNodesAccess);
        }

        try {

            Node scriptPath = (Node) xpath.evaluate(XPATH_SCRIPTPATH, groupNode, XPathConstants.NODE);

            if (scriptPath != null) {
                group.setScriptPath(GCMParserHelper.parsePathElementNode(scriptPath));
            }
        } catch (XPathExpressionException e) {
            GCMDeploymentLoggers.GCMD_LOGGER.error(e.getMessage(), e);
        }

        return group;
    }

}
