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
package org.objectweb.proactive.extensions.dataspaces.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.dataspaces.Utils;
import org.objectweb.proactive.extensions.dataspaces.api.Capability;
import org.objectweb.proactive.extensions.dataspaces.api.DataSpacesFileObject;
import org.objectweb.proactive.extensions.dataspaces.api.PADataSpaces;
import org.objectweb.proactive.extensions.dataspaces.core.naming.SpacesDirectory;
import org.objectweb.proactive.extensions.dataspaces.exceptions.ConfigurationException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.FileSystemException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.MalformedURIException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.NotConfiguredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceAlreadyRegisteredException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.SpaceNotFoundException;
import org.objectweb.proactive.extensions.dataspaces.exceptions.WrongApplicationIdException;


/**
 * Implements {@link PADataSpaces} API for a pair of node and application (with its identifier).
 * <p>
 * Instances of this class are thread-safe. Each instance for given node and application should
 * remain valid as long as this node has Data Spaces configured, for this application with
 * particular application identifier. For that reason, instances of this class are typically managed
 * by {@link NodeConfigurator} and {@link DataSpacesNodes} classes.
 */
public class DataSpacesImpl {
    private static final long RESOLVE_BLOCKING_RESEND_PERIOD_MILLIS = 5000;

    private static final Logger logger = ProActiveLogger.getLogger(Loggers.DATASPACES);

    /**
     * @throws ConfigurationException
     *             when expected capabilities are not fulfilled
     */
    private static void checkCapabilitiesOrWound(DataSpacesFileObject fo, SpaceType type, boolean owner)
            throws ConfigurationException {

        final Set<Capability> expected;
        switch (type) {
            case INPUT:
                expected = PADataSpaces.INPUT_SPACE_CAPABILITIES;
                break;
            case OUTPUT:
                expected = PADataSpaces.OUTPUT_SPACE_CAPABILITIES;
                break;
            case SCRATCH:
                expected = (owner ? PADataSpaces.SCRATCH_SPACE_OWNER_CAPABILITIES
                        : PADataSpaces.SCRATCH_SPACE_NONOWNER_CAPABILITIES);
                break;
            default:
                throw new IllegalArgumentException("Unexpected space type; World is not round-shaped");
        }

        if (logger.isTraceEnabled())
            logger.trace(String.format("Checking FS capabilities (count: %d) for %s type, owner: %b",
                    expected.size(), type, owner));

        try {
            Utils.assertCapabilitiesMatch(expected, fo);
        } catch (ConfigurationException x) {
            logger.error("Resolved space's file system: " + x.getMessage());
            throw x;
        }
    }

    private static void checkIsInputOrOutput(SpaceType type) {
        if (type == SpaceType.SCRATCH) {
            logger.debug("Wrong space type provided to the input/output-related method: " + type);
            throw new IllegalArgumentException("This method can be only used with input or output data space");
        }
    }

    private static void checkIsNotNullName(String name) {
        if (name == null) {
            logger.debug("Null name provided to the input/output-related method");
            throw new IllegalArgumentException("Input/data space name can not be null");
        }
    }

    private final SpacesMountManager spacesMountManager;

    private final SpacesDirectory spacesDirectory;

    private final ApplicationScratchSpace appScratchSpace;

    private final String appId;

    /**
     * Create Data Spaces implementation instance. It remains valid as provided services remain
     * valid.
     *
     * @param appId
     *            application id
     * @param smm
     *            spaces mount manager for this application
     * @param sd
     *            spaces directory for this application
     * @param ass
     *            application scratch space for this application; may be <code>null</code> if not
     *            available
     */
    public DataSpacesImpl(String appId, SpacesMountManager smm, SpacesDirectory sd,
            ApplicationScratchSpace ass) {
        this.appId = appId;
        appScratchSpace = ass;
        spacesDirectory = sd;
        spacesMountManager = smm;
    }

    /**
     * Implementation (more generic) method for resolveDefaultInput and resolveDefaultOutput.
     *
     * @param path
     *            of a file inside a data space
     * @return DataSpacesFileObject received from SpacesMountManager instance
     * @throws IllegalArgumentException
     * @throws FileSystemException
     * @throws SpaceNotFoundException
     * @throws ConfigurationException
     * @see PADataSpaces#resolveDefaultInput()
     * @see PADataSpaces#resolveDefaultOutput()
     */
    public DataSpacesFileObject resolveDefaultInputOutput(SpaceType type, String path)
            throws IllegalArgumentException, FileSystemException, SpaceNotFoundException,
            ConfigurationException {
        return resolveInputOutput(PADataSpaces.DEFAULT_IN_OUT_NAME, type, path);
    }

    /**
     * Implementation (more generic) method for resolveDefaultInputBlocking and
     * resolveDefaultOutputBlocking.
     *
     * @see PADataSpaces#resolveDefaultInputBlocking(long)
     * @see PADataSpaces#resolveDefaultOutputBlocking(long)
     */
    public DataSpacesFileObject resolveDefaultInputOutputBlocking(long timeoutMillis, SpaceType type,
            String path) throws IllegalArgumentException, FileSystemException, ProActiveTimeoutException,
            ConfigurationException {
        return resolveInputOutputBlocking(PADataSpaces.DEFAULT_IN_OUT_NAME, timeoutMillis, type, path);
    }

    /**
     * Implementation (more generic) method for resolveInput and resolveOutput.
     *
     * @see PADataSpaces#resolveInput(String)
     * @see PADataSpaces#resolveOutput(String)
     */
    public DataSpacesFileObject resolveInputOutput(String name, SpaceType type, String path)
            throws FileSystemException, IllegalArgumentException, SpaceNotFoundException,
            ConfigurationException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("Resolving request for %s with name %s", type, name));

        checkIsInputOrOutput(type);
        checkIsNotNullName(name);
        final DataSpacesURI uri;
        try {
            uri = DataSpacesURI.createInOutSpaceURI(appId, type, name, path);
        } catch (IllegalArgumentException x) {
            logger.debug("Illegal specification for resolve " + type, x);
            throw x;
        }

        try {
            final String aoId = Utils.getActiveObjectId(Utils.getCurrentActiveObjectBody());
            final DataSpacesFileObject fo = spacesMountManager.resolveFile(uri, aoId);
            if (logger.isTraceEnabled())
                logger.trace(String.format("Resolved request for %s with name %s (%s)", type, name, uri));

            checkCapabilitiesOrWound(fo, type, false);
            return fo;
        } catch (SpaceNotFoundException x) {
            logger.debug("Space not found for input/output space with URI: " + uri, x);
            throw x;
        } catch (FileSystemException x) {
            logger.debug("VFS-level problem during resolving input/output space", x);
            throw x;
        }
    }

    /**
     * Implementation (more generic) method for resolveInputBlocking and resolveOutputBlocking.
     *
     * @see PADataSpaces#resolveInputBlocking(String, long)
     * @see PADataSpaces#resolveOutputBlocking(String, long)
     */
    public DataSpacesFileObject resolveInputOutputBlocking(String name, long timeoutMillis, SpaceType type,
            String path) throws FileSystemException, IllegalArgumentException, ProActiveTimeoutException,
            ConfigurationException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("Resolving blocking request for %s with name %s", type, name));

        checkIsInputOrOutput(type);
        checkIsNotNullName(name);
        if (timeoutMillis < 1) {
            logger.debug("Illegal non-positive timeout specified for blocking resolve request");
            throw new IllegalArgumentException("Specified timeout should be positive integer");
        }
        final DataSpacesURI uri;
        try {
            uri = DataSpacesURI.createInOutSpaceURI(appId, type, name, path);
        } catch (IllegalArgumentException x) {
            logger.debug("Illegal specification for resolve " + type, x);
            throw x;
        }

        final long startTime = System.currentTimeMillis();
        long currTime = startTime;
        while (currTime < startTime + timeoutMillis) {
            try {
                final String aoId = Utils.getActiveObjectId(Utils.getCurrentActiveObjectBody());
                final DataSpacesFileObject fo = spacesMountManager.resolveFile(uri, aoId);
                if (logger.isTraceEnabled()) {
                    final String message = String.format(
                            "Resolved blocking request for %s with name %s (%s)", type, name, uri);
                    logger.trace(message);
                }
                checkCapabilitiesOrWound(fo, type, false);
                return fo;
            } catch (SpaceNotFoundException e) {
                logger.debug("Space not found for blocking try for input/output space with URI: " + uri, e);

                // request processing may have taken some time
                currTime = System.currentTimeMillis();
                final long sleepTime = Math.min(RESOLVE_BLOCKING_RESEND_PERIOD_MILLIS, startTime +
                    timeoutMillis - currTime);
                try {
                    if (logger.isTraceEnabled())
                        logger.trace("Going sleeping for " + sleepTime);
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e1) {
                }
                currTime = System.currentTimeMillis();
            } catch (FileSystemException x) {
                logger.debug("VFS-level problem during blocking resolving input/output space", x);
                throw x;
            }
        }

        if (logger.isDebugEnabled()) {
            final String message = String.format(
                    "Timeout expired for blocking resolve for %s with name %s (%s)", type, name, uri);
            logger.debug(message);
        }
        throw new ProActiveTimeoutException();
    }

    /**
     * @see PADataSpaces#resolveScratchForAO()
     */
    public DataSpacesFileObject resolveScratchForAO(String path) throws FileSystemException,
            NotConfiguredException, ConfigurationException {
        logger.trace("Resolving scratch for an Active Object");
        if (appScratchSpace == null) {
            logger.debug("Request scratch data space for AO on node without scratch space configured");
            throw new NotConfiguredException("Scratch data space not configured on this node");
        }

        final Body body = Utils.getCurrentActiveObjectBody();
        try {
            final DataSpacesURI scratchURI = appScratchSpace.getScratchForAO(body);
            final DataSpacesURI queryURI = scratchURI.withUserPath(path);
            final String aoId = Utils.getActiveObjectId(Utils.getCurrentActiveObjectBody());
            final DataSpacesFileObject fo = spacesMountManager.resolveFile(queryURI, aoId);

            if (logger.isTraceEnabled())
                logger.trace("Resolved scratch for an Active Object: " + queryURI);

            checkCapabilitiesOrWound(fo, SpaceType.SCRATCH, true);
            return fo;
        } catch (SpaceNotFoundException e) {
            ProActiveLogger.logImpossibleException(logger, e);
            throw new ProActiveRuntimeException("URI of scratch for Active Object can not be resolved", e);
        } catch (FileSystemException x) {
            logger.debug("VFS-level problem during resolving scratch fo AO: ", x);
            throw x;
        }
    }

    /**
     * Implementation (more generic) method for getAllKnownInputNames and getAllKnownInputNames.
     *
     * @see PADataSpaces#getAllKnownInputNames()
     * @see PADataSpaces#getAllKnownOutputNames()
     */
    public Set<String> getAllKnownInputOutputNames(SpaceType type) throws IllegalArgumentException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("Resolving known %s names: ", type));
        checkIsInputOrOutput(type);

        final DataSpacesURI aURI = DataSpacesURI.createURI(appId, type);
        final Set<SpaceInstanceInfo> infos = spacesDirectory.lookupMany(aURI);
        final Set<String> names = new HashSet<String>();

        if (infos != null) {
            for (SpaceInstanceInfo sii : infos) {
                names.add(sii.getName());
            }
        }
        if (logger.isTraceEnabled())
            logger.trace(String.format("Resolved known %s names: %s", type, new ArrayList<String>(names)));
        return names;
    }

    /**
     * Implementation (more generic) method for resolveAllKnownInputs and resolveAllKnownOutputs.
     *
     * @see PADataSpaces#resolveAllKnownInputs()
     * @see PADataSpaces#resolveAllKnownOutputs()
     */
    public Map<String, DataSpacesFileObject> resolveAllKnownInputsOutputs(SpaceType type)
            throws FileSystemException, IllegalArgumentException, ConfigurationException {
        if (logger.isTraceEnabled())
            logger.trace(String.format("Resolving known %s spaces: ", type));
        checkIsInputOrOutput(type);

        final DataSpacesURI uri = DataSpacesURI.createURI(appId, type);
        final Map<DataSpacesURI, DataSpacesFileObject> spaces;
        try {
            final String aoId = Utils.getActiveObjectId(Utils.getCurrentActiveObjectBody());
            spaces = spacesMountManager.resolveSpaces(uri, aoId);
        } catch (FileSystemException x) {
            logger.debug(String.format("VFS-level problem during resolving known %s spaces: ", type), x);
            throw x;
        }

        final Map<String, DataSpacesFileObject> ret = new HashMap<String, DataSpacesFileObject>(spaces.size());

        for (Entry<DataSpacesURI, DataSpacesFileObject> entry : spaces.entrySet()) {
            final String name = entry.getKey().getName();
            DataSpacesFileObject fo = entry.getValue();

            checkCapabilitiesOrWound(fo, type, false);
            ret.put(name, fo);
        }

        if (logger.isTraceEnabled()) {
            final ArrayList<String> namesList = new ArrayList<String>(ret.keySet());
            logger.trace(String.format("Resolved known %s spaces: %s", type, namesList));
        }

        return ret;
    }

    /**
     * @see PADataSpaces#resolveFile(String)
     */
    public DataSpacesFileObject resolveFile(String uri) throws MalformedURIException, FileSystemException,
            SpaceNotFoundException, ConfigurationException {
        if (logger.isTraceEnabled())
            logger.trace("Resolving file: " + uri);

        try {
            final DataSpacesURI dataSpacesURI = DataSpacesURI.parseURI(uri);
            if (!dataSpacesURI.isSuitableForUserPath())
                throw new MalformedURIException("Specified URI represents internal high-level directories");

            final String aoId = Utils.getActiveObjectId(Utils.getCurrentActiveObjectBody());
            final DataSpacesFileObject fo = spacesMountManager.resolveFile(dataSpacesURI, aoId);
            SpaceType type = dataSpacesURI.getSpaceType(); // as isComplete cannot be null

            if (logger.isTraceEnabled())
                logger.trace("Resolved file: " + uri);

            checkCapabilitiesOrWound(fo, type, Utils.isScratchOwnedByCallingThread(dataSpacesURI));
            return fo;
        } catch (MalformedURIException x) {
            logger.debug("Can not resolve malformed URI: " + uri, x);
            throw x;
        } catch (SpaceNotFoundException x) {
            logger.debug("Can not find space for URI: " + uri, x);
            throw x;
        } catch (FileSystemException x) {
            logger.debug("VFS-level problem during resolving URI: " + uri, x);
            throw x;
        }
    }

    /**
     * Implementation (more generic) method for addDefaultInput and addDefaultOutput.
     *
     * @see PADataSpaces#addDefaultInput(String, String)
     * @see PADataSpaces#addDefaultOutput(String, String)
     */
    public String addDefaultInputOutput(String url, String path, SpaceType type)
            throws SpaceAlreadyRegisteredException, ConfigurationException, IllegalArgumentException {
        return addInputOutput(PADataSpaces.DEFAULT_IN_OUT_NAME, url, path, type);
    }

    /**
     * Implementation (more generic) method for addInput and addOutput.
     *
     * @see PADataSpaces#addInput(String, String, String)
     * @see PADataSpaces#addOutput(String, String, String)
     */
    public String addInputOutput(String name, String url, String path, SpaceType type)
            throws SpaceAlreadyRegisteredException, ConfigurationException {
        logger.debug("Adding input/output data space");

        final SpaceInstanceInfo spaceInstanceInfo;
        try {
            String hostname = null;
            if (path == null)
                hostname = Utils.getHostname();

            // name and type are checked here
            final InputOutputSpaceConfiguration config = InputOutputSpaceConfiguration.createConfiguration(
                    url, path, hostname, name, type);
            // url is checked here
            spaceInstanceInfo = new SpaceInstanceInfo(appId, config);
        } catch (ConfigurationException x) {
            logger.debug("User-added input/output has wrong configuration", x);
            throw x;
        }

        try {
            spacesDirectory.register(spaceInstanceInfo);
        } catch (WrongApplicationIdException x) {
            ProActiveLogger.logImpossibleException(logger, x);
            throw new ProActiveRuntimeException(
                "This application id is not registered in used naming service", x);
        } catch (SpaceAlreadyRegisteredException x) {
            logger.debug(
                    String.format("User-added space %s is already registered",
                            spaceInstanceInfo.getMountingPoint()), x);
            throw x;
        }

        if (logger.isDebugEnabled())
            logger.debug("Added input/output data space: " + spaceInstanceInfo);
        return spaceInstanceInfo.getMountingPoint().toString();
    }
}
