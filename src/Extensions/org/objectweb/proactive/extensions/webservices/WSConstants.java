/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * If needed, contact us to obtain a release under GPL version 2 of
 * the License.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.webservices;

import java.util.Vector;


/**
 * Utility constants for deploying active objects and components as Web Services
 *
 * @author The ProActive Team
 */
public class WSConstants {

    public static final String SERVICES_PATH = "proactive/services/";
    public static final String SERVLET_PATH = "/services/*";

    public static final Vector<String> disallowedMethods = new Vector<String>();

    static {
        // Object methods
        disallowedMethods.addElement("equals");
        disallowedMethods.addElement("toString");
        disallowedMethods.addElement("runActivity");
        disallowedMethods.addElement("initActivity");
        disallowedMethods.addElement("endActivity");
        disallowedMethods.addElement("setProxy");
        disallowedMethods.addElement("getProxy");
        disallowedMethods.addElement("wait");
        disallowedMethods.addElement("notify");
        disallowedMethods.addElement("notifyAll");
        disallowedMethods.addElement("getClass");
        disallowedMethods.addElement("hashCode");

        // Component methods
        disallowedMethods.addElement("setFcItfName");
        disallowedMethods.addElement("isFcInternalItf");
        disallowedMethods.addElement("setFcOwner");
        disallowedMethods.addElement("setFcItfOwner");
        disallowedMethods.addElement("setSenderItfID");
        disallowedMethods.addElement("getSenderItfID");
        disallowedMethods.addElement("setFcIsInternal");
        disallowedMethods.addElement("getFcItfName");
        disallowedMethods.addElement("getFcItfType");
        disallowedMethods.addElement("getFcItfOwner");
        disallowedMethods.addElement("isFcInternalItf");
        disallowedMethods.addElement("setFcItfImpl");
        disallowedMethods.addElement("getFcItfImpl");
        disallowedMethods.addElement("setFcType");
        disallowedMethods.addElement("getFcItfImpl");
        disallowedMethods.addElement("setFcItfImpl");
    }
}
