package org.objectweb.proactive.extra.gcmdeployment.core;

import java.rmi.dgc.VMID;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import static org.objectweb.proactive.extra.gcmdeployment.GCMDeploymentLoggers.GCMA_LOGGER;


public class DeploymentID {
    static final public String SEPARATOR = "%";
    private String deployerVMID;
    private List<String> depNodes;

    public DeploymentID() {
        this.deployerVMID = ProActiveRuntimeImpl.getProActiveRuntime()
                                                .getVMInformation().getVMID()
                                                .toString();
        this.depNodes = new ArrayList<String>();
    }

    public DeploymentID(VMID deployerVMID) {
        this.deployerVMID = deployerVMID.toString();
        this.depNodes = new ArrayList<String>();
    }

    public DeploymentID(String id) {
        depNodes = new ArrayList<String>();

        String[] fields = id.split(DeploymentID.SEPARATOR);
        if (fields.length < 2) {
            IllegalArgumentException e = new IllegalArgumentException(
                    "Failed to parse DeploymentID: " + id);
            GCMA_LOGGER.error(e);
            throw e;
        }

        deployerVMID = fields[0];
        for (int i = 1; i < fields.length; i++) {
            depNodes.add(fields[i]);
        }
    }

    public String getDeplpoyerVMID() {
        return deployerVMID;
    }

    public List<String> getDepNodes() {
        return depNodes;
    }

    public String getDepNode(int index) {
        return depNodes.get(index);
    }

    public void addDepNode(String newNode) {
        if (newNode.contains(SEPARATOR)) {
            IllegalArgumentException e = new IllegalArgumentException(SEPARATOR +
                    " is not allowed inside a DeploymentID node name: " +
                    newNode);
            GCMA_LOGGER.error(e);
            throw e;
        }
        depNodes.add(newNode);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(deployerVMID);
        for (String depNode : depNodes) {
            sb.append(SEPARATOR);
            sb.append(depNode);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) +
            ((depNodes == null) ? 0 : depNodes.hashCode());
        result = (prime * result) +
            ((deployerVMID == null) ? 0 : deployerVMID.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DeploymentID other = (DeploymentID) obj;

        if (deployerVMID == null) {
            if (other.deployerVMID != null) {
                return false;
            }
        } else if (!deployerVMID.equals(other.deployerVMID)) {
            return false;
        }

        if (depNodes == null) {
            if (other.depNodes != null) {
                return false;
            }
        } else if (!depNodes.equals(other.depNodes)) {
            return false;
        }

        return true;
    }
}
