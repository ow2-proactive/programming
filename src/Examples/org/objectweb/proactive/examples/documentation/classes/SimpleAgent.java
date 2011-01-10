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
//@snippet-start class_SimpleAgent
package org.objectweb.proactive.examples.documentation.classes;

import java.io.Serializable;
import java.net.InetAddress;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAMobileAgent;


/**
 * @author The ProActive Team
 *
 */
public class SimpleAgent implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 500L;

    /**
     * Empty, no-arg constructor required by ProActive
     */
    public SimpleAgent() {
    }

    //@snippet-start method_moveTo
    /**
     * Migrates the active object
     * 
     * @param url - URL of the node where you want to 
     *              migrate your active object to
     */
    public void moveTo(String url) {
        try {
            PAMobileAgent.migrateTo(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //@snippet-end method_moveTo

    /**
     * Retrieves the name of the host where the active
     * object is.
     * 
     * @return The node url and the hostname where the active object is
     */
    public String whereAreYou() {
        try {
            return PAActiveObject.getActiveObjectNodeUrl(PAActiveObject.getStubOnThis()) + " on host " +
                InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "Localhost lookup failed";
        }
    }
}
//@snippet-end class_SimpleAgent