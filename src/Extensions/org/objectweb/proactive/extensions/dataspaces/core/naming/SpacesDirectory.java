/**
 *
 */
package org.objectweb.proactive.extensions.dataspaces.core.naming;

import java.util.Set;

import org.objectweb.proactive.extensions.dataspaces.core.DataSpacesURI;
import org.objectweb.proactive.extensions.dataspaces.core.SpaceInstanceInfo;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;


/**
 * Directory of data spaces information. It allows registering and unregistering space instances (
 * {@link SpaceInstanceInfo}: space with defined URI and access-related information) and performing
 * lookup-queries by space URI for these space-related information. It acts as a sort of "space URI"
 * to "space info" map.
 * <p>
 * Directory is assumed to work in append-only mode for each URI for some period of time, finally
 * allowing to remove information about removed space. It means, that once space with some URI is
 * registered, the directory should provide the same information for each query for that URI during
 * some period, until this space is explicitly removed from the directory. In other words, space
 * information should never be overridden, and should be removed from directory only where
 * interested parties that may have ask for this space, are aware of that.
 * <p>
 * Directory may be used for lookups for space with particular URI (
 * {@link #lookupOne(DataSpacesURI)}) or more abstract queries ({@link #lookupMany(DataSpacesURI)}).
 */
public interface SpacesDirectory {

    /**
     * Lookup for space instance info with given mounting point URI having exactly space part fully
     * defined and nothing else.
     *
     * @param uri
     *            mounting point URI of data space to look up (must have space part fully defined
     *            and nothing else)
     * @return SpaceInstanceInfo for that URI or <code>null</code> if there is no such URI
     *         registered
     * @throws IllegalArgumentException
     *             when specified DataSpacesURI does not have exactly space part defined.
     * @see DataSpacesURI#isSpacePartFullyDefined()
     * @see DataSpacesURI#isSpacePartOnly()
     */
    public SpaceInstanceInfo lookupOne(DataSpacesURI uri) throws IllegalArgumentException;

    /**
     * Lookup for all SpaceInstanceInfo with root of its mounting point that matches specified
     * abstract URI without space part being fully defined ( <code>ls -R</code> like).
     *
     * @param uri
     *            root URI to look up
     * @return SpaceInstanceInfo mappings or null if none is available
     * @throws IllegalArgumentException
     *             when specified URI is not abstract - has space part fully defined.
     * @see DataSpacesURI#isSpacePartFullyDefined()
     */
    public Set<SpaceInstanceInfo> lookupMany(DataSpacesURI uri) throws IllegalArgumentException;

    /**
     * Registers new space instance info. If mounting point of that space instance has been already
     * in the directory, an exception is raised as directory is append-only.
     *
     * @param spaceInstanceInfo
     *            space instance info to register (contract: SpaceInstanceInfo mounting point should
     *            have space part fully defined and nothing else)
     * @throws WrongApplicationIdException
     *             when directory is aware of all registered applications and there is no such
     *             application for SpaceInstanceInfo being registered
     * @throws SpaceAlreadyRegisteredException
     *             when directory already contains any space instance under specified mounting point
     */
    public void register(SpaceInstanceInfo spaceInstanceInfo) throws WrongApplicationIdException,
            SpaceAlreadyRegisteredException;

    /**
     * Unregisters space instance info specified by DataSpacesURI.
     *
     * @param uri
     *            mounting point URI that is to be unregistered
     * @return <code>true</code> if space instance with given DataSpacesURI has been found;
     *         <code>false</code> otherwise
     * @throws IllegalArgumentException
     *             when specified DataSpacesURI is not an URI with space part fully defined and
     *             nothing else.
     */
    public boolean unregister(DataSpacesURI uri) throws IllegalArgumentException;
}
