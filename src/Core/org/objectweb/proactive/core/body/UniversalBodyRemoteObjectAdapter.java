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
package org.objectweb.proactive.core.body;

import java.io.IOException;
import java.security.AccessControlException;
import java.security.PublicKey;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.ft.internalmsg.FTMessage;
import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.core.body.request.Request;
import org.objectweb.proactive.core.component.request.Shortcut;
import org.objectweb.proactive.core.gc.GCMessage;
import org.objectweb.proactive.core.gc.GCResponse;
import org.objectweb.proactive.core.mop.StubObject;
import org.objectweb.proactive.core.remoteobject.adapter.Adapter;
import org.objectweb.proactive.core.security.PolicyServer;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.SecurityContext;
import org.objectweb.proactive.core.security.TypedCertificate;
import org.objectweb.proactive.core.security.crypto.KeyExchangeException;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.security.securityentity.Entities;
import org.objectweb.proactive.core.security.securityentity.Entity;


public class UniversalBodyRemoteObjectAdapter extends Adapter<UniversalBody> implements UniversalBody {

    /**
     * Cache the ID of the Body locally for speed
     */
    protected UniqueID bodyID;

    protected String name;

    protected int hashcode;

    public UniversalBodyRemoteObjectAdapter() {
    }

    public UniversalBodyRemoteObjectAdapter(UniversalBody u) {
        super(u);
        if (bodyLogger.isDebugEnabled()) {
            //Thread.dumpStack();
            bodyLogger.debug(target.getClass());
        }
    }

    @Override
    protected void construct() {
        this.bodyID = target.getID();
        this.name = target.getName();
        this.hashcode = target.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UniversalBodyRemoteObjectAdapter) {
            return ((StubObject) this.target).getProxy().equals(
                    ((StubObject) ((UniversalBodyRemoteObjectAdapter) o).target).getProxy());
        }

        return false;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getID()
     */
    public UniqueID getID() {
        return bodyID;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#createShortcut(org.objectweb.proactive.core.component.request.Shortcut)
     */
    public void createShortcut(Shortcut shortcut) throws IOException {
        //      TODO implement
        throw new ProActiveRuntimeException("create shortcut method not implemented yet");
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getRemoteAdapter()
     */
    public UniversalBody getRemoteAdapter() {
        return this;
    }

    /**
     * @see org.objectweb.proactive.core.body.UniversalBody#getReifiedClassName()
     */
    public String getReifiedClassName() {
        return this.target.getReifiedClassName();
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    public void disableAC() throws IOException {
        target.disableAC();
    }

    public void enableAC() throws IOException {
        target.enableAC();
    }

    public String getNodeURL() {
        return target.getNodeURL();
    }

    public Object receiveFTMessage(FTMessage ev) throws IOException {
        return target.receiveFTMessage(ev);
    }

    public GCResponse receiveGCMessage(GCMessage toSend) throws IOException {
        return target.receiveGCMessage(toSend);
    }

    public int receiveReply(Reply r) throws IOException {
        return target.receiveReply(r);
    }

    public int receiveRequest(Request request) throws IOException, RenegotiateSessionException {
        return target.receiveRequest(request);
    }

    public String registerByName(String name, boolean rebind) throws IOException, ProActiveException {
        return target.registerByName(name, rebind);
    }

    public String registerByName(String name, boolean rebind, String protocol) throws IOException,
            ProActiveException {
        return target.registerByName(name, rebind, protocol);
    }

    public void setRegistered(boolean registered) throws IOException {
        target.setRegistered(registered);
    }

    public void updateLocation(UniqueID id, UniversalBody body) throws IOException {
        target.updateLocation(id, body);
    }

    public TypedCertificate getCertificate() throws SecurityNotAvailableException, IOException {
        return target.getCertificate();
    }

    public Entities getEntities() throws SecurityNotAvailableException, IOException {
        return target.getEntities();
    }

    public SecurityContext getPolicy(Entities local, Entities distant) throws SecurityNotAvailableException,
            IOException {
        return target.getPolicy(local, distant);
    }

    public PublicKey getPublicKey() throws SecurityNotAvailableException, IOException {
        return target.getPublicKey();
    }

    public byte[] publicKeyExchange(long sessionID, byte[] signature) throws SecurityNotAvailableException,
            RenegotiateSessionException, KeyExchangeException, IOException {
        return target.publicKeyExchange(sessionID, signature);
    }

    public byte[] randomValue(long sessionID, byte[] clientRandomValue) throws SecurityNotAvailableException,
            RenegotiateSessionException, IOException {
        return target.randomValue(sessionID, clientRandomValue);
    }

    public byte[][] secretKeyExchange(long sessionID, byte[] encodedAESKey, byte[] encodedIVParameters,
            byte[] encodedClientMacKey, byte[] encodedLockData, byte[] parametersSignature)
            throws SecurityNotAvailableException, RenegotiateSessionException, IOException {
        return target.secretKeyExchange(sessionID, encodedAESKey, encodedIVParameters, encodedClientMacKey,
                encodedLockData, parametersSignature);
    }

    public long startNewSession(long distantSessionID, SecurityContext policy,
            TypedCertificate distantCertificate) throws SecurityNotAvailableException, IOException,
            SessionException {
        return target.startNewSession(distantSessionID, policy, distantCertificate);
    }

    public void terminateSession(long sessionID) throws SecurityNotAvailableException, IOException {
        target.terminateSession(sessionID);
    }

    public ProActiveSecurityManager getProActiveSecurityManager(Entity user)
            throws SecurityNotAvailableException, AccessControlException, IOException {
        return this.target.getProActiveSecurityManager(user);
    }

    public void setProActiveSecurityManager(Entity user, PolicyServer policyServer)
            throws SecurityNotAvailableException, AccessControlException, IOException {
        this.target.setProActiveSecurityManager(user, policyServer);
    }

    public String getUrl() {
        return this.target.getUrl();
    }

}
