/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.objectweb.proactive.core.debug.debugger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.body.request.Request;


public class RequestInfo implements Serializable {

    private static final long serialVersionUID = -3263437546419006443L;
    private String senderID;
    private long sequenceNumber;

    private String methodCalled;
    private List<String> parameters = new ArrayList<String>();
    private String returnType;

    public RequestInfo(Request r) {
        // Sender
        if (r.getSender() != null) {
            senderID = r.getSender().getID().getCanonString();
        } else {
            senderID = "no sender";
        }
        sequenceNumber = r.getSequenceNumber();

        // MethodCalled
        if (r.getMethodName() != null) {
            methodCalled = r.getMethodName();
        } else {
            methodCalled = "not methodName";
        }
        if (r.getMethodCall() != null) {
            int sizeOfParams = r.getMethodCall().getNumberOfParameter();
            for (int i = 0; i < sizeOfParams; i++) {
                parameters.add(r.getParameter(i).getClass().getSimpleName());
            }
        }
        if (r.getMethodCall().getReifiedMethod() != null) {
            returnType = r.getMethodCall().getReifiedMethod().getReturnType().getSimpleName();
        } else {
            returnType = "no reifiedMethod";
        }
    }

    //
    // -- GETTERS AND SETTERS -----------------------------------------------
    //
    public String getMethodCalled() {
        return methodCalled;
    }

    public String getSenderID() {
        return senderID;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

}
