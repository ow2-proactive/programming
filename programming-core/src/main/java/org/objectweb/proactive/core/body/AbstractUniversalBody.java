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
package org.objectweb.proactive.core.body;

import java.io.Serializable;
import java.net.URI;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.remoteobject.RemoteObjectAdapter;
import org.objectweb.proactive.core.remoteobject.RemoteObjectExposer;
import org.objectweb.proactive.core.remoteobject.RemoteObjectHelper;
import org.objectweb.proactive.core.remoteobject.RemoteRemoteObject;


/**
 * <i><font size="-1" color="#FF0000">**For internal use only** </font></i><br>
 * <p>
 * This class gives a common implementation of the UniversalBody interface. It provides all
 * the non specific behavior allowing sub-class to write the detail implementation.
 * </p><p>
 * Each body is identify by an unique identifier.
 * </p>
 *
 * @author The ProActive Team
 * @version 1.0,  2002/06
 * @since   ProActive 0.9.3
 *
 */

public abstract class AbstractUniversalBody implements UniversalBody, Serializable {
    //
    // -- PROTECTED MEMBERS -----------------------------------------------
    //

    /** A table containing a mapping from a UniqueID toward a Body. The location table
       caches the location of all known bodies to whom this body communicated */
    protected BodyMap location;

    /** The URL of the node this body is attached to */
    protected String nodeURL;

    /** A remote version of this body that is used to send to remote peer */
    protected transient UniversalBody remoteBody;

    protected transient RemoteObjectExposer<UniversalBody> roe;

    /** Unique ID of the body. */
    protected UniqueID bodyID;

    /**
     * Name of this body, generally related to the reifiedObjectClassName
     */
    protected String name;

    /**
     * class name of the reified object associated with this body
     */
    protected String reifiedObjectClassName;

    //
    // -- PRIVATE MEMBERS -----------------------------------------------
    //
    //
    // -- CONSTRUCTORS -----------------------------------------------
    //

    /**
     * Creates a new AbstractBody.
     * Used for serialization.
     */
    public AbstractUniversalBody() {
    }

    /**
     * Creates a new AbstractBody for an active object attached to a given node.
     * @param nodeURL the URL of the node that body is attached to
     */
    public AbstractUniversalBody(Object reifiedObject, String nodeURL) throws ActiveObjectCreationException {
        this.nodeURL = nodeURL;
        if (reifiedObject instanceof StackTraceElement) {
            StackTraceElement ste = (StackTraceElement) reifiedObject;
            this.name = "HalfBody_" + ste.getClassName() + "#" + ste.getMethodName();

        } else {
            this.reifiedObjectClassName = reifiedObject.getClass().getName();
            this.name = "ActiveObject_" + reifiedObjectClassName;
        }
        this.bodyID = new UniqueID(this.name + "_");
        this.location = new BodyMap();

        this.roe = new RemoteObjectExposer<UniversalBody>(this.bodyID.toString(),
                                                          UniversalBody.class.getName(),
                                                          this,
                                                          UniversalBodyRemoteObjectAdapter.class);

        try {
            RemoteRemoteObject rro = this.roe.createRemoteObject(this.bodyID.toString(), false);
            this.remoteBody = (UniversalBody) new RemoteObjectAdapter(rro).getObjectProxy();
        } catch (Exception e) {
            throw new ActiveObjectCreationException(e);
        }
    }

    //
    // -- PUBLIC METHODS -----------------------------------------------
    //
    //
    // -- implements UniversalBody -----------------------------------------------
    //

    public String getName() {
        return name;
    }

    public String getUrl() {
        return this.roe.getURL();
    }

    public String[] getUrls() {
        return this.roe.getURLs();
    }

    public String getNodeURL() {
        return this.nodeURL;
    }

    public UniversalBody getRemoteAdapter() {
        return this.remoteBody;
    }

    public UniqueID getID() {
        return this.bodyID;
    }

    public void updateLocation(UniqueID bodyID, UniversalBody body) {
        this.location.updateBody(bodyID, body);
    }

    //
    // -- PROTECTED METHODS -----------------------------------------------
    //
    //
    // -- PRIVATE METHODS -----------------------------------------------
    //
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (this.bodyID == null) {
            // it may happen that the bodyID is set to null before serialization if we want to
            // create a copy of the Body that is distinct from the original
            if (name == null) {
                this.bodyID = new UniqueID();
            } else {
                this.bodyID = new UniqueID(name + "_");
            }

        }

        // remoteBody is transient so we recreate it here
        this.roe = new RemoteObjectExposer<UniversalBody>(this.bodyID.toString(),
                                                          UniversalBody.class.getName(),
                                                          this,
                                                          UniversalBodyRemoteObjectAdapter.class);

        try {
            // rebind must be true: if an object migrates between two JVM on the same machine (same rmi registry)
            RemoteRemoteObject rro = this.roe.createRemoteObject(this.bodyID.toString(), true);
            this.remoteBody = (UniversalBody) new RemoteObjectAdapter(rro).getObjectProxy();

        } catch (ProActiveException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
     * @see org.objectweb.proactive.core.body.UniversalBody#register(java.lang.String)
     */
    @Deprecated
    public void register(String url) throws ProActiveException {
        this.roe.createRemoteObject(RemoteObjectHelper.expandURI(URI.create(url)));
    }

    public String registerByName(String name, boolean rebind) throws ProActiveException {
        RemoteRemoteObject rro = this.roe.createRemoteObject(name, rebind);
        RemoteObjectAdapter roa = new RemoteObjectAdapter(rro);
        return roa.getURI().toString();
    }

    public String registerByName(String name, boolean rebind, String protocol) throws ProActiveException {
        RemoteRemoteObject rro = this.roe.createRemoteObject(name, rebind, protocol);
        RemoteObjectAdapter roa = new RemoteObjectAdapter(rro);
        return roa.getURI().toString();
    }

    public RemoteObjectExposer<UniversalBody> getRemoteObjectExposer() {
        return this.roe;
    }
}
