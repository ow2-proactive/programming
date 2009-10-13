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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;

import org.objectweb.proactive.core.runtime.ProActiveRuntime;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.core.util.OperatingSystem;
import org.objectweb.proactive.core.util.ProActiveCounter;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeploymentLoggers;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.GCMApplicationInternal;
import org.objectweb.proactive.extensions.gcmdeployment.GCMApplication.commandbuilder.CommandBuilderProActive;
import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfoImpl;
import org.objectweb.proactive.extensions.gcmdeployment.core.TopologyImpl;
import org.ow2.proactive.virtualizing.core.VirtualMachine;
import org.ow2.proactive.virtualizing.core.VirtualMachine2;
import org.ow2.proactive.virtualizing.core.VirtualMachineManager;
import org.ow2.proactive.virtualizing.core.error.VirtualServiceException;


/**
 * This class can be seen as a JavaBean for the first part of the GCMD parsing.
 * It just handles "set" operations during that step to keep being
 * "Serializable". It's only during the "start" method call that
 * everything (VMM & VM) will be initialized.
 *
 */
public class GCMVirtualMachineManager implements Serializable {

    /** user's info & id (infrastructure & resources identification) */
    private String pwd, user, id;
    /** to keep all hypervisor's uri */
    private ArrayList<String> uris = new ArrayList<String>();
    /** and every virtual machines */
    private ArrayList<VMBean> vms = new ArrayList<VMBean>();
    /** To know which vm we have to power off at the end of the application */
    private ArrayList<VirtualMachine> toPowerOff = new ArrayList<VirtualMachine>();
    /** To know which clone we have to destroy at the end of the application */
    private ArrayList<VirtualMachine2> toDestroy = new ArrayList<VirtualMachine2>();
    /** To be able to access VMM & VM after serialization */
    private ArrayList<VirtualMachineManagerHolder> virtualMachineManagers = new ArrayList<VirtualMachineManagerHolder>();
    /** To easily get a ref to topologyId */
    protected HashMap<VirtualMachine, TopologyImpl> virtualMachineToTopologyImplMapper = new HashMap<VirtualMachine, TopologyImpl>();
    /** To set the bootstrap url within the vm environment */
    private final String bootstrapUrlKey = "bootstrapURL";

    /**
     * To add a new Virtual Machine Manager, or, more precisely, the mean to
     * dynamically build it.
     * @param klass Virtual Machine Manager's class
     * @param cstArgs The VMM constructor's arguments type
     * @param params The arguments associated to cstArgs
     */
    public void addVirtualMachineManager(VMMBean vmmbean) {
        virtualMachineManagers.add(new VirtualMachineManagerHolder(vmmbean));
    }

    /**
     * Builds a new {@link VMBean} and adds it to the known list of {@link VMBean}.
     * the key parameter represents the virtual machine's name within your virtualization software
     * and the count parameter, the number of times you want to boot different virtual machines.
     * If count == 1, {@link #getId()#equals(#getName())} returns true, otherwise, a new "ProActive Unique name"
     * is given to the VMBean, and thus, the future cloned virtual machine.
     * @param key the name associated to your virtual machine in your virtual environment.
     * (The name you gave it from the GUI management tool)
     * @param count the number of time you want to boot the given virtual machine. Be sure that
     * the virtualization layer handles clones...
     */
    public void addVMBean(String key, int count) {
        addVMBean(key, count, null);
    }

    /**
     * see {@link #addVMBean(String, int, String)}
     * @param key
     * @param count
     * @param osType the operating system of your virtual machine (unix || windows).
     */
    public void addVMBean(String key, int count, String osType) {
        Random rand = new Random(System.currentTimeMillis());
        HostInfoImpl hostInfo = new HostInfoImpl();
        if (osType != null) {
            hostInfo.setOs(osType.equals(OperatingSystem.unix.name()) ? OperatingSystem.unix
                    : OperatingSystem.windows);
        }
        if (count == 1) {
            vms.add(new VMBean(key, false, key, hostInfo));
        } else {
            for (int i = 0; i < count; i++) {
                vms.add(new VMBean(key, true, key + "_PAClone" + (i + 1) + "_" + rand.nextInt(), hostInfo));
            }
        }
    }

    public void start(CommandBuilderProActive comm, GCMApplicationInternal gcma) {
        GCMDeploymentLoggers.GCMD_LOGGER.debug("in " + this.getClass() + ".start()");
        //contact hypervisors & setup environment
        for (VirtualMachineManagerHolder vmmh : virtualMachineManagers) {
            for (VMBean vm : vms) {
                VirtualMachine temp = null;
                try {
                    temp = vmmh.getVirtualMachine(vm.getId());
                } catch (Throwable e) {
                    GCMDeploymentLoggers.GCMD_LOGGER.error("Unable to get " + vm.getId(), e);
                    continue;
                }
                if (temp instanceof VirtualMachine2) {
                    if (vm.isClone()) {
                        try {
                            VirtualMachine2 clone = ((VirtualMachine2) temp).clone(vm.getName());
                            toDestroy.add(clone);
                            temp = clone;
                        } catch (Throwable e) {
                            GCMDeploymentLoggers.GCMD_LOGGER.error("Unable to clone " + vm.getId(), e);
                            continue;
                        }
                    }
                } else {
                    if (vm.isClone()) {
                        GCMDeploymentLoggers.GCMD_LOGGER
                                .warn("Clone feature not supported for the provided virtual machine: " +
                                    vm.getId() +
                                    " whereas required by user. Aborting this template clone startup.");
                        continue;
                    }
                }
                try {
                    startVM(temp, gcma, comm, vm.getHostInfo());
                    toPowerOff.add(temp);
                } catch (Throwable e) {
                    GCMDeploymentLoggers.GCMD_LOGGER.error("Unable to start " + vm.getName() +
                        " from VMware hypervisor.", e);
                    if (temp instanceof VirtualMachine2) {
                        if (vm.isClone()) {
                            VirtualMachine2 temp2 = (VirtualMachine2) temp;
                            try {
                                temp2.destroy();
                            } catch (Throwable t) {
                                GCMDeploymentLoggers.GCMD_LOGGER.error(
                                        "A sever exception occured while destroying unused " + vm.getName() +
                                            " virtual machine." + System.getProperty("line.separator") +
                                            ". You have to do it by yourself, be careful.", e);
                                continue;
                            }
                        }
                    }
                }
            }
        }
    }

    public void stop() {
        for (VirtualMachine vm : toPowerOff) {
            try {
                vm.powerOff();
            } catch (Throwable e) {
                try {
                    GCMDeploymentLoggers.GCMD_LOGGER.warn("Cannot power off virtual machine " + vm.getName() +
                        "." + " Please, do it manually.");
                } catch (VirtualServiceException e1) {
                    GCMDeploymentLoggers.GCMD_LOGGER
                            .warn("An error occured while cleaning virtualized environment. "
                                + "Please do it manually");
                }
            }
        }
        for (VirtualMachine2 vm : toDestroy) {
            try {
                vm.destroy();
            } catch (Throwable e) {
                try {
                    GCMDeploymentLoggers.GCMD_LOGGER.warn("Cannot destroy virtual machine " + vm.getName() +
                        "." + " Please, do it manually.");
                } catch (VirtualServiceException e1) {
                    GCMDeploymentLoggers.GCMD_LOGGER
                            .warn("An error occured while cleaning virtualized environment. "
                                + "Please do it manually");
                }
            }
        }
    }

    /**
     * This method is in charge of booting the virtual machine and registering a web page
     * ( see {@link BootstrapServlet} ) to allow remote PART to bootstrap.
     * @param vm the virtual machine to boot
     * @param gcma the associated GCMA object
     * @param comm the CommandBuilderProActive used to build the command of the remote PART
     * @param hostInfo the hostinfo used to build the command
     * @throws VirtualServiceException if a problem occurs
     */
    private void startVM(VirtualMachine vm, GCMApplicationInternal gcma, CommandBuilderProActive comm,
            HostInfoImpl hostInfo) throws VirtualServiceException {
        GCMDeploymentLoggers.GCMD_LOGGER.debug("in " + this.getClass() + ".startVM");
        //register appli within boostrapServlet singleton
        if (vm instanceof VirtualMachine2) {
            VirtualMachine2 vm2 = (VirtualMachine2) vm;
            BootstrapServlet bootstrapServlet = BootstrapServlet.get();
            String deploymentIdKey = new Long(gcma.getDeploymentId()).toString();
            long key = ProActiveCounter.getUniqID();
            String vmKey = deploymentIdKey + ":" + key;
            HashMap<String, String> values = new HashMap<String, String>();
            values.put(BootstrapServlet.PA_RT_COMMAND, comm.buildCommand(hostInfo, gcma));
            String bootstrapAddress = bootstrapServlet.registerAppli(vmKey, values);
            GCMDeploymentLoggers.GCMD_LOGGER.debug("Starting VM " + vm.getName());
            try {
                vm2.pushData(bootstrapUrlKey, bootstrapAddress);
            } catch (VirtualServiceException e) {
                GCMDeploymentLoggers.GCMD_LOGGER.error(
                        "Cannot pass the bootstrap URL to the virtual machine.", e);
            }
        } else {
            GCMDeploymentLoggers.GCMD_LOGGER.warn("Starting " + vm.getName() +
                " but won't be able to bootstrap ProActive " +
                "as it doesn't implements VirtualMachine2 interface.");
        }
        vm.powerOn();
    }

    /*------------------
     * Getters & Setters
     -------------------*/
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<VMBean> getVms() {
        return vms;
    }

    public void setVms(ArrayList<VMBean> vms) {
        this.vms = vms;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public ArrayList<String> getUris() {
        return uris;
    }

    public void setUris(ArrayList<String> uris) {
        this.uris = uris;
    }

    public void addHypervisorURI(String uri) {
        this.uris.add(uri);
    }

    /*---------------------
     * Member class
     ---------------------*/

    class VirtualMachineManagerHolder implements Serializable {
        private final VMMBean vmmbean;
        private transient VirtualMachineManager vmm;
        private transient Hashtable<String, VirtualMachine> vms;

        private VirtualMachineManagerHolder(VMMBean vmmbean) {
            this.vmmbean = vmmbean;
        }

        private VirtualMachine getVirtualMachine(String name) throws VirtualServiceException {
            if (vmm == null) {
                vmm = vmmbean.getInstance();
                if (vmm == null) {
                    throw new VirtualServiceException("Cannot instantiate VirtualMachineManager");
                }
            }
            if (vms == null) {
                vms = new Hashtable<String, VirtualMachine>();
            }
            if (vms.contains(name)) {
                return vms.get(name);
            } else {
                VirtualMachine res = vmm.getNewVM(name);
                vms.put(name, res);
                return res;
            }
        }
    }
}
