/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.objectweb.proactive.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * TODO hostsTable should be HashTable<InetAddress, Hashtable<String, String>>
 * but the key "all" need to be handled separately since a null key is not allowed
 * inside a HashTable
 */

//the Unique instance of HostInfos
public class HostsInfos {
    private static HostsInfos hostsInfos = new HostsInfos();

    static Logger logger = ProActiveLogger.getLogger(Loggers.UTIL);

    private static Map<String, Map<String, String>> hostsTable;

    private HostsInfos() {
        ProActiveConfiguration.load();
        hostsTable = new Hashtable<String, Map<String, String>>();
        hostsTable.put("all", new Hashtable<String, String>());
        loadProperties();
    }

    public static Map<String, Map<String, String>> getHostsInfos() {
        return hostsTable;
    }

    public static String hostnameToIP(String hostname) {
        if ((hostname == null) || hostname.equals("all")) {
            return hostname;
        }

        try {
            return InetAddress.getByName(hostname).getHostAddress();
        } catch (UnknownHostException e) {
            return hostname;
        }
    }

    public static Map<String, String> getHostInfos(String _hostname) {
        String hostname = hostnameToIP(_hostname);
        return getHostInfos(hostname, true);
    }

    public static String getUserName(String _hostname) {
        String hostname = hostnameToIP(_hostname);
        Map<String, String> host_infos = getHostInfos(hostname, true);
        return (String) host_infos.get("username");
    }

    public static String getSecondaryName(String _hostname) {
        String hostname = hostnameToIP(_hostname);
        Map<String, String> host_infos = getHostInfos(hostname, true);
        String secondary = (String) host_infos.get("secondary");
        if (secondary != null) {
            return secondary;
        } else {
            return hostname;
        }
    }

    public static void setUserName(String _hostname, String username) {
        String hostname = hostnameToIP(_hostname);
        Map<String, String> host_infos = getHostInfos(hostname, false);
        if (host_infos.get("username") == null) {
            host_infos.put("username", username);
            //        	try {
            //        		System.out.println(hostname+" --> "+username+ " on host "+ProActiveInet.getInstance().getLocal().getHostName());
            //        	} catch (UnknownHostException e) {
            //        		// TODO Auto-generated catch block
            //        		e.printStackTrace();
            //        	}
        }
    }

    public static void setSecondaryName(String _hostname, String secondary_name) {
        String hostname = hostnameToIP(_hostname);
        Map<String, String> host_infos = getHostInfos(hostname, false);
        if (host_infos.get("secondary") == null) {
            host_infos.put("secondary", secondary_name);
        }
    }

    protected static Map<String, String> getHostInfos(String _hostname, boolean common) {
        String hostname = hostnameToIP(_hostname);
        Map<String, String> host_infos = findHostInfos(hostname);
        if (host_infos == null) {
            if (common) {
                return hostsTable.get("all");
            } else {
                host_infos = new Hashtable<String, String>();
                hostsTable.put(hostname, host_infos);
                return host_infos;
            }
        }
        return host_infos;
    }

    /**
     *
     */
    final static String REGEXP_KEYVAL = "(([0-9]{1,3}\\.){3}[0-9]{1,3}:([0-9]{1,3}\\.){3}[0-9]{1,3})";

    private void loadProperties() {
        String secondaryNames = CentralPAPropertyRepository.PA_NET_SECONDARYNAMES.getValue();
        if (secondaryNames != null) {
            if (secondaryNames.matches(REGEXP_KEYVAL + "(," + REGEXP_KEYVAL + ")?")) {
                for (String keyval : secondaryNames.split(",")) {
                    String[] tmp = keyval.split(":");
                    setSecondaryName(tmp[0], tmp[1]);
                }
            } else {
                logger.error("Invalid value for proactive.secondaryNames: " + secondaryNames);
            }
        }
    }

    private static Map<String, String> findHostInfos(String _hostname) {
        String hostname = hostnameToIP(_hostname);
        Map<String, String> host_infos = hostsTable.get(hostname);
        if (host_infos == null) {
            try {
                //we have to identify the mapping between host and IP
                //we try with the canonical hostname
                String host = InetAddress.getByName(hostname).getCanonicalHostName();
                if (!hostsTable.containsKey(host)) {
                    //we try with the IP
                    host = InetAddress.getByName(hostname).getHostAddress();
                }
                return hostsTable.get(host);
            } catch (UnknownHostException e) {
                //same as return null
                return host_infos;
            }
        }
        return host_infos;
    }

    public static void main(String[] args) {
        System.out.println(HostsInfos.getUserName("138.96.90.12"));
    }
}
