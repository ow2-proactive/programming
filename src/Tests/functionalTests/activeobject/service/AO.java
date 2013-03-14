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
package functionalTests.activeobject.service;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.util.wrapper.IntWrapper;


public class AO implements RunActive {
    public AO() {
        // Empty
    }

    public IntWrapper foo() {
        return new IntWrapper(1);
    }

    public IntWrapper foo2() throws Exception {
        return new IntWrapper(2);
    }

    public IntWrapper foo3() {
        Body body = PAActiveObject.getBodyOnThis();
        AO aostub = (AO) PAActiveObject.getStubOnThis();
        aostub.foo();
        Service service = new Service(body);
        // this should trigger an IllegalArgumentException, as foo don't declare to throw a checked exception
        service.serveAllWithException("foo", new CustomException2());
        return new IntWrapper(2);
    }

    public void runActivity(Body body) {
        Service service = new Service(body);
        service.waitForRequest();
        service.serveAllWithException("foo", new CustomException());
        service.waitForRequest();
        service.serveAllWithException("foo2", new CustomException2());
        service.waitForRequest();
        service.serveAll();
    }

}
