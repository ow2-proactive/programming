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
package org.objectweb.proactive.examples.osgi.hello;

import java.util.Properties;

import org.objectweb.proactive.extensions.osgi.ProActiveService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


//@snippet-start HelloActivator_1
//@snippet-start HelloActivator_2
public class HelloActivator implements BundleActivator {
    //@snippet-break HelloActivator_1

    private BundleContext context;
    private ProActiveService proActiveService;

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        this.context = context;

        /* gets the ProActive Service */
        ServiceReference sr = this.context.getServiceReference(ProActiveService.class.getName());

        if (sr == null) {
            System.out.println("Couldn't start this bundle: The service '" +
                ProActiveService.class.getName() + "' is not started");
            return;
        }

        this.proActiveService = (ProActiveService) this.context.getService(sr);
        HelloService h = (HelloService) this.proActiveService.newActive(HelloSystemOut.class.getName(),
                new Object[] {});

        /* Register the service */
        Properties props = new Properties();
        props.put("name", "HelloSystemOut");
        props.put("out", "system");

        this.context.registerService(HelloService.class.getName(), h, props);

        h = (HelloService) this.proActiveService.newActive(HelloLogInfo.class.getName(), new Object[] {});

        /* Register the service */
        props = new Properties();
        props.put("name", "HelloLogInfo");
        props.put("out", "logger");

        this.context.registerService(HelloService.class.getName(), h, props);
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    }

    //@snippet-resume HelloActivator_1
}
//@snippet-end HelloActivator_1
//@snippet-end HelloActivator_2