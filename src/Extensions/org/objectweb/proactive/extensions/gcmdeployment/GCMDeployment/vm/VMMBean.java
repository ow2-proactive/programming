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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import java.io.Serializable;

import org.ow2.proactive.virtualizing.core.VirtualMachineManager;
import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;


/**
 * Interface used for VirtualMachineManager instantiation.
 * We need this VMMBean interface to dynamically instantiate VMM within
 * {@link GCMVirtualMachineManager}.
 */
public interface VMMBean extends Serializable {
    /**
     * Used to get the instance of the manager in charge of the underlying infrastructure.
     * @return the suitable {@link VirtualMachineManager} or null if a problem occured.
     */
    VirtualMachineManager getInstance() throws VirtualServiceException;

}
