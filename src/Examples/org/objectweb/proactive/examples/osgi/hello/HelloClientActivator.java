/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
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
package org.objectweb.proactive.examples.osgi.hello;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


//@snippet-start HelloClientActivator_1
public class HelloClientActivator implements BundleActivator {

    public void start(BundleContext arg0) throws Exception {

        String filter = "(out=system)";
        ServiceReference[] sr = arg0.getServiceReferences(HelloService.class.getName(), filter);

        if (sr == null) {
            System.out.println("Couldn't start this bundle: The service '" + HelloService.class.getName() +
                "' [filter = " + filter + "]is not started");
            return;
        }

        HelloService h = (HelloService) arg0.getService(sr[0]);

        h.sayHello();
        h.saySomething("Hello ProActive Team!");

        filter = "(out=logger)";
        sr = arg0.getServiceReferences(HelloService.class.getName(), filter);

        if (sr == null) {
            System.out.println("Couldn't start this bundle: The service '" + HelloService.class.getName() +
                "' [filter = " + filter + "]is not started");
            return;
        }

        h = (HelloService) arg0.getService(sr[0]);

        h.sayHello();
        h.saySomething("Hello ProActive Team!");

    }

    public void stop(BundleContext arg0) throws Exception {
        // TODO Auto-generated method stub
    }

}
//@snippet-end HelloClientActivator_1