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
package org.objectweb.proactive.extensions.dataspaces.vfs;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.commons.vfs2.CacheStrategy;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FilesCache;
import org.apache.commons.vfs2.UserAuthenticator;
import org.apache.commons.vfs2.auth.StaticUserAuthenticator;
import org.apache.commons.vfs2.cache.DefaultFilesCache;
import org.apache.commons.vfs2.cache.LRUFilesCache;
import org.apache.commons.vfs2.cache.NullFilesCache;
import org.apache.commons.vfs2.cache.SoftRefFilesCache;
import org.apache.commons.vfs2.cache.WeakRefFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileReplicator;
import org.apache.commons.vfs2.impl.DefaultFileSystemConfigBuilder;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.impl.PrivilegedFileReplicator;
import org.apache.commons.vfs2.operations.FileOperationProvider;
import org.apache.commons.vfs2.provider.FileProvider;
import org.apache.commons.vfs2.provider.ftp.FtpFileProvider;
import org.apache.commons.vfs2.provider.http.HttpFileProvider;
import org.apache.commons.vfs2.provider.https.HttpsFileProvider;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.sftp.BytesIdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.url.UrlFileProvider;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.api.UserCredentials;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileName;
import org.objectweb.proactive.extensions.vfsprovider.client.ProActiveFileProvider;
import org.objectweb.proactive.extensions.vfsprovider.client.vsftp.VSftpFileProvider;
import org.objectweb.proactive.extensions.vfsprovider.protocol.FileSystemServer;

import com.google.common.base.Strings;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;


/**
 * Factory class for creating configured VFS DefaultFileSystemManager instances.
 * <p>
 * Instances of managers created through this factory are guaranteed to have providers for following
 * protocols:
 * <ul>
 * <li>local files, scheme <code>file:</code></li>
 * <li>HTTP, scheme <code>http:</code></li>
 * <li>HTTPS, scheme <code>https:</code></li>
 * <li>FTP, scheme <code>ftp:</code></li>
 * <li>SFTP, scheme <code>sftp:</code> (with strict host-key checking disabled by default if no
 * other FileSystemOptions are provided)</li>
 * <li>ProActive protocol (see {@link FileSystemServer}), schemes acquired from
 * {@link ProActiveFileName#getAllVFSSchemes()}</li>
 * <li>default URL provider handled by Java URL class</code>
 * </ul>
 *
 * Configured replicator, temporary storage and default files cache are also guaranteed.
 */
public class VFSFactory {
    private static final Log4JLogger logger;

    static {
        final Logger rawLogger = ProActiveLogger.getLogger(Loggers.DATASPACES_VFS);
        if (rawLogger.getEffectiveLevel().isGreaterOrEqual(Level.INFO)) {
            // quite VFS a bit, as it is more verbose than ProActive conventions
            rawLogger.setLevel(Level.WARN);
        }
        logger = new Log4JLogger(rawLogger);
    }

    /**
     * Creates new DefaultSystemManager instance with configured providers, replicator, temporary
     * storage - as described in class description, <strong>files cache</strong> if required, and configured cache strategy.
     * <p>
     * Returned instance is initialized and it is a caller responsibility to close it to release
     * resources.
     *
     * @return configured and initialized DefaultFileSystemManager instance
     * @throws FileSystemException
     *             when initialization or configuration process fails
     */
    public static DefaultFileSystemManager createDefaultFileSystemManager() throws FileSystemException {
        return createAndConfigureFileSystemManager(ManagerType.BOTH, null);
    }

    /**
     * Creates new DefaultSystemManager instance with configured providers, replicator, temporary
     * storage - as described in class description, <strong>files cache</strong> if required, and configured cache strategy.
     * This system manager will use the provided user credentials on providers which support it
     * <p>
     * Returned instance is initialized and it is a caller responsibility to close it to release
     * resources.
     * @param credentials
     *             credentials used to access the file system (for implementations which support it)
     * @return
     * @throws FileSystemException
     */
    public static DefaultFileSystemManager createDefaultFileSystemManager(UserCredentials credentials)
            throws FileSystemException {
        return createAndConfigureFileSystemManager(ManagerType.BOTH, credentials);
    }

    /**
     * Creates new DefaultSystemManager instance with configured non-proactive providers, replicator, temporary
     * storage - as described in class description, and <strong>files cache</strong> if required.
     * <p>
     * Returned instance is initialized and it is a caller responsibility to close it to release
     * resources.
     *
     * @param credentials
     *             credentials used to access the file system (for implementations which support it)
     * @return configured and initialized DefaultFileSystemManager instance
     * @throws FileSystemException
     *             when initialization or configuration process fails
     */
    public static DefaultFileSystemManager createNonProActiveDefaultFileSystemManager(UserCredentials credentials)
            throws FileSystemException {
        return createAndConfigureFileSystemManager(ManagerType.NON_PROACTIVE, credentials);
    }

    /**
     * Creates new DefaultSystemManager instance with configured ProActive providers, replicator, temporary
     * storage - as described in class description, and <strong>DISABLED files cache</strong>
     * (NullFilesCache) .
     * <p>
     * Returned instance is initialized and it is a caller responsibility to close it to release
     * resources.
     *
     * @param credentials
     *             credentials used to access the file system (for implementations which support it)
     * @return configured and initialized DefaultFileSystemManager instance
     * @throws FileSystemException
     *             when initialization or configuration process fails
     */
    public static DefaultFileSystemManager createProActiveDefaultFileSystemManager(UserCredentials credentials)
            throws FileSystemException {
        return createAndConfigureFileSystemManager(ManagerType.PROACTIVE, credentials);
    }

    private static synchronized DefaultFileSystemManager createAndConfigureFileSystemManager(ManagerType managerType,
            UserCredentials credentials) throws FileSystemException {
        logger.debug("Creating new VFS manager");

        final DefaultFileSystemManager manager = buildManager(credentials);

        manager.setFilesCache(getCacheType());
        manager.setCacheStrategy(getCacheStrategy());

        if (managerType == ManagerType.NON_PROACTIVE || managerType == ManagerType.BOTH) {
            addNonProActiveProviders(manager);
        }

        if (managerType == ManagerType.PROACTIVE || managerType == ManagerType.BOTH) {
            addProActiveProviders(manager);
        }

        manager.init();
        logger.debug("Created and initialized new VFS manager");
        return manager;
    }

    private static DefaultFileSystemManager buildManager(UserCredentials credentials) throws FileSystemException {
        FileSystemOptions opts = createDefaultFileSystemOptions(credentials);
        final DefaultFileSystemManager manager = new DefaultOptionsFileSystemManager(opts);
        manager.setLogger(logger);

        final DefaultFileReplicator replicator = new DefaultFileReplicator();
        manager.setReplicator(new PrivilegedFileReplicator(replicator));
        manager.setTemporaryFileStore(replicator);
        return manager;
    }

    private static void addProActiveProviders(DefaultFileSystemManager manager) throws FileSystemException {
        final ProActiveFileProvider proactiveProvider = new ProActiveFileProvider();
        for (final String scheme : ProActiveFileName.getAllVFSSchemes()) {
            manager.addProvider(scheme, proactiveProvider);
        }
    }

    private static void addNonProActiveProviders(DefaultFileSystemManager manager) throws FileSystemException {
        manager.addProvider("file", new DefaultLocalFileProvider());
        manager.addProvider("http", new HttpFileProvider());
        manager.addProvider("https", new HttpsFileProvider());
        manager.addProvider("ftp", new FtpFileProvider());
        manager.addProvider("sftp", new SftpFileProvider());
        manager.addProvider("vsftp", new VSftpFileProvider());

        loadS3ExtensionIfPresent(manager);
        manager.setDefaultProvider(new UrlFileProvider());
    }

    private static void loadS3ExtensionIfPresent(DefaultFileSystemManager manager) {
        try {
            Class S3FileProviderClass = Class.forName("com.github.vfss3.S3FileProvider");
            Object S3FileProviderInstance = S3FileProviderClass.newInstance();
            Class S3FileOperationsProviderClass = Class.forName("com.github.vfss3.operations.S3FileOperationsProvider");
            Object S3FileOperationsProviderInstance = S3FileOperationsProviderClass.newInstance();
            manager.addProvider("s3", (FileProvider) S3FileProviderInstance);
            manager.addOperationProvider("s3", (FileOperationProvider) S3FileOperationsProviderInstance);
        } catch (Exception e) {
            logger.debug("S3 library cannot be found, disabling it", e);
        }
    }

    private static CacheStrategy getCacheStrategy() {
        String configuredStrategy = CentralPAPropertyRepository.PA_DATASPACES_CACHE_STRATEGY.getValue();
        CacheStrategy strategy;
        switch (configuredStrategy.trim()) {
            case "onresolve":
                strategy = CacheStrategy.ON_RESOLVE;
                break;
            case "oncall":
                strategy = CacheStrategy.ON_CALL;
                break;
            case "manual":
                strategy = CacheStrategy.MANUAL;
                break;
            default:
                logger.error("Unrecognized cache strategy : " + configuredStrategy + " revert to default");
                strategy = CacheStrategy.ON_RESOLVE;
        }
        return strategy;
    }

    private static FilesCache getCacheType() {
        String configuredType = CentralPAPropertyRepository.PA_DATASPACES_CACHE_TYPE.getValue();
        FilesCache cache;
        switch (configuredType.trim()) {
            case "default":
                cache = new DefaultFilesCache();
                break;
            case "null":
                cache = new NullFilesCache();
                break;
            case "softref":
                cache = new SoftRefFilesCache();
                break;
            case "weakref":
                cache = new WeakRefFilesCache();
                break;
            case "lru":
                cache = new LRUFilesCache();
                break;
            default:
                logger.error("Unrecognized cache type : " + configuredType + " revert to default");
                cache = new SoftRefFilesCache();
        }
        return cache;
    }

    private static synchronized FileSystemOptions createDefaultFileSystemOptions(UserCredentials credentials)
            throws FileSystemException {

        final FileSystemOptions options = new FileSystemOptions();
        if (logger.isTraceEnabled()) {
            logger.trace("CREDENTIALS = " + credentials);
        }

        if (credentials != null && !credentials.isEmpty()) {
            createEmptySshFolderForSftp();
            if (credentials.getPrivateKey() != null && credentials.getPrivateKey().length > 0) {
                String privateKeyAsString = null;
                try {
                    privateKeyAsString = new String(credentials.getPrivateKey(), Charset.defaultCharset());
                } catch (Throwable t) {
                    // private key cannot be decoded as string
                }
                if (!CentralPAPropertyRepository.PA_DATASPACES_SFTP_DISABLE_PRIVATEKEY.isTrue() &&
                    privateKeyAsString != null &&
                    (!privateKeyAsString.isEmpty() && !privateKeyAsString.equals("undefined") &&
                     !privateKeyAsString.equals("null"))) {
                    try {
                        final JSch jsch = new JSch();
                        KeyPair.load(jsch, credentials.getPrivateKey(), null);
                        BytesIdentityInfo identityInfo = new BytesIdentityInfo(credentials.getPrivateKey(), null);
                        SftpFileSystemConfigBuilder.getInstance().setIdentityProvider(options, identityInfo);
                        SftpFileSystemConfigBuilder.getInstance().setPreferredAuthentications(options,
                                                                                              "publickey,password");
                    } catch (FileSystemException | JSchException e) {
                        logger.warn("Error when adding private key information : " + privateKeyAsString, e);
                    }
                } else {
                    SftpFileSystemConfigBuilder.getInstance().setIdentityProvider(options, null);
                    SftpFileSystemConfigBuilder.getInstance().setPreferredAuthentications(options, "password");
                }
            } else {
                SftpFileSystemConfigBuilder.getInstance().setIdentityProvider(options, null);
                SftpFileSystemConfigBuilder.getInstance().setPreferredAuthentications(options, "password");
            }
            UserAuthenticator auth = new StaticUserAuthenticator(credentials.getDomain(),
                                                                 credentials.getLogin(),
                                                                 credentials.getPassword());
            try {
                DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, auth);
            } catch (FileSystemException ex) {
                logger.error("Error when setting user authentication", ex);
            }

        } else {
            DefaultFileSystemConfigBuilder.getInstance().setUserAuthenticator(options, null);
        }
        // TODO or try to configure known hosts somehow (look for OpenSSH file etc.)
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(options, "no");
        configureJSch();
        return options;
    }

    private static synchronized void createEmptySshFolderForSftp() {
        // VFS SFTP provider force usage of a private key if it's present in the .ssh folder of the current user (the user which runs proactive)
        // This prevents using proper user credentials as the system user is different from the credentials user
        // the following code asks VFS to use an empty ssh directory, thus no private key will be loaded and it will not prevent proper authentication
        File emptySshFolder = new File(System.getProperty("java.io.tmpdir"), "PA_SSH_EMPTY");
        if (!emptySshFolder.exists()) {
            try {
                emptySshFolder.mkdir();
            } catch (Exception e) {
                logger.error("Error when creating empty ssh folder", e);
            }
            if (emptySshFolder.exists()) {
                System.setProperty("vfs.sftp.sshdir", emptySshFolder.getAbsolutePath());
            }
        }
    }

    private static void configureJSch() {
        // configure JSCH to enable legacy algorithms that may be used by old ssh servers
        List<String> additionalCiphers = CentralPAPropertyRepository.PA_DATASPACES_JSCH_ADDITIONAL_CIPHERS.getValue();

        Set<String> defaultCipherSet = new HashSet(Arrays.asList(JSch.getConfig("cipher.s2c").split(",")));
        for (String cipher : additionalCiphers) {
            if (!cipher.isEmpty() && !defaultCipherSet.contains(cipher)) {
                JSch.setConfig("cipher.s2c", JSch.getConfig("cipher.s2c") + "," + cipher);
                JSch.setConfig("cipher.c2s", JSch.getConfig("cipher.c2s") + "," + cipher);
                defaultCipherSet.add(cipher);
            }
        }

        List<String> additionalKex = CentralPAPropertyRepository.PA_DATASPACES_JSCH_ADDITIONAL_KEX.getValue();
        Set<String> defaultKexSet = new HashSet(Arrays.asList(JSch.getConfig("kex").split(",")));
        for (String kex : additionalKex) {
            if (!kex.isEmpty() && !defaultKexSet.contains(kex)) {
                JSch.setConfig("kex", JSch.getConfig("kex") + "," + kex);
                defaultKexSet.add(kex);
            }
        }

        if (CentralPAPropertyRepository.PA_DATASPACES_JSCH_ADDITIONAL_MACS.isSet()) {
            List<String> additionalMacs = CentralPAPropertyRepository.PA_DATASPACES_JSCH_ADDITIONAL_MACS.getValue();
            Set<String> defaultMacSet = new HashSet(Arrays.asList(JSch.getConfig("mac.s2c").split(",")));
            for (String mac : additionalMacs) {
                if (!mac.isEmpty() && !defaultMacSet.contains(mac)) {
                    JSch.setConfig("mac.s2c", JSch.getConfig("mac.s2c") + "," + mac);
                    JSch.setConfig("mac.c2s", JSch.getConfig("mac.c2s") + "," + mac);
                    defaultMacSet.add(mac);
                }
            }
        }
    }

    public enum ManagerType {
        PROACTIVE,
        NON_PROACTIVE,
        BOTH
    }
}
