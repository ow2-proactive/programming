/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.mop;

/**
 * This class holds the type of a method call (OneWay, Asynchronous and Synchronous)
 * along with the reason associated if it is synchronous
 * @author The ProActive Team
 *
 */
public class MethodCallInfo {
    public enum CallType {
        OneWay, Asynchronous, Synchronous;
    }

    public enum SynchronousReason {
        NotApplicable, ThrowsCheckedException, NotReifiable;
    }

    private CallType type;
    private SynchronousReason reason;
    private String message;

    public MethodCallInfo() {
    }

    /**
     * Constructor used when we don't care what the reason for this call is
     * @param type
     */
    public MethodCallInfo(CallType type) {
        this.setType(type);
        this.setReason(SynchronousReason.NotApplicable);
        this.setMessage(null);
    }

    public MethodCallInfo(CallType type, SynchronousReason reason, String message) {
        this.setType(type);
        this.setReason(reason);
        this.setMessage(message);
    }

    public CallType getType() {
        return type;
    }

    public SynchronousReason getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public void setType(CallType type) {
        this.type = type;
    }

    public void setReason(SynchronousReason reason) {
        this.reason = reason;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
