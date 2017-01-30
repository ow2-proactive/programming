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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;


/**
 * In-memory implementation of {@link SpacesDirectory}.
 * <p>
 * Instances of this class are thread-safe.
 *
 * @see SpacesDirectory
 */
public class SpacesDirectoryImpl implements SpacesDirectory {
    private final SortedMap<DataSpacesURI, SpaceInstanceInfo> data = new TreeMap<DataSpacesURI, SpaceInstanceInfo>();

    protected static void checkAbstractURI(DataSpacesURI uri) {
        if (uri.isSpacePartFullyDefined())
            throw new IllegalArgumentException("Space part must not be fully defined in URI for this method call");
    }

    protected static void checkMountingPointURI(DataSpacesURI uri) {
        if (!uri.isSpacePartFullyDefined())
            throw new IllegalArgumentException("Space part must be fully defined in URI for this method call");

        if (!uri.isSpacePartOnly())
            throw new IllegalArgumentException("Space URI must define only space part for this method call");
    }

    public Set<SpaceInstanceInfo> lookupMany(DataSpacesURI uri) {
        checkAbstractURI(uri);

        final DataSpacesURI nextKey = uri.nextURI();
        final Set<SpaceInstanceInfo> ret = new HashSet<SpaceInstanceInfo>();

        synchronized (data) {
            final SortedMap<DataSpacesURI, SpaceInstanceInfo> sub = data.subMap(uri, nextKey);

            if (sub.size() == 0)
                return null;
            ret.addAll(sub.values());
        }
        return ret;
    }

    public SpaceInstanceInfo lookupOne(DataSpacesURI uri) {
        checkMountingPointURI(uri);

        synchronized (data) {
            return data.get(uri);
        }
    }

    public void register(SpaceInstanceInfo spaceInstanceInfo) throws SpaceAlreadyRegisteredException {
        final DataSpacesURI mpoint;

        // get mounting point URI that cannot be null
        synchronized (data) {
            mpoint = spaceInstanceInfo.getMountingPoint();

            if (data.containsKey(mpoint))
                throw new SpaceAlreadyRegisteredException("Mapping for a given space URI is already registered");
            data.put(mpoint, spaceInstanceInfo);
        }
    }

    public boolean unregister(DataSpacesURI uri) {
        checkMountingPointURI(uri);

        synchronized (data) {
            if (!data.containsKey(uri))
                return false;

            data.remove(uri);
        }
        return true;
    }

    /**
     * Helper method for bulked registration as obtaining lock is done only once.
     *
     * @param ssis
     */
    protected void register(Set<SpaceInstanceInfo> ssis) {
        synchronized (data) {
            for (SpaceInstanceInfo ssi : ssis)
                data.put(ssi.getMountingPoint(), ssi);
        }
    }

    /**
     * Helper method for bulked unregistration as obtaining lock is done only once.
     *
     * @param uris
     */
    protected void unregister(Set<DataSpacesURI> uris) {
        synchronized (data) {
            for (DataSpacesURI key : uris)
                data.remove(key);
        }
    }
}
