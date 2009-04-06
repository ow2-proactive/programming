/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.examples.webservices.helloWorld;

import org.objectweb.proactive.extensions.webservices.deployer.PADeployer;


/**
 * A simple example to expose an active object as a web service.
 *
 * @author The ProActive Team
 */
public class Undeploy {

    public static void main(String[] args) {
        String url = "";
        String serviceName = "";
        if (args.length == 1) {
            url = "http://localhost:8080/";
            serviceName = args[0];
        } else if (args.length == 2) {
            url = args[0];
            serviceName = args[1];
        } else {
            System.out.println("Wrong number of arguments:");
            System.out.println("Usage: java Undeploy [url] serviceName");
            return;
        }

        if (!url.startsWith("http://")) {
            url = "http://" + url;
        }

        PADeployer.unDeploy(url, serviceName);
    }
}
