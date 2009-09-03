/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.core.naming;

import java.util.Set;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;


/**
 * Decorator of {@link SpacesDirectory} that locally caches results from another directory instance.
 * <p>
 * Caching is performed assuming that in normal conditions, for spaces concerning clients of this
 * class, source directory should work in append-only mode. Therefore, unregistering space in an
 * original source directory, does not cause it to be removed from the cached directory. However,
 * unregistering this space explicitly through cache directory, cause it to be unregistered in both
 * directories.
 * <p>
 * Only {@link #lookupOne(DataSpacesURI)} queries are cached, while
 * {@link #lookupMany(DataSpacesURI)} queries are never cached.
 * <p>
 * Instances of this class are thread-safe.
 */
public class CachingSpacesDirectory implements SpacesDirectory {
    private final SpacesDirectoryImpl localDirectory;

    private final SpacesDirectory remoteDirectory;

    public CachingSpacesDirectory(SpacesDirectory directoryToCache) {
        localDirectory = new SpacesDirectoryImpl();
        remoteDirectory = directoryToCache;
    }

    /**
     * This method call is always delegated remotely.
     *
     * @see SpacesDirectory#lookupMany(DataSpacesURI)
     */
    public Set<SpaceInstanceInfo> lookupMany(DataSpacesURI uri) {
        SpacesDirectoryImpl.checkAbstractURI(uri);

        synchronized (this) {
            final Set<SpaceInstanceInfo> ret = remoteDirectory.lookupMany(uri);
            if (ret != null)
                localDirectory.register(ret);

            return ret;
        }
    }

    /**
     * Try in cache, if not found try remotely.
     *
     * @see SpacesDirectory#lookupOne(DataSpacesURI)
     */
    public SpaceInstanceInfo lookupOne(DataSpacesURI uri) {
        SpacesDirectoryImpl.checkMountingPointURI(uri);

        // double-checked locking, as it would be a pity if we have to wait for
        // remote lookups when we can answer some lookups using local directory
        SpaceInstanceInfo sii = localDirectory.lookupOne(uri);
        if (sii != null)
            return sii;

        synchronized (this) {
            sii = localDirectory.lookupOne(uri);
            if (sii != null)
                return sii;

            sii = remoteDirectory.lookupOne(uri);

            if (sii != null) {
                try {
                    localDirectory.register(sii);
                } catch (SpaceAlreadyRegisteredException e) {
                    final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES);
                    ProActiveLogger.logImpossibleException(logger, e);
                    throw new RuntimeException(e);
                }
            }
            return sii;
        }
    }

    public synchronized void register(SpaceInstanceInfo spaceInstanceInfo)
            throws SpaceAlreadyRegisteredException, WrongApplicationIdException {

        remoteDirectory.register(spaceInstanceInfo);
        localDirectory.register(spaceInstanceInfo);
    }

    public synchronized boolean unregister(DataSpacesURI uri) {
        SpacesDirectoryImpl.checkMountingPointURI(uri);

        localDirectory.unregister(uri);
        return remoteDirectory.unregister(uri);
    }
}
