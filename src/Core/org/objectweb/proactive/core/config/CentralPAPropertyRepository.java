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
package org.objectweb.proactive.core.config;

import java.net.Socket;

import org.objectweb.proactive.core.config.PAProperties.PAPropertiesLoaderSPI;
import org.objectweb.proactive.core.filetransfer.FileTransferService;
import org.objectweb.proactive.core.runtime.broadcast.BTCallbackDefaultImpl;
import org.objectweb.proactive.utils.OperatingSystem;


/**
 * The central repository of PAProperty
 *
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
     * If IPv6 is available on the operating system the default preference is to prefer an IPv4-mapped address over an IPv6 address
     */
    static public PAPropertyBoolean PREFER_IPV6_ADDRESSES = new PAPropertyBoolean(
        "java.net.preferIPv6Addresses", true);

    /**
     * If IPv6 is available on the operating system the underlying native socket will be an IPv6 socket. This allows Java(tm) applications to connect too, and accept connections from, both IPv4 and IPv6 hosts.
     */
    static public PAPropertyBoolean PREFER_IPV4_STACK = new PAPropertyBoolean("java.net.preferIPv4Stack",
        true);

    /**
     * Indicate the GCM provider class, to the ProActive implementation of
     * Fractal/GCM set it to org.objectweb.proactive.core.component.Fractive
     */
    static public PAPropertyString GCM_PROVIDER = new PAPropertyString("gcm.provider", true);

    /**
     * Indicate the Fractal provider class, to the ProActive implementation of
     * Fractal/GCM set it to org.objectweb.proactive.core.component.Fractive
     */
    static public PAPropertyString FRACTAL_PROVIDER = new PAPropertyString("fractal.provider", true);

    static public PAPropertyString JAVA_SECURITY_AUTH_LOGIN_CONFIG = new PAPropertyString(
        "java.security.auth.login.config", true);

    static public PAPropertyString JAVAX_XML_TRANSFORM_TRANSFORMERFACTORY = new PAPropertyString(
        "javax.xml.transform.TransformerFactory", true);

    /* ------------------------------------
     *  PROACTIVE
     */

    /**
     * ProActive Configuration file location
     *
     * If set ProActive will load the configuration file at the given location.
     */
    static public PAPropertyString PA_CONFIGURATION_FILE = new PAPropertyString("proactive.configuration",
        false);

    /**
     * Indicates where ProActive is installed
     *
     * Can be useful to write generic deployment descriptor by using a JavaPropertyVariable
     * to avoid hard coded path.
     *
     * Used in unit and functional tests
     */
    static public PAPropertyString PA_HOME = new PAPropertyString("proactive.home", false);

    /**
     * Indicates what is the operating system of the local machine
     *
     * It is not redundant with "os.name" since the only valid values those from <code>OperatingSystem</code>.
     * Often users are only interested to know if the computer is running unix or windows.
     *
     * @see OperatingSystem
     */
    static public PAPropertyString PA_OS = new PAPropertyString("proactive.os", true);

    /**
     * Log4j configuration file location
     *
     * If set the specified log4j configuration file is used. Otherwise the default one,
     * Embedded in the ProActive jar is used.
     */
    static public PAPropertyString LOG4J = new PAPropertyString("log4j.configuration", false);

    /**
     * URI of the remote log collector
     *
     */
    static public PAPropertyString PA_LOG4J_COLLECTOR = new PAPropertyString("proactive.log4j.collector",
        false);

    /**
     * Qualified name of the flushing provider to use
     */
    static public PAPropertyString PA_LOG4J_APPENDER_PROVIDER = new PAPropertyString(
        "proactive.log4j.appender.provider", false);

    /**
     * Specifies the name of the ProActive Runtime
     *
     * By default a random name is assigned to a ProActive Runtime. This property allows
     * to choose the name of the Runtime to be able to perform lookups.
     *
     * By default, the name of a runtime starts with PA_JVM</strong>
     */
    static public PAPropertyString PA_RUNTIME_NAME = new PAPropertyString("proactive.runtime.name", false);

    /**
     * this property should be used when one wants to start only a runtime without an additional main class
     */
    static public PAPropertyBoolean PA_RUNTIME_STAYALIVE = new PAPropertyBoolean(
        "proactive.runtime.stayalive", false);

    /**
     * Terminates the Runtime when the Runtime becomes empty
     *
     * If true, when all bodies have been terminated the ProActive Runtime will exit
     */
    static public PAPropertyBoolean PA_EXIT_ON_EMPTY = new PAPropertyBoolean("proactive.exit_on_empty", false);

    /**
     * Boolean to activate automatic continuations for this runtime.
     */
    static public PAPropertyBoolean PA_FUTURE_AC = new PAPropertyBoolean("proactive.future.ac", false);

    /**
     * Timeout value for future in synchronous requests.
     * can be used to set timeout on synchronous calls. Impossible otherwise 
     * default value 0, no timeout
     */
    static public PAPropertyLong PA_FUTURE_SYNCHREQUEST_TIMEOUT = new PAPropertyLong(
        "proactive.future.synchrequest.timeout", false, 0);

    /**
     * Period of the future monitoring ping, in milliseconds
     *
     * If set to 0, then future monitoring is disabled
     */
    static public PAPropertyInteger PA_FUTUREMONITORING_TTM = new PAPropertyInteger(
        "proactive.futuremonitoring.ttm", false);

    /**
     * Include client side calls in stack traces
     */
    static public PAPropertyBoolean PA_STACKTRACE = new PAPropertyBoolean("proactive.stack_trace", false);

    /**
     * Activates the legacy SAX ProActive Descriptor parser
     *
     * To check if the new JAXP parser introduced regressions.
     */
    static public PAPropertyBoolean PA_LEGACY_PARSER = new PAPropertyBoolean("proactive.legacy.parser", false);

    /* ------------------------------------
     *  NETWORK
     */

    /**
     * ProActive Communication protocol
     *
     * Suppported values are: rmi, rmissh, ibis, http
     */
    static public PAPropertyString PA_COMMUNICATION_PROTOCOL = new PAPropertyString(
        "proactive.communication.protocol", false);

    /**
     * ProActive Runtime Hostname (or IP Address)
     *
     * This option can be used to set manually the Runtime IP Address. Can be
     * useful when the Java networking stack return a bad IP address (example: multihomed machines).
     *
     */
    static public PAPropertyString PA_HOSTNAME = new PAPropertyString("proactive.hostname", false);

    /**
     * Toggle DNS resolution
     *
     * When true IP addresses are used instead of FQDNs. Can be useful with misconfigured DNS servers
     * or strange /etc/resolv.conf files. FQDNs passed by user or 3rd party tools are resolved and converted
     * into IP addresses
     *
     */
    static public PAPropertyBoolean PA_NET_USE_IP_ADDRESS = new PAPropertyBoolean("proactive.useIPaddress",
        false);

    /** Enable or disable IPv6
     *
     */
    static public PAPropertyBoolean PA_NET_DISABLE_IPv6 = new PAPropertyBoolean("proactive.net.disableIPv6",
        false);

    /**
     * Toggle loopback IP address usage
     *
     * When true loopback IP address usage is avoided. Since Remote adapters contain only one
     * endpoint the right IP address must be used. This property must be set to true if a loopback
     * address is returned by the Java INET stack.
     *
     * If only a loopback address exists, it is used.
     */
    static public PAPropertyBoolean PA_NET_NOLOOPBACK = new PAPropertyBoolean("proactive.net.nolocal", false);

    /**
     * Toggle Private IP address usage
     *
     * When true private IP address usage is avoided. Since Remote adapters contain only one
     * endpoint the right IP address must be used. This property must be set to true if a private
     * address is returned by the Java INET stack and this private IP is not reachable by other hosts.
     */
    static public PAPropertyBoolean PA_NET_NOPRIVATE = new PAPropertyBoolean("proactive.net.noprivate", false);

    /**
     * Select the network interface
     */
    static public PAPropertyString PA_NET_INTERFACE = new PAPropertyString("proactive.net.interface", false);

    /** Select the netmask to use (xxx.xxx.xxx.xxx/xx)
     *
     * Does not work with IPv6 addresses
     */
    static public PAPropertyString PA_NET_NETMASK = new PAPropertyString("proactive.net.netmask", false);

    /**
     * RMI/SSH black voodoo
     *
     * Can be used to fix broken networks (multihomed, broken DNS etc.). You probably want
     * to post on the public ProActive mailing list before using this property.
     */
    static public PAPropertyString PA_NET_SECONDARYNAMES = new PAPropertyString(
        "proactive.net.secondaryNames", false);

    static public PAPropertyBoolean SCHEMA_VALIDATION = new PAPropertyBoolean("schema.validation", true);

    /** SSL cipher suites used for RMISSL communications.
     * List of cipher suites used for RMISSL, separated by commas.
     * default is SSL_DH_anon_WITH_RC4_128_MD5. This cipher suite is used only
     * to have encrypted communications, without authentication, and works with default
     * JVM's keyStore/TrustStore
     *
     * Many others can be used. for implementing a certificate authentication...
     * see http://java.sun.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html
     *
     * */
    static public PAPropertyString PA_SSL_CIPHER_SUITES = new PAPropertyString("proactive.ssl.cipher.suites",
        false);

    /* ------------------------------------
     *  RMI
     */

    /**
     * Assigns a TCP port to RMI
     *
     * this property identifies the default port used by the RMI communication protocol
     */
    static public PAPropertyInteger PA_RMI_PORT = new PAPropertyInteger("proactive.rmi.port", false);

    static public PAPropertyString JAVA_RMI_SERVER_CODEBASE = new PAPropertyString(
        "java.rmi.server.codebase", true);

    static public PAPropertyBoolean JAVA_RMI_SERVER_USECODEBASEONLY = new PAPropertyBoolean(
        "java.rmi.server.useCodebaseOnly", true, false);

    /**
     * Sockets used by the RMI remote object factory connect to the remote server
     * with a specified timeout value. A timeout of zero is interpreted as an infinite timeout.
     * The connection will then block until established or an error occurs.
     */
    static public PAPropertyInteger PA_RMI_CONNECT_TIMEOUT = new PAPropertyInteger(
        "proactive.rmi.connect_timeout", false);

    static public PAPropertyString PA_CODEBASE = new PAPropertyString("proactive.codebase", true);

    static public PAPropertyBoolean PA_CLASSLOADING_USEHTTP = new PAPropertyBoolean(
        "proactive.classloading.useHTTP", false);
    /* ------------------------------------
     *  HTTP
     */

    /**
     * Assigns a TCP port to XML-HTTP
     *
     * this property identifies the default port for the xml-http protocol
     */
    static public PAPropertyInteger PA_XMLHTTP_PORT = new PAPropertyInteger("proactive.http.port", false);

    /**
     * Define a Connector to be used by Jetty
     *
     * By default a SelectChannelConnector is used. It is well suited to handle a lot
     * of mainly idle clients workload (like coarse grained master worker). If you have a
     * few very busy client better performances can be achieved by using a SocketConnect
     *
     * You can use a SocketConnect, a BlockingChannelConnector or a SelectChannelConnector
     * You CANNOT use a SSL connector.
     * Click
     * <a  href="http://docs.codehaus.org/display/JETTY/Architecture">here</a> for more
     * information on the Jetty architecture.
     */
    static public PAPropertyString PA_HTTP_JETTY_CONNECTOR = new PAPropertyString(
        "proactive.http.jetty.connector", false);

    /**
     * Jetty configuration file
     *
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
    static public PAPropertyInteger PA_HTTP_CONNECT_TIMEOUT = new PAPropertyInteger(
        "proactive.http.connect_timeout", false);

    /* ------------------------------------
     *  COMPONENTS
     */

    /** Timeout in seconds for parallel creation of components */
    static public PAPropertyInteger PA_COMPONENT_CREATION_TIMEOUT = new PAPropertyInteger(
        "components.creation.timeout", false);

    /** If 'true', the component framework should optimize communication between component using shortcut mechanism */
    static public PAPropertyBoolean PA_COMPONENT_USE_SHORTCUTS = new PAPropertyBoolean(
        "proactive.components.use_shortcuts", false);

    /* ------------------------------------
     *  MIGRATION
     */

    /** The class or interface of the location server to be looked up */
    static public PAPropertyString PA_LOCATION_SERVER = new PAPropertyString("proactive.locationserver",
        false);

    /** The bind name of a location server, used during lookup */
    static public PAPropertyString PA_LOCATION_SERVER_RMI = new PAPropertyString(
        "proactive.locationserver.rmi", false);

    /** The lifetime (in seconds) of a forwarder left after a migration when using the mixed location scheme */
    static public PAPropertyInteger PA_MIXEDLOCATION_TTL = new PAPropertyInteger(
        "proactive.mixedlocation.ttl", false);

    /** If set to true, a forwarder will send an update to a location server when reaching
     * the end of its lifetime */
    static public PAPropertyBoolean PA_MIXEDLOCATION_UPDATINGFORWARDER = new PAPropertyBoolean(
        "proactive.mixedlocation.updatingForwarder", false);

    /** The maximum number of migration allowed before an object must send its new location to a location server */
    static public PAPropertyInteger PA_MIXEDLOCATION_MAXMIGRATIONNB = new PAPropertyInteger(
        "proactive.mixedlocation.maxMigrationNb", false);

    /** The maximum time (in seconds) an object can spend on a site before updating its location to a location server */
    static public PAPropertyInteger PA_MIXEDLOCATION_MAXTIMEONSITE = new PAPropertyInteger(
        "proactive.mixedlocation.maxTimeOnSite", false);

    /* ------------------------------------
     *  RMISSH
     */

    /** this property identifies the location of RMISSH key directory */
    static public PAPropertyString PA_RMISSH_KEY_DIR = new PAPropertyString(
        "proactive.communication.rmissh.key_directory", false);

    /** this property identifies that when using SSH tunneling, a normal connection should be tried before tunneling */
    static public PAPropertyBoolean PA_RMISSH_TRY_NORMAL_FIRST = new PAPropertyBoolean(
        "proactive.communication.rmissh.try_normal_first", false);

    /** this property identifies the SSH garbage collector period
     *
     * If set to 0, tunnels and connections are not garbage collected
     */
    static public PAPropertyInteger PA_RMISSH_GC_PERIOD = new PAPropertyInteger(
        "proactive.communication.rmissh.gc_period", false);

    /** this property identifies the maximum idle time before a SSH tunnel or a connection is garbage collected */
    static public PAPropertyInteger PA_RMISSH_GC_IDLETIME = new PAPropertyInteger(
        "proactive.communication.rmissh.gc_idletime", false);

    /** this property identifies the know hosts file location when using ssh tunneling
     *  if undefined, the default value is user.home property concatenated to SSH_TUNNELING_DEFAULT_KNOW_HOSTS
     */
    static public PAPropertyString PA_RMISSH_KNOWN_HOSTS = new PAPropertyString(
        "proactive.communication.rmissh.known_hosts", false);

    /** Sock connect timeout, in ms
     *
     * The timeout to be used when a SSH Tunnel is opened. 0 is interpreted
     * as an infinite timeout. This timeout is also used for plain socket when try_normal_first is set to true
     *
     * @see Socket
     */
    static public PAPropertyInteger PA_RMISSH_CONNECT_TIMEOUT = new PAPropertyInteger(
        "proactive.communication.rmissh.connect_timeout", false);

    // Not documented, temporary workaround until 4.3.0
    static public PAPropertyString PA_RMISSH_REMOTE_USERNAME = new PAPropertyString(
        "proactive.communication.rmissh.username", false);

    // Not documented, temporary workaround until 4.3.0
    static public PAPropertyInteger PA_RMISSH_REMOTE_PORT = new PAPropertyInteger(
        "proactive.communication.rmissh.port", false);

    /* ------------------------------------
     *  REMOTE OBJECT - MULTI-PROTOCOL
     */

    /** Expose object using these protocols in addition to the default one (protocols separated by comma) */
    public static PAPropertyString PA_COMMUNICATION_ADDITIONAL_PROTOCOLS = new PAPropertyString(
        "proactive.communication.additional_protocols", false);

    /** Impose a static order for protocols selection, this automatically desactivate benchmark */
    public static PAPropertyString PA_COMMUNICATION_PROTOCOLS_ORDER = new PAPropertyString(
        "proactive.communication.protocols.order", false);

    /** Specify a parameter for benchmark */
    public static PAPropertyString PA_BENCHMARK_PARAMETER = new PAPropertyString(
        "proactive.communication.benchmark.parameter", false);

    /** The class to use for doing remoteObject Benchmark, must implement BenchmarkObject */
    public static PAPropertyString PA_BENCHMARK_CLASS = new PAPropertyString(
        "proactive.communication.benchmark.class", false,
        org.objectweb.proactive.core.remoteobject.benchmark.SelectionOnly.class.getName());

    /* ------------------------------------
     *  SECURITY
     */

    /** this property indicates if a RMISecurityManager has to be instanciated*/
    static public PAPropertyBoolean PA_SECURITYMANAGER = new PAPropertyBoolean("proactive.securitymanager",
        false);

    /** this property indicates the location of the runtime' security manager configuration file */
    static public PAPropertyString PA_RUNTIME_SECURITY = new PAPropertyString("proactive.runtime.security",
        false);

    /** this property indicates the url of the security domain the runtime depends on */
    static public PAPropertyString PA_RUNTIME_DOMAIN_URL = new PAPropertyString(
        "proactive.runtime.domain.url", false);

    /* ------------------------------------
     *  TIMIT
     */

    /** this property indicates the list (comma separated) of the TimIt counters to activate */
    static public PAPropertyString PA_TIMIT_ACTIVATION = new PAPropertyString("proactive.timit.activation",
        false);

    /* ------------------------------------
     *  MASTER/WORKER
     */

    /**
     * Master/Worker ping period in milliseconds
     *
     * The ping period is the default interval at which workers receive a ping message
     * (to check if they're alive). Default to ten seconds
     */
    static public PAPropertyInteger PA_MASTERWORKER_PINGPERIOD = new PAPropertyInteger(
        "proactive.masterworker.pingperiod", false);
    /**
     * Master/Worker compress tasks
     *
     * Parameter which decides wether tasks should be compressed when they are saved inside the task repository
     * compressing increases CPU usage on the master side, but decrease memory usage, default to false
     */
    static public PAPropertyBoolean PA_MASTERWORKER_COMPRESSTASKS = new PAPropertyBoolean(
        "proactive.masterworker.compresstasks", false);

    /* ------------------------------------
     *  DISTRIBUTED GARBAGE COLLECTOR
     */

    /** Enable the distributed garbage collector */
    static public PAPropertyBoolean PA_DGC = new PAPropertyBoolean("proactive.dgc", false);

    /**
     * TimeToAlone
     * After this delay, we suppose we got a message from all our referencers.
     */
    static public PAPropertyInteger PA_DGC_TTA = new PAPropertyInteger("proactive.dgc.tta", false);

    /**
     * TimeToBroadcast
     * Time is always in milliseconds. It is fundamental for this value
     * to be the same in all JVM of the distributed system, so think twice
     * before changing it.
     */
    static public PAPropertyInteger PA_DGC_TTB = new PAPropertyInteger("proactive.dgc.ttb", false);

    /* ------------------------------------
     *  DISTRIBUTED DEBUGGER
     */

    /** Enable the distributed debugger */
    static public PAPropertyBoolean PA_DEBUG = new PAPropertyBoolean("proactive.debug", false);

    /* ------------------------------------
     *  MESSAGE TAGGING
     */
    /** Set the max period for LocalMemoryTag lease time */
    static public PAPropertyInteger PA_MAX_MEMORY_TAG_LEASE = new PAPropertyInteger(
        "proactive.tagmemory.lease.max", false);

    /** Set the Period of the running thread for tag memory leasing check */
    static public PAPropertyInteger PA_MEMORY_TAG_LEASE_PERIOD = new PAPropertyInteger(
        "proactive.tagmemory.lease.period", false);

    /** Enable or disable the Distributed Service ID Tag */
    static public PAPropertyBoolean PA_TAG_DSF = new PAPropertyBoolean("proactive.tag.dsf", false);

    /* ------------------------------------
     *  FILE TRANSFER
     */

    /**
     * The maximum number of {@link FileTransferService} objects that can be spawned
     * on a Node to handle file transfer requests in parallel.
     */
    static public PAPropertyInteger PA_FILETRANSFER_MAX_SERVICES = new PAPropertyInteger(
        "proactive.filetransfer.services_number", false);

    /**
     * When sending a file, the maximum number of file blocks (parts) that can
     * be sent asynchronously before blocking for their arrival.
     */
    static public PAPropertyInteger PA_FILETRANSFER_MAX_SIMULTANEOUS_BLOCKS = new PAPropertyInteger(
        "proactive.filetransfer.blocks_number", false);

    /**
     * The size, in [KB], of file blocks (parts) used to send files.
     */
    static public PAPropertyInteger PA_FILETRANSFER_MAX_BLOCK_SIZE = new PAPropertyInteger(
        "proactive.filetransfer.blocks_size_kb", false);

    /**
     * The size, in [KB], of the buffers to use when reading and writing a file.
     */
    static public PAPropertyInteger PA_FILETRANSFER_MAX_BUFFER_SIZE = new PAPropertyInteger(
        "proactive.filetransfer.buffer_size_kb", false);

    // -------------- DATA SPACES

    /**
     * This property indicates an access URL to the scratch data space. If scratch is going to be
     * used on host, this property and/or {@link #PA_DATASPACES_SCRATCH_PATH} should be set.
     */
    static public PAPropertyString PA_DATASPACES_SCRATCH_URL = new PAPropertyString(
        "proactive.dataspaces.scratch_url", false);

    /**
     * This property indicates a location of the scratch data space. If scratch is going to be used
     * on host, this property and/or {@link #PA_DATASPACES_SCRATCH_URL} should be set.
     */
    static public PAPropertyString PA_DATASPACES_SCRATCH_PATH = new PAPropertyString(
        "proactive.dataspaces.scratch_path", false);

    // -------------- VFS PROVIDER

    /**
     * This property indicates how often an auto closing mechanism is started to collect and close
     * all unused streams open trough file system server interface.
     */
    static public PAPropertyInteger PA_VFSPROVIDER_SERVER_STREAM_AUTOCLOSE_CHECKING_INTERVAL_MILLIS = new PAPropertyInteger(
        "proactive.vfsprovider.server.stream_autoclose_checking_millis", false);

    /**
     * This property indicates a period after that a stream is perceived as unused and therefore can
     * be closed by auto closing mechanism.
     */
    static public PAPropertyInteger PA_VFSPROVIDER_SERVER_STREAM_OPEN_MAXIMUM_PERIOD_MILLIS = new PAPropertyInteger(
        "proactive.vfsprovider.server.stream_open_maximum_period_millis", false);

    // -------------- Misc

    /**
     * Indicates if a Runtime is running a functional test
     *
     * <strong>Internal use</strong>
     * This property is set to true by the functional test framework. JVM to be killed
     * after a functional test are found by using this property
     */
    static public PAPropertyBoolean PA_TEST = new PAPropertyBoolean("proactive.test", false);

    /** Duration of each performance test in ms */
    static public PAPropertyInteger PA_TEST_PERF_DURATION = new PAPropertyInteger(
        "proactive.test.perf.duration", false);

    /**
     * Functional test timeout in ms
     *
     * If 0 no timeout.
     */
    static public PAPropertyInteger PA_TEST_TIMEOUT = new PAPropertyInteger("proactive.test.timeout", false,
        300000);

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
    static public PAPropertyBoolean PA_IMPLICITGETSTUBONTHIS = new PAPropertyBoolean(
        "proactive.implicitgetstubonthis", false);

    /**
     * on unix system, define the shell that the GCM deployment invokes when creating new runtimes.
     */
    static public PAPropertyString PA_GCMD_UNIX_SHELL = new PAPropertyString("proactive.gcmd.unix.shell",
        false);

    /**
     * Web services framework
     *
     * Suppported value is cxf
     */
    static public PAPropertyString PA_WEBSERVICES_FRAMEWORK = new PAPropertyString(
        "proactive.webservices.framework", false);

    /**
     * Web services: WSDL elementFormDefault attribute
     *
     * When creating a web service, the generated WSDL contains an XSD schema which represents
     * the SOAP message format. This property allows to set the elementFormDefault of this schema
     * to "qualified" or "unqualified". It is set to false ("unqualified") by default.
     */
    static public PAPropertyBoolean PA_WEBSERVICES_ELEMENTFORMDEFAULT = new PAPropertyBoolean(
        "proactive.webservices.elementformdefault", false);

    /**
     * if true, write the bytecode of the generated stub on the disk
     *
     */
    static public PAPropertyBoolean PA_MOP_WRITESTUBONDISK = new PAPropertyBoolean(
        "proactive.mop.writestubondisk", false);

    /**
      * Specifies the location where to write the classes generated
      * using the mop
      */
    static public PAPropertyString PA_MOP_GENERATEDCLASSES_DIR = new PAPropertyString(
        "proactive.mop.generatedclassesdir", false);

    /**
     * activate or not the ping feature in ProActive -- each time a runtime
     * starts it pings a given web server.
     */
    static public PAPropertyBoolean PA_RUNTIME_PING = new PAPropertyBoolean("proactive.runtime.ping", false,
        true);
    /**
     * the url to ping
     */
    static public PAPropertyString PA_RUNTIME_PING_URL = new PAPropertyString("proactive.runtime.ping.url",
        false, "http://pinging.activeeon.com/ping.php");

    /**
     * Add Runtime the ability to broadcast their presence on the network
     */
    static public PAPropertyBoolean PA_RUNTIME_BROADCAST = new PAPropertyBoolean(
        "proactive.runtime.broadcast", false, false);
    /**
     * the address to use by the broadcast sockets
     */
    static public PAPropertyString PA_RUNTIME_BROADCAST_ADDRESS = new PAPropertyString(
        "proactive.runtime.broadcast.address", false, "230.0.1.1");

    /**
     * the port to use by the broadcast sockets
     */
    static public PAPropertyInteger PA_RUNTIME_BROADCAST_PORT = new PAPropertyInteger(
        "proactive.runtime.broadcast.port", false, 4554);

    /**
     * the address to use by the broadcast sockets
     */
    static public PAPropertyString PA_RUNTIME_BROADCAST_CALLBACK_CLASS = new PAPropertyString(
        "proactive.runtime.broadcast.callback.class", false, BTCallbackDefaultImpl.class.getName());

}
