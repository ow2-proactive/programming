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
package org.objectweb.proactive.extensions.vfsprovider.gui;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.DataSpacesException;
import org.objectweb.proactive.extensions.vfsprovider.FileSystemServerDeployer;


/**
 * Statically stores the information about currently available DataServers
 * <p>
 * 
 * 
 */
class DataServer {

    private static DataServer instance = null;

    private static final File serversHistoryFile = new File(System.getProperty("user.home") +
                                                            "/.proactive/dataservers.history");

    private Map<String, Server> servers = null;

    /**
     * Holds DataServer data
     */
    class Server {
        private FileSystemServerDeployer deployer = null;

        private List<String> urls = null;

        private String rootDir = null;

        private String name = null;

        private String proto = null;

        private boolean started = false;

        public String toString() {
            String str = proto + ":" + name + "@" + rootDir;
            if (started)
                str += " on " + urls;
            else
                str += " off";
            return str;
        }

        public FileSystemServerDeployer getDeployer() {
            return deployer;
        }

        public List<String> getUrls() {
            return urls;
        }

        public String getRootDir() {
            return rootDir;
        }

        public String getName() {
            return name;
        }

        public boolean isStarted() {
            return started;
        }

        public String getProtocol() {
            return this.proto;
        }

        /**
         * Start this DataServer
         * 
         * @param rebind
         *            rebind an existing object
         * @throws DataSpacesException
         *             deployment failed, or DS already started
         */
        public void start(boolean rebind) throws DataSpacesException {
            if (this.isStarted())
                throw new DataSpacesException("Server " + name + " is already running at " + urls);

            try {
                if (this.proto == null) {
                    this.deployer = new FileSystemServerDeployer(this.name, this.rootDir, true, rebind);
                } else {
                    this.deployer = new FileSystemServerDeployer(this.name, this.rootDir, true, rebind, this.proto);
                }
            } catch (Throwable e) {
                String msg = "";
                if (e.getMessage() != null && !e.getMessage().trim().equals("")) {
                    msg += ":\n" + e.getMessage();
                } else {
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        if (cause.getMessage() != null) {
                            msg += ":\n" + cause.getMessage();
                            cause = cause.getCause();
                        }
                    }
                }
                throw new DataSpacesException("Failed to deploy DataServer " + name + " at " + rootDir + msg, e);
            }

            this.urls = Arrays.asList(this.deployer.getVFSRootURLs());
            this.started = true;
        }

        /**
         * Stops this server
         * 
         * @throws DataSpacesException
         *             termination failed, or DS is not running
         */
        public void stop() throws DataSpacesException {
            if (!isStarted())
                throw new DataSpacesException("Server " + name + " is not running");

            try {
                this.deployer.terminate();
            } catch (ProActiveException e) {
                throw new DataSpacesException("Failed to terminate DataServer " + name + " at " + urls, e);
            }
            this.started = false;
            this.urls = null;
            this.deployer = null;
        }

        public Server(String rootDir, String name) {
            this(rootDir, name, null);
        }

        public Server(String rootDir, String name, String protocol) {
            this.rootDir = rootDir;
            this.name = name;
            this.started = false;
            this.proto = protocol;
        }
    }

    private DataServer() {
        servers = new HashMap<String, Server>();
    }

    /**
     * Adds a new Server to the list
     * 
     * @param rootDir
     *            the rootDir used to create the Deployer
     * @param name
     *            the name of the server represented by this deployer
     * @param rebind
     *            try to rebind an existing server
     * @param start
     *            if true, start the server, else add it in a stopped state
     * @param protocol
     *            communication protocol
     * @throws DataSpacesException
     *             DS was added but could not be started
     */
    void addServer(String rootDir, String name, boolean rebind, boolean start, String protocol)
            throws DataSpacesException {
        if (this.servers.containsKey(name))
            throw new DataSpacesException("Name " + name + " is already used");

        Server s = new Server(rootDir, name, protocol);

        if (start) {
            s.start(rebind);
        }

        this.servers.put(name, s);
    }

    /**
     * Remove the specified server, stop it if it was running
     * 
     * @param name
     *            the name of the Server to remove
     * @throws DataSpacesException
     *             DS was removed but could not be stopped
     */
    void removeServer(String name) throws DataSpacesException {
        Server s = this.servers.remove(name);

        if (s.isStarted()) {
            s.stop();
        }
    }

    /**
     * @return all the currently deployed servers
     */
    Map<String, Server> getServers() {
        return this.servers;
    }

    /**
     * @param name
     *            the name of a deployed server
     * @return the corresponding server
     */
    Server getServer(String name) {
        return this.servers.get(name);
    }

    /**
     * @return the singleton DataServers instance, cannot be null
     */
    static DataServer getInstance() {
        if (instance == null) {
            instance = new DataServer();
            instance.restoreHistory();
        }
        return instance;
    }

    private void restoreHistory() {
        String[] lines = ServerBrowser.getHistory(serversHistoryFile);
        for (int i = 0; i < lines.length; i += 3) {
            if (lines.length == i + 1)
                break;

            String proto, root, name;
            try {
                proto = lines[i];
                root = lines[i + 1];
                name = lines[i + 2];
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Warning: history parsing failure (" + e.getMessage() + ")");
                continue;
            }

            if (proto.trim().equals(""))
                proto = null;

            if (servers.containsKey(name))
                continue;

            try {
                this.addServer(root, name, false, false, proto);
            } catch (DataSpacesException e) {
                ServerBrowser.error("Failed to restore server from history: root=" + root + " name" + name, e);
            }
        }
    }

    /**
     * Terminate all running DataServers, destroy the singleton instance
     */
    public static void cleanup() {
        if (instance != null) {
            instance._cleanup();
            instance = null;
        }
    }

    private void _cleanup() {
        serversHistoryFile.delete();

        for (Server srv : this.servers.values()) {
            ServerBrowser.addHistory(serversHistoryFile, srv.getName(), true);
            ServerBrowser.addHistory(serversHistoryFile, srv.getRootDir(), true);
            String proto = srv.getProtocol();
            if (proto == null || proto.trim().equals(""))
                proto = "";
            ServerBrowser.addHistory(serversHistoryFile, proto, true);

            if (srv.isStarted()) {
                try {
                    srv.deployer.terminate();
                } catch (ProActiveException e) {
                    ServerBrowser.error("Failed to terminated DataServer " + srv.getName(), e);
                }
            }
        }
    }

}
