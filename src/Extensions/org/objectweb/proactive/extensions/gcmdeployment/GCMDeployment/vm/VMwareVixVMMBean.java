/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;
import org.ow2.proactive.virtualizing.vmwarevix.VMwareVMM;
import org.ow2.proactive.virtualizing.vmwarevix.VMwareVMM.Service;


/**
 * {@link VMMBean} implementation for VMwareVix library
 *
 */
public class VMwareVixVMMBean implements VMMBean {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
    private String uri, user, pwd;
    private int port = 904;
    private Service service = Service.vmwareDefault;

    public VMwareVixVMMBean(String uri, String user, String pwd, int port, Service service) {
        this.uri = uri;
        this.user = user;
        this.pwd = pwd;
        this.port = port;
        this.service = service;
    }

    /**
     * returns {@link VMwareVMM} instance
     * @throws VirtualServiceException
     */
    public VMwareVMM getInstance() throws VirtualServiceException {
        return new VMwareVMM(uri, user, pwd, port, service);
    }

}
