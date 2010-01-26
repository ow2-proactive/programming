/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
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
package org.objectweb.proactive.extra.closedworldlauncher;

import java.util.ArrayList;
import java.util.StringTokenizer;


public class WorldInfo {
    protected static final String TOTAL_HOSTS_NUMBER_PROPERTY = "total_hosts";
    protected static final String HOST_NUMBER_PROPERTY = "host_number";
    protected static final String HOST_LIST_PROPERTY = "host_names";
    protected int totalHostsNumber;
    protected int currentHostNumber;
    protected String[] hostListAsArray;
    protected String hostList;

    public WorldInfo() {
    }

    public void init() {
        String tmp = null;
        tmp = System.getProperty(TOTAL_HOSTS_NUMBER_PROPERTY);
        totalHostsNumber = (tmp == null) ? 0 : Integer.parseInt(tmp);
        tmp = System.getProperty(HOST_NUMBER_PROPERTY);
        currentHostNumber = (tmp == null) ? 0 : Integer.parseInt(tmp);
        tmp = System.getProperty(HOST_LIST_PROPERTY);
        hostList = (tmp == null) ? new String() : tmp;
        hostListAsArray = (tmp == null) ? new String[] { "" } : this.stringToArray(tmp);
    }

    public int getTotalHostNumber() {
        return this.totalHostsNumber;
    }

    public int getCurrenHostNumber() {
        return this.currentHostNumber;
    }

    public String getHostList() {
        return this.hostList;
    }

    public String[] getHostListAsArray() {
        return this.hostListAsArray;
    }

    /**
     * Divide a string into elements separated by a blank space
     */
    protected String[] stringToArray(String string) {
        ArrayList<String> tmp = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(string);

        while (st.hasMoreTokens()) {
            tmp.add(st.nextToken());
        }

        return tmp.toArray(new String[] { "" });
    }

    @Override
    public String toString() {
        StringBuffer tmp = new StringBuffer();
        tmp.append(TOTAL_HOSTS_NUMBER_PROPERTY).append(" = ").append(totalHostsNumber).append("\n");
        tmp.append(HOST_NUMBER_PROPERTY).append(" = ").append(currentHostNumber).append("\n");
        tmp.append(HOST_LIST_PROPERTY).append(" = ");

        for (int i = 0; i < hostListAsArray.length; i++) {
            tmp.append(hostListAsArray[i]).append(" ");
        }

        return tmp.toString();
    }

    public static void main(String[] args) {
        WorldInfo info = new WorldInfo();
        info.init();
        System.out.println(info);
    }
}
