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
package org.objectweb.proactive.core.util.log;

/**
 * This interfaces centralizes the names of the loggers.
 *
 * @author The ProActive Team
 *
 */
public interface Loggers {
    static final public String CORE = "proactive";

    static final public String CONFIGURATION = CORE + ".configuration";

    static final public String CONFIGURATION_NETWORK = CONFIGURATION + ".network";

    static final public String CLASSLOADING = CORE + ".classloading";

    static final public String EVENTS = CORE + ".events";

    static final public String RUNTIME = CORE + ".runtime";

    static final public String NODE = RUNTIME + ".node";

    static final public String BODY = CORE + ".body";

    static final public String MOP = CORE + ".mop";

    static final public String PAPROXY = MOP + ".paproxy";

    public static final String SYNC_CALL = CORE + ".sync_call";

    static final public String GROUPS = CORE + ".groups";

    public static final String GC = CORE + ".gc";

    public static final String HTTP_TRANSPORT = CORE + ".communication.transport.http";

    public static final String REQUESTS = CORE + ".communication.requests";

    static final public String UTIL = CORE + ".util";

    static final public String LOG = CORE + ".util.log";

    static final public String XML = CORE + ".xml";

    static final public String STUB_GENERATION = CORE + ".mop.stubgeneration";

    static final public String RMI = CORE + ".communication.rmi";

    static final public String SSH = CORE + ".communication.ssh";

    static final public String SSL = CORE + ".communication.ssl";

    static final public String DEPLOYMENT = CORE + ".deployment";

    static final public String DEPLOYMENT_PROCESS = DEPLOYMENT + ".process";

    static final public String DEPLOYMENT_LOG = DEPLOYMENT + ".log";

    static final public String DEPLOYMENT_FILETRANSFER = CORE + ".deployment.filetransfer";

    static final public String FILETRANSFER = CORE + ".filetransfer";

    public static final String LOAD_BALANCING = CORE + ".loadbalancing";

    public static final String IC2D = CORE + ".ic2d";

    public static final String EXAMPLES = CORE + ".examples";

    public static final String HTTP_SERVER = CORE + ".http.server";

    public static final String HTTP_CLIENT = CORE + ".http.client";

    public static final String DSI = CORE + ".dsi";

    // Security loggers
    public static final String SECURITY = CORE + ".security";

    public static final String SECURITY_NODE = SECURITY + ".node";

    public static final String SECURITY_SESSION = SECURITY + ".session";

    public static final String SECURITY_BODY = SECURITY + ".body";

    public static final String SECURITY_MANAGER = SECURITY + ".manager";

    public static final String SECURITY_REQUEST = SECURITY + ".request";

    public static final String SECURITY_RUNTIME = SECURITY + ".runtime";

    public static final String SECURITY_DOMAIN = SECURITY + ".domain";

    public static final String SECURITY_POLICY = SECURITY + ".policy";

    public static final String SECURITY_POLICYSERVER = SECURITY + ".policyserver";

    public static final String SECURITY_CRYPTO = SECURITY + ".crypto";

    public static final String SECURITY_DESCRIPTOR = SECURITY + ".descriptor";

    // Fault-tolerance loggers
    public static final String FAULT_TOLERANCE = CORE + ".ft";

    public static final String FAULT_TOLERANCE_CIC = FAULT_TOLERANCE + ".cic";

    public static final String FAULT_TOLERANCE_PML = FAULT_TOLERANCE + ".pml";

    // Skeleton loggers
    static final public String SKELETONS = CORE + ".skeletons";

    static final public String SKELETONS_STRUCTURE = SKELETONS + ".structure";

    static final public String SKELETONS_ENVIRONMENT = SKELETONS + ".environment";

    static final public String SKELETONS_KERNEL = SKELETONS + ".taskpool";

    static final public String SKELETONS_APPLICATION = SKELETONS + ".application";

    static final public String SKELETONS_DIAGNOSIS = SKELETONS + ".diagnosis";

    static final public String SKELETONS_SYSTEM = SKELETONS + ".system";

    // remote Objects
    static final public String REMOTEOBJECT = CORE + ".remoteobject";

    public static final String CLASS_SERVER = CORE + ".class_server";

    // JMX
    static final public String JMX = CORE + ".jmx";

    static final public String JMX_MBEAN = JMX + ".mbean";

    static final public String JMX_NOTIFICATION = JMX + ".notification";

    // Message Tagging
    static final public String MESSAGE_TAGGING = CORE + ".messagetagging";

    static final public String MESSAGE_TAGGING_LOCALMEMORY = MESSAGE_TAGGING + ".localmemory";

    static final public String MESSAGE_TAGGING_LOCALMEMORY_LEASING = MESSAGE_TAGGING_LOCALMEMORY + ".leasing";

    // Exceptions
    public static final String EXCEPTIONS = CORE + ".exceptions";

    public static final String EXCEPTIONS_ONE_WAY = EXCEPTIONS + ".one_way";

    public static final String EXCEPTIONS_SEND_REPLY = EXCEPTIONS + ".send_reply";

    public static final String WEB_SERVICES = CORE + ".webservices";

    public static final String UTILS = CORE + ".utils";

    public static final String SLEEPER = UTILS + ".sleeper";

    public static final String WAITER = UTILS + ".waiter";

    // Data Spaces
    public static final String DATASPACES = CORE + ".dataspaces";

    public static final String DATASPACES_VFS = DATASPACES + ".vfs";

    public static final String DATASPACES_MOUNT_MANAGER = DATASPACES + ".mountmanager";

    public static final String DATASPACES_CONFIGURATOR = DATASPACES + ".configurator";

    public static final String DATASPACES_NAMING_SERVICE = DATASPACES + ".namingservice";

    // VFS Provider
    public static final String VFS_PROVIDER = CORE + ".vfsprovider";

    public static final String VFS_PROVIDER_SERVER = VFS_PROVIDER + ".server";

    //Native code-wrapping loggers
    static final public String NATIVE = CORE + ".native";

    static final public String NATIVE_CONTROL = NATIVE + ".control";

    static final public String NATIVE_CONTROL_MANAGER = NATIVE_CONTROL + ".manager";

    static final public String NATIVE_CONTROL_COUPLING = NATIVE_CONTROL + ".coupling";

    // OSProcess builder
    static final public String OSPB = CORE + ".ospb";
}
