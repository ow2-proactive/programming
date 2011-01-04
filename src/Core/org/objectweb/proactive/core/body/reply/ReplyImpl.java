/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
package org.objectweb.proactive.core.body.reply;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.body.LocalBodyStore;
import org.objectweb.proactive.core.body.UniversalBody;
import org.objectweb.proactive.core.body.future.MethodCallResult;
import org.objectweb.proactive.core.body.message.MessageImpl;
import org.objectweb.proactive.core.body.tags.MessageTags;
import org.objectweb.proactive.core.mop.Utils;
import org.objectweb.proactive.core.security.ProActiveSecurityManager;
import org.objectweb.proactive.core.security.crypto.Session;
import org.objectweb.proactive.core.security.crypto.SessionException;
import org.objectweb.proactive.core.security.crypto.Session.ActAs;
import org.objectweb.proactive.core.security.exceptions.CommunicationForbiddenException;
import org.objectweb.proactive.core.security.exceptions.RenegotiateSessionException;
import org.objectweb.proactive.core.security.exceptions.SecurityNotAvailableException;
import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;


public class ReplyImpl extends MessageImpl implements Reply, Serializable {

    /**
     * The hypothetic result
     */
    protected MethodCallResult result;

    // security features

    /**
     * the encrypted result
     */
    protected byte[][] encryptedResult;
    protected boolean ciphered;

    // true if this reply is sent by automatic continuation
    private boolean isAC;

    /*
     * the session ID used to find the key and decrypt the reply
     */
    protected long sessionID;
    protected transient ProActiveSecurityManager psm = null;

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, MethodCallResult result,
            ProActiveSecurityManager psm, MessageTags tags) {
        this(senderID, sequenceNumber, methodName, result, psm, false, tags);
    }

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, MethodCallResult result,
            ProActiveSecurityManager psm) {
        this(senderID, sequenceNumber, methodName, result, psm, false, null);
    }

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, MethodCallResult result,
            ProActiveSecurityManager psm, boolean isAutomaticContinuation) {
        this(senderID, sequenceNumber, methodName, result, psm, isAutomaticContinuation, null);
    }

    public ReplyImpl(UniqueID senderID, long sequenceNumber, String methodName, MethodCallResult result,
            ProActiveSecurityManager psm, boolean isAutomaticContinuation, MessageTags tags) {
        super(senderID, sequenceNumber, true, methodName, tags);
        this.result = result;
        this.psm = psm;
        this.isAC = isAutomaticContinuation;
    }

    public MethodCallResult getResult() {
        return result;
    }

    public int send(UniversalBody destinationBody) throws IOException {
        // if destination body is on the same VM that the sender, we must
        // perform
        // a deep copy of result in order to preserve ProActive model.
        UniqueID destinationID = destinationBody.getID();

        // The following code ensures that if the destination body is located
        // on the same VM, the result will only be serialized once.
        // This also solves the bug PROACTIVE-81.
        UniversalBody localRef = LocalBodyStore.getInstance().getLocalBody(destinationID);
        if (localRef == null) {
            // halfBody ?
            localRef = LocalBodyStore.getInstance().getLocalHalfBody(destinationID);
        }

        if (localRef != null) {
            destinationBody = localRef;
            result = (MethodCallResult) Utils.makeDeepCopy(result);
        }

        // security
        if (!ciphered && (psm != null)) {
            try {
                Session session = this.psm.getSessionTo(destinationBody.getCertificate());
                if (!session.getSecurityContext().getSendReply().getCommunication()) {
                    throw new CommunicationForbiddenException();
                }
                this.sessionID = session.getDistantSessionID();
                long id = psm.getSessionIDTo(destinationBody.getCertificate());
                encryptedResult = psm.encrypt(id, result, ActAs.SERVER);
                ciphered = true;
            } catch (SecurityNotAvailableException e) {
                // do nothing
            } catch (CommunicationForbiddenException e) {
                e.printStackTrace();
            } catch (RenegotiateSessionException e) {
                psm.terminateSession(sessionID);
                try {
                    destinationBody.terminateSession(sessionID);
                } catch (SecurityNotAvailableException e1) {
                    e.printStackTrace();
                }
                this.send(destinationBody);
            } catch (SessionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // end security
        // fault-tolerance returned value
        return destinationBody.receiveReply(this);
    }

    // security issue
    public boolean isCiphered() {
        return ciphered;
    }

    public boolean decrypt(ProActiveSecurityManager psm) throws RenegotiateSessionException {
        if ((sessionID != 0) && ciphered) {
            byte[] decryptedMethodCall = psm.decrypt(sessionID, encryptedResult, ActAs.CLIENT);
            try {
                result = (MethodCallResult) ByteToObjectConverter.ObjectStream.convert(decryptedMethodCall);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.proactive.core.body.reply.Reply#getSessionId()
     */
    public long getSessionId() {
        return sessionID;
    }

    /**
     * @see org.objectweb.proactive.core.body.reply.Reply#isAutomaticContinuation()
     */
    public boolean isAutomaticContinuation() {
        return this.isAC;
    }
}
