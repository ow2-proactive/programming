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

import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Provide the local InetAddress to be used by ProActive
 * <p/>
 * Expected behavior of this class is described in the ProActive manual, chapter "ProActive basic configuration"
 */
public class ProActiveInet {
    static private Logger logger = ProActiveLogger.getLogger(Loggers.CONFIGURATION_NETWORK);

    static private ProActiveInet instance = null;

    final private InetAddress electedAddress;

    static public ProActiveInet getInstance() {
        if (instance == null) {
            instance = new ProActiveInet();
        }
        return instance;
    }

    private ProActiveInet() {
        electedAddress = electAnAddress();

        if (electedAddress == null) {
            logger.error("No suitable IP address found.");
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

    public static List<String> listAllInetAddress() {
        LinkedList<String> ret = new LinkedList<String>();

        Enumeration<NetworkInterface> nis;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();

                StringBuilder sb = new StringBuilder();
                sb.append(ni.getName());
                sb.append("\t");
                sb.append("MAC: ");
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    StringBuilder sbmac = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sbmac.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    sb.append(sbmac);
                } else {
                    sb.append("N/A");
                }

                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    sb.append("\t");
                    InetAddress ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        sb.append("IPV6: ");
                    } else {
                        sb.append("IP: ");
                    }
                    sb.append(ia.getHostAddress());
                }

                ret.add(sb.toString());

            }
        } catch (SocketException e) {
            logger.info("Error occurred when listing all InetAddresses", e);
        }

        return ret;
    }

    /**
     * Returns the host name of the local ProActive inet address
     * <p/>
     * If {@link CentralPAPropertyRepository#PA_NET_USE_IP_ADDRESS} is set then the IP address is
     * returned instead of an FQDN
     *
     * @return
     * @see CentralPAPropertyRepository
     */
    public String getHostname() {
        return URIBuilder.getHostNameorIP(getInetAddress());
    }

    private InetAddress electAnAddress() {
        InetAddress ia = null;

        if (defaultBehavior()) {
            logger.debug("Using default algorithm to elect an IP address");
            ia = getDefaultInterface();
        } else {
            // Force a specific address
            logger.debug(CentralPAPropertyRepository.PA_HOSTNAME.getName() +
                         " defined. Using getByName() to elected an IP address");
            // return the result of getByName
            try {
                ia = InetAddress.getByName(CentralPAPropertyRepository.PA_HOSTNAME.getValue());
            } catch (UnknownHostException e) {
                logger.error(CentralPAPropertyRepository.PA_HOSTNAME.getName() +
                             " is set, but no IP address is bound to this hostname");
            }
        }

        return ia;
    }

    /**
     * Elects the default {@link InetAddress}.
     * <p/>
     * A first match algorithm is used:
     * <ol>
     * <li>Public IP address</li>
     * <li>Private IP address</li>
     * <li>loopback address</li>
     * </ol>
     * <p/>
     * Eventually the interface which has the fastest internet connection can be chosen if the following property is enabled:
     * {@link CentralPAPropertyRepository#PA_NET_FASTEST_CONNECTION}
     * <p/>
     * IPv6 address can enabled/disabled with the
     * {@link CentralPAPropertyRepository#PA_NET_DISABLE_IPv6} property.
     */
    private InetAddress getDefaultInterface() {
        List<InetAddress> ias = getEligibleAdresses();

        if (ias.size() > 0) {

            if (CentralPAPropertyRepository.PA_NET_FASTEST_CONNECTION.isSet() &&
                CentralPAPropertyRepository.PA_NET_FASTEST_CONNECTION.isTrue()) {
                logger.debug("Property proactive.net.fastest.connection is set to true, compute now Internet connection delay for all interfaces...");
                InetAddress fastest = selectFastestConnectionToServer(ias);
                if (fastest != null) {
                    return fastest;
                }
            }

            return ias.get(0);
        }

        return null;
    }

    /**
     * returns all eligible {@link InetAddress}.
     * <p/>
     * The list is sorted using the following order:
     * <ol>
     * <li>Public IP address</li>
     * <li>Private IP address</li>
     * <li>loopback address</li>
     * </ol>
     * <p/>
     * IPv6 address can enabled/disabled with the
     * {@link CentralPAPropertyRepository#PA_NET_DISABLE_IPv6} property.
     */
    public List<InetAddress> getEligibleAdresses() {
        List<InetAddress> ias = getAllInetAddresses();
        LinkedHashSet<InetAddress> result = new LinkedHashSet<InetAddress>();
        // Search for a public address
        for (InetAddress ia : ias) {
            if (ia.isLoopbackAddress())
                continue;
            if (ia.isLinkLocalAddress())
                continue;
            if (ia.isSiteLocalAddress())
                continue;
            result.add(ia);
        }

        // Search for a private address
        for (InetAddress ia : ias) {
            if (ia.isLoopbackAddress())
                continue;
            if (ia.isLinkLocalAddress())
                continue;
            result.add(ia);
        }

        // Search for a loopback address
        for (InetAddress ia : ias) {
            result.add(ia);
        }

        return new ArrayList<>(result);
    }

    public static String getPublicAddress() {
        if (CentralPAPropertyRepository.PA_PUBLIC_ADDRESS.isSet()) {
            return CentralPAPropertyRepository.PA_PUBLIC_ADDRESS.getValue();
        }
        return null;
    }

    /**
     * Returns true if the hostname property is set (force binding).
     * False otherwise
     */
    private boolean defaultBehavior() {
        if (CentralPAPropertyRepository.PA_HOSTNAME.isSet())
            return false;

        return true;
    }

    /**
     * Return all inet addresses
     *
     * @return
     */
    private static List<InetAddress> getAllInetAddresses() {
        List<InetAddress> ret = new LinkedList<InetAddress>();

        String intf = CentralPAPropertyRepository.PA_NET_INTERFACE.getValue();

        Enumeration<NetworkInterface> nis;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();

                if (intf != null && !ni.getName().equals(intf)) {
                    // Skip this interface if it's not the one selected
                    continue;
                }

                if (!ni.isUp()) {
                    // Skip this interface if it's disabled
                    continue;
                }

                Enumeration<InetAddress> ias = ni.getInetAddresses();
                List<InetAddress> ial = Collections.list(ias);

                ret.addAll(ial);
            }
        } catch (SocketException e) {
            logger.info("Failed to find a suitable InetAddress", e);
        }

        ret = filterIPv6(ret);
        ret = filterLinkLocal(ret);
        ret = filterByNetmask(ret);
        ret = filterLoopback(ret);
        ret = filterPrivate(ret);

        List<InetAddress> iaspref = new LinkedList<>(ret);
        sortInetAddresses(iaspref);

        return iaspref;
    }

    private static List<InetAddress> filterPrivate(List<InetAddress> l) {
        if (!CentralPAPropertyRepository.PA_NET_NOPRIVATE.isSet() ||
            !CentralPAPropertyRepository.PA_NET_NOPRIVATE.isTrue()) {
            // All InetAddress match
            return l;
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

    private static List<InetAddress> filterLinkLocal(List<InetAddress> l) {
        if (!CentralPAPropertyRepository.PA_NET_NOLINKLOCAL.isSet() ||
            !CentralPAPropertyRepository.PA_NET_NOLINKLOCAL.isTrue()) {
            // All InetAddress match
            return l;
        }

        List<InetAddress> ret = new LinkedList<>();
        for (InetAddress ia : l) {
            if (!ia.isLinkLocalAddress()) {
                ret.add(ia);
            } else {
                logger.debug("Discarded " + ia + " because it's a link local address");
            }
        }

        return ret;
    }

    private static List<InetAddress> filterLoopback(List<InetAddress> l) {
        if (!CentralPAPropertyRepository.PA_NET_NOLOOPBACK.isSet() ||
            !CentralPAPropertyRepository.PA_NET_NOLOOPBACK.isTrue()) {
            // All InetAddress match
            return l;
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

    /**
     * Sort addresses according to IPv4 / IPv6 preferences
     *
     * @param iaList
     */
    private static void sortInetAddresses(List<InetAddress> iaList) {
        // By default we prefer IPv4 addresses, unless the prefer_ipv6_address is set
        final int cmpIp4Ip6 = ((CentralPAPropertyRepository.PREFER_IPV6_ADDRESSES.isSet() &&
                                CentralPAPropertyRepository.PREFER_IPV6_ADDRESSES.isTrue())) ? 1 : -1;
        Collections.sort(iaList, new Comparator<InetAddress>() {
            @Override
            public int compare(InetAddress o1, InetAddress o2) {
                boolean o1i4 = (o1 instanceof Inet4Address);
                boolean o2i4 = (o2 instanceof Inet4Address);
                if ((o1i4) && (o2i4)) {
                    return 0;
                } else if ((!o1i4) && (!o2i4)) {
                    return 0;
                } else if ((o1i4) && (!o2i4)) {
                    return cmpIp4Ip6;
                } else {
                    // ((!o1i4) && (o2i4))
                    return -cmpIp4Ip6;
                }
            }
        });
    }

    /**
     * Select the address which has the fastest connection to the given server
     *
     * @param listAdresses
     * @return the elected address or null if no address can connect to the given server
     */
    private InetAddress selectFastestConnectionToServer(List<InetAddress> listAdresses) {

        ExecutorService service = Executors.newFixedThreadPool(listAdresses.size());

        final int timeout = CentralPAPropertyRepository.PA_NET_FASTEST_CONNECTION_TIMEOUT.getValue();
        final String serverToConnectTo = CentralPAPropertyRepository.PA_NET_FASTEST_CONNECTION_SERVER.getValue();
        final int port = CentralPAPropertyRepository.PA_NET_FASTEST_CONNECTION_PORT.getValue();

        final List<Callable<Map.Entry<InetAddress, Integer>>> testConnectionCallables = new ArrayList<>(listAdresses.size());

        for (final InetAddress address : listAdresses) {
            testConnectionCallables.add(new Callable<Map.Entry<InetAddress, Integer>>() {

                @Override
                public Map.Entry<InetAddress, Integer> call() throws Exception {
                    try (SocketChannel socket = SocketChannel.open()) {

                        // again, use a big enough timeout
                        socket.socket().setSoTimeout(timeout);

                        // bind the socket to your local interface
                        socket.bind(new InetSocketAddress(address, 0));

                        long start = System.currentTimeMillis();

                        // try to connect to *somewhere*
                        socket.connect(new InetSocketAddress(InetAddress.getByName(serverToConnectTo), port));
                        long end = System.currentTimeMillis();
                        return new AbstractMap.SimpleImmutableEntry<>(address, (int) (end - start));
                    }
                }
            });
        }

        try {
            Map.Entry<InetAddress, Integer> fastestAddress = service.invokeAny(testConnectionCallables,
                                                                               (long) timeout * 2,
                                                                               TimeUnit.MILLISECONDS);
            logger.debug("Fastest address detected : " + fastestAddress.getKey() + " (" + fastestAddress.getValue() +
                         " ms)");
            return fastestAddress.getKey();
        } catch (Exception e) {
            return null;
        } finally {
            service.shutdownNow();
        }
    }

    private static List<InetAddress> filterIPv6(List<InetAddress> l) {
        if (!CentralPAPropertyRepository.PA_NET_DISABLE_IPv6.isSet() ||
            !CentralPAPropertyRepository.PA_NET_DISABLE_IPv6.isTrue()) {
            // All InetAddress match
            return l;
        }

        List<InetAddress> ret = new LinkedList<>();
        for (InetAddress ia : l) {
            if (!(ia instanceof Inet6Address)) {
                ret.add(ia);
            } else {
                logger.debug("Discarded " + ia + " because it's an IPv6 address");
            }
        }

        return ret;
    }

    private static List<InetAddress> filterByNetmask(List<InetAddress> l) {
        if (!CentralPAPropertyRepository.PA_NET_NETMASK.isSet()) {
            // All InetAddress match
            return l;
        }

        IPMatcher matcher;
        try {
            matcher = new IPMatcher(CentralPAPropertyRepository.PA_NET_NETMASK.getValue());
        } catch (Throwable e) {
            logger.fatal("Invalid format for property " + CentralPAPropertyRepository.PA_NET_NETMASK.getName() +
                         ". Must be xxx.xxx.xxx.xxx/xx");
            return new LinkedList<>();
        }

        List<InetAddress> ret = new LinkedList<>();
        for (InetAddress ia : l) {
            if (!(ia instanceof Inet6Address)) {
                if (matcher.match(ia)) {
                    ret.add(ia);
                } else {
                    logger.debug("Discarded " + ia + " because of netmask criteria is not compatible with IPv6");
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

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("-debug")) {
            logger.setLevel(Level.DEBUG);
        }
        String nl = System.lineSeparator();
        System.out.println("Available interfaces: ");
        for (String itf : listAllInetAddress()) {
            System.out.println(itf);
        }
        ProActiveInet painet = ProActiveInet.getInstance();
        System.out.println(nl + "Eligible addresses: ");
        for (InetAddress ia : painet.getEligibleAdresses()) {
            System.out.println(ia);
        }

        System.out.println(nl + "Elected address : " + painet.electedAddress);
    }
}
