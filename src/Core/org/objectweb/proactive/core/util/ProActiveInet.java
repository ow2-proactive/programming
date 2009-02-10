/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
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
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PALifeCycle;
import org.objectweb.proactive.core.config.PAProperties;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Provide the local InetAddress to be used by ProActive
 * 
 * Implementation should respect Java network Stack property
 * java.net.preferIPv6addresses
 */
public class ProActiveInet {
    static private ProActiveInet instance;
    static private Object singletonMutex = new Object();
    static private Logger logger = ProActiveLogger.getLogger(Loggers.CORE);
    private InetAddress electedAddress;

    static public ProActiveInet getInstance() {
        if (null == instance) {
            synchronized (singletonMutex) {
                if (null == instance) {
                    instance = new ProActiveInet();
                }
            }
        }

        return instance;
    }

    private ProActiveInet() {
        electedAddress = electAnAddress();
        if (electedAddress == null) {
            logger.error("No local Inet Address found. Exiting");
            PALifeCycle.exitFailure();
        }
    }

    /**
     * Returns the inet address used by the local ProActive Runtime
     * 
     * @return
     */
    public InetAddress getInetAddress() {
        return electedAddress;
    }

    /**
     * Returns the host name of the local ProActive inet address
     * 
     * If {@link PAProperties.PA_USE_IP_ADDRESS} is set then the IP address is
     * returned instead of an FQDN
     * 
     * @return
     * @see PAProperties
     */
    public String getHostname() {
        return URIBuilder.getHostNameorIP(getInetAddress());
    }

    private InetAddress electAnAddress() {
        InetAddress address;

        try {
            List<NetworkInterface> nis;
            nis = getNetworkInterfaces();
            if (PAProperties.PREFER_IPV6_ADDRESSES.isTrue()) {
                address = findAddress(nis, Inet6Address.class);
                if (address == null) {
                    address = findAddress(nis, Inet4Address.class);
                }
            } else {
                address = findAddress(nis, Inet4Address.class);
                if (address == null) {
                    address = findAddress(nis, Inet6Address.class);
                }
            }
        } catch (SocketException e) {
            address = null;
        }

        return address;
    }

    private InetAddress findAddress(List<NetworkInterface> nis, Class<?> cl) {
        for (NetworkInterface ni : nis) {
            Enumeration<InetAddress> ias = ni.getInetAddresses();
            while (ias.hasMoreElements()) {
                InetAddress ia = ias.nextElement();
                if (ia.getClass().equals(cl)) {
                    if (PAProperties.PA_NET_NOLOOPBACK.isTrue()) {
                        if (ia.isLoopbackAddress()) {
                            continue;
                        }
                    }

                    if (PAProperties.PA_NET_NOPRIVATE.isTrue()) {
                        if (ia.isSiteLocalAddress()) {
                            continue;
                        }
                    }

                    return ia;
                }
            }
        }
        return null;
    }

    private List<NetworkInterface> getNetworkInterfaces() throws SocketException {
        List<NetworkInterface> interfaces = new ArrayList<NetworkInterface>();

        if (PAProperties.PA_NET_INTERFACE.isSet()) {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (ni.getName().equals(PAProperties.PA_NET_INTERFACE.getValue())) {
                    interfaces.add(ni);
                }
            }
        } else {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                interfaces.add(nis.nextElement());
            }
        }

        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (NetworkInterface ni : interfaces) {
                sb.append(ni.getName());
                sb.append(" ");
            }
            logger.debug("Suitable Network Interfaces are: " + sb);
        }

        if (interfaces.isEmpty()) {
            logger.warn("No suitable network interface found");
        }

        return interfaces;
    }

    public List<String> getAlInetAddresses() {
        List<String> ret = new ArrayList<String>();

        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                StringBuilder sb = new StringBuilder();

                NetworkInterface ni = nis.nextElement();

                sb.append(ni.getName() + "\t");
                sb.append(macAddrToString(ni.getHardwareAddress()) + "\t");
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    sb.append(ia.getAddress().toString() + " ");
                }

                ret.add(sb.toString());
            }
        } catch (SocketException e) {
            logger.error("Failed to list all available netAdress", e);
        }

        return ret;
    }

    private String macAddrToString(byte[] macAddr) {
        StringBuffer sb = new StringBuffer(17);
        for (int i = 44; i >= 0; i -= 4) {
            int nibble = ((int) (byte2Long(macAddr) >>> i)) & 0xf;
            char nibbleChar = (char) (nibble > 9 ? nibble + ('A' - 10) : nibble + '0');
            sb.append(nibbleChar);
            if ((i & 0x7) == 0 && i != 0) {
                sb.append(':');
            }
        }
        return sb.toString();
    }

    private long byte2Long(byte addr[]) {
        long address = 0;
        if (addr != null) {
            if (addr.length == 6) {
                address = unsignedByteToLong(addr[5]);
                address |= (unsignedByteToLong(addr[4]) << 8);
                address |= (unsignedByteToLong(addr[3]) << 16);
                address |= (unsignedByteToLong(addr[2]) << 24);
                address |= (unsignedByteToLong(addr[1]) << 32);
                address |= (unsignedByteToLong(addr[0]) << 40);
            }
        }
        return address;
    }

    private long unsignedByteToLong(byte b) {
        return (long) b & 0xFF;
    }

}
