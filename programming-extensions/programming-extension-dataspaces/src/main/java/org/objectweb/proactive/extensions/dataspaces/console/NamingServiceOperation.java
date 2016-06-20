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
package org.objectweb.proactive.extensions.dataspaces.console;

import java.net.URISyntaxException;

import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.dataspaces.core.InputOutputSpaceConfiguration;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;


public class NamingServiceOperation {

    /**
     * @param args
     */
    public static void main(String[] args) throws ProActiveException, URISyntaxException {
        if (args.length != 4) {
            leave();
            return;
        }

        final String url = args[0];
        final String appIdString = args[1];
        final String inputName = args[2];
        final String inputURL = args[3];

        try {
            final InputOutputSpaceConfiguration conf = InputOutputSpaceConfiguration
                    .createInputSpaceConfiguration(inputURL, null, null, inputName);
            final SpaceInstanceInfo spaceInstanceInfo = new SpaceInstanceInfo(appIdString, conf);
            NamingService stub = NamingService.createNamingServiceStub(url);

            try {
                stub.register(spaceInstanceInfo);
            } catch (WrongApplicationIdException e) {
                stub.registerApplication(appIdString, null);
                stub.register(spaceInstanceInfo);
            }
        } finally {
            PALifeCycle.exitSuccess();
        }
    }

    private static void leave(String message) {
        if (message != null)
            System.out.println("Error: " + message);
        leave();
    }

    private static void leave() {
        final String name = NamingServiceOperation.class.getName();

        System.out.println("Usage: java " + name +
            " <naming service URL> <application id> <input name> <input URL>");
        System.out.println("Registers input with specified name and URL.");
        System.out.println("\t--help\tprints this screen");
    }

}
