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

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.proactive.extensions.dataspaces.exceptions.MalformedURIException;


/**
 * Represents any valid URI used in Data Spaces - abstract location in Data Spaces valid across set
 * of nodes.
 *
 * <p>
 * Example URI:
 *
 * <pre>
 * vfs:///439654/output/stats/some_dir/file.txt
 * </pre>
 *
 * URI is represented in meaningful way, i.e. its structure has semantic. It may consists of
 * following components forming a hierarchy, separated by slashes:
 * <ol>
 * <li>URI scheme, always present; always vfs:///
 * <li>identifier of application - long integer, always present; e.g. 439654</li>
 * <li>type of data space: input, output or scratch; e.g. output</li>
 * <li>name of input/output OR scratch runtime, node and active object id; these components are
 * non-empty strings without slashes; e.g. stats OR runtimeXX/nodeYY/aoZZ</li>
 * <li>user path within data space; this component is a non-empty string; e.g. some_dir/file.txt</li>
 * </ol>
 *
 * Every component except scheme and application id can be unspecified in URI. However, components
 * hierarchy must be obeyed - if higher component is not specified, then all lower components must
 * be unspecified.<br>
 * All described components can be directly accessed through methods.
 *
 * <p>
 * URI is said to have <strong>space part fully defined</strong> ({@link #isSpacePartFullyDefined()}
 * ), when at least following components are defined: application id, data space type and
 * input/output name or scratch runtime, node and active object id. Such URI always points to
 * concrete data space.
 *
 * <p>
 * URI without userPath and active object id are said to have <strong>space part only
 * defined</strong> ({@link #isSpacePartOnly()}). Space part only URI can be extracted from any URI
 * with <strong>space part fully defined</strong> through {@link #getSpacePartOnly()}. Such URI is
 * suitable for queries at data spaces granularity level.
 *
 * <p>
 * URI is said to be <strong>suitable for user path</strong> ({@link #isSuitableForUserPath()}) when
 * every its components are defined (userPath is optional). Such URI can be used by end-user.
 *
 * <p>
 * Instances of this class are comparable, in a way corresponding to described hierarchy,
 * <code>equals</code> and <code>hashCode</code> methods are also defined.
 *
 * <p>
 * URI instances are created through dedicated factory methods or parsing factory. Instances of this
 * class are immutable, thread-safe.
 *
 */
public final class DataSpacesURI implements Serializable, Comparable<DataSpacesURI> {

    private static final long serialVersionUID = 61L;

    /**
     * Scheme of Data Spaces URI.
     */
    public static final String SCHEME = "vfs:///";

    /**
     *
     */

    private static final Pattern PATTERN = Pattern
            .compile("^vfs:///(\\d+)(/(((input|output)(/(([^/]+)(/(.+)?)?)?)?)|scratch(/(([^/]+)(/(([^/]+)(/(([^/]+)(/(.+)?)?)?)?)?)?)?)?)?)?$");

    private static boolean isValidComponent(String component) {
        return component == null || (component.length() > 0 && component.indexOf('/') == -1);
    }

    /**
     * Creates URI with only application id being specified.
     *
     * This method is only a shortcut for {@link #createURI(long, SpaceType)} with <code>null</code>
     * spaceType argument.
     *
     * @param appId
     *            application id
     * @return URI for that specification
     */
    public static DataSpacesURI createURI(long appId) {
        return createURI(appId, null);
    }

    /**
     * Creates URI with only application id and type specified.
     *
     * @param appId
     *            application id
     * @param spaceType
     *            space type. May be <code>null</code>.
     * @return URI for that specification
     */
    public static DataSpacesURI createURI(long appId, SpaceType spaceType) {
        return new DataSpacesURI(appId, spaceType, null, null, null, null, null);
    }

    /**
     * Creates URI of scratch type with only runtimeId specified.
     *
     * This is only a shortcut for
     * {@link #createScratchSpaceURI(long, String, String, String, String)} with <code>null</code>
     * values for nodeId, activeObjectId and userPath.
     *
     * @param appId
     *            application id
     * @param runtimeId
     *            runtimeId where scratch space is located. May be <code>null</code>.
     * @return URI for that specification
     * @throws IllegalArgumentException
     *             when runtimeId is invalid.
     */
    public static DataSpacesURI createScratchSpaceURI(long appId, String runtimeId) {
        return createScratchSpaceURI(appId, runtimeId, null);
    }

    /**
     * Creates URI of scratch type with only runtimeId and nodeId specified. Created URI always has
     * space part fully defined if arguments are not <code>null</code>.
     *
     * This is only a shortcut for
     * {@link #createScratchSpaceURI(long, String, String, String, String)} with <code>null</code>
     * values for activeObjectId and userPath.
     *
     * @param appId
     *            application id
     * @param runtimeId
     *            runtimeId where scratch space is located. May be <code>null</code> if following
     *            argument nodeId is also <code>null</code>.
     * @param nodeId
     *            nodeId (for node being on runtime with runtimeId) where scratch space is located.
     *            May be <code>null</code>.
     * @return URI for that specification
     * @throws IllegalArgumentException
     *             when <code>null</code> values in arguments do not obey URI components hierarchy
     *             requirements or runtimeId or nodeId are invalid.
     */
    public static DataSpacesURI createScratchSpaceURI(long appId, String runtimeId, String nodeId) {
        return createScratchSpaceURI(appId, runtimeId, nodeId, null);
    }

    /**
     * Creates URI of scratch type with only runtimeId, nodeId and activeObjectId specified. Created
     * URI has always space part fully defined and is suitable for user path if arguments are not
     * <code>null</code>.
     *
     * @param appId
     *            application id
     * @param runtimeId
     *            runtimeId where scratch space is located. May be <code>null</code> if following
     *            arguments are also <code>null</code>.
     * @param nodeId
     *            nodeId (for node being on runtime with runtimeId) where scratch space is located.
     *            May be <code>null</code> if following argument activeObjectId is also
     *            <code>null</code>.
     * @param activeObjectId
     *            activeObjectId (for AO being on node with nodeId) who is owner of that scratch
     *            space part. May be <code>null</code>.
     * @return URI for that specification
     * @throws IllegalArgumentException
     *             when <code>null</code> values in arguments do not obey URI components hierarchy
     *             requirements or runtimeId, nodeId, activeObjectId are invalid.
     */
    public static DataSpacesURI createScratchSpaceURI(long appId, String runtimeId, String nodeId,
            String activeObjectId) {
        return createScratchSpaceURI(appId, runtimeId, nodeId, activeObjectId, null);
    }

    /**
     * Creates URI of scratch type. Created URI has always space part fully defined and is suitable
     * for user path if arguments are not <code>null</code>.
     *
     * @param appId
     *            application id
     * @param runtimeId
     *            runtimeId where scratch space is located. May be <code>null</code> if following
     *            arguments are also <code>null</code>.
     * @param nodeId
     *            nodeId (for node being on runtime with runtimeId) where scratch space is located.
     *            May be <code>null</code> if following arguments are also <code>null</code>.
     * @param activeObjectId
     *            activeObjectId (for AO being on node with nodeId) who is owner of that scratch
     *            space part. May be <code>null</code> if following argument userPath is also
     *            <code>null</code>.
     * @param userPath
     *            userPath within data space. May be <code>null</code>.
     * @return URI for that specification
     * @throws IllegalArgumentException
     *             when <code>null</code> values in arguments do not obey URI components hierarchy
     *             requirements or runtimeId, nodeId, activeObjectId or userPath are invalid.
     */
    public static DataSpacesURI createScratchSpaceURI(long appId, String runtimeId, String nodeId,
            String activeObjectId, String userPath) {
        return new DataSpacesURI(appId, SpaceType.SCRATCH, null, runtimeId, nodeId, activeObjectId, userPath);
    }

    /**
     * Creates URI of input or output type with only appId and name specified. Created URI has
     * always space part fully defined and is suitable for user path if arguments are not
     * <code>null</code>.
     *
     * This method is only a shortcut for
     * {@link #createInOutSpaceURI(long, SpaceType, String, String)} with <code>null</code> value
     * for argument userPath.
     *
     * @param appId
     *            application id
     * @param spaceType
     *            space type - only {@link SpaceType#INPUT} or {@link SpaceType#OUTPUT} is allowed
     *            here. May be <code>null</code> if following argument name is also
     *            <code>null</code>.
     * @param name
     *            name of input. May be <code>null</code>.
     * @return URI for that specification
     * @throws IllegalArgumentException
     *             when <code>null</code> values in arguments do not obey URI components hierarchy
     *             requirements or name is invalid.
     */
    public static DataSpacesURI createInOutSpaceURI(long appId, SpaceType spaceType, String name) {
        return createInOutSpaceURI(appId, spaceType, name, null);
    }

    /**
     * Creates URI of input or output type. Created URI has always space part fully defined and is
     * suitable for user path if arguments are not <code>null</code>.
     *
     * @param appId
     *            application id
     * @param spaceType
     *            space type - only {@link SpaceType#INPUT} or {@link SpaceType#OUTPUT} is allowed
     *            here. May be <code>null</code> if following arguments are also <code>null</code>.
     * @param name
     *            name of input. May be <code>null</code> if following userPath argument is also
     *            <code>null</code>.
     * @param userPath
     *            userPath within data space. May be <code>null</code>.
     * @return URI for that specification
     * @throws IllegalArgumentException
     *             when <code>null</code> values in arguments do not obey URI components hierarchy
     *             requirements or name or userPath are invalid.
     */
    public static DataSpacesURI createInOutSpaceURI(long appId, SpaceType spaceType, String name,
            String userPath) {
        return new DataSpacesURI(appId, spaceType, name, null, null, null, userPath);
    }

    /**
     * Parses string to URI instance.
     * <p>
     * Input string should conform rules mentioned in class description. Any valid URI string is
     * parsable. Scheme and application need to be always present in provided string, while other
     * components are optional.
     * <p>
     * End slash after last component (except userPath) is allowed, but not required. It is
     * recommended to not use it, as it is not used in URI canonical form returned by
     * {@link #toString()} method.
     *
     * @param uri
     *            string with URI to parse
     * @return parsed URI
     * @throws MalformedURIException
     *             when provided string does not conform to URI format.
     */
    public static DataSpacesURI parseURI(String uri) throws MalformedURIException {
        final Matcher m = PATTERN.matcher(uri);
        if (!m.matches()) {
            throw new MalformedURIException("Unexpected URI format");
        }

        final String appIdString = m.group(1);
        final long appId;
        try {
            appId = Long.parseLong(appIdString);
        } catch (NumberFormatException x) {
            throw new MalformedURIException("Wrong application id format", x);
        }

        if (m.group(3) == null) {
            // just vfs:///123/
            return new DataSpacesURI(appId, null, null, null, null, null, null);
        }

        if (m.group(4) != null) {
            // vfs:///123/input/ OR vfs:///123/output/

            final String spaceTypeString = m.group(5).toUpperCase();
            // regexp patter guarantees correct space type enum name
            final SpaceType spaceType = SpaceType.valueOf(spaceTypeString);

            // both name and userPath may be null,
            // but hierarchy is guaranteed by the expression
            final String name = m.group(8);
            final String path = m.group(10);
            return new DataSpacesURI(appId, spaceType, name, null, null, null, path);
        } else {
            // vfs://123/scratch/

            // any of these can be null,
            // but hierarchy is guaranteed by the expression
            final String runtimeId = m.group(13);
            final String nodeId = m.group(16);
            final String aoId = m.group(19);
            final String path = m.group(21);
            return new DataSpacesURI(appId, SpaceType.SCRATCH, null, runtimeId, nodeId, aoId, path);
        }
    }

    private final long appId;

    private final SpaceType spaceType;

    private final String name;

    private final String runtimeId;

    private final String nodeId;

    private final String activeObjectId;

    private final String userPath;

    private DataSpacesURI(long appId, SpaceType spaceType, String name, String runtimeId, String nodeId,
            String activeObjectId, String userPath) {

        if ((spaceType == null && (name != null || runtimeId != null)) ||
            (runtimeId == null && nodeId != null) || (nodeId == null && activeObjectId != null) ||
            (activeObjectId == null && name == null && userPath != null)) {
            throw new IllegalArgumentException(
                "Malformed URI. Provided arguments do not meet hierarchy consistency requirement.");
        }

        if ((spaceType == SpaceType.INPUT || spaceType == SpaceType.OUTPUT) && runtimeId != null) {
            throw new IllegalArgumentException("Malformed URI. Input/output can not have runtime id.");
        }

        if (spaceType == SpaceType.SCRATCH && name != null) {
            throw new IllegalArgumentException("Malformed URI. Scratch can not have name.");
        }

        if (!isValidComponent(name) || !isValidComponent(runtimeId) || !isValidComponent(nodeId) ||
            !isValidComponent(activeObjectId)) {
            throw new IllegalArgumentException(
                "Data Spaces URI component can not be empty nor contain slashes.");
        }

        if (userPath != null && userPath.length() == 0) {
            throw new IllegalArgumentException("Data Spaces URI userPath can not be empty string.");
        }

        this.appId = appId;
        this.spaceType = spaceType;
        this.name = name;
        this.runtimeId = runtimeId;
        this.nodeId = nodeId;
        this.activeObjectId = activeObjectId;
        this.userPath = userPath;
    }

    /**
     * @return application id
     */
    public long getAppId() {
        return appId;
    }

    /**
     * @return space type. May be <code>null</code>.
     */
    public SpaceType getSpaceType() {
        return spaceType;
    }

    /**
     * @return name for input and output spaces. May be <code>null</code> for input/output, is
     *         <code>null</code> for scratch or undefined space type.
     */
    public String getName() {
        return name;
    }

    /**
     * @return runtimeId for scratch space. May be <code>null</code> for scratch, is
     *         <code>null</code> for input/output or undefined space type.
     */
    public String getRuntimeId() {
        return runtimeId;
    }

    /**
     * @return nodeId for scratch space. May be <code>null</code> for scratch, is <code>null</code>
     *         for input/output or undefined space type.
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * @return activeObjectId for scratch space. May be <code>null</code> for scratch, is
     *         <code>null</code> for input/output or undefined space type.
     */
    public String getActiveObjectId() {
        return activeObjectId;
    }

    /**
     * @return userPath within space. May be <code>null</code>.
     */
    public String getUserPath() {
        return userPath;
    }

    /**
     * Checks whether URI has space part fully defined, i.e. points to concrete data space.
     *
     * Such URI has space type and name (or runtimeId and nodeId in case of scratch) defined.
     * userPath and activeObjectId specification are optional.
     *
     * @return <code>true</code> if this URI has space part fully defined; <code>false</code>
     *         otherwise.
     */
    public boolean isSpacePartFullyDefined() {
        return spaceType != null && (name != null || (runtimeId != null && nodeId != null));
    }

    /**
     * Checks whether URI has only space part defined, i.e. without activeObjectId and userPath
     * being specified.
     *
     * @return <true>true</code> if this URI has only space part defined; <code>false</code>
     *         otherwise.
     */
    public boolean isSpacePartOnly() {
        return activeObjectId == null && userPath == null;
    }

    /**
     * Checks whether URI is suitable for having userPath defined, through
     * {@link #withUserPath(String)} method.
     * <p>
     * Such URI can be used by end-user.
     *
     * @return <code>true</code> when URI is suitable for having userPath defined,
     *         <code>false</code> otherwise.
     */
    public boolean isSuitableForUserPath() {
        return isSpacePartFullyDefined() && (spaceType != SpaceType.SCRATCH || activeObjectId != null);
    }

    /**
     * Returns copy of this URI with only space part being defined, without activeObjectId or
     * userPath. Such URI is suitable for data spaces granularity level queries.
     *
     * @return copy of this this URI with only space part components defined, without activeObjectId
     *         or userPath.
     */
    public DataSpacesURI getSpacePartOnly() {
        return new DataSpacesURI(appId, spaceType, name, runtimeId, nodeId, null, null);
    }

    /**
     * Returns the string representation of a DataSpacesURI without a space URI prefix. This can be
     * used for resolving files within a particular space.
     *
     * @return string representation of a DataSpacesURI without space URI prefix or
     *         <code>null</code> if DataSpacesURI contains space part only
     * @see #isSpacePartOnly()
     */
    public String getRelativeToSpace() {
        if (isSpacePartOnly())
            return null;

        final StringBuilder sb = new StringBuilder();

        if (spaceType == SpaceType.SCRATCH) {
            sb.append(activeObjectId);
            if (userPath != null) {
                sb.append('/').append(userPath);
            }
        } else {
            sb.append(userPath);
        }

        return sb.toString();
    }

    /**
     * Creates copy of this URI with new activeObjectId.
     *
     * @param newActiveObjectId
     *            activeObjectId to set in newly created URI. May be <code>null</code>. Can be
     *            non-null and nonempty String without slashes if this URI has space part fully
     *            defined and is scratch space type.
     * @return copy of this URI with provided activeObjectId set.
     * @throws IllegalArgumentException
     *             when nonempty activeObjectId was requested for URI without space part being fully
     *             defined or non-scratch space type or newActiveObjectId is invalid (empty string
     *             or string containing slashes).
     * @see #isSpacePartFullyDefined()
     */
    public DataSpacesURI withActiveObjectId(final String newActiveObjectId) {
        return new DataSpacesURI(appId, spaceType, name, runtimeId, nodeId, newActiveObjectId, userPath);
    }

    /**
     * Creates copy of this URI with new userPath.
     *
     * @param newPath
     *            userPath to set in newly created URI. May be <code>null</code>. Can be non-null
     *            and nonempty String if this URI is suitable for having userPath.
     * @return copy of this URI with provided userPath set.
     * @throws IllegalArgumentException
     *             when nonempty newPath was requested for URI unsuitable for having user path or
     *             newPath is invalid (empty string).
     * @see #isSuitableForUserPath()
     */
    public DataSpacesURI withUserPath(String newPath) {
        return new DataSpacesURI(appId, spaceType, name, runtimeId, nodeId, activeObjectId, newPath);
    }

    /**
     * Creates copy of this URI with new part being relative to space - concerning activeObjectId
     * (only for scratches) and userPath.
     *
     * @param newRelativeToSpace
     *            new part relative to space. For scratch space URI this consists of
     *            activeObjectId/userPath, for other types of spaces it is just userPath. May be
     *            <code>null</code>. Can be non-null and nonempty String if this URI has space part
     *            fully defined.
     * @return copy of this URI with provided new part relative to space.
     * @throws IllegalArgumentException
     *             when nonempty newRelativeToSpace was requested for URI without space part fully
     *             defined or newRelativeToSpace is invalid (empty activeObjectId or userPath
     *             components etc.).
     * @see #getRelativeToSpace()
     * @see #isSpacePartFullyDefined()
     */
    public DataSpacesURI withRelativeToSpace(String newRelativeToSpace) {
        if (newRelativeToSpace == null)
            return new DataSpacesURI(appId, spaceType, name, runtimeId, nodeId, null, null);

        if (!isSpacePartFullyDefined()) {
            throw new IllegalArgumentException("");
        }

        final String newActiveObjectId;
        final String newUserPath;
        if (spaceType == SpaceType.SCRATCH) {
            final String parts[] = newRelativeToSpace.split("/", 2);
            newActiveObjectId = parts[0];
            if (parts.length == 2) {
                newUserPath = parts[1];
            } else {
                newUserPath = null;
            }
        } else {
            newActiveObjectId = null;
            newUserPath = newRelativeToSpace;
        }

        return new DataSpacesURI(appId, spaceType, name, runtimeId, nodeId, newActiveObjectId, newUserPath);
    }

    /**
     * Generates next URI key (in sense of comparator of this class). This is method for
     * <strong>internal usage only</strong>.
     *
     * @return next URI key
     * @throws IllegalStateException
     *             if this URI has space part fully defined.
     * @see #isSpacePartFullyDefined()
     */
    public DataSpacesURI nextURI() {
        long newAppId = this.appId;
        SpaceType newSpaceType = this.spaceType;
        String newRuntimeId = this.runtimeId;

        // case: appid/type/name/
        // case: appid/type/rt/node/
        if (isSpacePartFullyDefined())
            throw new IllegalStateException("Base URI has space part fully. Doesn't make sens, giving up.");

        // case: appid/ - just ++
        if (newSpaceType == null) {
            newAppId++;
            return new DataSpacesURI(newAppId, null, null, null, null, null, null);
        }

        // case: appid/SCRATCH/rt/ - just build next rt string
        if (newSpaceType == SpaceType.SCRATCH && newRuntimeId != null) {
            newRuntimeId = newRuntimeId + '\0';
            return new DataSpacesURI(newAppId, newSpaceType, null, newRuntimeId, null, null, null);
        }

        // case: appid/type/ - paste a next type
        newSpaceType = newSpaceType.succ();

        // case: appid/last_type/ - there was no next type?
        if (newSpaceType == null) {
            newAppId++;
        }

        return new DataSpacesURI(newAppId, newSpaceType, null, null, null, null, null);
    }

    /**
     * Returns string representation of this URI. This string may be directly used by user-level
     * code.
     *
     * Returned URI does not have end slash.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(SCHEME);

        sb.append(Long.toString(appId));

        if (spaceType == null) {
            return sb.toString();
        }
        sb.append('/');
        sb.append(spaceType.getDirectoryName());

        switch (spaceType) {
            case INPUT:
            case OUTPUT:
                if (name == null) {
                    return sb.toString();
                }
                sb.append('/');
                sb.append(name);
                break;
            case SCRATCH:
                if (runtimeId == null) {
                    return sb.toString();
                }
                sb.append('/');
                sb.append(runtimeId);

                if (nodeId == null) {
                    return sb.toString();
                }
                sb.append('/');
                sb.append(nodeId);

                if (activeObjectId == null) {
                    return sb.toString();
                }
                sb.append('/');
                sb.append(activeObjectId);
                break;
            default:
                throw new IllegalStateException("Unexpected space type");
        }

        if (userPath != null) {
            sb.append('/');
            sb.append(userPath);
        }

        return sb.toString();
    }

    public int compareTo(DataSpacesURI other) {
        if (this == other) {
            return 0;
        }
        if (other == null) {
            throw new NullPointerException();
        }

        if (appId != other.appId) {
            if (appId < other.appId) {
                return -1;
            }
            return 1;
        }

        if (spaceType == null) {
            if (other.spaceType != null) {
                return -1;
            }
            return 0;
        } else {
            if (other.spaceType == null) {
                return 1;
            }
            final int cmp = spaceType.compareTo(other.spaceType);
            if (cmp != 0) {
                return cmp;
            }
        }

        switch (spaceType) {
            case INPUT:
            case OUTPUT:
                if (name == null) {
                    if (other.name != null) {
                        return -1;
                    }
                    return 0;
                } else {
                    if (other.name == null) {
                        return 1;
                    }
                    final int cmp = name.compareTo(other.name);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                break;
            case SCRATCH:
                if (runtimeId == null) {
                    if (other.runtimeId != null) {
                        return -1;
                    }
                    return 0;
                } else {
                    if (other.runtimeId == null) {
                        return 1;
                    }
                    final int cmp = runtimeId.compareTo(other.runtimeId);
                    if (cmp != 0) {
                        return cmp;
                    }
                }

                if (nodeId == null) {
                    if (other.nodeId != null) {
                        return -1;
                    }
                    return 0;
                } else {
                    if (other.nodeId == null) {
                        return 1;
                    }
                    final int cmp = nodeId.compareTo(other.nodeId);
                    if (cmp != 0) {
                        return cmp;
                    }
                }

                if (activeObjectId == null) {
                    if (other.activeObjectId != null) {
                        return -1;
                    }
                    return 0;
                } else {
                    if (other.activeObjectId == null) {
                        return 1;
                    }
                    final int cmp = activeObjectId.compareTo(other.activeObjectId);
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                break;
        }

        if (userPath == null) {
            if (other.userPath != null) {
                return -1;
            }
            return 0;
        } else {
            if (other.userPath == null) {
                return 1;
            }
            return userPath.compareTo(other.userPath);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DataSpacesURI)) {
            return false;
        }

        DataSpacesURI other = (DataSpacesURI) obj;
        return compareTo(other) == 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (appId ^ (appId >>> 32));
        result = prime * result + ((spaceType == null) ? 0 : spaceType.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((runtimeId == null) ? 0 : runtimeId.hashCode());
        result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
        result = prime * result + ((activeObjectId == null) ? 0 : activeObjectId.hashCode());
        result = prime * result + ((userPath == null) ? 0 : userPath.hashCode());
        return result;
    }
}
