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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionalTests.remoteobject.fifoproperty;

import static org.junit.Assert.assertTrue;

import org.objectweb.proactive.api.PARemoteObject;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.remoteobject.RemoteObject;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;

import functionalTests.FunctionalTest;


/**
 * a junit test that tries to check if the protocol keeps the fifo ordering  
 *
 */

public class FifoPropertyTest extends FunctionalTest {

    @org.junit.Test
    public void fifoPropertyTester() throws Exception {

        int range = 100000;

        // get an object
        FifoPropertyObject f = new FifoPropertyObject(range);
        ;

        // create a remote object exposer for this object
        RemoteObjectExposer<FifoPropertyObject> roe = PARemoteObject.newRemoteObject(
                FifoPropertyObject.class.getName(), f);

        // create the remote object 
        RemoteRemoteObject rro = roe.createRemoteObject("fifo-1", true);

        // get a protocol-specific reference onto the remote object
        RemoteObject<FifoPropertyObject> ro = roe
                .getRemoteObject(CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL.getValue());

        FifoPropertyObject f2 = ro.getObjectProxy();

        // call iteratively the same method
        // accumulating a value
        for (int i = 0; i < range; i++) {
            f2.add(i);
        }

        assertTrue(f2.check());
    }

}
