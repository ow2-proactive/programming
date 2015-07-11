/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.util;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/** Provide the local InetAddress to be used by ProActive
 *  
 * Expected behavior of this class is described in the ProActive manual, chapter "ProActive basic configuration"  
 */
public class ProActiveInet {
    static private Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION_NETWORK);

    final static private ProActiveInet instance = new ProActiveInet();

    final private InetAddress electedAddress;

    static public ProActiveInet getInstance() {
        return instance;
    }

    private ProActiveInet() {
        electedAddress = electAnAddress();

        if (electedAddress == null) {
            logger.error("No local Inet Address found. Exiting");
        }
    }

    /**
     * Returns the inet address used by the local ProActive Runtime
     * 
     * @return
     */
    public InetAddress getInetAddress() {
        if (electedAddress == null) {
            throw new IllegalStateException("No suitable IP address found");
        }

        return electedAddress;
    }

    public List<String> listAllInetAddress() {
        LinkedList<String> ret = new LinkedList<String>();

        Enumeration<NetworkInterface> nis;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();

                StringBuilder sb = new StringBuilder();
                sb.append(ni.getName());
                sb.append("\t");
                sb.append("MAC n/a");
                sb.append("\t");

                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    InetAddress ia = ias.nextElement();

                    sb.append(ia.getHostAddress());
                    sb.append(" ");
                }
                ret.add(sb.toString());
            }
        } catch (SocketException e) {
            logger.info("Failed to find a suitable InetAddress", e);
        }

        return ret;
    }

    /**
     * Returns the host name of the local ProActive inet address
     * 
     * If {@link CentralPAPropertyRepository.PA_USE_IP_ADDRESS} is set then the IP address is
     * returned instead of an FQDN
     * 
     * @return
     * @see PAProperties
     */
    public String getHostname() {
        return URIBuilder.getHostNameorIP(getInetAddress());
    }

    private InetAddress electAnAddress() {
        InetAddress ia = null;

        if (defaultBehavior()) {
            logger.debug("Using default algorithm to elected an IP address");
            ia = getDefaultInterface();
        } else {
            // Follow the user defined rules
            if (CentralPAPropertyRepository.PA_HOSTNAME.isSet()) {
                logger.debug(CentralPAPropertyRepository.PA_HOSTNAME.getName() +
                    " defined. Using getByName() to elected an IP address");
                // return the result of getByName
                try {
                    ia = InetAddress.getByName(CentralPAPropertyRepository.PA_HOSTNAME.getValue());
                } catch (UnknownHostException e) {
                    logger.info(CentralPAPropertyRepository.PA_HOSTNAME.getName() +
                        " is set, but no IP address is bound to this hostname");
                }
            } else {
                logger.debug("At least one proactive.net.* property defined. Using the matching algorithm to elect an IP address");
                // Use the filter algorithm
                if (ia == null) {
                    List<InetAddress> l;
                    l = getAllInetAddresses();
                    l = filterByNetmask(l);
                    l = filterLoopback(l);
                    l = filterPrivate(l);

                    for (InetAddress addr : l) {

                        if (CentralPAPropertyRepository.PA_NET_DISABLE_IPv6.isTrue() &&
                            addr instanceof Inet6Address) {
                            continue;
                        }

                        ia = addr;
                        break;
                    }
                }
            }

        }

        return ia;
    }

    /**
     * Elects the default {@link InetAddress}.
     * 
     * A first match algorithm is used:
     * <ol>
     * <li>Public IP address</li>
     * <li>Private IP address</li>
     * <li>loopback address</li>
     * </ol>
     * 
     * IPv6 address can enabled/disabled with the
     * {@link PAProperties#PA_NET_DISABLE_IPv6} property.
     */
    private InetAddress getDefaultInterface() {
        List<InetAddress> ias = getAllInetAddresses();

        // Search for a public IPv4 address
        for (InetAddress ia : ias) {
            if (ia.isLoopbackAddress())
                continue;
            if (ia.isSiteLocalAddress())
                continue;
            if (CentralPAPropertyRepository.PA_NET_DISABLE_IPv6.isTrue() && ia instanceof Inet6Address)
                continue;

            return ia;
        }

        // Search for a private IPv4 address
        for (InetAddress ia : ias) {
            if (ia.isLoopbackAddress())
                continue;
            if (CentralPAPropertyRepository.PA_NET_DISABLE_IPv6.isTrue() && ia instanceof Inet6Address)
                continue;

            return ia;
        }

        // Search for a loopback address
        for (InetAddress ia : ias) {
            if (CentralPAPropertyRepository.PA_NET_DISABLE_IPv6.isTrue() && ia instanceof Inet6Address)
                continue;

            return ia;
        }

        return null;
    }

    /**
     * Returns true if a property that can affect interface binding is set.
     * False otherwise
     */
    private boolean defaultBehavior() {
        if (CentralPAPropertyRepository.PA_HOSTNAME.isSet())
            return false;

        if (CentralPAPropertyRepository.PA_NET_NOLOOPBACK.isSet())
            return false;

        if (CentralPAPropertyRepository.PA_NET_NOPRIVATE.isSet())
            return false;

        if (CentralPAPropertyRepository.PA_NET_NETMASK.isSet())
            return false;

        if (CentralPAPropertyRepository.PA_NET_INTERFACE.isSet())
            return false;

        return true;
    }

    private List<InetAddress> getAllInetAddresses() {
        LinkedList<InetAddress> ret = new LinkedList<InetAddress>();

        String intf = CentralPAPropertyRepository.PA_NET_INTERFACE.getValue();

        Enumeration<NetworkInterface> nis;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();

                if (intf != null && !ni.getName().equals(intf)) {
                    // Skip this interface
                    continue;
                }

                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    InetAddress ia = ias.nextElement();

                    ret.add(ia);
                }
            }
        } catch (SocketException e) {
            logger.info("Failed to find a suitable InetAddress", e);
        }

        return ret;
    }

    private List<InetAddress> filterPrivate(List<InetAddress> l) {
        if (!CentralPAPropertyRepository.PA_NET_NOPRIVATE.isSet() ||
            !CentralPAPropertyRepository.PA_NET_NOPRIVATE.isTrue()) {
            // All InetAddress match
            return new LinkedList<InetAddress>(l);
        }

        List<InetAddress> ret = new LinkedList<InetAddress>();
        for (InetAddress ia : l) {
            if (!ia.isSiteLocalAddress()) {
                ret.add(ia);
            } else {
                logger.debug("Discarded " + ia + " because of the no private criteria");
            }
        }

        return ret;
    }

    private List<InetAddress> filterLoopback(List<InetAddress> l) {
        if (!CentralPAPropertyRepository.PA_NET_NOLOOPBACK.isSet() ||
            !CentralPAPropertyRepository.PA_NET_NOLOOPBACK.isTrue()) {
            // All InetAddress match
            return new LinkedList<InetAddress>(l);
        }

        List<InetAddress> ret = new LinkedList<InetAddress>();
        for (InetAddress ia : l) {
            if (!ia.isLoopbackAddress()) {
                ret.add(ia);
            } else {
                logger.debug("Discarded " + ia + " because of the no loopback criteria");
            }
        }

        return ret;
    }

    private List<InetAddress> filterByNetmask(List<InetAddress> l) {
        if (!CentralPAPropertyRepository.PA_NET_NETMASK.isSet()) {
            // All InetAddress match
            return new LinkedList<InetAddress>(l);
        }

        IPMatcher matcher;
        try {
            matcher = new IPMatcher(CentralPAPropertyRepository.PA_NET_NETMASK.getValue());
        } catch (Throwable e) {
            logger.fatal("Invalid format for property " +
                CentralPAPropertyRepository.PA_NET_NETMASK.getName() + ". Must be xxx.xxx.xxx.xxx/xx");
            return new LinkedList<InetAddress>();
        }

        List<InetAddress> ret = new LinkedList<InetAddress>();
        for (InetAddress ia : l) {
            if (!(ia instanceof Inet6Address)) {
                if (matcher.match(ia)) {
                    ret.add(ia);
                } else {
                    logger.debug("Discarded " + ia +
                        " because of netmask criteria is not compatible with IPv6");
                }
            } else {
                logger.debug("Discarded " + ia + " because of the  netmask criteria");
            }
        }
        return ret;
    }

    static private class IPMatcher {

        final private int networkPortion;
        final private int mask;

        public IPMatcher(String str) throws Throwable {
            String[] array = str.split("/");
            if (array.length != 2)
                throw new IllegalArgumentException("Invalid string, xxx.xxx.xxx.xxx/xx expected");

            int ip = stringToInt(array[0]);
            int significantBits = Integer.parseInt(array[1]);

            this.mask = computeMask(ip, significantBits);
            this.networkPortion = ip & this.mask;

        }

        private int computeMask(int ip, int mask) {
            int shift = Integer.SIZE - mask;
            return (~0 >> shift) << shift;
        }

        public boolean match(String ip) throws Exception {
            return match(stringToInt(ip));
        }

        public boolean match(InetAddress ia) {
            try {
                return match(stringToInt(ia.getHostAddress()));
            } catch (Exception e) {
                ProActiveLogger.logImpossibleException(logger, e);
                return false;
            }
        }

        public boolean match(int ip) throws Exception {
            return (ip & mask) == networkPortion;
        }

        private static String getBits(int value) {
            int displayMask = 1 << 31;
            StringBuilder buf = new StringBuilder(35);
            for (int c = 1; c <= 32; c++) {
                buf.append((value & displayMask) == 0 ? '0' : '1');
                value <<= 1;

                if (c % 8 == 0)
                    buf.append(' ');
            }

            return buf.toString();
        }

        private int stringToInt(String ip) throws Exception {
            String[] parts = ip.split("\\.");
            int tmp = 0;
            for (int i = 0; i < parts.length; i++) {
                int parsedInt = Integer.parseInt(parts[i]);
                if (parsedInt > 255 || parsedInt < 0) {
                    throw new Exception("An octet must be a number between 0 and 255.");
                }
                tmp |= parsedInt << ((3 - i) * 8);
            }

            return tmp;
        }
    }
}
