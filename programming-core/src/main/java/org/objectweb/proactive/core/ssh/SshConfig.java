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
package org.objectweb.proactive.core.ssh;

import static org.objectweb.proactive.core.ssh.SSH.logger;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.core.ssh.SshConfigFileParser.SshToken;
import org.objectweb.proactive.core.ssh.proxycommand.SubnetChecker;


/**
 * This class store all information read both in ssh configuration file
 * and in the PA_SSH_PROXY_GATEWAY PAProperties
 *
 */
public class SshConfig {
    private final static String defaultPath = System.getProperty("user.home") + File.separator + ".ssh" +
                                              File.separator;

    private final static String IPv4Regexp = "^.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";

    private final AtomicBoolean readOnly = new AtomicBoolean(false);

    private boolean tryPlainSocket;

    private boolean tryProxyCommand;

    private long gcInterval;

    private long gcIdleTime;

    private int connectTimeout;

    private String sshDirPath;

    private String DEFAULT = "*";

    /**
     * A Map of String and a Map of Token,String is used to represent the ssh configuration file,
     * information are stored for host (key of the global map table) and for each host,
     * each field of the ssh configuration file are represent by a SshToken in an inner map table
     *
     * easily extensible, only add a new token the SshToken, and fill, if special processing is needed,
     * the switch/case with the right method.
     */
    private Map<String, Map<SshToken, String>> sshInfos;

    private SshToken[] capabilities = { SshToken.HOSTNAME, SshToken.USERNAME, SshToken.PORT, SshToken.PRIVATEKEY,
                                        SshToken.GATEWAY };

    // Store here the mapping subnet / gateway
    private SubnetChecker subnetChecker = new SubnetChecker();

    private String knownHostFile;

    private String keyDir;

    /**
     *  This class represent all the information about ssh connection.
     *  All fields have to be set, classicaly by using the SshConfigFileParser
     *
     *  This constructor use the default path
     *
     * @see SshConfigFileParser
     * @see SshConnection
     */
    public SshConfig() {
        this(defaultPath);
    }

    /**
     *  This class represent all the information about ssh connection.
     *  All fields have to be set, classically by using the SshConfigFileParser
     *
     * @param sshDirPath
     *          The complete path to the directory where configuration file and
     *          keys are placed
     *
     * @see SshConfigFileParser
     * @see SshConnection
     */
    public SshConfig(String sshDirPath) {
        this.sshDirPath = sshDirPath;
        this.keyDir = sshDirPath;
        this.sshInfos = new Hashtable<String, Map<SshToken, String>>();

        this.tryPlainSocket = false;
        this.tryProxyCommand = false;

        this.gcInterval = 5000;
        this.gcIdleTime = 10000;
    }

    /**
     * Check if the token is supported by this SshConfig
     *
     *  @param toTest
     *          The token to test
     */
    private boolean isCapable(SshToken toTest) {
        for (SshToken tok : capabilities) {
            if (toTest.equals(tok)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method store in the Maptable the <code> information </code> (gateway name, port,
     * username, ...) to use for contacting the host (<code> hostname </code>)
     *
     * If the information was already declared, then the new one is ignored
     *
     * @param hostname
     *            The host to contact
     *
     * @param request
     *            The field to set (username,port,gateway, ...) declare using an enum SshToken
     *
     * @param information
     *            The value of the field
     *
     *
     * @see SshConfigFileParser
     */
    public void addHostInformation(String hostname, SshToken request, String information) {
        if (isCapable(request)) {
            if ((hostname.charAt(0) == '*') && (hostname.length() > 1)) {
                hostname = hostname.substring(1);
            }
            Map<SshToken, String> hostsInfos = sshInfos.get(hostname);

            if (hostsInfos == null) {
                // No information already record for the host
                hostsInfos = new Hashtable<SshToken, String>();
                hostsInfos.put(request, information);
                sshInfos.put(hostname, hostsInfos);
            } else {
                if (hostsInfos.get(request) != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Ssh configuration : information " + information + " as " +
                                     request.toString().toLowerCase() +
                                     (hostname.equalsIgnoreCase(DEFAULT) ? " as default" : " for " + hostname) +
                                     " is already declared, ignored");
                    }
                    return;
                }
                hostsInfos.put(request, information);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Ssh configuration : " + information + " as " + request.toString().toLowerCase() +
                             " stored " +
                             (hostname.equalsIgnoreCase(DEFAULT) ? "as default." : "for " + hostname + "."));
            }
        } else {
            logger.warn("Ssh configuration option \"" + request.getValue() + " = " + information + "has been ignored.");
        }
    }

    public void addDefaultHostInformation(SshToken request, String information) {
        this.addHostInformation(DEFAULT, request, information);
    }

    /**
     * Replace host by hostname in the map table because
     * ssh allow to use nickname in it configuration file
     *
     * @param host
     *           nickname
     *
     * @param hostname
     *          real host name
     *
     */
    public void changeHostname(String host, String hostname) {
        Map<SshToken, String> infos = sshInfos.get(host);
        if (infos != null) {
            sshInfos.remove(host);
            sshInfos.put(hostname, infos);
        }
    }

    /** Seals the configuration */
    final void setReadOnly() {
        this.readOnly.set(true);
    }

    final private void checkReadOnly() {
        if (this.readOnly.get())
            throw new IllegalStateException(SshConfig.class.getName() + " bean is now read only, cannot be modified");
    }

    //////////////////////////////////////////////////////////////////////////////////
    // ACCESS METHOD
    //////////////////////////////////////////////////////////////////////////////////

    // Default method
    /**
     * Do a wildcarded search in the maptable and return all the information
     * related to the machine <code> hostname </code>
     *
     * @param hostname
     *          the name of the machine on which information are requested
     * @return
     *          all information stored as a Map table of SshToken and String
     *
     */
    public String getInformation(String hostname, SshToken tok) {
        Map<SshToken, String> hostInfos;
        int index;
        while (hostname.contains(".")) {
            hostInfos = sshInfos.get(hostname);
            if (hostInfos != null && hostInfos.get(tok) != null) {
                return hostInfos.get(tok);
            } else {
                // eat the first point
                hostname = hostname.substring(1);
                index = hostname.indexOf('.');
                if (index < 0)
                    break;
                hostname = hostname.substring(hostname.indexOf('.'));
            }
        }
        hostInfos = sshInfos.get(hostname);
        if (hostInfos != null && hostInfos.get(tok) != null) {
            return hostInfos.get(tok);
        } else {
            Map<SshToken, String> map = sshInfos.get(DEFAULT);
            if (map != null) {
                String defaultValue = map.get(tok);
                if (defaultValue != null) {
                    return defaultValue;
                }
            }
        }
        return null;
    }

    /**
     * Never return null, if no information stored, return system username
     */
    public String getUsername(String host) {
        String user = getInformation(host, SshToken.USERNAME);
        if (user == null)
            return System.getProperty("user.name");
        return user;
    }

    /**
     * Return the hostname of the gateway to use to contact to host <code> hostname </code>
     *
     * @param host
     *          The host to contact
     */
    public String getGateway(String host) {
        String hostname = host;

        // Special case for protocol like RMI, which use ip address for answer
        if (host.matches(IPv4Regexp)) {
            String subnetTest = subnetChecker.getGateway(host);
            if (subnetTest != null)
                return subnetTest;
        }

        String gateway = getInformation(hostname, SshToken.GATEWAY);
        if (gateway == null || gateway.equalsIgnoreCase("none"))
            return null;
        return gateway;
    }

    /**
     * Never return null, if no information stored, return ssh default port : 22
     */
    public int getPort(String host) {
        String port = getInformation(host, SshToken.PORT);
        if (port != null)
            return Integer.parseInt(port);
        else
            return 22;
    }

    /**
     * Returns a resolvable host name for the associated public <code>host</code> provided.
     * If none is defined, returns the public host provided.
     *
     * @param host the public host name of the remote-side of the SSH Tunnel
     * @return A host name the remote-side of the SSH Tunnel can resolve
     */
    public String getHostName(String host) {
        String hostName = getInformation(host, SshToken.HOSTNAME);
        if (hostName != null) {
            return hostName;
        } else {
            return host;
        }
    }

    /**
     * Never return null, if no information stored, return ssh default private key
     */
    public String[] getPrivateKeyPath(String host) throws IOException {
        String key = getInformation(host, SshToken.PRIVATEKEY);
        if (key != null)
            return new String[] { key };
        else
            return new SSHKeys(getKeyDir()).getKeys();
    }

    /**
     * Search the gateway to use for the host and return all rules related to this gateway
     */
    public String getRule(String host) {
        String gateway = this.getInformation(host, SshToken.GATEWAY);
        StringBuilder sb = new StringBuilder();

        // Add all rules defined by regular expression on hostname for this gateway
        for (String rule : sshInfos.keySet()) {
            String gatewayTest = sshInfos.get(rule).get(SshToken.GATEWAY);
            if (gatewayTest != null && gatewayTest.equalsIgnoreCase(gateway)) {
                sb.append(rule);
                sb.append(":");
                sb.append(gateway);
                sb.append(":");
                sb.append(getInformation(gateway, SshToken.PORT));
                sb.append(";");
            }
        }

        // Add all rules defined by ip address and cidr definition for this gateway
        for (String rule : subnetChecker.getRule(gateway).split(";")) {
            if (rule != null && !"".equals(rule.trim())) {
                sb.append(rule);
                sb.append(":");
                sb.append(gateway);
                sb.append(":");
                sb.append(getInformation(gateway, SshToken.PORT));
                sb.append(";");
            }
        }
        return sb.toString();
    }

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Store subnet definition
     */
    public void addSubnetInformation(String subnet, String gateway) {
        checkReadOnly();
        this.subnetChecker.setGateway(subnet, gateway);
    }

    //////////////////////////////////
    // Classic getters and setters
    //////////////////////////////////

    /** SSH tunnels and connections are garbage collected if unused longer than this amount of time (ms) */
    public long getGcIdleTime() {
        return gcIdleTime;
    }

    public void setGcIdleTime(long gcIdleTime) {
        checkReadOnly();
        this.gcIdleTime = gcIdleTime;
    }

    public boolean tryProxyCommand() {
        return tryProxyCommand;
    }

    public void setTryProxyCommand(boolean b) {
        checkReadOnly();
        this.tryProxyCommand = b;
    }

    /** Should plain socket be used if direction is possible ? */
    final public boolean tryPlainSocket() {
        checkReadOnly();
        return tryPlainSocket;
    }

    final public void setTryPlainSocket(boolean tryNormalFirst) {
        checkReadOnly();
        this.tryPlainSocket = tryNormalFirst;
    }

    /** The amount of time to wait between each garbage collection */
    public long getGcInterval() {
        return gcInterval;
    }

    public void setGcInterval(long gcInterval) {
        checkReadOnly();
        this.gcInterval = gcInterval;
    }

    /** The connect timeout for plain and ssh socket */
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        checkReadOnly();
        this.connectTimeout = connectTimeout;
    }

    public String getSshDirPath() {
        return sshDirPath;
    }

    public void setSshDirPath(String sshDirPath) {
        checkReadOnly();
        this.sshDirPath = sshDirPath;
    }

    public void setKnowHostFile(String knownHostFile) {
        checkReadOnly();
        this.knownHostFile = knownHostFile;
    }

    public String getKnowHostFile() {
        return this.knownHostFile;
    }

    public void setKeyDir(String keyDir) {
        checkReadOnly();
        this.keyDir = keyDir;
    }

    public String getKeyDir() {
        return this.keyDir;
    }
}
