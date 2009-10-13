package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import java.io.Serializable;

import org.ow2.proactive.virtualizing.core.VirtualMachineManager;


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
    VirtualMachineManager getInstance();

}
