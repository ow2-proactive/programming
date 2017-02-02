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
