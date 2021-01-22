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
package org.objectweb.proactive.extensions.vfsprovider.client.vsftp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.AbstractFileName;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystem;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;


/**
 * @author ActiveEon Team
 * @since 07/01/2021
 */
public class VSftpFileSystem extends AbstractFileSystem {

    private static final Log LOG = LogFactory.getLog(SftpFileSystem.class);

    private static final int UNIDENTIFED = -1;

    private static final int SLEEP_MILLIS = 100;

    private static final int EXEC_BUFFER_SIZE = 128;

    private static final long LAST_MOD_TIME_ACCURACY = 1000L;

    /**
     * Session; never null.
     * <p>
     * DCL pattern requires that the ivar be volatile.
     * </p>
     */
    private volatile Session session;

    private volatile ChannelSftp idleChannel;

    private final int connectTimeoutMillis;

    /**
     * Cache for the user ID (-1 when not set)
     * <p>
     * DCL pattern requires that the ivar be volatile.
     * </p>
     */
    private volatile int uid = UNIDENTIFED;

    /**
     * Cache for the user groups ids (null when not set)
     * <p>
     * DCL pattern requires that the ivar be volatile.
     * </p>
     */
    private volatile int[] groupsIds;

    /**
     * Some SFTP-only servers disable the exec channel. When exec is disabled, things like getUId() will always fail.
     */
    private final boolean execDisabled;

    private Map<String, String> environmentVariables = new LinkedHashMap<>();

    protected VSftpFileSystem(final GenericFileName rootName, final Session session,
            final FileSystemOptions fileSystemOptions) {
        super(rootName, null, fileSystemOptions);
        this.session = Objects.requireNonNull(session, "session");
        this.connectTimeoutMillis = SftpFileSystemConfigBuilder.getInstance()
                                                               .getConnectTimeoutMillis(fileSystemOptions);

        if (SftpFileSystemConfigBuilder.getInstance().isDisableDetectExecChannel(fileSystemOptions)) {
            this.execDisabled = true;
        } else {
            this.execDisabled = detectExecDisabled();
        }
    }

    @Override
    protected void doCloseCommunicationLink() {
        if (idleChannel != null) {
            synchronized (this) {
                if (idleChannel != null) {
                    idleChannel.disconnect();
                    idleChannel = null;
                }
            }
        }

        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * Returns an SFTP channel to the server.
     *
     * @return new or reused channel, never null.
     * @throws FileSystemException if a session cannot be created.
     * @throws IOException         if an I/O error is detected.
     */
    protected ChannelSftp getChannel() throws IOException {
        try {
            // Use the pooled channel, or create a new one
            ChannelSftp channel = null;
            if (idleChannel != null) {
                synchronized (this) {
                    if (idleChannel != null) {
                        channel = idleChannel;
                        idleChannel = null;
                    }
                }
            }

            if (channel == null) {
                channel = (ChannelSftp) getSession().openChannel("sftp");
                channel.connect(connectTimeoutMillis);
                final Boolean userDirIsRoot = false;
                // In VSFTP the following setting is ignored
                //                final Boolean userDirIsRoot = SftpFileSystemConfigBuilder.getInstance()
                //                                                                         .getUserDirIsRoot(getFileSystemOptions());
                final String workingDirectory = getRootName().getPath();
                if (workingDirectory != null && (userDirIsRoot == null || !userDirIsRoot.booleanValue())) {
                    try {
                        channel.cd(workingDirectory);
                    } catch (final SftpException e) {
                        throw new FileSystemException("vfs.provider.sftp/change-work-directory.error",
                                                      workingDirectory,
                                                      e);
                    }
                }
            }

            final String fileNameEncoding = SftpFileSystemConfigBuilder.getInstance()
                                                                       .getFileNameEncoding(getFileSystemOptions());

            if (fileNameEncoding != null) {
                try {
                    channel.setFilenameEncoding(fileNameEncoding);
                } catch (final SftpException e) {
                    throw new FileSystemException("vfs.provider.sftp/filename-encoding.error", fileNameEncoding);
                }
            }
            return channel;
        } catch (final JSchException e) {
            throw new FileSystemException("vfs.provider.sftp/connect.error", getRootName(), e);
        }
    }

    /**
     * Ensures that the session link is established.
     *
     * @throws FileSystemException if a session cannot be created.
     */
    private Session getSession() throws FileSystemException {
        if (!this.session.isConnected()) {
            synchronized (this) {
                if (!this.session.isConnected()) {
                    doCloseCommunicationLink();
                    this.session = VSftpFileProvider.createSession((GenericFileName) getRootName(),
                                                                   getFileSystemOptions());
                }
            }
        }
        return this.session;
    }

    /**
     * Returns a channel to the pool.
     *
     * @param channel the used channel.
     */
    protected void putChannel(final ChannelSftp channel) {
        if (idleChannel == null) {
            synchronized (this) {
                if (idleChannel == null) {
                    // put back the channel only if it is still connected
                    if (channel.isConnected() && !channel.isClosed()) {
                        idleChannel = channel;
                    }
                } else {
                    channel.disconnect();
                }
            }
        } else {
            channel.disconnect();
        }
    }

    /**
     * Adds the capabilities of this file system.
     */
    @Override
    protected void addCapabilities(final Collection<Capability> caps) {
        caps.addAll(VSftpFileProvider.capabilities);
    }

    /**
     * Creates a file object. This method is called only if the requested file is not cached.
     */
    @Override
    protected FileObject createFile(final AbstractFileName name) throws FileSystemException {
        return new VSftpFileObject(name, this);
    }

    /**
     * Last modification time is only an int and in seconds, thus can be off by 999.
     *
     * @return 1000
     */
    @Override
    public double getLastModTimeAccuracy() {
        return LAST_MOD_TIME_ACCURACY;
    }

    public synchronized Map<String, String> getEnvironmentVariables() throws IOException {
        if (environmentVariables.isEmpty()) {
            Log logger = getLogger();
            List<String> varNames = CentralPAPropertyRepository.PA_VFSPROVIDER_VSFTP_VAR_NAMES.getValue();
            String baseCommand = CentralPAPropertyRepository.PA_VFSPROVIDER_VSFTP_VAR_COMMAND.getValue();
            if (varNames.isEmpty()) {
                throw new FileSystemException("Value of property " +
                                              CentralPAPropertyRepository.PA_VFSPROVIDER_VSFTP_VAR_NAMES.getName() +
                                              " is incorrect (empty)");
            }
            for (String var : varNames) {
                if (!var.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    throw new FileSystemException("Value of variable " + var + " in property " +
                                                  CentralPAPropertyRepository.PA_VFSPROVIDER_VSFTP_VAR_NAMES.getName() +
                                                  " is incorrect");
                }
                final StringBuilder output = new StringBuilder();
                final String command = baseCommand.replace("%VAR%", var);

                if (logger.isTraceEnabled()) {
                    logger.trace("Running command: " + command);
                }
                final int code;
                try {
                    code = executeCommand(command, output);
                } catch (JSchException e) {
                    throw new FileSystemException("Could not get the environment variable " + var, e);
                }
                if (code != 0) {
                    throw new FileSystemException("Could not get the environment variable " + var + " (error code: " +
                                                  code + ")");
                }
                String value = output.toString().trim();
                if (logger.isTraceEnabled()) {
                    logger.trace("Result: " + value);
                }
                if (value.isEmpty()) {
                    throw new FileSystemException("Value of environment variable " + var + " is empty");
                }
                environmentVariables.put("$" + var, value);

            }
        }
        return environmentVariables;

    }

    /**
     * Gets the (numeric) group IDs.
     *
     * @return the (numeric) group IDs.
     * @throws JSchException If a problem occurs while retrieving the group IDs.
     * @throws IOException   if an I/O error is detected.
     * @since 2.1
     */
    public int[] getGroupsIds() throws JSchException, IOException {
        if (groupsIds == null) {
            synchronized (this) {
                // DCL pattern requires that the ivar be volatile.
                if (groupsIds == null) {
                    final StringBuilder output = new StringBuilder();
                    final int code = executeCommand("id -G", output);
                    if (code != 0) {
                        throw new JSchException("Could not get the groups id of the current user (error code: " + code +
                                                ")");
                    }
                    // Retrieve the different groups
                    final String[] groups = output.toString().trim().split("\\s+");

                    final int[] groupsIds = new int[groups.length];
                    for (int i = 0; i < groups.length; i++) {
                        groupsIds[i] = Integer.parseInt(groups[i]);
                    }
                    this.groupsIds = groupsIds;

                }
            }
        }
        return groupsIds;
    }

    /**
     * Gets the (numeric) group IDs.
     *
     * @return The numeric user ID
     * @throws JSchException If a problem occurs while retrieving the group ID.
     * @throws IOException   if an I/O error is detected.
     * @since 2.1
     */
    public int getUId() throws JSchException, IOException {
        if (uid == UNIDENTIFED) {
            synchronized (this) {
                if (uid == UNIDENTIFED) {
                    final StringBuilder output = new StringBuilder();
                    final int code = executeCommand("id -u", output);
                    if (code != 0) {
                        throw new FileSystemException("Could not get the user id of the current user (error code: " +
                                                      code + ")");
                    }
                    final String uidString = output.toString().trim();
                    try {
                        uid = Integer.parseInt(uidString);
                    } catch (final NumberFormatException e) {
                        LOG.debug("Cannot convert UID to integer: '" + uidString + "'", e);
                    }
                }
            }
        }
        return uid;
    }

    /**
     * Executes a command and returns the (standard) output through a StringBuilder.
     *
     * @param command The command
     * @param output  The output
     * @return The exit code of the command
     * @throws JSchException       if a JSch error is detected.
     * @throws FileSystemException if a session cannot be created.
     * @throws IOException         if an I/O error is detected.
     */
    private int executeCommand(final String command, final StringBuilder output) throws JSchException, IOException {
        final ChannelExec channel = (ChannelExec) getSession().openChannel("exec");
        try {
            channel.setCommand(command);
            channel.setInputStream(null);
            try (final InputStreamReader stream = new InputStreamReader(channel.getInputStream())) {
                channel.setErrStream(System.err, true);
                channel.connect(connectTimeoutMillis);

                // Read the stream
                final char[] buffer = new char[EXEC_BUFFER_SIZE];
                int read;
                while ((read = stream.read(buffer, 0, buffer.length)) >= 0) {
                    output.append(buffer, 0, read);
                }
            }

            // Wait until the command finishes (should not be long since we read the output stream)
            while (!channel.isClosed()) {
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (final Exception ee) {
                    // TODO: swallow exception, really?
                }
            }
        } finally {
            channel.disconnect();
        }
        return channel.getExitStatus();
    }

    /**
     * @return Whether the exec channel is disabled.
     * @see VSftpFileSystem#execDisabled
     */
    public boolean isExecDisabled() {
        return execDisabled;
    }

    /**
     * Some SFTP-only servers disable the exec channel.
     *
     * Attempt to detect this by calling getUid.
     */
    private boolean detectExecDisabled() {
        try {
            return getUId() == UNIDENTIFED;
        } catch (JSchException | IOException e) {
            LOG.debug("Cannot get UID, assuming no exec channel is present", e);
            return true;
        }
    }
}
