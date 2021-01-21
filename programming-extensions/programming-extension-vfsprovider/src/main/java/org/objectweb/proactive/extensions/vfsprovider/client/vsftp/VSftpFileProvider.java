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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystem;
import org.apache.commons.vfs2.FileSystemConfigBuilder;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.UserAuthenticationData;
import org.apache.commons.vfs2.provider.AbstractOriginatingFileProvider;
import org.apache.commons.vfs2.provider.GenericFileName;
import org.apache.commons.vfs2.provider.sftp.SftpClientFactory;
import org.apache.commons.vfs2.provider.sftp.SftpFileNameParser;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs2.util.UserAuthenticatorUtils;

import com.jcraft.jsch.Session;


/**
 * @author ActiveEon Team
 * @since 06/01/2021
 */
public class VSftpFileProvider extends AbstractOriginatingFileProvider {
    /** User Information. */
    public static final String ATTR_USER_INFO = "UI";

    /** Authentication types. */
    public static final UserAuthenticationData.Type[] AUTHENTICATOR_TYPES = new UserAuthenticationData.Type[] { UserAuthenticationData.USERNAME,
                                                                                                                UserAuthenticationData.PASSWORD };

    /** The provider's capabilities. */
    protected static final Collection<Capability> capabilities = Collections.unmodifiableCollection(Arrays.asList(Capability.CREATE,
                                                                                                                  Capability.DELETE,
                                                                                                                  Capability.RENAME,
                                                                                                                  Capability.GET_TYPE,
                                                                                                                  Capability.LIST_CHILDREN,
                                                                                                                  Capability.READ_CONTENT,
                                                                                                                  Capability.URI,
                                                                                                                  Capability.WRITE_CONTENT,
                                                                                                                  Capability.GET_LAST_MODIFIED,
                                                                                                                  Capability.SET_LAST_MODIFIED_FILE,
                                                                                                                  Capability.RANDOM_ACCESS_READ,
                                                                                                                  Capability.APPEND_CONTENT));

    /**
     * Creates a new Session.
     *
     * @return A Session, never null.
     */
    static Session createSession(final GenericFileName rootName, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        UserAuthenticationData authData = null;
        try {
            authData = UserAuthenticatorUtils.authenticate(fileSystemOptions, AUTHENTICATOR_TYPES);

            return SftpClientFactory.createConnection(rootName.getHostName(),
                                                      rootName.getPort(),
                                                      UserAuthenticatorUtils.getData(authData,
                                                                                     UserAuthenticationData.USERNAME,
                                                                                     UserAuthenticatorUtils.toChar(rootName.getUserName())),
                                                      UserAuthenticatorUtils.getData(authData,
                                                                                     UserAuthenticationData.PASSWORD,
                                                                                     UserAuthenticatorUtils.toChar(rootName.getPassword())),
                                                      fileSystemOptions);
        } catch (final Exception e) {
            throw new FileSystemException("vfs.provider.sftp/connect.error", rootName, e);
        } finally {
            UserAuthenticatorUtils.cleanup(authData);
        }
    }

    /**
     * Constructs a new provider.
     */
    public VSftpFileProvider() {
        super();
        setFileNameParser(SftpFileNameParser.getInstance());
    }

    /**
     * Creates a {@link FileSystem}.
     */
    @Override
    protected FileSystem doCreateFileSystem(final FileName name, final FileSystemOptions fileSystemOptions)
            throws FileSystemException {
        // Create the file system
        return new VSftpFileSystem((GenericFileName) name,
                                   createSession((GenericFileName) name, fileSystemOptions),
                                   fileSystemOptions);
    }

    @Override
    public FileSystemConfigBuilder getConfigBuilder() {
        return SftpFileSystemConfigBuilder.getInstance();
    }

    @Override
    public Collection<Capability> getCapabilities() {
        return capabilities;
    }
}
