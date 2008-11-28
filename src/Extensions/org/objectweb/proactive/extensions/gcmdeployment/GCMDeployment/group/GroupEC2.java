package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.group;

import java.util.List;

import org.objectweb.proactive.extensions.gcmdeployment.PathElement;

public class GroupEC2 extends AbstractGroup {

    private PathElement privateKey;
    private PathElement certification;

    @Override
    public List<String> internalBuildCommands() {
        // ec2-run-instances ?
        return null;
    }

    public void setPrivateKey(PathElement privateKey) {
        this.privateKey = privateKey;
    }

    public void setCertification(PathElement certification) {
        this.certification = certification;
    }

}
