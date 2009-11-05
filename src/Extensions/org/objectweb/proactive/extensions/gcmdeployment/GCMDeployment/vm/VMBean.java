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

import java.io.Serializable;

import org.objectweb.proactive.extensions.gcmdeployment.GCMDeployment.hostinfo.HostInfoImpl;
import org.objectweb.proactive.extensions.gcmdeployment.core.TopologyImpl;


public class VMBean implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 42L;
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
