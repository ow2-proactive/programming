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
package org.objectweb.proactive.examples.webservices.helloWorld;

import java.net.MalformedURLException;
import java.net.URL;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.webservices.AbstractWebServicesFactory;
import org.objectweb.proactive.extensions.webservices.WebServices;
import org.objectweb.proactive.extensions.webservices.WebServicesFactory;


/**
 * A simple example to unexpose a web service of an active object.
 *
 * @author The ProActive Team
 */
public class Undeploy {

    public static void main(String[] args) {
        String url = "";
        String wsFrameWork = "";
        String serviceName = "";
        if (args.length == 1) {
            url = "http://localhost:8080/";
            wsFrameWork = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
            serviceName = args[0];
        } else if (args.length == 2) {
            try {
                new URL(args[0]);
                url = args[0];
                wsFrameWork = CentralPAPropertyRepository.PA_WEBSERVICES_FRAMEWORK.getValue();
                serviceName = args[1];
            } catch (MalformedURLException me) {
                // Given argument is not the URL to use to expose the web service, it should be the web service framework to use
                url = "http://localhost:8080/";
                wsFrameWork = args[0];
                serviceName = args[1];
            }
        } else if (args.length == 3) {
            url = args[0];
            wsFrameWork = args[1];
            serviceName = args[2];
        } else {
            System.out.println("Wrong number of arguments:");
            System.out.println("Usage: java Undeploy [url] [wsFrameWork] serviceName");
            System.out.println("with wsFrameWork should be 'cxf'");
            return;
        }

        try {
            WebServicesFactory wsf = AbstractWebServicesFactory.getWebServicesFactory(wsFrameWork);
            WebServices ws = wsf.getWebServices(url);
            ws.unExposeAsWebService(serviceName);
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
    }
}
