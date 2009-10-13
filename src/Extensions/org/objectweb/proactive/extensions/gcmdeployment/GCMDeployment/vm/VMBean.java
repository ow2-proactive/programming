package org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.vm;

import java.io.Serializable;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfoImpl;
import org.objectweb.proactive.extensions.gcmdeployment.core.TopologyImpl;


public class VMBean implements Serializable {
    String id;
    String name;
    String userName;
    String userPwd;
    TopologyImpl node;
    HostInfoImpl hostInfo;

    boolean clone;

    VMBean(String id, boolean c, String name, HostInfoImpl hostInfo) {
        this.id = id;
        this.clone = c;
        this.name = name;
        this.hostInfo = hostInfo;
    }

    VMBean(String id, boolean c, String name, String user, String pwd, HostInfoImpl hostInfo) {
        this(id, c, name, hostInfo);
        this.userName = user;
        this.userPwd = pwd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TopologyImpl getNode() {
        return node;
    }

    public void setNode(TopologyImpl node) {
        this.node = node;
        hostInfo.setTopologyId(this.node.getId());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isClone() {
        return this.clone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPwd() {
        return userPwd;
    }

    public void setUserPwd(String userPwd) {
        this.userPwd = userPwd;
    }

    public HostInfoImpl getHostInfo() {
        return hostInfo;
    }

    public void setHostInfo(HostInfoImpl hostInfo) {
        this.hostInfo = hostInfo;
    }
}
