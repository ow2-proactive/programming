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
package org.objectweb.proactive.core.ssh.proxycommand;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.util.Set;
import java.util.TreeSet;


/**
 * This class store the mapping subnetwork definition/gateway:port specified with the parameter
 * proactive.ssh.proxy.gateway.
 *
 * Only support IPv4 address
 */
public class SubnetChecker {
    // Where the network definition and the related gateway are stored
    private Set<IPMatcher> gatewaysSet;

    public SubnetChecker() {
        // TreeSet is use because no copy are wanted
        // And network definition are sorted by CIDR
        gatewaysSet = new TreeSet<IPMatcher>();
    }

    /**
     * Add an entry to the Set
     *
     * @param subnet
     *          The network definition as an IPv4 address, the character '/' and then the CIDR like
     *          xxx.xxx.xxx.xxx/yy
     * @param gateway
     *          The gateway to use to access the hosts of this network
     */
    public void setGateway(String subnet, String gateway) {
        if (logger.isDebugEnabled()) {
            logger.debug("Subnet Infos : " + gateway + " as " + "gateway" + " added for subnet " + subnet);
        }
        // separate network address and cidr
        String[] subnetDef = subnet.split("\\/");
        try {
            IPMatcher gateway_infos = new IPMatcher(subnetDef[0], Integer.parseInt(subnetDef[1]), gateway);
            gatewaysSet.add(gateway_infos);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Return the gateway where a proxy command can be used on to contact the host's IP address.
     *
     * @param ipAddress The host IP address.
     *
     * @return The gateway which is the relay to the host.
     * @throws Exception
     */
    public String getGateway(String ipAddress) {
        IPMatcher sd = checkIP(ipAddress);
        if (sd != null) {
            return sd.getGateway();
        }
        return null;
    }

    /**
     * Search in the set a network definition for this IPv4 address
     *
     * @param ip
     *          IPv4 address as a String
     */
    private IPMatcher checkIP(String ip) {
        for (IPMatcher matcher : gatewaysSet) {
            if (matcher.match(ip)) {
                return matcher;
            }
        }
        return null;
    }

    /**
     * This class store the gateway for a sub network
     * define by network/cidr : xxx.xxx.xxx.xxx/yy
     *
     * Only support IPv4 address
     */
    static private class IPMatcher implements Comparable<IPMatcher> {

        // Store for toString method
        final private int cidr;

        final private String network;

        final private int networkPortion;

        final private int mask;

        private String gateway = null;

        public IPMatcher(String network, int cidr, String gateway) {
            this.gateway = gateway;
            this.mask = computeMask(cidr);
            this.networkPortion = stringToInt(network) & this.mask;
            this.cidr = cidr;
            this.network = network;
        }

        public String getGateway() {
            return this.gateway;
        }

        private int computeMask(int mask) {
            int shift = Integer.SIZE - mask;
            return (~0 >> shift) << shift;
        }

        public boolean match(String ip) {
            return match(stringToInt(ip));
        }

        public boolean match(int ip) {
            return (ip & mask) == networkPortion;
        }

        private int stringToInt(String ip) throws IllegalArgumentException {
            String[] parts = ip.split("\\.");
            int tmp = 0;
            if (parts.length != 4) {
                throw new IllegalArgumentException("An IPv4 address must be like xxx.xxx.xxx.xxx : " + ip);
            }
            for (int i = 0; i < 4; i++) {
                int parsedInt = Integer.parseInt(parts[i]);
                if (parsedInt > 255 || parsedInt < 0) {
                    throw new IllegalArgumentException("An octet must be a number between 0 and 255.");
                }
                tmp |= parsedInt << ((3 - i) * 8);
            }
            return tmp;
        }

        /**
         * IPMatcher are ordered by CIDR
         *
         * The higher CIDR is the more specific so the prefered one
         *
         *  The higher the cidr is, the higher the mask will be
         */
        public int compareTo(IPMatcher other) {
            if (this.networkPortion == other.networkPortion)
                return other.cidr - this.cidr;
            // If network are different, then the order doesn't matter
            return this.network.compareTo(other.network);
        }

        @Override
        public String toString() {
            return this.network + "/" + this.cidr;
        }

    }

    /**
     * Return the rule(s) which correspond to <code> gateway </code>
     */
    public String getRule(String gateway) {
        StringBuilder sb = new StringBuilder("");
        for (IPMatcher ipm : gatewaysSet) {
            if (ipm.getGateway().equalsIgnoreCase(gateway)) {
                sb.append(ipm.toString());
                sb.append(";");
            }
        }
        return sb.toString();
    }
}
