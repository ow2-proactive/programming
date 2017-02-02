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
package functionalTests.multiprotocol;

import java.lang.reflect.Field;
import java.net.URISyntaxException;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingService;
import org.objectweb.proactive.extensions.dataspaces.core.naming.NamingServiceDeployer;


/**
 * AONamingServiceSwitch
 *
 * @author The ProActive Team
 */
public class AONamingServiceSwitch {

    NamingServiceDeployer namingServiceDeployer;

    NamingService namingService;

    public AONamingServiceSwitch() {

    }

    /**
     * Creates a namign service
     * @return
     * @throws org.objectweb.proactive.core.ProActiveException
     * @throws java.net.URISyntaxException
     */
    public NamingService createNamingService() throws ProActiveException, URISyntaxException {
        if (namingServiceDeployer == null) {
            namingServiceDeployer = new NamingServiceDeployer(true);
        }
        namingService = NamingService.createNamingServiceStub(namingServiceDeployer.getNamingServiceURLs());
        return namingService;
    }

    /**
     * Disable protocol in naming service
     * @param protocol
     * @return
     * @throws Exception
     */
    public boolean disableProtocol(String protocol) throws Exception {

        Field roeField = NamingServiceDeployer.class.getDeclaredField("roe");

        roeField.setAccessible(true);

        RemoteObjectExposer roe = (RemoteObjectExposer) roeField.get(namingServiceDeployer);

        System.out.println("trying to disable " + protocol);

        try {
            PARemoteObject.disableProtocol(roe, protocol);
        } catch (Exception e) {

        }

        return true;
    }
}
