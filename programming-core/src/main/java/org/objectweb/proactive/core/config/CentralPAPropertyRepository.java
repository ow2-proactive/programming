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
package org.objectweb.proactive.core.config;

import java.net.Socket;

import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * The central repository of PAProperty
 * <p/>
 * Ideally the PAProperty should be defined in their own module then registered into
 * the {@link PAProperties} singleton. Unfortunately, until ProActive 4.3.0 it was only
 * possible to declare a ProActive property in a centralized enumeration. This
 * central repository contains all the already existing properties.
 */
public class CentralPAPropertyRepository implements PAPropertiesLoaderSPI {

    /**
     * Java security policy file location
     */
    static public PAPropertyString JAVA_SECURITY_POLICY = new PAPropertyString("java.security.policy", true);

    /**
     * Indicate the Fractal provider class, to the ProActive implementation of
     * Fractal/GCM set it to org.objectweb.proactive.core.component.Fractive
     */
    static public PAPropertyString FRACTAL_PROVIDER = new PAPropertyString("fractal.provider", true);

    static public PAPropertyString JAVA_SECURITY_AUTH_LOGIN_CONFIG = new PAPropertyString("java.security.auth.login.config",
                                                                                          true);

    static public PAPropertyString JAVAX_XML_TRANSFORM_TRANSFORMERFACTORY = new PAPropertyString("javax.xml.transform.TransformerFactory",
                                                                                                 true);

    /*
     * ------------------------------------
     * PROACTIVE
     */

    /**
     * ProActive Configuration file location
     * <p/>
     * If set ProActive will load the configuration file at the given location.
     */
    static public PAPropertyString PA_CONFIGURATION_FILE = new PAPropertyString("proactive.configuration", false);

    /**
     * Indicates where ProActive is installed
     * <p/>
     * Can be useful to write generic deployment descriptor by using a JavaPropertyVariable
     * to avoid hard coded path.
     * <p/>
     * Used in unit and functional tests
     */
    static public PAPropertyString PA_HOME = new PAPropertyString("proactive.home", false);

    /**
     * Indicates what is the operating system of the local machine
     * <p/>
     * It is not redundant with "os.name" since the only valid values those from <code>OperatingSystem</code>.
     * Often users are only interested to know if the computer is running unix or windows.
     *
     * @see OperatingSystem
     */
    static public PAPropertyString PA_OS = new PAPropertyString("proactive.os", true);

    /**
     * Log4j configuration file location
     * <p/>
     * If set the specified log4j configuration file is used. Otherwise the default one,
     * Embedded in the ProActive jar is used.
     */
    static public PAPropertyString LOG4J = new PAPropertyString("log4j.configuration", false);

    /**
     * URI of the remote log collector
     */
    static public PAPropertyString PA_LOG4J_COLLECTOR = new PAPropertyString("proactive.log4j.collector", false);

    /**
     * Qualified name of the flushing provider to use
     */
    static public PAPropertyString PA_LOG4J_APPENDER_PROVIDER = new PAPropertyString("proactive.log4j.appender.provider",
                                                                                     false,
                                                                                     "org.objectweb.proactive.core.util.log.remote.ThrottlingProvider");

    /**
     * Specifies the name of the ProActive Runtime
     * <p/>
     * By default a random name is assigned to a ProActive Runtime. This property allows
     * to choose the name of the Runtime to be able to perform lookups.
     * <p/>
     * By default, the name of a runtime starts with PA_JVM</strong>
     */
    static public PAPropertyString PA_RUNTIME_NAME = new PAPropertyString("proactive.runtime.name", false);

    /**
     * this property should be used when one wants to start only a runtime without an additional main class
     */
    static public PAPropertyBoolean PA_RUNTIME_STAYALIVE = new PAPropertyBoolean("proactive.runtime.stayalive",
                                                                                 false,
                                                                                 true);

    /**
     * Terminates the Runtime when the Runtime becomes empty
     * <p/>
     * If true, when all bodies have been terminated the ProActive Runtime will exit
     */
    static public PAPropertyBoolean PA_EXIT_ON_EMPTY = new PAPropertyBoolean("proactive.exit_on_empty", false, false);

    /**
     * Boolean to activate automatic continuations for this runtime.
     */
    static public PAPropertyBoolean PA_FUTURE_AC = new PAPropertyBoolean("proactive.future.ac", false, true);

    /**
     * Timeout value for future in synchronous requests.
     * can be used to set timeout on synchronous calls. Impossible otherwise
     * default value 0, no timeout
     */
    static public PAPropertyLong PA_FUTURE_SYNCHREQUEST_TIMEOUT = new PAPropertyLong("proactive.future.synchrequest.timeout",
                                                                                     false,
                                                                                     0);

    /**
     * Period of the future monitoring ping, in milliseconds
     * <p/>
     * If set to 0, then future monitoring is disabled
     */
    static public PAPropertyInteger PA_FUTUREMONITORING_TTM = new PAPropertyInteger("proactive.futuremonitoring.ttm",
                                                                                    false);

    /**
     * When this property is set to true, at each proactive call, the stack trace context of the call is embedded in the future.
     * It is specially useful when debugging automatic continuations. If an exception is thrown consecutively to a proactive call,
     * all methods traversed during this call will be displayed, even if the call recursively called other active objects.
     * If the property is set to false (which is the default), the itinerary will still be followed but only the main call
     * in the stack will be kept and forwarded
     */
    static public PAPropertyBoolean PA_STACKTRACE = new PAPropertyBoolean("proactive.stack_trace", false, false);

    /**
     * Activates the legacy SAX ProActive Descriptor parser
     * <p/>
     * To check if the new JAXP parser introduced regressions.
     */
    static public PAPropertyBoolean PA_LEGACY_PARSER = new PAPropertyBoolean("proactive.legacy.parser", false);

    /*
     * ------------------------------------
     * NETWORK
     */

    /**
     * ProActive Communication protocol
     * <p/>
     * Suppported values are: rmi, rmissh, http
     */
    static public PAPropertyString PA_COMMUNICATION_PROTOCOL = new PAPropertyString("proactive.communication.protocol",
                                                                                    false,
                                                                                    "rmi");

    /**
     * ProActive Runtime Hostname (or IP Address)
     * <p/>
     * This option can be used to set manually the Runtime IP Address. Can be
     * useful when the Java networking stack return a bad IP address (example: multihomed machines).
     */
    static public PAPropertyString PA_HOSTNAME = new PAPropertyString("proactive.hostname", false);

    /**
     * ProActive Runtime Public Address (or IP Address)
     * <p/>
     * This option can be used when a host is behind a NAT. In that case, the host IP address is not visible outside the NAT,
     * and a public IP address with a NAT port forwarding must be used to contact the ProActive Node.
     *
     * The underlying ProActive protocol must support this parameter in order to be effective.
     * Currently, only PNP supports this parameter.
     */
    static public PAPropertyString PA_PUBLIC_ADDRESS = new PAPropertyString("proactive.net.public_address", false);

    /**
     * Toggle DNS resolution
     * <p/>
     * When true IP addresses are used instead of FQDNs. Can be useful with misconfigured DNS servers
     * or strange /etc/resolv.conf files. FQDNs passed by user or 3rd party tools are resolved and converted
     * into IP addresses
     */
    static public PAPropertyBoolean PA_NET_USE_IP_ADDRESS = new PAPropertyBoolean("proactive.useIPaddress", false);

    /**
     * Enable or disable IPv6. When IPv6 is disabled, all IPv6 addresses will be ignored.
     */
    static public PAPropertyBoolean PA_NET_DISABLE_IPv6 = new PAPropertyBoolean("proactive.net.disableIPv6",
                                                                                false,
                                                                                true);

    /**
     * IPv6 stack is preferred by default, since on a dual-stack machine IPv6 socket can talk to both IPv4 and IPv6 peers.
     * <p/>
     * This setting can be changed through the java.net.preferIPv4Stack=<true|false> system property.
     */
    static public PAPropertyBoolean PREFER_IPV4_STACK = new PAPropertyBoolean("java.net.preferIPv4Stack", true);

    /**
     * By default, we would prefer IPv4 addresses over IPv6 addresses, i.e., when querying the name service (e.g., DNS service),
     * we would return Ipv4 addresses ahead of IPv6 addresses. There are two reasons for this choice:
     * <p/>
     * - There are some applications that expect an IPv4 address textual format, i.e. "%d.%d.%d.%d". Using an IPv4 address minimizes the surprises;
     * - Using IPv4 address, we can use one call (with an IPv6 socket) to reach either a legacy IPv4-only service, or an IPv6 service (unless the IPv6 service is on a Ipv6 only node).
     * This setting can be changed through the system property java.net.preferIPv6Addresses=true|false
     */
    static public PAPropertyBoolean PREFER_IPV6_ADDRESSES = new PAPropertyBoolean("java.net.preferIPv6Addresses",
                                                                                  true,
                                                                                  false);

    /**
     * Toggle loopback IP address usage
     * <p/>
     * When true (default) loopback IP address usage is avoided. Usage of the loopback address could be allowed when
     * deploying a service which communicates only locally, otherwise it must remain disabled.
     * <p/>
     * If only a loopback address exists, it is used.
     */
    static public PAPropertyBoolean PA_NET_NOLOOPBACK = new PAPropertyBoolean("proactive.net.nolocal", false, false);

    /**
     * Toggle Private IP address usage
     * <p/>
     * When true private IP address usage is avoided. Private addresses are :
     * <p/>
     * 10.0.0.0/8
     * 172.16.0.0/12
     * 192.168.0.0/16
     * <p/>
     * Warning: Often a network will only contain private addresses, in that case setting this property to true
     * will result in finding no suitable interface for ProActive.
     * <p/>
     * Refer to:
     * https://en.wikipedia.org/wiki/Private_network
     */
    static public PAPropertyBoolean PA_NET_NOPRIVATE = new PAPropertyBoolean("proactive.net.noprivate", false, false);

    /**
     * Toggle Link Local IP address usage
     * <p/>
     * When true, link local IP address usage is avoided. Link local addresses are not routed and thus should be discarded.
     * In addition, an extra IPv6 link-local address is now assigned to each network interface,
     * (in addition to the standard interface address), and this prevents the correct IP address to be chosen.
     * https://en.wikipedia.org/wiki/Link-local_address
     */
    static public PAPropertyBoolean PA_NET_NOLINKLOCAL = new PAPropertyBoolean("proactive.net.nolinklocal",
                                                                               false,
                                                                               false);

    /**
     * Select the network interface
     */
    static public PAPropertyString PA_NET_INTERFACE = new PAPropertyString("proactive.net.interface", false);

    /**
     * Select the netmask to use (xxx.xxx.xxx.xxx/xx)
     * <p/>
     * Does not work with IPv6 addresses
     */
    static public PAPropertyString PA_NET_NETMASK = new PAPropertyString("proactive.net.netmask", false);

    /**
     * Select the network interface which has the fastest Internet connection.
     * <p/>
     * When several interfaces are installed, usually only one has access to the Internet, which means that this algorithm
     * can be used to automatically select the most "visible" interface among those available.
     * <p/>
     * This property can be enabled if you are connected to the Internet, otherwise it should be set to false,
     * or the proactive.net.fastest.connection.server property must be used to check connection with a well-known server.
     */
    static public PAPropertyBoolean PA_NET_FASTEST_CONNECTION = new PAPropertyBoolean("proactive.net.fastest.connection",
                                                                                      false,
                                                                                      false);

    /**
     * Timeout used for the internet connection detection
     */
    static public PAPropertyInteger PA_NET_FASTEST_CONNECTION_TIMEOUT = new PAPropertyInteger("proactive.net.fastest.connection.timeout",
                                                                                              false,
                                                                                              1000);

    /**
     * Server to connect to during the internet connection check.
     * <p/>
     * If you are behind a proxy, you can replace this server/port by the proxy hostname to detect the most "visible" interface.
     */
    static public PAPropertyString PA_NET_FASTEST_CONNECTION_SERVER = new PAPropertyString("proactive.net.fastest.connection.server",
                                                                                           false,
                                                                                           "google.com");

    /**
     * Port to connect to during the internet connection check.
     * <p/>
     * If you are behind a proxy, you can replace this server/port by the proxy hostname to detect the most "visible" interface.
     */
    static public PAPropertyInteger PA_NET_FASTEST_CONNECTION_PORT = new PAPropertyInteger("proactive.net.fastest.connection.port",
                                                                                           false,
                                                                                           80);

    /**
     * RMI/SSH black voodoo
     * <p/>
     * Can be used to fix broken networks (multihomed, broken DNS etc.). You probably want
     * to post on the public ProActive mailing list before using this property.
     */
    static public PAPropertyString PA_NET_SECONDARYNAMES = new PAPropertyString("proactive.net.secondaryNames", false);

    static public PAPropertyBoolean SCHEMA_VALIDATION = new PAPropertyBoolean("schema.validation", true, true);

    /*
     * ------------------------------------
     * RMI
     */

    /**
     * Assigns a TCP port to RMI
     * <p/>
     * this property identifies the default port used by the RMI communication protocol
     */
    static public PAPropertyInteger PA_RMI_PORT = new PAPropertyInteger("proactive.rmi.port", false, 1099);

    static public PAPropertyString JAVA_RMI_SERVER_CODEBASE = new PAPropertyString("java.rmi.server.codebase", true);

    static public PAPropertyBoolean JAVA_RMI_SERVER_USECODEBASEONLY = new PAPropertyBoolean("java.rmi.server.useCodebaseOnly",
                                                                                            true);

    /**
     * Sockets used by the RMI remote object factory connect to the remote server
     * with a specified timeout value. A timeout of zero is interpreted as an infinite timeout.
     * The connection will then block until established or an error occurs.
     */
    static public PAPropertyInteger PA_RMI_CONNECT_TIMEOUT = new PAPropertyInteger("proactive.rmi.connect_timeout",
                                                                                   false);

    static public PAPropertyString PA_CODEBASE = new PAPropertyString("proactive.codebase", true);

    static public PAPropertyBoolean PA_CLASSLOADING_USEHTTP = new PAPropertyBoolean("proactive.classloading.useHTTP",
                                                                                    false,
                                                                                    true);
    /*
     * ------------------------------------
     * HTTP
     */

    /**
     * Assigns a TCP port to XML-HTTP
     * <p/>
     * this property identifies the default port for the xml-http protocol
     */
    static public PAPropertyInteger PA_XMLHTTP_PORT = new PAPropertyInteger("proactive.http.port", false);

    /**
     * Jetty configuration file
     * <p/>
     * Jetty can be configured by providing a
     * <a  href="http://docs.codehaus.org/display/JETTY/jetty.xml">jetty.xml</a>
     * file. Click
     * <a  href="http://docs.codehaus.org/display/JETTY/Syntax+Reference">here </a>
     * for the Jetty syntax reference.
     */
    static public PAPropertyString PA_HTTP_JETTY_XML = new PAPropertyString("proactive.http.jetty.xml", false);

    /**
     * Sockets used by the HTTP remote object factory connect to the remote server
     * with a specified timeout value. A timeout of zero is interpreted as an infinite timeout.
     * The connection will then block until established or an error occurs.
     */
    static public PAPropertyInteger PA_HTTP_CONNECT_TIMEOUT = new PAPropertyInteger("proactive.http.connect_timeout",
                                                                                    false);

    /*
     * ------------------------------------
     * RMISSH
     */

    /**
     * this property identifies the location of RMISSH key directory
     */
    static public PAPropertyString PA_RMISSH_KEY_DIR = new PAPropertyString("proactive.communication.rmissh.key_directory",
                                                                            false);

    /**
     * this property identifies that when using SSH tunneling, a normal connection should be tried before tunneling
     */
    static public PAPropertyBoolean PA_RMISSH_TRY_NORMAL_FIRST = new PAPropertyBoolean("proactive.communication.rmissh.try_normal_first",
                                                                                       false,
                                                                                       false);

    /**
     * this property identifies the SSH garbage collector period
     * <p/>
     * If set to 0, tunnels and connections are not garbage collected
     */
    static public PAPropertyInteger PA_RMISSH_GC_PERIOD = new PAPropertyInteger("proactive.communication.rmissh.gc_period",
                                                                                false);

    /**
     * this property identifies the maximum idle time before a SSH tunnel or a connection is garbage collected
     */
    static public PAPropertyInteger PA_RMISSH_GC_IDLETIME = new PAPropertyInteger("proactive.communication.rmissh.gc_idletime",
                                                                                  false);

    /**
     * this property identifies the know hosts file location when using ssh tunneling
     * if undefined, the default value is user.home property concatenated to SSH_TUNNELING_DEFAULT_KNOW_HOSTS
     */
    static public PAPropertyString PA_RMISSH_KNOWN_HOSTS = new PAPropertyString("proactive.communication.rmissh.known_hosts",
                                                                                false);

    /**
     * Sock connect timeout, in ms
     * <p/>
     * The timeout to be used when a SSH Tunnel is opened. 0 is interpreted
     * as an infinite timeout. This timeout is also used for plain socket when try_normal_first is set to true
     *
     * @see Socket
     */
    static public PAPropertyInteger PA_RMISSH_CONNECT_TIMEOUT = new PAPropertyInteger("proactive.communication.rmissh.connect_timeout",
                                                                                      false);

    // Not documented, temporary workaround until 4.3.0
    static public PAPropertyString PA_RMISSH_REMOTE_USERNAME = new PAPropertyString("proactive.communication.rmissh.username",
                                                                                    false);

    // Not documented, temporary workaround until 4.3.0
    static public PAPropertyInteger PA_RMISSH_REMOTE_PORT = new PAPropertyInteger("proactive.communication.rmissh.port",
                                                                                  false);

    /*
     * ------------------------------------
     * REMOTE OBJECT - MULTI-PROTOCOL
     */

    /**
     * Expose object using these protocols in addition to the default one (protocols separated by comma)
     */
    // The list of additional protocols must not contain duplicates nor the main protocol
    public static PAPropertyList PA_COMMUNICATION_ADDITIONAL_PROTOCOLS = new PAPropertyList("proactive.communication.additional_protocols",
                                                                                            ",",
                                                                                            false,
                                                                                            CentralPAPropertyRepositoryUtils.ADDITIONAL_PROTOCOLS_VALIDATOR,
                                                                                            "");

    /**
     * Impose a static order for protocols selection, this order will supersede the benchmark order, if benchmark is
     * activated (The list of protocols must not contain duplicate elements)
     */
    public static PAPropertyList PA_COMMUNICATION_PROTOCOLS_ORDER = new PAPropertyList("proactive.communication.protocols.order",
                                                                                       ",",
                                                                                       false,
                                                                                       CentralPAPropertyRepositoryUtils.IS_SET);

    /**
     * Activate the protocol benchmark, false by default
     */
    public static PAPropertyBoolean PA_BENCHMARK_ACTIVATE = new PAPropertyBoolean("proactive.communication.benchmark.activate",
                                                                                  false,
                                                                                  false);

    /**
     * Specify a parameter for benchmark
     */
    public static PAPropertyString PA_BENCHMARK_PARAMETER = new PAPropertyString("proactive.communication.benchmark.parameter",
                                                                                 false);

    /**
     * The class to use for doing remoteObject Benchmark, must implement BenchmarkObject
     */
    public static PAPropertyString PA_BENCHMARK_CLASS = new PAPropertyString("proactive.communication.benchmark.class",
                                                                             false,
                                                                             "org.objectweb.proactive.core.remoteobject.benchmark.SelectionOnly");

    /**
     * time waited before two benchmarks
     */
    public static PAPropertyLong PA_BENCHMARK_PERIOD = new PAPropertyLong("proactive.communication.benchmark.period",
                                                                          false,
                                                                          600000L);

    /*
     * ------------------------------------
     * MESSAGE TAGGING
     */
    /**
     * Set the max period for LocalMemoryTag lease time
     */
    static public PAPropertyInteger PA_MAX_MEMORY_TAG_LEASE = new PAPropertyInteger("proactive.tagmemory.lease.max",
                                                                                    false,
                                                                                    60);

    /**
     * Set the Period of the running thread for tag memory leasing check
     */
    static public PAPropertyInteger PA_MEMORY_TAG_LEASE_PERIOD = new PAPropertyInteger("proactive.tagmemory.lease.period",
                                                                                       false,
                                                                                       21);

    /**
     * Enable or disable the Distributed Service ID Tag
     */
    static public PAPropertyBoolean PA_TAG_DSF = new PAPropertyBoolean("proactive.tag.dsf", false, false);

    /*
     * ------------------------------------
     * FILE TRANSFER
     */

    /**
     * The maximum number of FileTransferService objects that can be spawned
     * on a Node to handle file transfer requests in parallel.
     */
    static public PAPropertyInteger PA_FILETRANSFER_MAX_SERVICES = new PAPropertyInteger("proactive.filetransfer.services_number",
                                                                                         false,
                                                                                         16);

    /**
     * When sending a file, the maximum number of file blocks (parts) that can
     * be sent asynchronously before blocking for their arrival.
     */
    static public PAPropertyInteger PA_FILETRANSFER_MAX_SIMULTANEOUS_BLOCKS = new PAPropertyInteger("proactive.filetransfer.blocks_number",
                                                                                                    false,
                                                                                                    8);

    /**
     * The size, in [KB], of file blocks (parts) used to send files.
     */
    static public PAPropertyInteger PA_FILETRANSFER_MAX_BLOCK_SIZE = new PAPropertyInteger("proactive.filetransfer.blocks_size_kb",
                                                                                           false,
                                                                                           512);

    /**
     * The size, in [KB], of the buffers to use when reading and writing a file.
     */
    static public PAPropertyInteger PA_FILETRANSFER_MAX_BUFFER_SIZE = new PAPropertyInteger("proactive.filetransfer.buffer_size_kb",
                                                                                            false,
                                                                                            256);

    // -------------- DATA SPACES

    /**
     * This property indicates the access URLs to the scratch data space. If the scratch space is going to be
     * used on host, this property and/or {@link #PA_DATASPACES_SCRATCH_PATH} should be set.
     */
    static public PAPropertyList PA_DATASPACES_SCRATCH_URLS = new PAPropertyList("proactive.dataspaces.scratch_urls",
                                                                                 ",",
                                                                                 false);

    /**
     * This property indicates a location of the scratch data space. If scratch is going to be used
     * on host, this property and/or {@link #PA_DATASPACES_SCRATCH_URLS} should be set.
     */
    static public PAPropertyString PA_DATASPACES_SCRATCH_PATH = new PAPropertyString("proactive.dataspaces.scratch_path",
                                                                                     false);

    /**
     * This property sets the vfs cache type to use for all dataspaces. It can be null, default, softref, lru, weakref. Default is softref.
     *
     * @see <a href="https://commons.apache.org/proper/commons-vfs/apidocs/org/apache/commons/vfs2/cache/package-frame.html"></a>
     */
    static public PAPropertyString PA_DATASPACES_CACHE_TYPE = new PAPropertyString("proactive.dataspaces.cache_type",
                                                                                   false,
                                                                                   "softref");

    /**
     * This property sets the vfs cache strategy to use for all dataspaces. It can be onresolve, oncall or manual. Default is onresolve.
     *
     * @see <a href="https://commons.apache.org/proper/commons-vfs/apidocs/org/apache/commons/vfs2/CacheStrategy.html"></a>
     */
    static public PAPropertyString PA_DATASPACES_CACHE_STRATEGY = new PAPropertyString("proactive.dataspaces.cache_strategy",
                                                                                       false,
                                                                                       "onresolve");

    /**
     * This property disable using private key for SFTP or VSFTP dataspaces
     */
    static public PAPropertyBoolean PA_DATASPACES_SFTP_DISABLE_PRIVATEKEY = new PAPropertyBoolean("proactive.dataspaces.sftp.disablePrivateKey",
                                                                                                  false,
                                                                                                  true);

    /**
     * This property enables legacy ciphers that maybe be used by old SSH servers
     */
    static public PAPropertyList PA_DATASPACES_JSCH_ADDITIONAL_CIPHERS = new PAPropertyList("proactive.jsch.additional.ciphers",
                                                                                            ",",
                                                                                            false,
                                                                                            "aes128-cbc,aes192-cbc,aes256-cbc,3des-cbc");

    /**
     * This property enables legacy key exchange algorithms that maybe be used by old SSH servers
     */
    static public PAPropertyList PA_DATASPACES_JSCH_ADDITIONAL_KEX = new PAPropertyList("proactive.jsch.additional.kexalgorithms",
                                                                                        ",",
                                                                                        false,
                                                                                        "diffie-hellman-group-exchange-sha1,diffie-hellman-group14-sha1");

    /**
     * This property enables legacy message authentication codes that maybe be used by old SSH servers
     */
    static public PAPropertyList PA_DATASPACES_JSCH_ADDITIONAL_MACS = new PAPropertyList("proactive.jsch.additional.macs",
                                                                                         ",",
                                                                                         false);

    /**
     * This property enables legacy server host key format that maybe be used by old SSH servers
     */
    static public PAPropertyList PA_DATASPACES_JSCH_ADDITIONAL_SERVER_HOST_KEYS = new PAPropertyList("proactive.jsch.additional.host.keys",
                                                                                                     ",",
                                                                                                     false,
                                                                                                     "ssh-rsa");

    // -------------- VFS PROVIDER

    /**
     * This property indicates how often an auto closing mechanism is started to collect and close
     * all unused streams open trough file system server interface.
     */
    static public PAPropertyInteger PA_VFSPROVIDER_SERVER_STREAM_AUTOCLOSE_CHECKING_INTERVAL_MILLIS = new PAPropertyInteger("proactive.vfsprovider.server.stream_autoclose_checking_millis",
                                                                                                                            false);

    /**
     * This property indicates a period after that a stream is perceived as unused and therefore can
     * be closed by auto closing mechanism.
     */
    static public PAPropertyInteger PA_VFSPROVIDER_SERVER_STREAM_OPEN_MAXIMUM_PERIOD_MILLIS = new PAPropertyInteger("proactive.vfsprovider.server.stream_open_maximum_period_millis",
                                                                                                                    false);

    /**
     * This property indicates the number of threads used to find files in a remote ProActive virtual file system.
     * A number <= 1 indicates that parallel processing is disabled when finding files
     */
    static public PAPropertyInteger PA_VFSPROVIDER_CLIENT_FIND_FILES_THREAD_NUMBER = new PAPropertyInteger("proactive.vfsprovider.client.find_files_thread_number",
                                                                                                           false);

    /**
     * This property contains a comma-separated list of user environment variables resolved and used as root directories for the VSFTP protocol.
     * Example: "HOME,SCRATCH,WORK". Default is the HOME environment variable.
     */
    static public PAPropertyList PA_VFSPROVIDER_VSFTP_VAR_NAMES = new PAPropertyList("proactive.vfsprovider.vsftp.var_names",
                                                                                     ",",
                                                                                     false,
                                                                                     "HOME");

    /**
     * This property contains a command which will be executed on the SFTP server to resolve user variables defined in proactive.vfsprovider.vsftp.var_names. The %VAR% pattern will be replaced at execution with the variable name.
     */
    static public PAPropertyString PA_VFSPROVIDER_VSFTP_VAR_COMMAND = new PAPropertyString("proactive.vfsprovider.vsftp.var_command",
                                                                                           false,
                                                                                           "echo $%VAR%");
    // -------------- Misc

    /**
     * Indicates if a Runtime is running a functional test
     * <p/>
     * <strong>Internal use</strong>
     * This property is set to true by the functional test framework. JVM to be killed
     * after a functional test are found by using this property
     */
    static public PAPropertyBoolean PA_TEST = new PAPropertyBoolean("proactive.test", false);

    /**
     * Duration of each performance test in ms
     */
    static public PAPropertyInteger PA_TEST_PERF_DURATION = new PAPropertyInteger("proactive.test.perf.duration",
                                                                                  false,
                                                                                  30000);

    /**
     * Functional test timeout in ms
     * <p/>
     * If 0 no timeout.
     */
    static public PAPropertyInteger PA_TEST_TIMEOUT = new PAPropertyInteger("proactive.test.timeout", false, 300000);

    /**
     * TODO vlegrand Describe this property
     */
    static public PAPropertyString CATALINA_BASE = new PAPropertyString("catalina.base", true);

    /**
     * if true, any reference on the reified object within an outgoing request or reply is
     * replaced by a reference on the active object. This feature can be used when activating
     * an object whose source code cannot be modified to replace the code that return <code>this</code>
     * by the reference on the active object using <code>PAActiveObject.getStubOnThis()</code>
     */
    static public PAPropertyBoolean PA_IMPLICITGETSTUBONTHIS = new PAPropertyBoolean("proactive.implicitgetstubonthis",
                                                                                     false,
                                                                                     false);

    /**
     * on unix system, define the shell that the GCM deployment invokes when creating new runtimes.
     */
    static public PAPropertyString PA_GCMD_UNIX_SHELL = new PAPropertyString("proactive.gcmd.unix.shell",
                                                                             false,
                                                                             "/bin/sh");

    /**
     * if true, write the bytecode of the generated stub on the disk
     */
    static public PAPropertyBoolean PA_MOP_WRITESTUBONDISK = new PAPropertyBoolean("proactive.mop.writestubondisk",
                                                                                   false,
                                                                                   false);

    /**
     * Specifies the location where to write the classes generated
     * using the mop
     */
    static public PAPropertyString PA_MOP_GENERATEDCLASSES_DIR = new PAPropertyString("proactive.mop.generatedclassesdir",
                                                                                      false);

    /**
     * activate or not the ping feature in ProActive -- each time a runtime
     * starts it pings a given web server.
     */
    static public PAPropertyBoolean PA_RUNTIME_PING = new PAPropertyBoolean("proactive.runtime.ping", false, true);

    /**
     * the url to ping
     */
    static public PAPropertyString PA_RUNTIME_PING_URL = new PAPropertyString("proactive.runtime.ping.url",
                                                                              false,
                                                                              "http://pinging.activeeon.com/ping.php");

    /**
     * Add Runtime the ability to broadcast their presence on the network
     */
    static public PAPropertyBoolean PA_RUNTIME_BROADCAST = new PAPropertyBoolean("proactive.runtime.broadcast",
                                                                                 false,
                                                                                 false);

    /**
     * the address to use by the broadcast sockets
     */
    static public PAPropertyString PA_RUNTIME_BROADCAST_ADDRESS = new PAPropertyString("proactive.runtime.broadcast.address",
                                                                                       false,
                                                                                       "230.0.1.1");

    /**
     * the port to use by the broadcast sockets
     */
    static public PAPropertyInteger PA_RUNTIME_BROADCAST_PORT = new PAPropertyInteger("proactive.runtime.broadcast.port",
                                                                                      false,
                                                                                      4554);

    /**
     * the address to use by the broadcast sockets
     */
    static public PAPropertyString PA_RUNTIME_BROADCAST_CALLBACK_CLASS = new PAPropertyString("proactive.runtime.broadcast.callback.class",
                                                                                              false,
                                                                                              "org.objectweb.proactive.core.runtime.broadcast.BTCallbackDefaultImpl");

}
